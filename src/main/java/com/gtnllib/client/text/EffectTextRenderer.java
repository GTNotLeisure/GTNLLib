package com.gtnllib.client.text;

import java.util.Set;
import java.util.function.BooleanSupplier;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import com.gtnewhorizon.gtnhlib.util.font.FontRendering;
import com.gtnewhorizon.gtnhlib.util.font.IFontParameters;
import com.gtnllib.client.text.EffectTextLayout.DrawRun;
import com.gtnllib.client.text.EffectTextLayout.Layout;
import com.gtnllib.client.text.TextMaskCache.Mask;
import com.gtnllib.client.text.TypewriterTimeline.Typing;
import com.gtnllib.client.text.compat.AngelicaTextAdapter;
import com.gtnllib.client.text.compat.AngelicaTextAdapter.FontSettings;
import com.gtnllib.client.text.compat.FontBatchBridge;
import com.gtnllib.text.EffectTextParser;
import com.gtnllib.text.TextEffectStyle;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/** Draws marked spans while leaving glyph generation to the current font. */
public class EffectTextRenderer implements IResourceManagerReloadListener {

    public static final EffectTextRenderer INSTANCE = new EffectTextRenderer();
    private static final Logger LOGGER = LogManager.getLogger("GTNLTextEffects");
    private static final long START_TIME = System.nanoTime();
    private static final String CARET = "_";
    private static int captureDepth;
    private static BooleanSupplier enabled = () -> true;
    private static int nativeDepth;
    private final TextMaskCache masks = new TextMaskCache();
    private final Object2ObjectLinkedOpenHashMap<LayoutKey, Layout> layouts = new Object2ObjectLinkedOpenHashMap<>(32);
    private final Set<String> failed = new ObjectOpenHashSet<>();

    public static void enabledWith(BooleanSupplier setting) {
        enabled = setting;
    }

    public static boolean isCapturing() {
        return captureDepth > 0;
    }

    public static void beginCapture() {
        captureDepth++;
    }

    public static void endCapture() {
        captureDepth--;
    }

    public static boolean handles(String text) {
        return !isBypassingEffects() && EffectTextParser.containsMarkers(text);
    }

    public static boolean isBypassingEffects() {
        return isCapturing() || nativeDepth > 0;
    }

    // The part of string between offset and offset + length, or null when it has no effect markers to draw.
    public static String markedText(CharSequence string, int offset, int length) {
        if (isBypassingEffects() || string == null) return null;
        int start = Math.max(0, Math.min(offset, string.length()));
        int end = start + Math.max(0, Math.min(length, string.length() - start));
        if (!EffectTextParser.containsMarkers(string, start, end)) return null;
        return start == 0 && end == string.length() ? string.toString()
            : string.subSequence(start, end)
                .toString();
    }

    public float drawWithUnicodeFlag(FontRenderer font, String text, float x, float y, int color, boolean shadow,
        boolean unicodeFlag) {
        boolean previousUnicode = font.getUnicodeFlag();
        font.setUnicodeFlag(unicodeFlag);
        try {
            draw(font, text, x, y, color, shadow);
            return x + layout(font, text).width();
        } finally {
            font.setUnicodeFlag(previousUnicode);
        }
    }

    public Layout layout(FontRenderer font, String text) {
        IFontParameters parameters = (IFontParameters) font;
        LayoutKey key = new LayoutKey(
            font,
            text,
            font.getUnicodeFlag(),
            font.FONT_HEIGHT,
            parameters.getGlyphScaleX(),
            parameters.getGlyphScaleY(),
            parameters.getGlyphSpacing(),
            parameters.getWhitespaceScale(),
            FontRendering.preprocessText("&q&z&v"),
            FontRendering.hexColorResetsStyles(),
            AngelicaTextAdapter.fontSettings());
        Layout layout = layouts.getAndMoveToLast(key);
        if (layout == null) {
            layout = EffectTextLayout.create(font, text);
            if (text.length() < 16384) {
                layouts.putAndMoveToLast(key, layout);
                if (layouts.size() > 256) layouts.removeFirst();
            }
        }
        return layout;
    }

    public int draw(FontRenderer font, String text, float x, float y, int color, boolean shadow) {
        if (text == null) return 0;
        if ((color & 0xFC000000) == 0) color |= 0xFF000000;
        Layout layout = layout(font, text);
        boolean styled = false;
        for (DrawRun run : layout.runs()) {
            if (run.style() != null) {
                styled = true;
                break;
            }
        }
        if (!styled || !enabled.getAsBoolean()) {
            // Plain rendering keeps parsed text visible without touching deferred font or model batches.
            Typing typing = TypewriterTimeline.INSTANCE.plan(font, text, layout, x, y, seconds());
            float[] advances = new float[layout.runs()
                .size()];
            for (int i = 0; i < advances.length; i++) {
                DrawRun run = layout.runs()
                    .get(i);
                int count = typing == null ? -1 : typing.count(i);
                if (count != 0) advances[i] = drawPlain(
                    font,
                    count < 0 ? run.text() : run.typedText(count),
                    x + run.x(),
                    y + run.y(),
                    color,
                    shadow);
            }
            drawCaret(font, layout, typing, advances, x, y, color, shadow);
            return endX(font, layout, x, shadow);
        }
        if (!DeferredTextEffects.isFlushing() && AngelicaTextAdapter.shouldDeferEffects()) {
            DeferredTextEffects.enqueue(font, text, x, y, color, shadow);
            return endX(font, layout, x, shadow);
        }
        FontBatchBridge bridge = AngelicaTextAdapter.bridge(font);
        int depth = bridge == null ? 0 : bridge.gtnl$suspendBatch();
        try {
            AngelicaTextAdapter.beginForeignDraw();
            return drawLayout(
                font,
                layout,
                TypewriterTimeline.INSTANCE.plan(font, text, layout, x, y, seconds()),
                x,
                y,
                color,
                shadow);
        } finally {
            try {
                AngelicaTextAdapter.endForeignDraw();
            } finally {
                if (bridge != null) bridge.gtnl$resumeBatch(depth);
            }
        }
    }

    private int drawLayout(FontRenderer font, Layout layout, Typing typing, float x, float y, int color,
        boolean shadow) {
        try (TextRenderState ignored = new TextRenderState()) {
            float[] advances = new float[layout.runs()
                .size()];
            for (int i = 0; i < advances.length; i++) {
                DrawRun run = layout.runs()
                    .get(i);
                int count = typing == null ? -1 : typing.count(i);
                if (count == 0) continue;
                // A partly typed run uses a mask of its typed glyphs alone, so hidden glyphs cannot bleed outlines in.
                advances[i] = drawRun(
                    font,
                    count < 0 ? run.text() : run.typedText(count),
                    run.style(),
                    x + run.x(),
                    y + run.y(),
                    run.width(),
                    layout.height(),
                    color,
                    shadow);
            }
            drawCaret(font, layout, typing, advances, x, y, color, shadow);
        }
        return endX(font, layout, x, shadow);
    }

    // Vanilla measures unicode glyphs wider than it draws them, so the caret uses the drawn advance of the typed text.
    private void drawCaret(FontRenderer font, Layout layout, Typing typing, float[] advances, float x, float y,
        int color, boolean shadow) {
        if (typing == null || typing.caretRun() < 0) return;
        DrawRun run = layout.runs()
            .get(typing.caretRun());
        String formatting = run.glyphs()
            .isEmpty() ? ""
                : run.glyphs()
                    .get(0)
                    .formatting()
                    .replace("§k", "")
                    .replace("§K", "");
        String caret = formatting + CARET;
        float slot = run.x() + advances[typing.caretRun()];
        if (slot + layout(font, caret).width() > layout.width()) return;
        TextEffect effect = run.style() == null ? null
            : TextEffectRegistry.get(
                run.style()
                    .rendererId());
        drawPlain(font, caret, x + slot, y + run.y(), fallbackColor(run.style(), effect, color), shadow);
    }

    private static int endX(FontRenderer font, Layout layout, float x, boolean shadow) {
        return (int) Math.ceil(x + layout.width() + (shadow ? ((IFontParameters) font).getShadowOffset() : 0));
    }

    private float drawRun(FontRenderer font, String text, TextEffectStyle style, float x, float y, float width,
        float height, int color, boolean shadow) {
        TextEffect effect = style == null ? null : TextEffectRegistry.get(style.rendererId());
        if (effect == null || width <= 0
            || failed.contains(style.rendererId())
            || !AngelicaTextAdapter.supportsEffects(font)
            || !OpenGlHelper.isFramebufferEnabled()
            || !GLContext.getCapabilities().OpenGL20) {
            return drawPlain(font, text, x, y, fallbackColor(style, effect, color), shadow);
        }
        try (TextRenderState ignored = new TextRenderState()) {
            float padding = Math.max(height, effect.padding(width, height));
            Mask mask = masks.get(font, text, width, height, padding);
            if (mask == null) return drawPlain(font, text, x, y, fallbackColor(style, effect, color), shadow);
            double seconds = seconds();
            if (shadow) {
                float offset = ((IFontParameters) font).getShadowOffset();
                effect.render(new TextRenderContext(mask, style, x + offset, y + offset, color, seconds, true));
            }
            effect.render(new TextRenderContext(mask, style, x, y, color, seconds, false));
            return mask.advance();
        } catch (RuntimeException exception) {
            if (failed.add(style.rendererId()))
                LOGGER.warn("Text effect unavailable until resource reload: {}", style.rendererId(), exception);
            return drawPlain(font, text, x, y, fallbackColor(style, effect, color), shadow);
        }
    }

    private static double seconds() {
        return (System.nanoTime() - START_TIME) * 1e-9;
    }

    private static int fallbackColor(TextEffectStyle style, TextEffect effect, int color) {
        if (style == null) return color;
        int rgb = !style.colors()
            .isEmpty() ? style.colors()
                .getInt(0) : effect == null ? color & 0xFFFFFF : effect.fallbackColor();
        return color & 0xFF000000 | rgb;
    }

    private static float drawPlain(FontRenderer font, String text, float x, float y, int color, boolean shadow) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        nativeDepth++;
        try {
            int end = font.drawString(text, 0, 0, color, shadow);
            // Vanilla returns the end of whichever pass reaches further, which is the shadow pass.
            return Math.max(0, end - (shadow ? ((IFontParameters) font).getShadowOffset() : 0));
        } finally {
            nativeDepth--;
            GL11.glPopMatrix();
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        DeferredTextEffects.clear();
        TypewriterTimeline.INSTANCE.clear();
        layouts.clear();
        failed.clear();
        if (GLContext.getCapabilities().OpenGL20) {
            try (TextRenderState ignored = new TextRenderState()) {
                masks.clear();
                TextEffectRegistry.reload();
            }
        }
    }

    public record LayoutKey(FontRenderer font, String text, boolean unicode, int fontHeight, float scaleX, float scaleY,
        float spacing, float whitespace, String preprocessing, boolean hexResetsStyles, FontSettings fontSettings) {

        public LayoutKey(FontRenderer font, String text, boolean unicode, int fontHeight, float scaleX, float scaleY,
            float spacing, float whitespace, String preprocessing, boolean hexResetsStyles) {
            this(
                font,
                text,
                unicode,
                fontHeight,
                scaleX,
                scaleY,
                spacing,
                whitespace,
                preprocessing,
                hexResetsStyles,
                null);
        }
    }
}

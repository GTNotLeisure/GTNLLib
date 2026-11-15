package com.gtnllib.client.text.effect;

import java.nio.FloatBuffer;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.gtnllib.client.text.TextEffect;
import com.gtnllib.client.text.TextMaskCache.Mask;
import com.gtnllib.client.text.TextRenderContext;

import it.unimi.dsi.fastutil.ints.IntList;

/** A shader and its default palette form an independently registerable effect. */
public class ShaderTextEffect implements TextEffect {

    private static final int MAX_PALETTE_COLORS = 8;
    private final TextShader shader;
    private final FloatBuffer palette = BufferUtils.createFloatBuffer(MAX_PALETTE_COLORS * 3);
    private final int[] colors;
    private final float paddingScale;
    private final boolean premultipliedAlpha;
    private IntList packedColors;

    public ShaderTextEffect(ResourceLocation fragment, int... colors) {
        this(fragment, 1, false, colors);
    }

    public ShaderTextEffect(ResourceLocation fragment, float paddingScale, boolean premultipliedAlpha, int... colors) {
        if (colors.length == 0 || colors.length > MAX_PALETTE_COLORS)
            throw new IllegalArgumentException("A palette needs one to eight colors");
        if (!Float.isFinite(paddingScale) || paddingScale < 0)
            throw new IllegalArgumentException("Padding scale must be finite and nonnegative");
        this.shader = new TextShader(fragment);
        this.colors = colors.clone();
        this.paddingScale = paddingScale;
        this.premultipliedAlpha = premultipliedAlpha;
    }

    @Override
    public float padding(float height) {
        return height * paddingScale;
    }

    @Override
    public int fallbackColor() {
        return colors[0];
    }

    @Override
    public void render(TextRenderContext context) {
        shader.use();
        Mask mask = context.mask();
        IntList overrides = context.style()
            .colors();
        int count = overrides.isEmpty() ? colors.length : overrides.size();
        GL20.glUniform1i(shader.uniform("textMask"), 0);
        GL20.glUniform1i(shader.uniform("paletteCount"), count);
        GL20.glUniform1i(shader.uniform("paletteOverridden"), overrides.isEmpty() ? 0 : 1);
        GL20.glUniform2f(shader.uniform("textOrigin"), context.x(), context.y());
        packPalette(overrides);
        shader.uniform3fv("palette", palette);
        GL20.glUniform2f(shader.uniform("maskSize"), mask.textureWidth(), mask.textureHeight());
        GL20.glUniform1f(shader.uniform("maskResolution"), mask.resolution());
        GL20.glUniform2f(shader.uniform("textSize"), Math.max(1, mask.width()), Math.max(1, mask.height()));
        GL20.glUniform1f(shader.uniform("padding"), mask.padding());
        GL20.glUniform1f(
            shader.uniform("time"),
            (float) (context.seconds() * context.style()
                .speed() % 3600));
        GL20.glUniform1f(shader.uniform("opacity"), (context.color() >>> 24) / 255f);
        GL20.glUniform1i(shader.uniform("shadowPass"), context.shadow() ? 1 : 0);
        configureUniforms(shader, context);
        configureTextures(shader, context);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mask.target().framebufferTexture);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        OpenGlHelper.glBlendFunc(
            premultipliedAlpha ? GL11.GL_ONE : GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,
            GL11.GL_ONE_MINUS_SRC_ALPHA);
        float left = context.x() - mask.padding();
        float top = context.y() - mask.padding();
        float right = left + mask.textureWidth();
        float bottom = top + mask.textureHeight();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex3f(left, top, 0);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex3f(left, bottom, 0);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex3f(right, bottom, 0);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex3f(right, top, 0);
        GL11.glEnd();
    }

    private void packPalette(IntList overrides) {
        if (overrides == packedColors || overrides.equals(packedColors)) return;
        palette.clear();
        for (int i = 0; i < MAX_PALETTE_COLORS; i++) {
            int color = overrides.isEmpty() ? colors[Math.min(i, colors.length - 1)]
                : overrides.getInt(Math.min(i, overrides.size() - 1));
            palette.put((color >> 16 & 255) / 255f);
            palette.put((color >> 8 & 255) / 255f);
            palette.put((color & 255) / 255f);
        }
        palette.flip();
        packedColors = overrides;
    }

    /** Supplies effect-specific uniforms after the shared draw parameters have been bound. */
    protected void configureUniforms(TextShader shader, TextRenderContext context) {}

    /** Binds effect-specific textures after uniforms and before the mask quad is drawn. */
    protected void configureTextures(TextShader shader, TextRenderContext context) {}

    @Override
    public void close() {
        shader.close();
    }
}

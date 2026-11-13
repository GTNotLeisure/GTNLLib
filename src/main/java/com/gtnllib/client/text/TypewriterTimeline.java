package com.gtnllib.client.text;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

import net.minecraft.client.gui.FontRenderer;

import com.gtnllib.client.text.EffectTextLayout.DrawRun;
import com.gtnllib.client.text.EffectTextLayout.Glyph;
import com.gtnllib.client.text.EffectTextLayout.Layout;
import com.gtnllib.text.TextEffectStyle;
import com.gtnllib.text.TextEffectStyle.Roll;
import com.gtnllib.text.TextEffectStyle.Typewriter;
import com.gtnllib.text.TextEntrance;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Render-thread-owned typing clock. A marked string starts typing when it appears and starts over after it has not
 * been drawn for a frame. Lines inside one string type one after another, and so do strings drawn lower at the same x
 * in the same frame, such as the lines of a tooltip.
 */
public class TypewriterTimeline {

    public static final TypewriterTimeline INSTANCE = new TypewriterTimeline();
    private static final double CARET_BLINK_SECONDS = 0.4;
    private static final long FORGET_AFTER_FRAMES = 2;
    private static final long SWEEP_INTERVAL_FRAMES = 120;
    private static final int MAX_APPEARANCES = 256;
    private static final int MAX_CACHED_TEXT_LENGTH = 16384;
    private static BooleanSupplier enabled = () -> true;
    private final Object2ObjectLinkedOpenHashMap<AppearanceKey, Appearance> appearances = new Object2ObjectLinkedOpenHashMap<>();
    private final Chain chain = new Chain();
    private long frame;
    private int stillDepth;

    public static void enabledWith(BooleanSupplier setting) {
        enabled = setting;
    }

    /** Text being edited, such as a text field, shows in full until the matching {@link #endStill()}. */
    public void beginStill() {
        stillDepth++;
    }

    public void endStill() {
        stillDepth--;
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event) {
        if (event.phase == Phase.START) nextFrame();
    }

    public void nextFrame() {
        frame++;
        if (frame % SWEEP_INTERVAL_FRAMES == 0) appearances.values()
            .removeIf(appearance -> appearance.lastFrame < frame - FORGET_AFTER_FRAMES);
    }

    public void clear() {
        appearances.clear();
        chain.frame = -1;
        chain.font = null;
    }

    /** Returns null for complete, disabled or oversized animations; oversized text remains fully visible. */
    public Typing plan(FontRenderer font, String text, Layout layout, float x, float y, double seconds) {
        if (stillDepth > 0 || !enabled.getAsBoolean() || text.length() >= MAX_CACHED_TEXT_LENGTH) return null;
        AppearanceKey key = new AppearanceKey(font, text);
        Appearance appearance = appearances.getAndMoveToLast(key);
        if ((appearance == null || appearance.layout != layout) && !typed(layout)) return null;
        if (appearance == null) {
            appearance = new Appearance();
            appearances.putAndMoveToLast(key, appearance);
            if (appearances.size() > MAX_APPEARANCES) appearances.removeFirst();
        }
        if (appearance.lastFrame < frame - FORGET_AFTER_FRAMES) appearance.firstSeen = seconds;
        appearance.lastFrame = frame;
        double start = appearance.firstSeen;
        if (chain.continues(frame, font, x, y)) start = Math.max(start, chain.end);
        if (appearance.layout == layout && seconds - start >= appearance.settled) {
            chain.extend(frame, font, x, y, start + appearance.duration);
            return null;
        }
        Typing typing = schedule(layout, seconds - start, seconds);
        appearance.layout = layout;
        appearance.duration = typing.duration();
        appearance.settled = typing.settled();
        chain.extend(frame, font, x, y, start + typing.duration());
        return seconds - start >= typing.settled() ? null : typing;
    }

    private static boolean typed(Layout layout) {
        for (Glyph glyph : layout.glyphs()) {
            TextEffectStyle style = glyph.style();
            if (glyph.pause() > 0 || style != null && (style.typewriter()
                .rate() > 0
                || style.roll()
                    .seconds() > 0))
                return true;
        }
        return false;
    }

    private static Typing schedule(Layout layout, double elapsed, double seconds) {
        Schedule schedule = new Schedule(layout.runs(), elapsed);
        List<DrawRun> runs = layout.runs();
        for (int start = 0; start < runs.size();) {
            int end = start + 1;
            while (end < runs.size() && runs.get(end)
                .y()
                == runs.get(start)
                    .y())
                end++;
            schedule.line(start, end);
            start = end;
        }
        return schedule.finish(seconds);
    }

    private static boolean hasDigits(DrawRun run) {
        for (Glyph glyph : run.glyphs()) {
            if (NumberRoll.isDigit(glyph.text())) return true;
        }
        return false;
    }

    private static boolean hasVisibleGlyph(List<DrawRun> runs, int start, int end) {
        for (int index = start; index < end; index++) {
            for (Glyph glyph : runs.get(index)
                .glyphs()) {
                if (glyph.width() > 0) return true;
            }
        }
        return false;
    }

    /**
     * {@code counts[i]} is how many glyphs of run {@code i} show, or -1 when all of them do. {@code rolls[i]} is how
     * far the number in run {@code i} has rolled, from 0 to 1, or -1 when it is not rolling. {@code entrances[i]}
     * lists the glyphs of run {@code i} still entering, or null. {@code caretRun} is the run whose next glyph slot
     * holds the visible caret, or -1 when no caret shows. {@code duration} is when the last glyph starts and
     * {@code settled} when every entrance has also finished.
     */
    public record Typing(int[] counts, float[] rolls, GlyphEntrance[][] entrances, int caretRun, double duration,
        double settled) {

        public int count(int run) {
            return counts[run];
        }

        public float roll(int run) {
            return rolls[run];
        }

        public GlyphEntrance[] entrances(int run) {
            return entrances[run];
        }
    }

    /**
     * Glyph {@code index} of a run is {@code progress} (0 to 1) through its entrance {@code kind}; a slam starts at
     * {@code size} times the glyph's size.
     */
    public record GlyphEntrance(int index, float progress, TextEntrance kind, float size) {}

    // Hidden glyphs still advance the clock; a waiting roll advances it once for the entire run.
    private static class Schedule {

        private final List<DrawRun> runs;
        private final double elapsed;
        private final int[] counts;
        private final float[] rolls;
        private final GlyphEntrance[][] entrances;
        private double lineStart;
        private double settled;
        private int caretRun = -1;
        private boolean caretPausing;
        private boolean anyCaret;
        private boolean rolling;
        private int waitingRun = -1;
        private boolean previousPausing;
        private Typewriter lastTypewriter = Typewriter.NONE;
        private double time;
        private double clock;
        private boolean blocked;
        private boolean pausing;

        private Schedule(List<DrawRun> runs, double elapsed) {
            this.runs = runs;
            this.elapsed = elapsed;
            counts = new int[runs.size()];
            rolls = new float[runs.size()];
            Arrays.fill(rolls, -1);
            entrances = new GlyphEntrance[runs.size()][];
        }

        private void line(int start, int end) {
            time = elapsed - lineStart;
            clock = 0;
            blocked = false;
            pausing = false;
            for (int index = start; index < end; index++) {
                DrawRun run = runs.get(index);
                Roll roll = run.style() == null ? Roll.NONE
                    : run.style()
                        .roll();
                if (roll.seconds() > 0 && hasDigits(run)) roll(index, roll);
                else type(index, run);
            }
            if (!anyCaret && !rolling && waitingRun < 0 && time < 0 && hasVisibleGlyph(runs, start, end)) {
                waitingRun = start;
                caretPausing = previousPausing;
            }
            previousPausing = pausing && !blocked;
            lineStart += clock;
        }

        // A number that does not wait lets the text after it type on while it rolls; its end still counts as settling.
        private void roll(int index, Roll roll) {
            float seconds = roll.seconds();
            if (blocked || time < clock) {
                holdCaret(index);
                counts[index] = 0;
                blocked = true;
            } else {
                double progress = (time - clock) / seconds;
                counts[index] = -1;
                if (progress < 1) {
                    rolls[index] = (float) progress;
                    blocked = roll.waits();
                    rolling = rolling || roll.waits();
                }
            }
            if (roll.waits()) clock += seconds;
            else settled = Math.max(settled, lineStart + clock + seconds);
        }

        private void type(int index, DrawRun run) {
            Typewriter typewriter = typewriterOf(run.style());
            float rate = typewriter.rate();
            double entrance = rate > 0 && typewriter.entrance() != TextEntrance.NONE ? typewriter.entranceDuration()
                : 0;
            List<Glyph> glyphs = run.glyphs();
            List<GlyphEntrance> fresh = null;
            int count = -1;
            for (int i = 0; i < glyphs.size(); i++) {
                Glyph glyph = glyphs.get(i);
                if (glyph.pause() > 0) {
                    if (!blocked && time >= clock && time < clock + glyph.pause()) pausing = true;
                    clock += glyph.pause();
                    continue;
                }
                if (glyph.width() <= 0) continue;
                double due = rate > 0 ? clock + 1 / rate : clock;
                if (blocked || time < due) {
                    holdCaret(index);
                    if (count < 0) count = i;
                    blocked = true;
                } else if (entrance > 0 && time - due < entrance) {
                    if (fresh == null) fresh = new ObjectArrayList<>();
                    fresh.add(
                        new GlyphEntrance(
                            i,
                            (float) ((time - due) / entrance),
                            typewriter.entrance(),
                            typewriter.slamSize()));
                }
                if (entrance > 0) settled = Math.max(settled, lineStart + due + entrance);
                clock = due;
            }
            counts[index] = count;
            if (fresh != null) entrances[index] = fresh.toArray(new GlyphEntrance[0]);
        }

        private Typewriter typewriterOf(TextEffectStyle style) {
            if (style == null) return Typewriter.NONE;
            Typewriter own = style.typewriter();
            if (style.roll()
                .seconds() > 0 && own.rate() == 0) return lastTypewriter;
            if (own.rate() > 0) lastTypewriter = own;
            return own;
        }

        private void holdCaret(int index) {
            if (blocked || time < 0) return;
            caretRun = index;
            caretPausing = pausing;
            anyCaret = true;
        }

        private Typing finish(double seconds) {
            if (!anyCaret && !rolling && waitingRun >= 0) caretRun = waitingRun;
            boolean caretVisible = caretRun >= 0
                && (!caretPausing || Math.floorMod((long) Math.floor(seconds / CARET_BLINK_SECONDS), 2) == 0);
            return new Typing(
                counts,
                rolls,
                entrances,
                caretVisible ? caretRun : -1,
                lineStart,
                Math.max(lineStart, settled));
        }
    }

    private static class Appearance {

        private double firstSeen;
        private long lastFrame = Long.MIN_VALUE / 2;
        private Layout layout;
        private double duration;
        private double settled;
    }

    private record AppearanceKey(FontRenderer font, String text) {}

    private static class Chain {

        private long frame = -1;
        private FontRenderer font;
        private float x;
        private float y;
        private double end;

        private boolean continues(long currentFrame, FontRenderer currentFont, float currentX, float currentY) {
            return frame == currentFrame && font == currentFont && Math.abs(x - currentX) < 0.5f && currentY > y;
        }

        private void extend(long currentFrame, FontRenderer currentFont, float currentX, float currentY,
            double typedUntil) {
            frame = currentFrame;
            font = currentFont;
            x = currentX;
            y = currentY;
            end = typedUntil;
        }
    }
}

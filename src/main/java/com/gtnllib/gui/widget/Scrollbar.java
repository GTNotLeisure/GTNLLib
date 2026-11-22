package com.gtnllib.gui.widget;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Mouse;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Unit.Measure;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2Look;

public class Scrollbar extends Widget<Scrollbar> implements Interactable {

    private static final int BORDER = 1, PLAIN_MIN_HANDLE = 8;
    private final IntSupplier position, maxPosition;
    private final IntConsumer scrollTo;
    private @Nullable IntSupplier plainVisible;
    private final PressRepeat paging = new PressRepeat();
    private int step = 1, plainColor, grab = -1, pressMouse, pressPosition;
    private boolean small, pages;

    /**
     * Creates a scroll bar over an owner's position. The caller sets its height; its width follows the look unless the
     * caller sets one.
     *
     * @param position    supplier of the current position, read every frame and on every input; values outside zero to
     *                    the maximum are treated as the nearest end
     * @param maxPosition supplier of the last position, the number of positions that do not fit; zero or less means
     *                    everything fits and the bar is disabled
     * @param scrollTo    receives every new position chosen with the bar or the wheel, from zero to the maximum; it is
     *                    not called when the position stays the same
     */
    public Scrollbar(IntSupplier position, IntSupplier maxPosition, IntConsumer scrollTo) {
        this.position = position;
        this.maxPosition = maxPosition;
        this.scrollTo = scrollTo;
        width(this::lookWidth, Measure.PIXEL);
    }

    private int lookWidth() {
        return small ? AE2Theme.SMALL_SCROLLER_WIDTH
            : AE2Look.of(this)
                .scrollbarWidth();
    }

    /**
     * Sets how far a wheel notch moves, and a press beside the handle when paging is on, like the page size of AE2's
     * scroll bar. AE2's network status and crafting screens move one row, its terminals a sixth of the visible rows and
     * its crafting CPU list a third.
     *
     * @param step positions per notch and per page, at least one; the default is one
     * @return this scroll bar
     */
    public Scrollbar step(int step) {
        this.step = Math.max(1, step);
        return this;
    }

    /**
     * Chooses what a press on the track beside the handle does.
     *
     * @param pages {@code true} to page one {@link #step(int) step} towards the pointer like AE2, repeating while the
     *              button is held; {@code false}, the default, to move the handle's centre to the pointer and keep
     *              dragging
     * @return this scroll bar
     */
    public Scrollbar pageOnTrackPress(boolean pages) {
        this.pages = pages;
        return this;
    }

    /**
     * Chooses the small scroll bar, as in AE2's processing pattern panel, instead of the large one. Unless the caller
     * sets a width the bar is then {@link AE2Theme#SMALL_SCROLLER_WIDTH} pixels wide in every look; other themes draw
     * Minecraft's handle narrowed to fit.
     *
     * @param small whether to draw the 7-pixel-wide scroll bar
     * @return this scroll bar
     */
    public Scrollbar small(boolean small) {
        this.small = small;
        return this;
    }

    /**
     * Replaces the vanilla scroll bar outside AE2 themes with a plain handle as wide as the bar, whose length is the
     * visible share of all positions (at least eight pixels), with no track; for lists that have a dark look of their
     * own. It is drawn only while there is something to scroll.
     *
     * @param color   ARGB color of the handle
     * @param visible supplier of how many positions are visible at once
     * @return this scroll bar
     */
    public Scrollbar plainBar(int color, IntSupplier visible) {
        plainColor = color;
        plainVisible = visible;
        return this;
    }

    /**
     * Returns the current position, clamped to the range.
     *
     * @return position from zero to the maximum
     */
    public int position() {
        return Math.max(0, Math.min(max(), position.getAsInt()));
    }

    /**
     * Moves one {@link #step(int) step} for a wheel notch, as the wheel over the bar does.
     *
     * @param direction wheel direction; up moves towards position zero
     * @return whether there is a range to scroll, as AE2's scroll bar reports it
     */
    public boolean scroll(UpOrDown direction) {
        if (max() == 0) return false;
        moveTo(position() - (long) direction.modifier * step);
        return true;
    }

    private int max() {
        return Math.max(0, maxPosition.getAsInt());
    }

    private void moveTo(long target) {
        int clamped = (int) Math.max(0, Math.min(max(), target)), current = position();
        if (clamped != current) scrollTo.accept(clamped);
    }

    private boolean plain(AE2Look look) {
        return !look.isAE2() && plainVisible != null;
    }

    private int border(AE2Look look) {
        return plain(look) ? 0 : BORDER;
    }

    private int handleLength(AE2Look look) {
        if (!plain(look)) return AE2Theme.SCROLLER_HEIGHT;
        int visible = Math.max(1, plainVisible.getAsInt()), height = getArea().height;
        return (int) Math.min(height, Math.max(PLAIN_MIN_HANDLE, (long) height * visible / ((long) max() + visible)));
    }

    private int travel(AE2Look look) {
        return getArea().height - 2 * border(look) - handleLength(look);
    }

    private int handleOffset(AE2Look look) {
        int max = max(), travel = travel(look);
        return max == 0 || travel <= 0 ? 0 : (int) ((long) travel * position() / max);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        AE2Look look = AE2Look.of(this);
        boolean active = max() > 0;
        int height = getArea().height, offset = handleOffset(look);
        if (plain(look)) {
            if (active) GuiDraw.drawRect(0, offset, getArea().width, handleLength(look), plainColor);
        } else if (small) look.drawSmallScrollbar(0, 0, height, offset, active);
        else look.drawScrollbar(0, 0, height, offset, active);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (mouseButton != 0) return Result.IGNORE;
        grab = -1;
        paging.stop();
        if (max() == 0) return Result.SUCCESS;
        AE2Look look = AE2Look.of(this);
        int mouseY = getContext().getMouseY(), top = border(look) + handleOffset(look), length = handleLength(look);
        if (mouseY >= top && mouseY < top + length) {
            grab = mouseY - top;
            pressMouse = mouseY;
            pressPosition = position();
        } else if (pages) {
            paging.start(mouseY < top ? -1 : 1);
            page();
        } else {
            int max = max(), travel = travel(look);
            long target = travel <= 0 ? position()
                : Math.round(((double) mouseY - border(look) - length / 2) * max / travel);
            pressPosition = (int) Math.max(0, Math.min(max, target));
            moveTo(pressPosition);
            grab = length / 2;
            pressMouse = mouseY;
        }
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        if (grab < 0 || mouseButton != 0) return;
        AE2Look look = AE2Look.of(this);
        int max = max(), travel = travel(look), mouseY = getContext().getMouseY();
        if (max == 0 || travel <= 0) return;
        int handle = mouseY - grab - border(look);
        if (mouseY == pressMouse) moveTo(pressPosition);
        else if (handle <= 0) moveTo(0);
        else if (handle >= travel) moveTo(max);
        else moveTo(Math.round(pressPosition + ((double) mouseY - pressMouse) * max / travel));
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        if (mouseButton != 0) return false;
        boolean handled = grab >= 0 || paging.isActive();
        grab = -1;
        paging.stop();
        return handled;
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        return scroll(scrollDirection);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isValid() || !areAncestorsEnabled()) {
            grab = -1;
            paging.stop();
            return;
        }
        if (grab >= 0 && !Mouse.isButtonDown(0)) grab = -1;
        if (paging.due()) page();
    }

    @Override
    public void dispose() {
        grab = -1;
        paging.stop();
        super.dispose();
    }

    private void page() {
        moveTo(position() + (long) paging.direction() * step);
    }
}

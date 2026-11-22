package com.gtnllib.gui.widget;

import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.AE2Variant;
import com.gtnllib.gui.theme.ae2.PlainLook;

public class ProgressBar extends ProgressWidget {

    /**
     * Creates a bar of AE2's size that fills upwards. Set its value with {@code value(...)} or {@code syncHandler}.
     */
    public ProgressBar() {
        size(AE2Theme.PROGRESS_WIDTH, AE2Theme.PROGRESS_HEIGHT);
        direction(Direction.UP);
    }

    /**
     * Sets the direction the bar fills in.
     *
     * @param direction {@code UP}, {@code DOWN}, {@code LEFT} or {@code RIGHT}
     * @throws IllegalArgumentException for {@code CIRCULAR_CW}, which needs a texture split into quarters
     */
    @Override
    public ProgressWidget direction(Direction direction) {
        if (direction == Direction.CIRCULAR_CW)
            throw new IllegalArgumentException("A ProgressBar fills in a straight line, not around a circle");
        return super.direction(direction);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        AE2Look look = AE2Look.of(this);
        AE2Variant variant = look.variant();
        int width = getArea().width, height = getArea().height;
        boolean upright = getDirection() != Direction.LEFT && getDirection() != Direction.RIGHT;
        if (variant != null) {
            texture(variant.progressEmpty, variant.progressFull, upright ? height : width);
            super.draw(context, widgetTheme);
            return;
        }
        look.drawWell(0, 0, width, height);
        int innerWidth = width - 2, innerHeight = height - 2;
        double progress = MathUtils.clamp(getCurrentProgress(), 0, 1);
        int filled = (int) Math.floor(progress * (upright ? innerHeight : innerWidth));
        if (filled <= 0) return;
        switch (getDirection()) {
            case RIGHT -> GuiDraw.drawRect(1, 1, filled, innerHeight, PlainLook.GOOD);
            case LEFT -> GuiDraw.drawRect(1 + innerWidth - filled, 1, filled, innerHeight, PlainLook.GOOD);
            case DOWN -> GuiDraw.drawRect(1, 1, innerWidth, filled, PlainLook.GOOD);
            default -> GuiDraw.drawRect(1, 1 + innerHeight - filled, innerWidth, filled, PlainLook.GOOD);
        }
    }
}

package com.gtnllib.gui.widget;

import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.AE2Variant;

/**
 * AE2's progress bar: the inscriber's {@link AE2Theme#PROGRESS_WIDTH} by {@link AE2Theme#PROGRESS_HEIGHT} bar that
 * fills upwards, drawn from the skin of the panel it is in. Outside AE2 themes it is a sunken slot-style well that
 * fills
 * with green. AE2's sprite is upright, so under AE2 themes the bar is meant to fill up or down; other directions
 * stretch
 * it.
 * <p>
 * It is MUI2's {@link ProgressWidget}, so it takes an {@code IDoubleValue} from 0 to 1 or a synced value and works in
 * GUIs the server opens. Like AE2's, it fills in whole pixels.
 */
public class ProgressBar extends ProgressWidget {

    private static final int PLAIN_FILL = 0xFF60E060;

    public ProgressBar() {
        size(AE2Theme.PROGRESS_WIDTH, AE2Theme.PROGRESS_HEIGHT);
        direction(Direction.UP);
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
            case RIGHT -> GuiDraw.drawRect(1, 1, filled, innerHeight, PLAIN_FILL);
            case LEFT -> GuiDraw.drawRect(1 + innerWidth - filled, 1, filled, innerHeight, PLAIN_FILL);
            case DOWN -> GuiDraw.drawRect(1, 1, innerWidth, filled, PLAIN_FILL);
            default -> GuiDraw.drawRect(1, 1 + innerHeight - filled, innerWidth, filled, PLAIN_FILL);
        }
    }
}

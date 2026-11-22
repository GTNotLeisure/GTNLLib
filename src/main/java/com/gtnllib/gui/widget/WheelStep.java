package com.gtnllib.gui.widget;

import java.util.function.IntPredicate;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.IWidget;

/**
 * The mouse-wheel rule of every GTNLLib widget that changes a value with the wheel. The wheel is off until
 * {@code wheel(true)}, so a wheel turned over a scrolling list keeps scrolling the list. It acts only while the pointer
 * is over the widget, so a focused text field elsewhere does not take it, and never while the widget is unavailable,
 * so the wheel goes to the widgets around it. Widgets keep one of these behind {@link IWheelStep}, say what one notch
 * does, and route {@code onMouseScroll} through {@link #scroll}.
 */
public class WheelStep {

    private boolean on;

    /**
     * Turns the wheel on or off.
     *
     * @param on true to let the wheel change the value
     */
    public void set(boolean on) {
        this.on = on;
    }

    /**
     * Reports whether the wheel is on.
     *
     * @return {@code false} until {@link #set(boolean)} turns it on
     */
    public boolean isOn() {
        return on;
    }

    /**
     * Answers a wheel notch by the rule above.
     *
     * @param widget    widget the wheel turned over, or the focused widget MUI2 offers the wheel to first
     * @param available whether the widget can be used right now
     * @param direction wheel direction
     * @param notch     what one notch does, given {@code 1} for up and {@code -1} for down; returns whether it changed
     *                  anything, false when there is nothing to change
     * @return whether the wheel was used, which stops MUI2 offering it to other widgets
     */
    public boolean scroll(IWidget widget, boolean available, UpOrDown direction, IntPredicate notch) {
        return on && available && widget.isBelowMouse() && notch.test(direction.modifier);
    }
}

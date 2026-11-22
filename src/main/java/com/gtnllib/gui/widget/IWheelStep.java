package com.gtnllib.gui.widget;

/**
 * A widget whose value the mouse wheel can change, by the rule of {@link WheelStep}. The setter is written once here: a
 * widget keeps a {@link WheelStep}, returns it from {@link #wheelStep()} and says in its class documentation what one
 * notch does.
 *
 * @param <W> the widget's own type, so chained calls keep returning it
 */
public interface IWheelStep<W extends IWheelStep<W>> {

    /**
     * Returns the state behind {@link #wheel(boolean)}.
     *
     * @return the same object on every call
     */
    WheelStep wheelStep();

    /**
     * Sets whether the mouse wheel over the widget changes its value. Off by default, so the wheel goes to the widgets
     * around it, such as a scrolling list.
     *
     * @param wheel true to let the wheel change the value
     * @return this widget
     */
    @SuppressWarnings("unchecked")
    default W wheel(boolean wheel) {
        wheelStep().set(wheel);
        return (W) this;
    }
}

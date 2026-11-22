package com.gtnllib.gui.widget;

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
     */
    @SuppressWarnings("unchecked")
    default W wheel(boolean wheel) {
        wheelStep().set(wheel);
        return (W) this;
    }
}

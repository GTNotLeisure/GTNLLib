package com.gtnllib.gui.widget;

import java.util.function.BooleanSupplier;

/**
 * A widget that can be made unavailable. While it cannot be used it looks disabled, shows no hover, swallows mouse
 * presses ({@link Availability#press}) and leaves the mouse wheel to the widgets around it ({@link WheelStep}). The
 * setter is written once here: a widget keeps an {@link Availability}, returns it from {@link #availability()}, routes
 * its presses through it and reads {@link #isAvailable()} wherever it draws or handles input.
 *
 * @param <W> the widget's own type, so chained calls keep returning it
 */
public interface IAvailable<W extends IAvailable<W>> {

    /**
     * Returns the state behind {@link #available(BooleanSupplier)}.
     *
     * @return the same object on every call
     */
    Availability availability();

    /**
     * Sets when the widget can be used; it looks disabled otherwise.
     *
     * @param available supplier read every frame and on every input
     * @return this widget
     */
    @SuppressWarnings("unchecked")
    default W available(BooleanSupplier available) {
        availability().set(available);
        return (W) this;
    }

    /**
     * Reports whether the widget can be used right now.
     *
     * @return the current answer of the supplier given to {@link #available(BooleanSupplier)}
     */
    default boolean isAvailable() {
        return availability().get();
    }
}

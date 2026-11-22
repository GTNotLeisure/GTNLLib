package com.gtnllib.gui.widget;

import java.util.function.BooleanSupplier;

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

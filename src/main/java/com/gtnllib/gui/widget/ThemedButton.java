package com.gtnllib.gui.widget;

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.widgets.ButtonWidget;

/**
 * The button the AE2 buttons build on: {@link AE2Button}, {@link IconButton} and {@link TabButton}. On its own, with
 * an empty background, it is an invisible click area. While it is not {@link #available(BooleanSupplier) available}
 * it looks disabled, shows no hover and swallows every mouse button, so clicks never reach a widget below it. It can
 * be built on either side, so it also works in GUIs the server opens.
 */
public class ThemedButton extends ButtonWidget<ThemedButton> {

    private BooleanSupplier available = () -> true, selected = () -> false;

    /**
     * Sets when the button can be used; it looks disabled otherwise.
     *
     * @param available supplier read every frame and on every click
     * @return this button
     */
    public ThemedButton available(BooleanSupplier available) {
        this.available = available;
        return this;
    }

    /**
     * Sets when the button shows the theme's selected look, such as the button of the current choice.
     *
     * @param selected supplier read every frame
     * @return this button
     */
    public ThemedButton selected(BooleanSupplier selected) {
        this.selected = selected;
        return this;
    }

    /**
     * Sets what a left click does while the button is available; other mouse buttons do nothing. A mouse handler set
     * afterwards replaces it.
     *
     * @param action run on a left click
     * @return this button
     */
    public ThemedButton action(Runnable action) {
        return onMousePressed(button -> {
            if (button != 0) return false;
            action.run();
            return true;
        });
    }

    protected boolean isAvailable() {
        return available.getAsBoolean();
    }

    protected boolean isSelected() {
        return selected.getAsBoolean();
    }

    @Override
    public boolean canBeSeen(IViewportStack stack) {
        return isEnabled() && super.canBeSeen(stack);
    }

    @Override
    public @NotNull Result onMousePressed(int button) {
        return isAvailable() ? super.onMousePressed(button) : Result.STOP;
    }
}

package com.gtnllib.gui.widget;

import java.util.function.Supplier;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2Buttons;
import com.gtnllib.gui.theme.ae2.IconSource;

/**
 * AE2's toolbar icon button ({@code IconButton}): {@link AE2Buttons#iconButton}, {@link AE2Theme#ICON_BUTTON_WIDTH} by
 * {@link AE2Theme#ICON_BUTTON_HEIGHT} pixels, drawn by the theme of its panel; a selected one shows AE2's selected
 * frame. It reacts to the left mouse button on its whole frame, where AE2's react only on the 16-by-16 icon square;
 * see {@link ThemedButton} for the available and selected states.
 */
public class IconButton extends ThemedButton {

    /**
     * Creates a toolbar icon button.
     *
     * @param icon   supplier of the icon, read every frame: a built-in {@code AE2Icon}, or any texture or item through
     *               {@link IconSource#of}
     * @param action run on a left click while the button is available
     */
    public IconButton(Supplier<? extends IconSource> icon, Runnable action) {
        IDrawable look = AE2Buttons.iconButton(this, icon, this::isAvailable, this::isSelected);
        background(look).hoverBackground(look)
            .size(AE2Theme.ICON_BUTTON_WIDTH, AE2Theme.ICON_BUTTON_HEIGHT)
            .action(action);
    }
}

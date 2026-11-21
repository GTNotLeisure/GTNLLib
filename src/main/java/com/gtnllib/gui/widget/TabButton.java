package com.gtnllib.gui.widget;

import java.util.function.Supplier;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2ButtonPainter;
import com.gtnllib.gui.theme.ae2.IconSource;

/**
 * AE2's framed tab button ({@code TabButton}): {@link AE2ButtonPainter#tabButton}, {@link AE2Theme#TAB_BUTTON_SIZE}
 * pixels square, drawn by the theme of its panel. AE2 uses it for the button that returns from a sub-screen and places
 * it five pixels above the panel's top edge. Other themes draw a plain button with the icon. It reacts to the left
 * mouse button; see {@link ThemedButton} for the available state. Like AE2's, it has no selected look, so
 * {@link #selected} changes nothing.
 */
public class TabButton extends ThemedButton<TabButton> {

    /**
     * Creates a tab button.
     *
     * @param icon   supplier of the icon, read every frame
     * @param action run on a left click while the button is available
     */
    public TabButton(Supplier<? extends IconSource> icon, Runnable action) {
        IDrawable look = AE2ButtonPainter.tabButton(this, icon, this::isAvailable);
        background(look).hoverBackground(look)
            .size(AE2Theme.TAB_BUTTON_SIZE, AE2Theme.TAB_BUTTON_SIZE)
            .action(action);
    }
}

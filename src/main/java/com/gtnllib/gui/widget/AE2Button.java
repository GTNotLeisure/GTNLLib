package com.gtnllib.gui.widget;

import java.util.function.Supplier;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.gtnllib.gui.theme.ae2.AE2ButtonPainter;

public class AE2Button extends ThemedButton<AE2Button> {

    /**
     * Creates a text button.
     *
     * @param label  supplier of the label, read every frame; under AE2 themes a label wider than the button scrolls
     *               inside it, other themes trim it to the button width
     * @param action run on a left click while the button is available
     */
    public AE2Button(Supplier<String> label, Runnable action) {
        IDrawable look = AE2ButtonPainter.button(this, this::isAvailable, this::isSelected);
        background(look).hoverBackground(look)
            .overlay(AE2ButtonPainter.label(this, label, this::isAvailable, this::isSelected))
            .action(action);
    }
}

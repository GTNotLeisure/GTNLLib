package com.gtnllib.gui.widget;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.value.IBoolValue;
import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.widget.sizer.Unit.Measure;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2CheckboxPainter;

/**
 * AE2's check box with its label ({@code AECheckbox}), or one radio button of a group, drawn by the theme of its panel
 * ({@link AE2CheckboxPainter#checkbox}, {@link AE2CheckboxPainter#radio}). A left click anywhere on it flips the check
 * box, or selects the radio button. Radio buttons that share one integer value form a group: each stands for one value,
 * and selecting it sets the value, so only one is selected at a time.
 * <p>
 * Values are MUI2 values, so a sync value registered with the panel's sync manager makes it work in GUIs the server
 * opens. Unless the caller sets a size, it is as wide as its box and one line of label, measured while the panel is
 * laid out, and as high as the box. It is a {@link ThemedButton}, so {@code available} disables it.
 */
public class AECheckbox extends ThemedButton<AECheckbox> {

    /**
     * Creates a check box.
     *
     * @param label supplier of the label, read every frame
     * @param value value the box shows and flips
     */
    public AECheckbox(Supplier<String> label, IBoolValue<?> value) {
        this(AE2Theme.CHECKBOX_HEIGHT, () -> AE2CheckboxPainter.checkboxWidth(label.get()));
        overlay(AE2CheckboxPainter.checkbox(this, value::getBoolValue, label, this::isAvailable));
        action(() -> value.setBoolValue(!value.getBoolValue()));
    }

    private AECheckbox(int height, IntSupplier width) {
        background(IDrawable.EMPTY).hoverBackground(IDrawable.EMPTY)
            .width(width::getAsInt, Measure.PIXEL)
            .height(height);
    }

    /**
     * Creates a radio button of the group that shares a value.
     *
     * @param label  supplier of the label, read every frame
     * @param value  value shared by the group's radio buttons
     * @param option value this radio button stands for
     * @return the radio button
     */
    public static AECheckbox radio(Supplier<String> label, IIntValue<?> value, int option) {
        AECheckbox radio = new AECheckbox(AE2Theme.RADIO_SIZE, () -> AE2CheckboxPainter.radioWidth(label.get()));
        radio.overlay(AE2CheckboxPainter.radio(radio, () -> value.getIntValue() == option, label, radio::isAvailable));
        radio.action(() -> value.setIntValue(option));
        return radio;
    }
}

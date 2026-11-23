package com.gtnllib.gui.widget;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.widget.sizer.Unit.Measure;
import com.cleanroommc.modularui.widgets.AbstractCycleButtonWidget;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2ButtonPainter;
import com.gtnllib.gui.theme.ae2.AE2Icon;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.IconSource;

public class SettingToggleButton extends AbstractCycleButtonWidget<SettingToggleButton>
    implements IAvailable<SettingToggleButton> {

    private IconSource[] icons = new IconSource[0];
    private final Availability availability = new Availability();
    private IKey title = IKey.EMPTY;

    /**
     * Creates a setting button of the toolbar button size: AE2's 18 by 20 under AE2 themes, an 18-pixel square in
     * others ({@link AE2Look#iconButtonHeight()}). Set its value, then its states.
     */
    public SettingToggleButton() {
        IDrawable look = AE2ButtonPainter.iconButton(this, this::icon, this::isAvailable);
        background(look).hoverBackground(look)
            .width(AE2Theme.ICON_BUTTON_WIDTH)
            .height(
                () -> AE2Look.of(this)
                    .iconButtonHeight(),
                Measure.PIXEL);
    }

    /**
     * Sets the value the button shows and changes.
     *
     * @param value an integer, enum or boolean value, or a synced one
     */
    @Override
    public SettingToggleButton value(IIntValue<?> value) {
        return super.value(value);
    }

    /**
     * Sets how many values a plain integer value cycles through, from 0 to {@code count - 1}.
     *
     * @param count number of values, at least one
     */
    @Override
    public SettingToggleButton stateCount(int count) {
        return super.stateCount(count);
    }

    /**
     * Sets the first tooltip line of every value, such as the name of the setting.
     *
     * @param title setting name
     */
    public SettingToggleButton title(IKey title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the icon and the tooltip description of one value.
     *
     * @param state       value index, from zero
     * @param icon        icon shown while the value is current
     * @param description tooltip lines below the title while the value is current
     */
    public SettingToggleButton state(int state, IconSource icon, IKey... description) {
        if (state >= icons.length) icons = Arrays.copyOf(icons, state + 1);
        icons[state] = icon;
        return tooltipBuilder(state, tooltip -> {
            tooltip.addLine(title);
            for (IKey line : description) tooltip.addLine(line);
        });
    }

    /**
     * Sets the icon and the tooltip description of one enum value.
     *
     * @param state       enum value
     * @param icon        icon shown while the value is current
     * @param description tooltip lines below the title while the value is current
     * @param <E>         enum type of the button's value
     */
    public <E extends Enum<E>> SettingToggleButton state(E state, IconSource icon, IKey... description) {
        return state(state.ordinal(), icon, description);
    }

    /**
     * Sets the icon and the tooltip description of the on or off value of a boolean value.
     *
     * @param state       {@code true} for on
     * @param icon        icon shown while the value is current
     * @param description tooltip lines below the title while the value is current
     */
    public SettingToggleButton state(boolean state, IconSource icon, IKey... description) {
        return state(state ? 1 : 0, icon, description);
    }

    @Override
    public Availability availability() {
        return availability;
    }

    private IconSource icon() {
        int state = getState();
        return state >= 0 && state < icons.length && icons[state] != null ? icons[state] : AE2Icon.MORE;
    }

    @Override
    public boolean canBeSeen(IViewportStack stack) {
        return isEnabled() && super.canBeSeen(stack);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        return availability.press(() -> super.onMousePressed(mouseButton));
    }
}

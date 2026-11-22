package com.gtnllib.gui.widget;

import java.util.function.IntConsumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.gtnllib.gui.theme.ae2.AE2Look;

public class ValueStepper extends ParentWidget<ValueStepper>
    implements Interactable, IListRowControl, IAvailable<ValueStepper>, IWheelStep<ValueStepper> {

    /**
     * Width of each arrow button in pixels.
     */
    public static final int ARROW_WIDTH = 12;
    private final IntConsumer step;
    private final TextWidget<?> name;
    private final Availability availability = new Availability();
    private final WheelStep wheelStep = new WheelStep();
    private boolean inListRow;

    /**
     * Creates a stepper; the caller sets its size, and the arrows fill its height.
     *
     * @param value supplier of the current value's name, read every frame
     * @param step  called with -1 or +1 for an arrow click or a wheel notch
     */
    public ValueStepper(Supplier<String> value, IntConsumer step) {
        this.step = step;
        name = IKey.dynamic(value)
            .asWidget();
        child(
            Flow.row()
                .sizeRel(1f)
                .childPadding(1)
                .child(
                    new AE2Button(() -> "<", () -> step.accept(-1)).available(this::isAvailable)
                        .width(ARROW_WIDTH)
                        .heightRel(1f))
                .child(
                    name.alignment(Alignment.Center)
                        .expanded()
                        .heightRel(1f))
                .child(
                    new AE2Button(() -> ">", () -> step.accept(1)).available(this::isAvailable)
                        .width(ARROW_WIDTH)
                        .heightRel(1f)));
    }

    @Override
    public Availability availability() {
        return availability;
    }

    @Override
    public WheelStep wheelStep() {
        return wheelStep;
    }

    @Override
    public void placedInListRow() {
        inListRow = true;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        return availability.press(() -> Result.IGNORE);
    }

    @Override
    public boolean canHover() {
        return true;
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        return wheelStep.scroll(this, isAvailable(), scrollDirection, sign -> {
            step.accept(sign);
            return true;
        });
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        boolean usable = isAvailable();
        AE2Look look = AE2Look.of(this);
        if (inListRow) name.color(usable ? look.listText() : look.listMuted())
            .shadow(usable && look.listShadow());
        else name.color(look.panelText(usable))
            .shadow(false);
    }
}

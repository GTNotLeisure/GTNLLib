package com.gtnllib.gui.widget;

import java.math.BigDecimal;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.regex.Pattern;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.value.ILongValue;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.utils.ParseResult;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Unit.Measure;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.AE2Variant;
import com.gtnllib.utils.LongMath;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class NumberEntryWidget extends ParentWidget<NumberEntryWidget>
    implements IAvailable<NumberEntryWidget>, IWheelStep<NumberEntryWidget> {

    /**
     * Default widget dimensions in GUI pixels; lower entries use the single-row layout.
     */
    public static final int WIDTH = 138, HEIGHT = 62;
    /**
     * Width of the {@code <} and {@code >} buttons of the single-row layout.
     */
    public static final int ARROW_WIDTH = 12;
    private static final long[] STEPS = { 1, 10, 100, 1000 }, SHIFT_STEPS = { 1, 16, 32, 64 };
    private static final int[] BUTTON_X = { 0, 28, 62, 100 }, BUTTON_WIDTH = { 22, 28, 32, 38 };
    private static final int FIELD_X = 28, FIELD_Y = 25, FIELD_WIDTH = 66, ICON_X = 104, ICON_Y = 27, ARROW_GAP = 1,
        FINE_STEP = 1, SHIFT_FINE_STEP = 10, MAX_LENGTH = 64;
    private static final String LANG = "gtnllib.gui.number_entry.";
    private static final Pattern INTEGER = Pattern.compile("-?\\d+");

    private final Field field = new Field();
    private final Warning warning = new Warning();
    private final List<String> messages = new ObjectArrayList<>();
    private String text = "0";
    private long min, max = Long.MAX_VALUE, value, reset;
    private boolean inputValid = true;
    private LongConsumer listener = ignored -> {};
    private final Availability availability = new Availability();
    private final WheelStep wheelStep = new WheelStep();

    /**
     * Creates an entry with value zero and inclusive bounds from zero to {@link Long#MAX_VALUE}. Buttons, text field
     * and validation feedback follow the theme the entry is drawn with.
     */
    public NumberEntryWidget() {
        size(WIDTH, HEIGHT);
        for (int i = 0; i < STEPS.length; i++) {
            int index = i;
            child(
                step(i, 1).left(() -> scaled(BUTTON_X[index]), Measure.PIXEL)
                    .width(() -> scaledWidth(BUTTON_X[index], BUTTON_WIDTH[index]), Measure.PIXEL)
                    .top(0)
                    .height(AE2Theme.BUTTON_HEIGHT));
            child(
                step(i, -1).left(() -> scaled(BUTTON_X[index]), Measure.PIXEL)
                    .width(() -> scaledWidth(BUTTON_X[index], BUTTON_WIDTH[index]), Measure.PIXEL)
                    .bottom(0)
                    .height(AE2Theme.BUTTON_HEIGHT));
        }
        child(
            arrow(-1).left(0)
                .top(0)
                .width(ARROW_WIDTH)
                .heightRel(1f));
        child(
            arrow(1).right(0)
                .top(0)
                .width(ARROW_WIDTH)
                .heightRel(1f));
        field.setMaxLength(MAX_LENGTH)
            .setTooltipOverride(true)
            .autoUpdateOnChange(true)
            .value(new StringValue.Dynamic(() -> text, next -> {
                text = next;
                validate(true);
            }))
            .tooltipAutoUpdate(true)
            .tooltipBuilder(tooltip -> messages.forEach(tooltip::addLine));
        child(
            field.left(() -> compact() ? ARROW_WIDTH + ARROW_GAP : scaled(FIELD_X), Measure.PIXEL)
                .width(
                    () -> compact() ? getArea().width - 2 * (ARROW_WIDTH + ARROW_GAP)
                        : scaledWidth(FIELD_X, FIELD_WIDTH),
                    Measure.PIXEL)
                .top(() -> compact() ? 0 : FIELD_Y + centerShift(), Measure.PIXEL)
                .height(() -> compact() ? getArea().height : AE2Theme.TEXT_FIELD_HEIGHT, Measure.PIXEL));
        child(
            warning.left(() -> scaled(ICON_X), Measure.PIXEL)
                .top(() -> ICON_Y + centerShift(), Measure.PIXEL)
                .size(AE2Theme.VALIDATION_ICON_SIZE, AE2Theme.VALIDATION_ICON_SIZE)
                .setEnabledIf(icon -> !inputValid && !compact()));
        validate(false);
    }

    /**
     * Sets the value chosen by right-clicking the text field. It is clamped to the bounds when applied.
     *
     * @param reset value to apply; the default is zero
     */
    public NumberEntryWidget resetValue(long reset) {
        this.reset = reset;
        return this;
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
    public void onUpdate() {
        super.onUpdate();
        if (isEditing() && (!isAvailable() || !areAncestorsEnabled())) getContext().removeFocus();
    }

    @Override
    public void dispose() {
        if (isEditing()) getContext().removeFocus();
        super.dispose();
    }

    /**
     * Reports whether the text field has keyboard focus, so callers can avoid replacing text the player is typing.
     *
     * @return {@code true} while the text field is focused
     */
    public boolean isEditing() {
        return field.isValid() && field.isFocused();
    }

    private boolean compact() {
        return getArea().height < HEIGHT;
    }

    private int scaled(int x) {
        return Math.round(x * (float) getArea().width / WIDTH);
    }

    private int scaledWidth(int x, int width) {
        return scaled(x + width) - scaled(x);
    }

    private int centerShift() {
        return (getArea().height - HEIGHT) / 2;
    }

    /**
     * Sets inclusive bounds and revalidates the current text without changing it or notifying the listener.
     *
     * @param min minimum accepted value
     * @param max maximum accepted value; values below {@code min} are replaced with {@code min}
     */
    public NumberEntryWidget bounds(long min, long max) {
        max = Math.max(min, max);
        if (this.min == min && this.max == max) return this;
        this.min = min;
        this.max = max;
        validate(false);
        return this;
    }

    /**
     * Replaces the text with a decimal integer and revalidates it without notifying the listener.
     *
     * @param value value to display; values outside the bounds remain visible as invalid input
     */
    public NumberEntryWidget value(long value) {
        setText(Long.toString(value));
        validate(false);
        return this;
    }

    /**
     * Sets the callback for valid values produced by user input.
     * <p>
     * The callback runs when the accepted value changes or invalid input becomes valid. Editing an expression without
     * changing its valid result, losing focus and stepping against a bound do not repeat the callback. Programmatic
     * calls to {@link #value(long)} and {@link #bounds(long, long)} do not invoke it.
     *
     * @param listener non-null callback receiving the accepted value on the client thread
     */
    public NumberEntryWidget onChange(LongConsumer listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Keeps the shown value equal to a source that can also change elsewhere, such as through another control. The
     * value is copied every update while the field is not being edited; user input still goes to
     * {@link #onChange(LongConsumer)}.
     *
     * @param source supplier of the current value, read on the client thread every update
     */
    public NumberEntryWidget follow(LongSupplier source) {
        value(source.getAsLong());
        onUpdateListener(widget -> {
            long current = source.getAsLong();
            if (!isEditing() && (!inputValid || this.value != current)) value(current);
        }, true);
        return this;
    }

    /**
     * Binds the entry to a value: it shows the value as {@link #follow(LongSupplier)} does and writes every valid value
     * the player enters into it, replacing any {@link #onChange(LongConsumer)} listener. In a GUI the server opens,
     * pass
     * a MUI2 {@code LongSyncValue} registered with the panel's sync manager
     * ({@code syncManager.syncValue(name, value)}),
     * so the server's value shows on the client and the player's input reaches the server. The bounds are checked on
     * the client only, so the server's setter should clamp the value again.
     *
     * @param value value to show and to write
     */
    public NumberEntryWidget value(ILongValue<?> value) {
        onChange(value::setLongValue);
        return follow(value::getLongValue);
    }

    /**
     * Sets where the text sits inside the field, such as {@link Alignment#Center} for a centred number.
     *
     * @param alignment placement of the text inside the field; MUI2's default is {@link Alignment#CenterLeft}
     */
    public NumberEntryWidget textAlignment(Alignment alignment) {
        field.setTextAlignment(alignment);
        return this;
    }

    /**
     * Returns the accepted value of the current text.
     *
     * @return the integer value, or an empty result while the input is invalid
     */
    public OptionalLong value() {
        return inputValid ? OptionalLong.of(value) : OptionalLong.empty();
    }

    /**
     * Reports whether the current text evaluates to an integer within the configured bounds.
     *
     * @return {@code true} if the input is valid
     */
    public boolean isInputValid() {
        return inputValid;
    }

    private AE2Button step(int index, int sign) {
        return new AE2Button(() -> (sign > 0 ? "+" : "-") + steps()[index], () -> add(sign * steps()[index]))
            .available(this::isAvailable)
            .setEnabledIf(widget -> !compact());
    }

    private AE2Button arrow(int sign) {
        return new AE2Button(() -> sign > 0 ? ">" : "<", () -> add(sign * fineStep())).available(this::isAvailable)
            .setEnabledIf(widget -> compact());
    }

    private static long[] steps() {
        return GuiScreen.isShiftKeyDown() || GuiScreen.isCtrlKeyDown() ? SHIFT_STEPS : STEPS;
    }

    private static long fineStep() {
        return GuiScreen.isShiftKeyDown() ? SHIFT_FINE_STEP : FINE_STEP;
    }

    private void reset() {
        setText(Long.toString(Math.max(min, Math.min(max, reset))));
        validate(true);
    }

    private void add(long delta) {
        long current = inputValid ? value : 0;
        long next = current == 1 && delta > 1 ? delta : LongMath.saturatedAdd(current, delta);
        setText(Long.toString(Math.max(min, Math.min(max, next))));
        validate(true);
    }

    private void setText(String next) {
        text = next;
        field.setText(next);
    }

    private void validate(boolean notify) {
        boolean wasValid = inputValid;
        long previous = value;
        messages.clear();
        BigDecimal number = parse(text);
        if (number == null) messages.add(tr("invalid"));
        else if (number.stripTrailingZeros()
            .scale() > 0) messages.add(tr("whole"));
        else if (number.compareTo(BigDecimal.valueOf(min)) < 0) messages.add(tr("min", min));
        else if (number.compareTo(BigDecimal.valueOf(max)) > 0) messages.add(tr("max", max));
        inputValid = messages.isEmpty();
        if (!inputValid) return;
        value = number.longValueExact();
        if (!INTEGER.matcher(text.strip())
            .matches()) messages.add("= " + value);
        if (notify && (!wasValid || previous != value)) listener.accept(value);
    }

    private static @Nullable BigDecimal parse(String text) {
        String trimmed = text.strip();
        if (trimmed.isEmpty()) return null;
        if (INTEGER.matcher(trimmed)
            .matches()) return new BigDecimal(trimmed);
        try {
            ParseResult result = MathUtils.parseExpression(trimmed, 0);
            if (result.isFailure() || !result.getResult()
                .isNumberValue()) return null;
            return result.getResult()
                .getNumberValue();
        } catch (ArithmeticException | NumberFormatException ignored) {
            return null;
        }
    }

    private static String tr(String key, Object... args) {
        return StatCollector.translateToLocalFormatted(LANG + key, args);
    }

    private class Field extends TextFieldWidget {

        @Override
        public void setText(String text) {
            super.setText(text);
            handler.clampCursors();
            getScrollData().scrollTo(getScrollArea(), 0);
        }

        @Override
        public @NotNull Result onMousePressed(int mouseButton) {
            return availability.press(() -> {
                if (mouseButton != 1 || !isHovering()) return super.onMousePressed(mouseButton);
                reset();
                return Result.SUCCESS;
            });
        }

        @Override
        public @NotNull Result onKeyPressed(char character, int keyCode) {
            return isAvailable() && areAncestorsEnabled() ? super.onKeyPressed(character, keyCode) : Result.IGNORE;
        }

        @Override
        public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
            return wheelStep.scroll(this, isAvailable(), scrollDirection, sign -> {
                add(sign * fineStep());
                return true;
            });
        }

        @Override
        public @Nullable IDrawable getThemeBackground(WidgetThemeEntry<?> widgetTheme, WidgetTheme activeTheme) {
            AE2Variant shown = isAvailable() ? null
                : AE2Look.of(this)
                    .variant();
            return shown == null ? super.getThemeBackground(widgetTheme, activeTheme) : shown.textFieldDisabled;
        }

        @Override
        public void preDraw(ModularGuiContext context, boolean transformed) {
            Integer color = textColor;
            if (!isAvailable()) textColor = AE2Theme.FIELD_DISABLED_TEXT;
            else if (!inputValid) textColor = AE2Look.of(this)
                .error();
            super.preDraw(context, transformed);
            textColor = color;
        }
    }

    private class Warning extends Widget<Warning> {

        private Warning() {
            tooltipAutoUpdate(true).tooltipBuilder(tooltip -> messages.forEach(tooltip::addLine));
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            AE2Look.of(this)
                .invalidIcon()
                .draw(0, 0, AE2Theme.ICON_SIZE, AE2Theme.ICON_SIZE);
        }
    }
}

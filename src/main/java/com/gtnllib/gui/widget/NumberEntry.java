package com.gtnllib.gui.widget;

import java.math.BigDecimal;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.BooleanSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.regex.Pattern;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.utils.ParseResult;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Unit.Measure;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.AE2Theme.Variant;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Integer input with a text field, validation feedback and step controls. It can be built on either side, so it also
 * works in GUIs the server opens.
 * <p>
 * The layout follows the widget's size. From {@link #HEIGHT} pixels up the entry shows AE2's four pairs of step
 * buttons above and below the text field, stretched to the width; the default size is {@link #WIDTH} by
 * {@link #HEIGHT}. A lower entry is a single row of {@code <}, the text field and {@code >}, for example 64 by 16
 * pixels inside a list row.
 * <p>
 * Input may contain a signed {@code long} literal or a numeric expression with SI suffixes, such as {@code 2 * 64}
 * or {@code 1.5k}. Only integral results within the configured bounds are valid. Invalid text remains editable and
 * is accompanied by an error tooltip, and in the tall layout an error icon. The text field accepts up to 64
 * characters.
 * <p>
 * Step button sizes are 1, 10, 100 and 1000, or 1, 16, 32 and 64 while Shift or Ctrl is held. The {@code <} and
 * {@code >} buttons and the mouse wheel over the text field step by 1, or by 10 while Shift is held. Steps are
 * clamped to the configured bounds. Increasing a value of one by a step larger than one selects that step value.
 * Invalid input is treated as zero when applying a step. Right-clicking the text field sets the reset value,
 * clamped to the bounds, so the default reset value of zero becomes one when the minimum is one.
 * <p>
 * While {@link #available(BooleanSupplier)} reports false the entry looks disabled like AE2's: the buttons are drawn
 * disabled and the text field gets AE2's dark uneditable background and grey text; clicks, typing and the wheel do
 * nothing.
 * <p>
 * {@link #isInputValid()} describes the input. Inherited {@link #isValid()} describes the MUI2 widget lifecycle.
 * <p>
 * For example, {@code new NumberEntry(Variant.LIGHT).bounds(1, 1_000_000).value(64)} creates a bounded input with an
 * initial value of 64.
 */
public class NumberEntry extends ParentWidget<NumberEntry> {

    /** Default widget dimensions in GUI pixels; lower entries use the single-row layout. */
    public static final int WIDTH = 138, HEIGHT = 62;
    /** Width of the {@code <} and {@code >} buttons of the single-row layout. */
    public static final int ARROW_WIDTH = 12;
    private static final long[] STEPS = { 1, 10, 100, 1000 }, SHIFT_STEPS = { 1, 16, 32, 64 };
    private static final int[] BUTTON_X = { 0, 28, 62, 100 }, BUTTON_WIDTH = { 22, 28, 32, 38 };
    private static final int FIELD_X = 28, FIELD_Y = 25, FIELD_WIDTH = 66, ICON_X = 104, ICON_Y = 27, ARROW_GAP = 1,
        FINE_STEP = 1, SHIFT_FINE_STEP = 10, MAX_LENGTH = 64, PLAIN_ERROR = 0xFFFF5555;
    private static final String LANG = "gtnllib.gui.number_entry.";
    private static final Pattern INTEGER = Pattern.compile("-?\\d+");

    private final @Nullable Variant variant;
    private final Field field = new Field();
    private final Warning warning = new Warning();
    private final List<String> messages = new ObjectArrayList<>();
    private String text = "0";
    private long min, max = Long.MAX_VALUE, value, reset;
    private boolean inputValid = true;
    private LongConsumer listener = ignored -> {};
    private BooleanSupplier available = () -> true;

    /**
     * Creates an entry with value zero and inclusive bounds from zero to {@link Long#MAX_VALUE}.
     *
     * @param variant palette for validation feedback, or {@code null} for the default error color and light icon;
     *                button and text-field styling follows the containing panel's theme
     */
    public NumberEntry(@Nullable Variant variant) {
        this.variant = variant;
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
     * @return this widget
     */
    public NumberEntry resetValue(long reset) {
        this.reset = reset;
        return this;
    }

    /**
     * Sets when the entry can be used; it looks disabled and ignores input otherwise, and gives up keyboard focus.
     *
     * @param available supplier read every frame and on every input
     * @return this widget
     */
    public NumberEntry available(BooleanSupplier available) {
        this.available = available;
        return this;
    }

    private boolean isAvailable() {
        return available.getAsBoolean();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isAvailable() && isEditing()) getContext().removeFocus();
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
     * @return this widget
     */
    public NumberEntry bounds(long min, long max) {
        this.min = min;
        this.max = Math.max(min, max);
        validate(false);
        return this;
    }

    /**
     * Replaces the text with a decimal integer and revalidates it without notifying the listener.
     *
     * @param value value to display; values outside the bounds remain visible as invalid input
     * @return this widget
     */
    public NumberEntry value(long value) {
        setText(Long.toString(value));
        validate(false);
        return this;
    }

    /**
     * Sets the callback for valid values produced by user input.
     * <p>
     * The callback runs when the accepted value changes or invalid input becomes valid. Editing an expression
     * without changing its valid result, losing focus and stepping against a bound do not repeat the callback.
     * Programmatic calls to {@link #value(long)} and {@link #bounds(long, long)} do not invoke it.
     *
     * @param listener non-null callback receiving the accepted value on the client thread
     * @return this widget
     */
    public NumberEntry onChange(LongConsumer listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Keeps the shown value equal to a source that can also change elsewhere, such as through another control. The
     * value is copied every update while the field is not being edited; user input still goes to
     * {@link #onChange(LongConsumer)}.
     *
     * @param source supplier of the current value, read on the client thread every update
     * @return this widget
     */
    public NumberEntry follow(LongSupplier source) {
        value(source.getAsLong());
        onUpdateListener(widget -> {
            long current = source.getAsLong();
            if (!isEditing() && value().orElse(Long.MIN_VALUE) != current) value(current);
        }, true);
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

    private ActionButton step(int index, int sign) {
        return ActionButton.text(() -> (sign > 0 ? "+" : "-") + steps()[index], () -> add(sign * steps()[index]))
            .available(this::isAvailable)
            .setEnabledIf(widget -> !compact());
    }

    private ActionButton arrow(int sign) {
        return ActionButton.text(() -> sign > 0 ? ">" : "<", () -> add(sign * fineStep()))
            .available(this::isAvailable)
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
        BigDecimal current = inputValid ? BigDecimal.valueOf(value) : BigDecimal.ZERO;
        BigDecimal next = current.compareTo(BigDecimal.ONE) == 0 && delta > 1 ? BigDecimal.valueOf(delta)
            : current.add(BigDecimal.valueOf(delta));
        setText(
            next.max(BigDecimal.valueOf(min))
                .min(BigDecimal.valueOf(max))
                .toPlainString());
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
        field.error(inputValid ? null : variant == null ? PLAIN_ERROR : variant.colors.error());
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
            if (result.isFailure() || !result.hasValue()
                || !result.getResult()
                    .isNumberValue())
                return null;
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
            if (!isAvailable()) return Result.STOP;
            if (mouseButton != 1 || !isHovering()) return super.onMousePressed(mouseButton);
            reset();
            return Result.SUCCESS;
        }

        @Override
        public @NotNull Result onKeyPressed(char character, int keyCode) {
            return isAvailable() ? super.onKeyPressed(character, keyCode) : Result.IGNORE;
        }

        @Override
        public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
            if (!isAvailable()) return false;
            add(scrollDirection.modifier * fineStep());
            return true;
        }

        @Override
        public @Nullable IDrawable getThemeBackground(WidgetThemeEntry<?> widgetTheme, WidgetTheme activeTheme) {
            Variant shown = isAvailable() ? null : AE2Theme.active(getContext());
            return shown == null ? super.getThemeBackground(widgetTheme, activeTheme) : shown.textFieldDisabled;
        }

        @Override
        public void preDraw(ModularGuiContext context, boolean transformed) {
            Integer color = textColor;
            if (!isAvailable()) textColor = AE2Theme.FIELD_DISABLED_TEXT;
            super.preDraw(context, transformed);
            textColor = color;
        }

        private void error(@Nullable Integer color) {
            textColor = color;
        }
    }

    private class Warning extends Widget<Warning> {

        private Warning() {
            tooltipAutoUpdate(true).tooltipBuilder(tooltip -> messages.forEach(tooltip::addLine));
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            (variant == null ? Variant.LIGHT : variant).invalid.draw(0, 0, AE2Theme.ICON_SIZE, AE2Theme.ICON_SIZE);
        }
    }
}

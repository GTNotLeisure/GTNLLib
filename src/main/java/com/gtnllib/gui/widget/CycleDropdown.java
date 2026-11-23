package com.gtnllib.gui.widget;

import java.util.List;
import java.util.Objects;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.ScrollingTextWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.menu.DropdownWidget;
import com.cleanroommc.modularui.widgets.menu.Menu;
import com.cleanroommc.modularui.widgets.menu.MenuPanel;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.MUI2Fixes;
import com.gtnllib.gui.theme.ae2.AE2ButtonDrawable;
import com.gtnllib.gui.theme.ae2.AE2ButtonDrawable.State;
import com.gtnllib.gui.theme.ae2.AE2ButtonPainter;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.AE2PanelPainter;
import com.gtnllib.gui.theme.ae2.ButtonText;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CycleDropdown<T> extends DropdownWidget<T, CycleDropdown<T>>
    implements IAvailable<CycleDropdown<T>>, IWheelStep<CycleDropdown<T>> {

    /**
     * Height of an {@link #iconOption} row and of its icon in pixels.
     */
    public static final int OPTION_HEIGHT = 16;
    private static final int VALUE_INSET = 3, CARET_SPACE = AE2Theme.CARET_WIDTH + 4;
    private final List<T> choices = new ObjectArrayList<>();
    private final IDrawable disabledLook = AE2ButtonPainter.button(this, () -> false, () -> false);
    private final List<TextWidget<?>> labels = new ObjectArrayList<>();
    private final Availability availability = new Availability();
    private final WheelStep wheelStep = new WheelStep();
    private T shown;
    private int menuWidth;
    private boolean centredText;

    /**
     * Creates an empty dropdown. Set its value, its option widgets ({@code optionToWidget}) and its
     * {@link #choices(List) choices} afterwards; the caller sets its size.
     *
     * @param panelName name of the menu panel MUI2 opens for the options, unique within the screen
     * @param valueType type of the values to choose from
     */
    public CycleDropdown(String panelName, Class<T> valueType) {
        super(panelName, valueType);
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
    public @NotNull Result onMousePressed(int mouseButton) {
        return availability.press(() -> super.onMousePressed(mouseButton));
    }

    @Override
    public @Nullable IDrawable getThemeBackground(WidgetThemeEntry<?> widgetTheme, WidgetTheme activeTheme) {
        return isAvailable() ? super.getThemeBackground(widgetTheme, activeTheme) : disabledLook;
    }

    @Override
    public void openMenu(boolean soft) {
        super.openMenu(soft);
        Menu<?> menu = getMenu();
        if (!menu.isValid() || !(menu.getPanel() instanceof MenuPanel panel) || panel == getPanel()) return;
        MUI2Fixes.menuPanelTheme(panel, AE2Look.of(this));
    }

    // MUI2 keeps the menu of a removed dropdown open, or open until its closing animation ends, and a click meanwhile
    // asks the removed dropdown for its parent.
    @Override
    public void dispose() {
        if (isValid() && isOpen()) closeMenu(false);
        super.dispose();
    }

    @Override
    protected void checkClose(boolean soft, boolean requireNoHover) {
        if (isValid()) super.checkClose(soft, requireNoHover);
    }

    @Override
    protected IWidget valueToWidget(T value, boolean forSelectedDisplay) {
        IWidget widget = super.valueToWidget(value, forSelectedDisplay);
        if (widget instanceof Widget<?>option) {
            option.margin(VALUE_INSET, forSelectedDisplay ? CARET_SPACE : VALUE_INSET, 0, 0);
            if (option instanceof TextWidget<?>text) {
                text.alignment(Alignment.CenterLeft);
                if (forSelectedDisplay) text.heightRel(1f);
                else if (!text.resizer()
                    .hasHeight()) text.height(OPTION_HEIGHT);
            }
            if (forSelectedDisplay) {
                centredText = option instanceof TextWidget<?>;
                option.transform((self, stack) -> stack.translate(0, labelShift()));
                labels.clear();
                followButtonText(option);
            }
        }
        return widget;
    }

    // The shown value is a plain text widget, which MUI2 draws in the panel's text style rather than the button's.
    private void followButtonText(IWidget widget) {
        if (widget instanceof TextWidget<?>text && text.getColor() == null) labels.add(text);
        for (IWidget child : widget.getChildren()) followButtonText(child);
    }

    private ButtonText labelText() {
        return AE2Look.of(this)
            .buttonText(State.of(this, isAvailable(), false), getArea().height);
    }

    private int labelSink() {
        if (!AE2Look.of(this)
            .isAE2()) return 0;
        int height = getArea().height;
        return AE2ButtonDrawable.textY(State.of(this, isAvailable(), false), height)
            - AE2ButtonDrawable.textY(State.NORMAL, height);
    }

    // MUI2 centres a text line at round(height / 2) - 4; AE2 places a button label where textY says.
    private int labelShift() {
        if (!centredText || !AE2Look.of(this)
            .isAE2()) return labelSink();
        int height = getArea().height;
        return labelSink() + AE2ButtonDrawable.textY(State.NORMAL, height)
            - (Math.round(height / 2f) - AE2Theme.LABEL_HEIGHT / 2);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        ButtonText text = labelText();
        for (TextWidget<?> label : labels) label.color(text.color())
            .shadow(text.shadow());
        AE2PanelPainter.drawCaret(
            getArea().width - CARET_SPACE + 1,
            (getArea().height - AE2Theme.CARET_HEIGHT) / 2 + labelSink(),
            isOpen(),
            text.color());
    }

    @Override
    protected void setValue(T value, boolean updateValue) {
        super.setValue(value, updateValue);
        shown = value;
    }

    // MUI2 only refreshes the shown option on its own selections; follow values changed from elsewhere too.
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isValid()) return;
        T current = getValue().getValue();
        if (!Objects.equals(current, shown)) setValue(current, false);
        if (isOpen() && (!isAvailable() || !areAncestorsEnabled())) closeMenu(false);
    }

    /**
     * Sets the values the menu offers and the wheel cycles through, in order. Calling it again with an equal list does
     * nothing, so it is safe to call every update; a changed list rebuilds the menu.
     *
     * @param choices values to offer; the list is copied
     */
    public CycleDropdown<T> choices(List<T> choices) {
        if (this.choices.equals(choices)) return this;
        this.choices.clear();
        this.choices.addAll(choices);
        clearOptions().options(choices);
        deleteMenu();
        if (isValid()) setValue(getValue().getValue(), false);
        return this;
    }

    @Override
    public void deleteMenu() {
        if (isValid() && isOpen()) closeMenu(false);
        super.deleteMenu();
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        return wheelStep.scroll(this, isAvailable(), scrollDirection, this::cycle);
    }

    private boolean cycle(int sign) {
        IValue<T> value = getValue();
        if (value == null || choices.isEmpty()) return false;
        int index = Math.max(0, choices.indexOf(value.getValue()));
        setValue(choices.get(Math.floorMod(index - sign, choices.size())), true);
        return true;
    }

    /**
     * Makes the opened menu at least this wide so long option names fit; its right edge stays on the dropdown's right
     * edge, so a dropdown at the right of a panel does not push the menu past it.
     *
     * @param width menu width in pixels; zero keeps the dropdown's own width
     */
    public CycleDropdown<T> menuWidth(int width) {
        if (menuWidth == width) return this;
        menuWidth = width;
        deleteMenu();
        return this;
    }

    /**
     * Creates an option row for {@link #optionToWidget}: an optional item icon, then the name, which scrolls while
     * hovered when it is too long.
     *
     * @param icon item drawn as a {@value #OPTION_HEIGHT}-pixel icon, or {@code null} for none
     * @param name option name
     * @return the row, {@value #OPTION_HEIGHT} pixels high and as wide as the dropdown
     */
    public static IWidget iconOption(@Nullable ItemStack icon, String name) {
        return iconOption(icon, name, Alignment.CenterLeft);
    }

    /**
     * Creates an option row like {@link #iconOption(ItemStack, String)} with the name placed by the given alignment in
     * the space right of the icon, such as {@link Alignment#Center} for centred tier names. Formatting codes in the
     * name, such as GT's tier colors, are kept.
     *
     * @param icon      item drawn as a {@value #OPTION_HEIGHT}-pixel icon, or {@code null} for none
     * @param name      option name
     * @param alignment placement of the name
     * @return the row, {@value #OPTION_HEIGHT} pixels high and as wide as the dropdown
     */
    public static IWidget iconOption(@Nullable ItemStack icon, String name, Alignment alignment) {
        Flow row = Flow.row()
            .widthRel(1f)
            .height(OPTION_HEIGHT)
            .childPadding(2);
        if (icon != null) row.child(
            new ItemDrawable(icon).asWidget()
                .size(OPTION_HEIGHT, OPTION_HEIGHT));
        return row.child(
            new ScrollingTextWidget(IKey.str(name)).alignment(alignment)
                .expanded()
                .heightRel(1f));
    }

    @Override
    protected Menu<?> createMenu() {
        Menu<?> menu = super.createMenu();
        if (menuWidth > getArea().width) menu.width(menuWidth)
            .right(0);
        MUI2Fixes.menuPadding(menu, AE2Look.of(this));
        for (IWidget child : menu.getChildren())
            if (child instanceof ListWidget<?, ?>list) MUI2Fixes.noScrollShadows(list);
        return menu;
    }
}

package com.gtnllib.gui.widget;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.INotifyEnabled;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Unit.Measure;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.AE2PanelPainter;
import com.gtnllib.gui.theme.ae2.AE2Variant;
import com.gtnllib.gui.theme.ae2.IconSource;
import com.gtnllib.gui.theme.ae2.PlainLook;

public class VerticalButtonBar extends ParentWidget<VerticalButtonBar> implements INotifyEnabled {

    /**
     * Strip width, distance between button tops under AE2 themes and pixels drawn over the panel's left border.
     */
    public static final int WIDTH = 21, PITCH = 22, OVERLAP = 2;
    private static final int BUTTON_X = 3, BUTTON_Y = 3, EDGES = 6, BUTTON_GAP = PITCH - AE2Theme.ICON_BUTTON_HEIGHT,
        PANEL_OUTLINE = 1, BORDER_SAMPLE = 16;
    private final IDrawable panelBackground = AE2PanelPainter.panelBackground(this, 0, 0);
    private int buttonWidth = AE2Theme.ICON_BUTTON_WIDTH, buttonHeight, shownButtons;

    /**
     * Creates an empty bar; add buttons with {@code button} and {@code toggle}.
     */
    public VerticalButtonBar() {
        size(WIDTH, EDGES).excludeAreaInRecipeViewer();
    }

    /**
     * Adds a button below the existing ones.
     *
     * @param icon    supplier of the icon, read every frame so a toggle can show its state
     * @param tooltip supplier of the tooltip line, read every frame
     * @param action  run on a left click
     */
    public VerticalButtonBar button(Supplier<? extends IconSource> icon, Supplier<String> tooltip, Runnable action) {
        return button(icon, lines -> lines.addLine(tooltip.get()), mouse -> {
            if (mouse != 0) return false;
            action.run();
            return true;
        });
    }

    /**
     * Adds a button below the existing ones that can tell the mouse buttons apart.
     *
     * @param icon    supplier of the icon, read every frame
     * @param tooltip fills the tooltip, again every frame
     * @param click   receives the pressed mouse button (0 left, 1 right) and returns whether it handled it
     */
    public VerticalButtonBar button(Supplier<? extends IconSource> icon, Consumer<RichTooltip> tooltip,
        IntPredicate click) {
        return add(icon, tooltip, click, () -> false);
    }

    /**
     * Adds an on/off button below the existing ones; it shows AE2's selected frame while on.
     *
     * @param icon    supplier of the icon, read every frame
     * @param tooltip supplier of the tooltip line, read every frame
     * @param on      supplier of the on state, read every frame
     * @param flip    run on a left click
     */
    public VerticalButtonBar toggle(Supplier<? extends IconSource> icon, Supplier<String> tooltip, BooleanSupplier on,
        Runnable flip) {
        return add(icon, lines -> lines.addLine(tooltip.get()), mouse -> {
            if (mouse != 0) return false;
            flip.run();
            return true;
        }, on);
    }

    /**
     * Adds any widget below the existing buttons, sized to the bar's button size; it keeps its own look, tooltip and
     * clicks. Use it for a {@link SettingToggleButton} or a button you built yourself.
     *
     * @param widget widget to add, not added anywhere else
     */
    public VerticalButtonBar add(Widget<?> widget) {
        child(widget);
        return this;
    }

    @Override
    public boolean addChild(IWidget child, int index) {
        boolean changed = super.addChild(child, index);
        if (changed) layoutButtons();
        return changed;
    }

    @Override
    public boolean remove(IWidget child) {
        boolean changed = super.remove(child);
        if (changed) layoutButtons();
        return changed;
    }

    @Override
    public boolean remove(int index) {
        boolean changed = super.remove(index);
        if (changed) layoutButtons();
        return changed;
    }

    @Override
    public boolean removeAll() {
        boolean changed = super.removeAll();
        if (changed) layoutButtons();
        return changed;
    }

    /**
     * Sets when the button added last can be used; while it cannot, it looks disabled, shows no hover and swallows
     * clicks.
     *
     * @param available supplier read every frame and on every click
     * @throws IllegalStateException if no button was added yet, or the widget added last has no available state of its
     *                               own ({@link IAvailable})
     */
    public VerticalButtonBar available(BooleanSupplier available) {
        if (!hasChildren() || !(getChildren().get(getChildren().size() - 1) instanceof IAvailable<?>widget))
            throw new IllegalStateException("The widget added last has no available state");
        widget.available(available);
        return this;
    }

    /**
     * Sets the size of every button in every look, also of buttons added before, instead of the look's toolbar button
     * size. Buttons stay {@code height + 2} pixels apart, and the strip becomes {@code width + 3} pixels wide, so place
     * a bar with other buttons at {@code panelLeft - getArea().width + OVERLAP}. The frame stretches and the icon stays
     * 16 by 16.
     *
     * @param width  button width in pixels, at least AE2's 18
     * @param height button height in pixels, at least AE2's 20
     */
    public VerticalButtonBar buttonSize(int width, int height) {
        buttonWidth = Math.max(AE2Theme.ICON_BUTTON_WIDTH, width);
        buttonHeight = Math.max(AE2Theme.ICON_BUTTON_HEIGHT, height);
        layoutButtons();
        return this;
    }

    @Override
    public void onInit() {
        super.onInit();
        layoutButtons();
    }

    @Override
    public void onChildChangeEnabled(IWidget child, boolean enabled) {
        layoutButtons();
    }

    private int buttonHeight() {
        return buttonHeight > 0 ? buttonHeight
            : AE2Look.of(this)
                .iconButtonHeight();
    }

    private int pitch() {
        return buttonHeight() + BUTTON_GAP;
    }

    private void layoutButtons() {
        int shown = 0;
        for (IWidget button : getChildren()) {
            if (!button.isEnabled()) continue;
            int slot = shown++;
            button.resizer()
                .left(BUTTON_X)
                .top(() -> BUTTON_Y + slot * pitch(), Measure.PIXEL)
                .width(buttonWidth)
                .height(this::buttonHeight, Measure.PIXEL);
        }
        shownButtons = shown;
        width(BUTTON_X + buttonWidth)
            .height(() -> shownButtons == 0 ? 0 : shownButtons * pitch() + EDGES, Measure.PIXEL);
        if (isValid()) scheduleResize();
    }

    private VerticalButtonBar add(Supplier<? extends IconSource> icon, Consumer<RichTooltip> tooltip,
        IntPredicate click, BooleanSupplier selected) {
        return add(
            new IconButton(icon, () -> {}).selected(selected)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(tooltip)
                .onMousePressed(click::test));
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (shownButtons == 0) return;
        int width = getArea().width, height = getArea().height, right = width;
        AE2Variant variant = AE2Look.of(this)
            .variant();
        if (variant != null) AE2PanelPainter.drawButtonBar(variant, 0, 0, width, height);
        else {
            right = width + PlainLook.PANEL_BORDER - OVERLAP;
            Stencil.apply(0, 0, right, height, context);
            panelBackground.draw(context, 0, 0, right + PlainLook.PANEL_BORDER, height, widgetTheme.getTheme());
            Stencil.remove();
        }
        if (getArea().y > getPanel().getArea().y) joinPanelBorder(context, width - OVERLAP, right, widgetTheme);
    }

    // The strip's top outline would cut across the panel's left border, so the border is drawn again over that one row,
    // from just inside the panel's outline to the strip's right end, sampled away from the panel's corners.
    private void joinPanelBorder(ModularGuiContext context, int panelLeft, int right, WidgetThemeEntry<?> widgetTheme) {
        Stencil.apply(panelLeft + PANEL_OUTLINE, 0, right - panelLeft - PANEL_OUTLINE, 1, context);
        panelBackground
            .draw(context, panelLeft, -BORDER_SAMPLE, BORDER_SAMPLE * 2, BORDER_SAMPLE * 2, widgetTheme.getTheme());
        Stencil.remove();
    }
}

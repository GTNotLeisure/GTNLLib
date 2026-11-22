package com.gtnllib.gui.widget;

import java.util.function.BooleanSupplier;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Unit.Measure;
import com.gtnllib.gui.theme.ae2.AE2Draw;
import com.gtnllib.gui.theme.ae2.AE2ListRowPainter;
import com.gtnllib.gui.theme.ae2.AE2Look;

public class ListRow extends ParentWidget<ListRow> implements Interactable, IAvailable<ListRow> {

    /**
     * Pixels from the row's left edge to the label and from its right edge to the control or check box.
     */
    public static final int TEXT_INSET = 4, END_INSET = 3;
    private static final int EDGE = AE2Look.WELL_EDGE, GAP = 4;
    private final Supplier<String> label;
    private final boolean header;
    private final Availability availability = new Availability();
    private IntPredicate click;
    private BooleanSupplier checked;
    private Widget<?> control;
    private int controlWidth;
    private boolean cut;

    /**
     * Creates a row with a label.
     *
     * @param label supplier of the label, read every frame
     */
    public ListRow(Supplier<String> label) {
        this(label, false);
    }

    private ListRow(Supplier<String> label, boolean header) {
        this.label = label;
        this.header = header;
        height(AE2ListRowPainter.ROW_HEIGHT);
        tooltipAutoUpdate(true);
        tooltipBuilder(tooltip -> { if (cut) tooltip.addLine(label.get()); });
    }

    /**
     * Creates a section header row: muted text and, under AE2 themes, a divider line.
     *
     * @param label supplier of the header text, read every frame
     * @return the new row
     */
    public static ListRow header(Supplier<String> label) {
        return new ListRow(label, true);
    }

    /**
     * Places a control against the row's right end, centred vertically. While the control is disabled, the label is
     * drawn muted. A control that is an {@link IListRowControl} is told that it sits in a row.
     *
     * @param control control widget; its size is set here
     * @param width   control width in pixels
     * @param height  control height in pixels, at most the row's height
     */
    public ListRow control(Widget<?> control, int width, int height) {
        if (this.control != null && this.control != control) remove(this.control);
        this.control = control;
        controlWidth = width;
        if (control instanceof IListRowControl rowControl) rowControl.placedInListRow();
        child(
            control.size(width, height)
                .right(END_INSET)
                .top(() -> (getArea().height - height) / 2, Measure.PIXEL));
        return this;
    }

    @Override
    protected void onChildRemove(IWidget child) {
        super.onChildRemove(child);
        if (child == control) {
            control = null;
            controlWidth = 0;
        }
    }

    /**
     * Shows a check box against the row's right end; a left click anywhere on the row flips it.
     *
     * @param on   supplier of the checked state, read every frame
     * @param flip run on a left click
     */
    public ListRow checkbox(BooleanSupplier on, Runnable flip) {
        checked = on;
        return onClick(mouse -> {
            if (mouse != 0) return false;
            flip.run();
            return true;
        });
    }

    /**
     * Makes the row clickable; it then highlights under the mouse.
     *
     * @param click receives the pressed mouse button (0 left, 1 right) and returns whether it handled it
     */
    public ListRow onClick(IntPredicate click) {
        this.click = click;
        return this;
    }

    @Override
    public Availability availability() {
        return availability;
    }

    // MUI2 lets the mouse fall through a parent widget without a theme background; this one draws its own.
    @Override
    public boolean canHover() {
        return true;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (click == null) return Result.IGNORE;
        return availability.press(() -> {
            if (!click.test(mouseButton)) return Result.IGNORE;
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        });
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        AE2Look look = AE2Look.of(this);
        int width = getArea().width, height = getArea().height;
        boolean usable = isAvailable(), hovered = usable && click != null && isHovering();
        if (hovered) GuiDraw.drawRect(EDGE, 0, width - 2 * EDGE, height, look.hover());
        int right = width - END_INSET;
        if (checked != null)
            right -= AE2ListRowPainter.drawCheckbox(look, right, 0, height, checked.getAsBoolean(), hovered, usable)
                + GAP;
        if (control != null) right -= controlWidth + GAP;
        String full = label.get();
        String shown = Minecraft.getMinecraft().fontRenderer.trimStringToWidth(full, Math.max(0, right - TEXT_INSET));
        cut = shown.length() < full.length();
        if (header) AE2ListRowPainter.drawHeader(look, shown, TEXT_INSET, 0, height, width - EDGE);
        else {
            boolean muted = !usable || control != null && !control.isEnabled();
            int color = muted ? look.listMuted() : look.listText();
            AE2Draw.text(shown, TEXT_INSET, AE2ListRowPainter.textTop(height), color, !muted && look.listShadow());
        }
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }
}

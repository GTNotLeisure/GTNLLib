package com.gtnllib.gui.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerIngredientProvider;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2Checkboxes;
import com.gtnllib.gui.theme.ae2.AE2Draw;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.AE2Panels;
import com.gtnllib.gui.theme.ae2.AE2Variant;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * A scrolling list whose rows are drawn directly instead of being widgets, so it stays fast with hundreds of rows.
 * The rows are read from a supplier every tick. A row can carry an item icon, a right-aligned detail, an on/off
 * switch, a dropdown whose options open as indented rows under it, or a number field. Number fields are
 * {@link NumberEntryWidget} children placed over the visible number rows, so only those rows cost a widget.
 * <p>
 * Under an AE2 theme the list draws as an AE2 inset with AE2 list-row highlights and switches, and AE2's scroll bar
 * beside the inset; otherwise it draws as Minecraft's sunken slot box with MUI2's check boxes and a thin bar, in the
 * colors of {@link AE2Look#plain()}. The scroll bar is a
 * {@link Scrollbar} child: the wheel moves one row, as in AE2's network status and
 * crafting CPU screens. The wheel scrolls the list everywhere, also over number fields, unless
 * {@link #numberWheel(boolean) numberWheel(true)} lets it step the field under the pointer instead.
 */
public class IconRowList extends ParentWidget<IconRowList> implements Interactable, RecipeViewerIngredientProvider {

    public static final int ROW = 18, SCROLLBAR = 3, INDENT = 10, CARET = 9, NUMBER_WIDTH = 64;
    /**
     * Row colors that stand for the look's own colors: a row in {@code TEXT} uses the look's list text color, and
     * header rows carry {@code HEADER}.
     */
    public static final int HEADER = 0xFFA0A0A8, TEXT = 0xFFE8E8E8;
    /** Row colors that AE2 themes replace with their own good and bad colors. */
    public static final int GOOD = 0xFF60E060, BAD = 0xFFFF6060;
    /**
     * Width taken by the scroll bar right of the inset under AE2 themes, like AE2's lists: the groove sits on the panel
     * background, eight pixels from the inset's edge, as in AE2's network status and crafting CPU screens.
     */
    public static final int AE2_SCROLL_COLUMN = AE2Theme.BIG_SCROLLER_WIDTH + 5;
    private static final int AE2_GUTTER = AE2_SCROLL_COLUMN + 1, PLAIN_GUTTER = SCROLLBAR + AE2Look.WELL_EDGE,
        HEADER_LINE_ALPHA = 0x40000000, ICON = 16, TEXT_HEIGHT = 8, MIN_ROW = ICON, MIN_NUMBER_WIDTH = 32;

    /**
     * The number field of a row.
     *
     * @param value shown value; it replaces the text whenever the player is not typing
     * @param min   inclusive minimum
     * @param max   inclusive maximum
     * @param reset value set by right-clicking the field, clamped to the bounds
     * @param set   receives every valid value the player enters
     */
    public record Numeric(long value, long min, long max, long reset, LongConsumer set) {}

    /**
     * One row. Only one of click, options, toggle and number is normally set; options turn the row into a
     * dropdown, a non-null toggle draws a switch whose state is that value, and a number puts a
     * {@link NumberEntryWidget}
     * on the right of the row. A right click runs {@code secondary} when it is set. A nonzero {@code detailColor}
     * colors only the detail; zero uses {@code color} for both. An unavailable row is drawn muted with a faded switch,
     * does not highlight and ignores clicks; its number field looks disabled.
     */
    public record Row(ItemStack icon, String text, String detail, int color, boolean selected, boolean header,
        Runnable click, List<Row> options, Boolean toggle, Numeric number, Runnable secondary, int detailColor,
        boolean available) {

        public static Row header(String text) {
            return new Row(null, text, "", HEADER, false, true, null, null, null, null, null, 0, true);
        }

        public static Row item(ItemStack icon, String text, String detail, boolean selected, Runnable click) {
            return new Row(icon, text, detail, TEXT, selected, false, click, null, null, null, null, 0, true);
        }

        public static Row colored(ItemStack icon, String text, String detail, int color) {
            return new Row(icon, text, detail, color, false, false, null, null, null, null, null, 0, true);
        }

        public static Row toggle(String text, boolean on, Runnable click) {
            return new Row(null, text, "", TEXT, false, false, click, null, on, null, null, 0, true);
        }

        public static Row dropdown(ItemStack icon, String text, String detail, boolean selected, List<Row> options) {
            return new Row(icon, text, detail, TEXT, selected, false, null, options, null, null, null, 0, true);
        }

        public static Row number(String text, long value, long min, long max, long reset, LongConsumer set) {
            return new Row(
                null,
                text,
                "",
                TEXT,
                false,
                false,
                null,
                null,
                null,
                new Numeric(value, min, max, reset, set),
                null,
                0,
                true);
        }

        /**
         * Returns a copy of this row that can or cannot be used.
         *
         * @param available whether the row reacts to the mouse
         * @return the new row
         */
        public Row withAvailable(boolean available) {
            return new Row(
                icon,
                text,
                detail,
                color,
                selected,
                header,
                click,
                options,
                toggle,
                number,
                secondary,
                detailColor,
                available);
        }

        /**
         * Returns a copy of this row that runs the action on a right click.
         *
         * @param action action for the right mouse button
         * @return the new row
         */
        public Row onRightClick(Runnable action) {
            return new Row(
                icon,
                text,
                detail,
                color,
                selected,
                header,
                click,
                options,
                toggle,
                number,
                action,
                detailColor,
                available);
        }

        /**
         * Returns a copy of this row whose detail alone is drawn in another color; {@link #GOOD} and {@link #BAD}
         * follow the AE2 theme like row colors do.
         *
         * @param color ARGB color of the detail
         * @return the new row
         */
        public Row withDetailColor(int color) {
            return new Row(
                icon,
                text,
                detail,
                this.color,
                selected,
                header,
                click,
                options,
                toggle,
                number,
                secondary,
                color,
                available);
        }
    }

    private final Supplier<List<Row>> source;
    private final List<NumberEntryWidget> entries = new ArrayList<>();
    private final List<Row> entryRows = new ArrayList<>();
    private final IntList entryTops = new IntArrayList();
    private List<Row> rows = Collections.emptyList();
    private boolean[] nested = new boolean[0];
    private int[] occurrence = new int[0];
    private String expanded;
    private int expandedOccurrence;
    private int scroll, edge, gutter = SCROLLBAR, rowHeight = ROW, numberWidth = NUMBER_WIDTH;
    private boolean revealSelected, numberWheel;
    private final Scrollbar scrollbar = new Scrollbar(() -> scroll, this::maxScroll, row -> scroll = row)
        .plainBar(AE2Look.plainScrollBar(), this::visibleRows);

    public IconRowList(Supplier<List<Row>> source) {
        this.source = source;
        child(
            scrollbar.right(0)
                .top(0)
                .heightRel(1f)
                .width(SCROLLBAR));
    }

    public void scrollToTop() {
        scroll = 0;
    }

    /**
     * Scrolls, once the list has been laid out, so that the first selected row sits in the middle of the view.
     *
     * @return this list
     */
    public IconRowList revealSelected() {
        revealSelected = true;
        return this;
    }

    /**
     * Sets the height of every row; the standard is {@link #ROW}. Text, icons, switches, carets and number fields stay
     * centred in the row. Make the list's inner height a multiple of it, or a strip stays empty at the bottom.
     *
     * @param rowHeight row height in pixels, at least 16 so a 16-pixel item icon fits
     * @return this list
     */
    public IconRowList rowHeight(int rowHeight) {
        this.rowHeight = Math.max(MIN_ROW, rowHeight);
        resizeEntries();
        return this;
    }

    /**
     * Sets the width of the number fields of number rows; the standard is {@link #NUMBER_WIDTH}.
     *
     * @param numberWidth field width in pixels, at least 32 so the arrows and a few digits fit
     * @return this list
     */
    public IconRowList numberWidth(int numberWidth) {
        this.numberWidth = Math.max(MIN_NUMBER_WIDTH, numberWidth);
        resizeEntries();
        return this;
    }

    /**
     * Sets whether the mouse wheel over a number row's field steps its value, as
     * {@link NumberEntryWidget#wheel(boolean)}
     * does. Off by default, so the wheel scrolls the list there too.
     *
     * @param numberWheel true to let the wheel step number fields
     * @return this list
     */
    public IconRowList numberWheel(boolean numberWheel) {
        this.numberWheel = numberWheel;
        for (NumberEntryWidget entry : entries) entry.wheel(numberWheel);
        return this;
    }

    private void resizeEntries() {
        for (NumberEntryWidget entry : entries) entry.size(numberWidth, rowHeight - 2);
        entryTops.replaceAll(top -> -1);
        scheduleResize();
    }

    // MUI2 lets the mouse fall through a parent widget without a theme background; this one draws its own.
    @Override
    public boolean canHover() {
        return true;
    }

    @Override
    public boolean canClickThrough() {
        return false;
    }

    @Override
    public boolean canHoverThrough() {
        return false;
    }

    /**
     * Collapses an open dropdown.
     */
    public void collapse() {
        expanded = null;
    }

    private boolean isExpanded(String text, int occurrence) {
        return text.equals(expanded) && occurrence == expandedOccurrence;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        // MUI2 updates hidden widgets too; a hidden list has nothing to rebuild.
        if (!isEnabled()) return;
        List<Row> flat = new ArrayList<>();
        List<Boolean> inside = new ArrayList<>();
        IntList seen = new IntArrayList();
        Object2IntOpenHashMap<String> counts = new Object2IntOpenHashMap<>();
        for (Row row : source.get()) {
            int count = counts.addTo(row.text(), 1);
            flat.add(row);
            inside.add(false);
            seen.add(count);
            if (row.options() == null || !isExpanded(row.text(), count)) continue;
            if (!row.available()) {
                expanded = null;
                continue;
            }
            for (Row option : row.options()) {
                flat.add(option);
                inside.add(true);
                seen.add(-1);
            }
        }
        rows = flat;
        nested = new boolean[flat.size()];
        for (int i = 0; i < nested.length; i++) nested[i] = inside.get(i);
        occurrence = seen.toIntArray();
        if (revealSelected && getArea().height >= rowHeight) {
            revealSelected = false;
            for (int i = 0; i < flat.size(); i++) {
                if (!flat.get(i)
                    .selected()) continue;
                scroll = i - visibleRows() / 2;
                break;
            }
        }
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
        placeEntries();
    }

    private void placeEntries() {
        int used = 0;
        boolean moved = false;
        for (int i = 0; i < visibleRows() && scroll + i < rows.size(); i++) {
            Row row = rows.get(scroll + i);
            if (row.number() == null) continue;
            if (used == entries.size()) {
                addEntry();
                moved = true;
            }
            NumberEntryWidget entry = entries.get(used);
            int top = edge + i * rowHeight + 1;
            if (entryTops.getInt(used) != top) {
                entry.top(top);
                entryTops.set(used, top);
                moved = true;
            }
            entryRows.set(used, row);
            entry.setEnabled(true);
            Numeric number = row.number();
            entry.bounds(number.min(), number.max())
                .resetValue(number.reset());
            if (!entry.isEditing() && entry.value()
                .orElse(Long.MIN_VALUE) != number.value()) entry.value(number.value());
            used++;
        }
        for (int i = used; i < entries.size(); i++) {
            entries.get(i)
                .setEnabled(false);
            entryRows.set(i, null);
        }
        if (moved) scheduleResize();
    }

    private void addEntry() {
        int index = entries.size();
        NumberEntryWidget entry = new NumberEntryWidget().right(gutter + 2)
            .top(0)
            .size(numberWidth, rowHeight - 2)
            .wheel(numberWheel)
            .available(() -> {
                Row row = entryRows.get(index);
                return row == null || row.available();
            })
            .onChange(value -> {
                Row row = entryRows.get(index);
                if (row != null && row.number() != null) row.number()
                    .set()
                    .accept(value);
            });
        entries.add(entry);
        entryRows.add(null);
        entryTops.add(-1);
        addChild(entry, -1);
    }

    private void layout(boolean themed) {
        int nextGutter = themed ? AE2_GUTTER : PLAIN_GUTTER;
        if (edge == AE2Look.WELL_EDGE && nextGutter == gutter) return;
        edge = AE2Look.WELL_EDGE;
        gutter = nextGutter;
        scrollbar.width(themed ? AE2Theme.BIG_SCROLLER_WIDTH : SCROLLBAR)
            .right(themed ? 0 : AE2Look.WELL_EDGE);
        for (NumberEntryWidget entry : entries) entry.right(gutter + 2);
        entryTops.replaceAll(top -> -1);
        placeEntries();
        scheduleResize();
    }

    private int visibleRows() {
        return Math.max(1, (getArea().height - 2 * edge) / rowHeight);
    }

    private int maxScroll() {
        return Math.max(0, rows.size() - visibleRows());
    }

    private boolean inGutter() {
        return getContext().getMouseX() >= getArea().width - gutter;
    }

    private int hoveredRow() {
        if (!isHovering() || inGutter()) return -1;
        int mouseY = getContext().getMouseY() - edge;
        int row = scroll + Math.floorDiv(mouseY, rowHeight);
        return mouseY >= 0 && row < rows.size() && mouseY < visibleRows() * rowHeight ? row : -1;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        AE2Look look = AE2Look.of(this);
        AE2Variant variant = look.variant();
        layout(variant != null);
        int width = getArea().width, height = getArea().height;
        look.drawWell(0, 0, variant == null ? width : width - AE2_SCROLL_COLUMN, height);
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        int hovered = hoveredRow(), visible = visibleRows();
        int rowRight = width - gutter, textRight = rowRight - 2;
        for (int i = 0; i < visible && scroll + i < rows.size(); i++) {
            int index = scroll + i;
            Row row = rows.get(index);
            int top = edge + i * rowHeight, left = edge + (nested[index] ? INDENT : 0);
            boolean usable = row.available(), lit = usable && index == hovered, shadow = usable && look.listShadow();
            int textColor = usable ? color(look, row.color(), look.listText()) : look.listMuted();
            int detailColor = usable
                ? color(look, row.detailColor() != 0 ? row.detailColor() : row.color(), look.listDetail())
                : look.listMuted();
            if (nested[index]) GuiDraw.drawRect(edge, top, rowRight - edge, rowHeight, look.nested());
            if (row.selected()) {
                look.drawSelectedRow(left, top, rowRight - left, rowHeight);
                textColor = detailColor = look.selectedText();
            }
            if (lit && !row.header() && !row.selected())
                GuiDraw.drawRect(left, top, rowRight - left, rowHeight, look.hover());
            int textLeft = left + 3;
            if (row.icon() != null) {
                new ItemDrawable(row.icon())
                    .draw(context, left + 1, top + (rowHeight - ICON) / 2, ICON, ICON, widgetTheme.getTheme());
                textLeft = left + 19;
            }
            int right = textRight;
            if (row.number() != null) right -= numberWidth + 2;
            if (row.options() != null) {
                AE2Panels.drawCaret(
                    textRight - CARET + 2,
                    top + (rowHeight - AE2Theme.CARET_HEIGHT) / 2,
                    isExpanded(row.text(), occurrence[index]),
                    textColor);
                right -= CARET;
            }
            if (row.toggle() != null) {
                GlStateManager.color(1, 1, 1, usable ? 1 : AE2Theme.INACTIVE_ALPHA);
                right -= drawSwitch(look, textRight, top, rowHeight, row.toggle(), lit) + 3;
                GlStateManager.color(1, 1, 1, 1);
            }
            String detail = row.detail() == null ? "" : row.detail();
            int detailWidth = detail.isEmpty() ? 0 : font.getStringWidth(detail) + 3;
            if (!detail.isEmpty())
                font.drawString(detail, right - detailWidth + 3, top + textTop(rowHeight), detailColor, shadow);
            String text = font.trimStringToWidth(row.text(), Math.max(0, right - detailWidth - textLeft));
            if (row.header()) drawHeader(look, font, text, textLeft, top, rowHeight, rowRight);
            else font.drawString(text, textLeft, top + textTop(rowHeight), textColor, shadow);
        }
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private static int color(AE2Look look, int color, int plain) {
        if (color == TEXT) return plain;
        return color == GOOD ? look.good() : color == BAD ? look.bad() : color;
    }

    /**
     * Returns how far below a row's top its one-line text starts, so the text is centred in the row; 5 for the
     * standard {@link #ROW}.
     *
     * @param rowHeight row height in pixels
     * @return text offset in pixels
     */
    public static int textTop(int rowHeight) {
        return (rowHeight - TEXT_HEIGHT) / 2;
    }

    /**
     * Draws a row's on/off switch against the row's right end, centered in the row; see
     * {@link AE2Checkboxes#drawCheckbox}.
     *
     * @param look      look of the panel the row is in
     * @param right     pixel the switch ends at
     * @param top       top of the row
     * @param rowHeight row height in pixels, {@link #ROW} for the standard rows
     * @param on        whether the switch is on
     * @param hovered   whether the row is under the mouse
     * @return width of the drawn switch
     */
    public static int drawSwitch(AE2Look look, int right, int top, int rowHeight, boolean on, boolean hovered) {
        int width = look.isAE2() ? AE2Theme.SWITCH_WIDTH : AE2Theme.SWITCH_HEIGHT;
        return AE2Checkboxes
            .drawCheckbox(look.variant(), right - width, top + (rowHeight - AE2Theme.SWITCH_HEIGHT) / 2, on, hovered);
    }

    /**
     * Draws a section header: text in the look's muted color, followed under AE2 themes by a divider line up to the
     * row's end.
     *
     * @param look      look of the panel the row is in
     * @param font      font to draw with
     * @param text      header text, already cut to the available width
     * @param left      left edge of the text
     * @param top       top of the row
     * @param rowHeight row height in pixels, {@link #ROW} for the standard rows
     * @param right     right end of the row
     */
    public static void drawHeader(AE2Look look, FontRenderer font, String text, int left, int top, int rowHeight,
        int right) {
        int textY = top + textTop(rowHeight);
        AE2Draw.text(text, left, textY, look.listMuted(), false);
        AE2Variant variant = look.variant();
        if (variant == null) return;
        int start = left + font.getStringWidth(text) + 4, width = right - 4 - start;
        if (width <= 0) return;
        GuiDraw.drawRect(start, textY + 4, width, 1, variant.colors.line());
        GuiDraw.drawRect(start, textY + 5, width, 1, HEADER_LINE_ALPHA | variant.colors.edge() & 0xFFFFFF);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        int index = hoveredRow();
        if (index < 0) return Result.ACCEPT;
        Row row = rows.get(index);
        if (!row.available()) return Result.STOP;
        if (mouseButton == 1) {
            if (row.secondary() == null) return Result.ACCEPT;
            row.secondary()
                .run();
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }
        if (mouseButton != 0) return Result.ACCEPT;
        if (row.options() != null) {
            if (isExpanded(row.text(), occurrence[index])) expanded = null;
            else {
                expanded = row.text();
                expandedOccurrence = occurrence[index];
            }
            Interactable.playButtonClickSound();
        } else if (row.click() != null) {
            row.click()
                .run();
            if (nested[index]) expanded = null;
            Interactable.playButtonClickSound();
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        scrollbar.scroll(scrollDirection);
        return true;
    }

    @Override
    public ItemStack getStackForRecipeViewer() {
        int index = hoveredRow();
        return index < 0 ? null
            : rows.get(index)
                .icon();
    }
}

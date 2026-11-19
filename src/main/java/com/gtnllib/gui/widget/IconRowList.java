package com.gtnllib.gui.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;
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
import com.cleanroommc.modularui.widget.Widget;

/**
 * A scrolling list whose rows are drawn directly instead of being widgets, so it stays fast with hundreds of rows.
 * The rows are read from a supplier every tick. A row can carry an item icon, a right-aligned detail, a ‹ › stepper, an
 * on/off switch, or a dropdown whose options open as indented rows under it.
 */
public class IconRowList extends Widget<IconRowList> implements Interactable, RecipeViewerIngredientProvider {

    public static final int ROW = 18, ARROW = 10, SCROLLBAR = 3, INDENT = 10, CARET = 9, SWITCH = 16;
    public static final int BACKGROUND = 0xFF1C1C22, NESTED = 0xFF26262E, HOVER = 0x30FFFFFF, SELECTED = 0x5040A0FF,
        BAR = 0xFF808090, HEADER = 0xFFA0A0A8, TEXT = 0xFFE8E8E8, ACCENT = 0xFFFFD040, SWITCH_ON = 0xFF3C9A50,
        SWITCH_OFF = 0xFF4A4A54, KNOB = 0xFFF0F0F0;

    /**
     * One row. Only one of click, adjust, options and toggle is normally set; options turn the row into a dropdown,
     * and a non-null toggle draws a switch whose state is that value.
     */
    public record Row(ItemStack icon, String text, String detail, int color, boolean selected, boolean header,
        Runnable click, IntConsumer adjust, List<Row> options, Boolean toggle) {

        public static Row header(String text) {
            return new Row(null, text, "", HEADER, false, true, null, null, null, null);
        }

        public static Row item(ItemStack icon, String text, String detail, boolean selected, Runnable click) {
            return new Row(icon, text, detail, TEXT, selected, false, click, null, null, null);
        }

        public static Row colored(ItemStack icon, String text, String detail, int color) {
            return new Row(icon, text, detail, color, false, false, null, null, null, null);
        }

        public static Row stepper(String text, String detail, IntConsumer adjust) {
            return new Row(null, text, detail, TEXT, false, false, null, adjust, null, null);
        }

        public static Row toggle(String text, boolean on, Runnable click) {
            return new Row(null, text, "", TEXT, false, false, click, null, null, on);
        }

        public static Row dropdown(ItemStack icon, String text, String detail, boolean selected, List<Row> options) {
            return new Row(icon, text, detail, TEXT, selected, false, null, null, options, null);
        }
    }

    private final Supplier<List<Row>> source;
    private List<Row> rows = Collections.emptyList();
    private boolean[] nested = new boolean[0];
    private String expanded;
    private int scroll;

    public IconRowList(Supplier<List<Row>> source) {
        this.source = source;
    }

    public void scrollToTop() {
        scroll = 0;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        List<Row> flat = new ArrayList<>();
        List<Boolean> inside = new ArrayList<>();
        for (Row row : source.get()) {
            flat.add(row);
            inside.add(false);
            if (row.options() == null || !row.text()
                .equals(expanded)) continue;
            for (Row option : row.options()) {
                flat.add(option);
                inside.add(true);
            }
        }
        rows = flat;
        nested = new boolean[flat.size()];
        for (int i = 0; i < nested.length; i++) nested[i] = inside.get(i);
        scroll = Math.max(0, Math.min(scroll, rows.size() - visibleRows()));
    }

    private int visibleRows() {
        return Math.max(1, getArea().height / ROW);
    }

    private int hoveredRow() {
        if (!isHovering()) return -1;
        int row = scroll + getContext().getMouseY() / ROW;
        return row >= 0 && row < rows.size() && getContext().getMouseY() < visibleRows() * ROW ? row : -1;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        int width = getArea().width, height = getArea().height;
        GuiDraw.drawRect(0, 0, width, height, BACKGROUND);
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        int hovered = hoveredRow(), visible = visibleRows();
        int textRight = width - SCROLLBAR - 2;
        for (int i = 0; i < visible && scroll + i < rows.size(); i++) {
            int index = scroll + i;
            Row row = rows.get(index);
            int top = i * ROW, left = nested[index] ? INDENT : 0;
            if (nested[index]) GuiDraw.drawRect(0, top, width - SCROLLBAR, ROW, NESTED);
            if (row.selected()) GuiDraw.drawRect(left, top, width - SCROLLBAR - left, ROW, SELECTED);
            if (index == hovered && !row.header()) GuiDraw.drawRect(left, top, width - SCROLLBAR - left, ROW, HOVER);
            int textLeft = left + 3;
            if (row.icon() != null) {
                new ItemDrawable(row.icon()).draw(context, left + 1, top + 1, 16, 16, widgetTheme.getTheme());
                textLeft = left + 19;
            }
            int right = textRight;
            if (row.adjust() != null) {
                font.drawString("<", textRight - ARROW * 2 + 3, top + 5, ACCENT);
                font.drawString(">", textRight - ARROW + 3, top + 5, ACCENT);
                right -= ARROW * 2;
            }
            if (row.options() != null) {
                caret(
                    textRight - CARET + 2,
                    top + 7,
                    row.text()
                        .equals(expanded));
                right -= CARET;
            }
            if (row.toggle() != null) {
                toggle(textRight - SWITCH, top + 5, row.toggle());
                right -= SWITCH + 3;
            }
            String detail = row.detail() == null ? "" : row.detail();
            int detailWidth = detail.isEmpty() ? 0 : font.getStringWidth(detail) + 3;
            if (!detail.isEmpty()) font.drawString(detail, right - detailWidth + 3, top + 5, row.color());
            String text = font.trimStringToWidth(row.text(), Math.max(0, right - detailWidth - textLeft));
            font.drawString(text, textLeft, top + 5, row.header() ? HEADER : row.color());
        }
        if (rows.size() > visible) {
            int barHeight = Math.max(8, height * visible / rows.size());
            int barTop = (height - barHeight) * scroll / Math.max(1, rows.size() - visible);
            GuiDraw.drawRect(width - SCROLLBAR, barTop, SCROLLBAR, barHeight, BAR);
        }
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private static void caret(int x, int y, boolean open) {
        for (int line = 0; line < 3; line++) {
            int row = open ? 2 - line : line;
            GuiDraw.drawRect(x + line, y + row, 5 - line * 2, 1, ACCENT);
        }
    }

    private static void toggle(int x, int y, boolean on) {
        GuiDraw.drawRect(x, y, SWITCH, 8, on ? SWITCH_ON : SWITCH_OFF);
        GuiDraw.drawRect(on ? x + SWITCH - 7 : x + 1, y + 1, 6, 6, KNOB);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        int index = hoveredRow();
        if (index < 0) return Result.ACCEPT;
        Row row = rows.get(index);
        int textRight = getArea().width - SCROLLBAR - 2, mouseX = getContext().getMouseX();
        if (row.options() != null) {
            expanded = row.text()
                .equals(expanded) ? null : row.text();
            Interactable.playButtonClickSound();
        } else if (row.adjust() != null && mouseX >= textRight - ARROW * 2) {
            row.adjust()
                .accept(mouseX >= textRight - ARROW ? 1 : -1);
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
        scroll = Math.max(0, Math.min(rows.size() - visibleRows(), scroll - scrollDirection.modifier));
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

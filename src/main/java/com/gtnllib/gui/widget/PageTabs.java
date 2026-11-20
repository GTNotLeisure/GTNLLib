package com.gtnllib.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.AE2Theme.IconSource;

/**
 * AE2's page tabs, the mode tabs of its pattern encoding terminal, as one widget: 22-by-22 tabs stacked
 * {@link AE2Theme#PAGE_TAB_PITCH} apart down the right edge of a panel, one per page. The owner keeps the open page;
 * the tabs read it every frame and hand every choice back, so code may also switch pages itself. It can be built on
 * either side, so it also works in GUIs the server opens.
 * <p>
 * Place the widget at {@code panelRight - AE2Theme.PAGE_TAB_INSET} and add it after the panel's other children: the
 * open tab then covers the panel border and joins the panel, and the others stand out. {@link #page(int, Widget)}
 * shows a page widget only while its tab is open. Other themes draw the tabs as buttons, the open one hovered. A tab
 * made unavailable with {@link #available(BooleanSupplier)} shows no hover, draws its icon faded and swallows clicks.
 */
public class PageTabs extends ParentWidget<PageTabs> {

    private final IntSupplier open;
    private final IntConsumer select;
    private final List<BooleanSupplier[]> usable = new ArrayList<>();

    /**
     * Creates an empty column of tabs.
     *
     * @param open   supplier of the open page's index, read every frame
     * @param select receives the index of a tab the player clicks while another page is open
     */
    public PageTabs(IntSupplier open, IntConsumer select) {
        this.open = open;
        this.select = select;
        size(AE2Theme.PAGE_TAB_SIZE, 0);
    }

    /**
     * Adds a tab below the existing ones; the first tab is page zero.
     *
     * @param icon    supplier of the icon, read every frame: a built-in {@code Icon}, or any texture or item through
     *                {@link IconSource#of}
     * @param tooltip supplier of the tooltip line, such as the page name, read every frame
     * @return these tabs
     */
    public PageTabs tab(Supplier<? extends IconSource> icon, Supplier<String> tooltip) {
        int index = usable.size();
        BooleanSupplier[] available = { () -> true };
        usable.add(available);
        ActionButton tab = new ActionButton();
        IDrawable look = AE2Theme.pageTab(tab, icon, () -> open.getAsInt() == index, () -> available[0].getAsBoolean());
        tab.available(() -> available[0].getAsBoolean())
            .background(look)
            .hoverBackground(look)
            .disableHoverThemeBackground(true)
            .pos(0, index * AE2Theme.PAGE_TAB_PITCH)
            .size(AE2Theme.PAGE_TAB_SIZE, AE2Theme.PAGE_TAB_SIZE)
            .tooltipAutoUpdate(true)
            .tooltipBuilder(lines -> lines.addLine(tooltip.get()))
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                if (open.getAsInt() != index) select.accept(index);
                return true;
            });
        child(tab);
        height(usable.size() * AE2Theme.PAGE_TAB_PITCH + AE2Theme.PAGE_TAB_SIZE - AE2Theme.PAGE_TAB_PITCH);
        return this;
    }

    /**
     * Sets when the tab added last can be opened.
     *
     * @param available supplier read every frame and on every click
     * @return these tabs
     * @throws IllegalStateException if no tab was added yet
     */
    public PageTabs available(BooleanSupplier available) {
        if (usable.isEmpty()) throw new IllegalStateException("Add a tab before setting when it is available");
        usable.get(usable.size() - 1)[0] = available;
        return this;
    }

    /**
     * Shows a page widget only while its tab is open, by setting the widget's enabled condition.
     *
     * @param index page index, the position of its tab
     * @param page  widget holding the page
     * @param <W>   widget type
     * @return the same widget
     */
    public <W extends Widget<?>> W page(int index, W page) {
        page.setEnabledIf(widget -> open.getAsInt() == index);
        return page;
    }
}

package com.gtnllib.gui.widget;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2ButtonPainter;
import com.gtnllib.gui.theme.ae2.IconSource;

public class PageTabs extends ParentWidget<PageTabs> {

    private static final int MIN_WIDTH = AE2Theme.PAGE_TAB_INSET + AE2Theme.ICON_SIZE + 2,
        MIN_HEIGHT = AE2Theme.PAGE_TAB_SIZE;

    private final IntSupplier open;
    private final IntConsumer select;
    private int tabWidth = AE2Theme.PAGE_TAB_SIZE, tabHeight = AE2Theme.PAGE_TAB_SIZE;
    private int nextPage;

    /**
     * Creates an empty column of tabs.
     *
     * @param open   supplier of the open page's index, read every frame
     * @param select receives the index of a tab the player clicks while another page is open
     */
    public PageTabs(IntSupplier open, IntConsumer select) {
        this.open = open;
        this.select = select;
        size(AE2Theme.PAGE_TAB_SIZE, 0).excludeAreaInRecipeViewer();
    }

    /**
     * Sets the size of every tab, also of tabs added before; AE2's are {@link AE2Theme#PAGE_TAB_SIZE} pixels square.
     * Neighbouring tabs share one outline row, so tabs start {@code height - 1} pixels apart. The widget becomes as
     * wide as a tab.
     *
     * @param width  tab width in pixels, at least 21 so a 16-pixel icon fits beside the covered panel border
     * @param height tab height in pixels, at least 22 so a 16-pixel icon fits between the frame's top and bottom
     * @return these tabs
     */
    public PageTabs tabSize(int width, int height) {
        tabWidth = Math.max(MIN_WIDTH, width);
        tabHeight = Math.max(MIN_HEIGHT, height);
        resize();
        return this;
    }

    private int pitch() {
        return tabHeight - 1;
    }

    private void resize() {
        int index = 0;
        for (IWidget tab : getChildren()) tab.resizer()
            .pos(0, index++ * pitch())
            .size(tabWidth, tabHeight);
        size(tabWidth, hasChildren() ? getChildren().size() * pitch() + 1 : 0);
    }

    @Override
    public boolean addChild(IWidget child, int index) {
        boolean changed = super.addChild(child, index);
        if (changed) resize();
        return changed;
    }

    @Override
    public boolean remove(IWidget child) {
        boolean changed = super.remove(child);
        if (changed) resize();
        return changed;
    }

    @Override
    public boolean remove(int index) {
        boolean changed = super.remove(index);
        if (changed) resize();
        return changed;
    }

    @Override
    public boolean removeAll() {
        boolean changed = super.removeAll();
        nextPage = 0;
        if (changed) resize();
        return changed;
    }

    /**
     * Adds a tab using the next page index. Removing a tab does not renumber existing pages; clearing all tabs resets
     * numbering to zero.
     *
     * @param icon    supplier of the icon, read every frame: an {@code AE2Icon} or {@code AE2SettingIcon}, or any
     *                texture or item through {@link IconSource#of}
     * @param tooltip supplier of the tooltip line, such as the page name, read every frame
     * @return these tabs
     */
    public PageTabs tab(Supplier<? extends IconSource> icon, Supplier<String> tooltip) {
        int index = nextPage++;
        ThemedButton<?> tab = new ThemedButton<>();
        IDrawable look = AE2ButtonPainter.pageTab(tab, icon, () -> open.getAsInt() == index, tab::isAvailable);
        tab.background(look)
            .hoverBackground(look)
            .disableHoverThemeBackground(true)
            .tooltipAutoUpdate(true)
            .tooltipBuilder(lines -> lines.addLine(tooltip.get()))
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                if (open.getAsInt() != index) select.accept(index);
                return true;
            });
        child(tab);
        return this;
    }

    /**
     * Sets when the tab added last can be opened.
     *
     * @param available supplier read every frame and on every click
     * @return these tabs
     * @throws IllegalStateException if no child exists or the last child has no availability state
     */
    public PageTabs available(BooleanSupplier available) {
        if (!hasChildren() || !(getChildren().get(getChildren().size() - 1) instanceof IAvailable<?>tab))
            throw new IllegalStateException("The widget added last has no available state");
        tab.available(available);
        return this;
    }

    /**
     * Shows a page widget only while its tab is open, by setting the widget's enabled condition.
     *
     * @param index page index assigned when its tab was added
     * @param page  widget holding the page
     * @param <W>   widget type
     * @return the same widget
     */
    public <W extends Widget<?>> W page(int index, W page) {
        page.setEnabledIf(widget -> open.getAsInt() == index);
        return page;
    }
}

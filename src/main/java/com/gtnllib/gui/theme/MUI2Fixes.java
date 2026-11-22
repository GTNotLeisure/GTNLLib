package com.gtnllib.gui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.widget.AbstractScrollWidget;
import com.cleanroommc.modularui.widget.scroll.ScrollArea;
import com.cleanroommc.modularui.widget.scroll.ScrollData;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.menu.Menu;
import com.cleanroommc.modularui.widgets.menu.MenuPanel;
import com.gtnllib.gui.theme.ae2.AE2ButtonDrawable;
import com.gtnllib.gui.theme.ae2.AE2ButtonDrawable.State;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.AE2Variant;

public class MUI2Fixes {

    /**
     * Pixels a menu keeps clear inside the border of a menu panel in a plain theme.
     */
    public static final int MENU_EDGE = 1;
    // The thread name FML's getEffectiveSide tests for; FML's handler itself cannot load outside a game.
    private static final String SERVER_THREAD = "Server thread";

    private MUI2Fixes() {}

    /**
     * Turns off MUI2's darkened top and bottom edges of a scroll area, which AE2's lists do not have.
     *
     * @param scroll scroll widget, such as a list
     */
    public static void noScrollShadows(AbstractScrollWidget<?, ?> scroll) {
        scroll.showScrollShadows(false);
    }

    /**
     * Sets the color MUI2 paints behind a scroll area's scroll bar, which no theme entry holds.
     *
     * @param scroll scroll widget, such as a list
     * @param color  ARGB track color
     */
    public static void scrollTrack(AbstractScrollWidget<?, ?> scroll, int color) {
        scroll.getScrollArea()
            .setScrollBarBackgroundColor(color);
    }

    /**
     * Scrolls one wheel step without overflowing MUI2's integer target calculation.
     */
    public static boolean scrollWheel(ScrollArea area, ScrollData data, UpOrDown direction) {
        if (!data.isScrollBarActive(area)) return false;
        int current = data.getScroll(), max = data.getScrollSize() - data.getFullVisibleSize(area);
        int from = data.isAnimating() ? data.getAnimatingTo() : current;
        int target = (int) Math.max(0, Math.min(max, from - (long) direction.modifier * data.getScrollSpeed()));
        data.scrollTo(area, target);
        boolean changed = data.getScroll() != current;
        data.scrollTo(area, current);
        if (changed) data.animateTo(area, target);
        return changed || data.isCancelScrollEdge();
    }

    /**
     * Gives an opened menu panel the menu theme of a look: the skin's menu theme under an AE2 skin, MUI2's context menu
     * otherwise. MUI2 sets the context-menu theme on every menu panel it creates; a theme registered for the panel's
     * name still wins.
     *
     * @param panel menu panel that is open
     * @param look  look of the panel the menu was opened from
     */
    public static void menuPanelTheme(MenuPanel panel, AE2Look look) {
        panel.themeOverride(menuTheme(look));
    }

    /**
     * Registers the menu theme of a look for a menu panel of a screen, so the menu follows the screen's skin when MUI2
     * opens it: the skin's menu theme under an AE2 skin, MUI2's context menu otherwise. MUI2 keeps the registration
     * for the screen name until it is registered again. Only the client registers: MUI2's table is read while drawing,
     * and a GUI the server opens is also built on the server's thread, which never opens a menu.
     *
     * @param owner  identifier of the mod that owns the screen
     * @param screen screen name, which is the name of its main panel
     * @param menu   name of the menu panel, which is the name given to the menu button
     * @param look   look of the screen
     */
    public static void registerMenuTheme(String owner, String screen, String menu, AE2Look look) {
        if (SERVER_THREAD.equals(
            Thread.currentThread()
                .getName()))
            return;
        IThemeApi.get()
            .registerThemeForScreen(owner + ":" + screen + ":" + menu, menuTheme(look));
    }

    /**
     * Keeps a menu's contents inside its panel's border: under an AE2 skin, whose panel border has a shadow below, two
     * pixels at the top and sides and four at the bottom; {@link #MENU_EDGE} otherwise.
     *
     * @param look look the menu is drawn in
     */
    public static void menuPadding(Menu<?> menu, AE2Look look) {
        if (look.isAE2())
            menu.padding(AE2Theme.PANEL_EDGE, AE2Theme.PANEL_EDGE, AE2Theme.PANEL_EDGE, AE2Theme.PANEL_BOTTOM_EDGE);
        else menu.padding(MENU_EDGE);
    }

    /**
     * Draws a slider's handle as an AE2 button of a skin instead of MUI2's own handle.
     *
     * @param variant AE2 skin
     */
    public static void sliderHandle(SliderWidget slider, AE2Variant variant) {
        slider.sliderTexture(new AE2ButtonDrawable(State.NORMAL, variant.folder));
    }

    private static String menuTheme(AE2Look look) {
        AE2Variant variant = look.variant();
        return variant == null ? AE2Theme.CONTEXT_MENU_ID : variant.menuId;
    }
}

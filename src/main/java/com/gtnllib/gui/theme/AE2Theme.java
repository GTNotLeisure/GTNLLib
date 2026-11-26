package com.gtnllib.gui.theme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.ReloadThemeEvent;
import com.cleanroommc.modularui.theme.ThemeBuilder;
import com.cleanroommc.modularui.widget.AbstractScrollWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.menu.AbstractMenuButton;
import com.cleanroommc.modularui.widgets.menu.Menu;
import com.cleanroommc.modularui.widgets.textfield.BaseTextFieldWidget;
import com.gtnllib.GTNLLib;
import com.gtnllib.gui.theme.ae2.AE2ButtonDrawable.State;
import com.gtnllib.gui.theme.ae2.AE2ButtonPainter;
import com.gtnllib.gui.theme.ae2.AE2CheckboxPainter;
import com.gtnllib.gui.theme.ae2.AE2Draw;
import com.gtnllib.gui.theme.ae2.AE2Legacy;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.AE2PanelPainter;
import com.gtnllib.gui.theme.ae2.AE2ScrollbarPainter;
import com.gtnllib.gui.theme.ae2.AE2ThemeDefinitions;
import com.gtnllib.gui.theme.ae2.AE2ThemeLoader;
import com.gtnllib.gui.theme.ae2.AE2UpgradePainter;
import com.gtnllib.gui.theme.ae2.AE2Variant;
import com.gtnllib.gui.theme.ae2.ButtonText;
import com.gtnllib.gui.theme.ae2.IconSource;
import com.gtnllib.gui.widget.CycleDropdown;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AE2Theme {

    /**
     * Light panel theme identifier.
     */
    public static final String LIGHT_ID = "gtnllib:ae2_light";
    /**
     * Light context-menu theme identifier.
     */
    public static final String LIGHT_MENU_ID = "gtnllib:ae2_light_menu";
    /**
     * Dark panel theme identifier.
     */
    public static final String DARK_ID = "gtnllib:ae2_dark";
    /**
     * Dark context-menu theme identifier.
     */
    public static final String DARK_MENU_ID = "gtnllib:ae2_dark_menu";
    /**
     * Dark panel theme identifier with purple selection highlights.
     */
    public static final String DARK_PURPLE_ID = "gtnllib:ae2_dark_purple";
    /**
     * Dark context-menu theme identifier with purple selection highlights.
     */
    public static final String DARK_PURPLE_MENU_ID = "gtnllib:ae2_dark_purple_menu";
    /**
     * MUI2 context-menu theme used outside AE2 panels.
     */
    public static final String CONTEXT_MENU_ID = "modularui.context_menu";
    /**
     * ARGB light-blue selection fill, top edge and shadow of the light and dark variants.
     */
    public static final int HIGHLIGHT = 0xFF9CD3FF, HIGHLIGHT_TOP = 0xFFDAFFFF, HIGHLIGHT_SHADOW = 0xFF708CBA,
        FIELD_SELECTION = 0xFF0000FF;
    /**
     * ARGB fluix-purple selection fill, top edge and shadow of the purple dark variant.
     */
    public static final int PURPLE_HIGHLIGHT = 0xFF644595, PURPLE_HIGHLIGHT_TOP = 0xFFE2A3E3,
        PURPLE_HIGHLIGHT_SHADOW = 0xFF212040;
    /**
     * ARGB text color of a text field that cannot be edited, as Minecraft draws uneditable fields.
     */
    public static final int FIELD_DISABLED_TEXT = 0xFF707070;
    /**
     * Widget dimensions, label offsets, border widths and layout spacing in GUI pixels. {@code SMALL_SCROLLER_WIDTH}
     * and {@code BIG_SCROLLER_WIDTH} are AE2's two scroll-bar handles; {@code LIST_ROW_HEIGHT} is the height of AE2's
     * row sprite, not of GTNLLib's list rows ({@code IconRowList.ROW}); {@code UPGRADE_SLOTS_PER_COLUMN} is a count.
     * The {@code CHECKBOX} sizes are those of AE2's check box, a 22-by-12 on/off sprite, and of its label.
     */
    public static final int BUTTON_HEIGHT = 20, TEXT_FIELD_HEIGHT = 12, ICON_SIZE = 16, ICON_BUTTON_WIDTH = 18,
        ICON_BUTTON_HEIGHT = 20, TAB_BUTTON_SIZE = 20, CHECKBOX_WIDTH = 22, CHECKBOX_HEIGHT = 12, CHECKBOX_LABEL_X = 26,
        CHECKBOX_LABEL_Y = 4, SMALL_SCROLLER_WIDTH = 7, INSET_TOP = 3, INSET_EDGE = 1, BIG_SCROLLER_WIDTH = 12,
        SCROLLER_HEIGHT = 15, RADIO_SIZE = 14, RADIO_LABEL_X = 16, RADIO_LABEL_Y = 4, LIST_ROW_HEIGHT = 22,
        PROGRESS_WIDTH = 6, PROGRESS_HEIGHT = 18, BUTTON_BAR_MARGIN = 2, BUTTON_BAR_SPACING = 6, UPGRADE_PADDING = 5,
        UPGRADE_SLOTS_PER_COLUMN = 8, VALIDATION_ICON_SIZE = 8, PANEL_EDGE = 2, PANEL_BOTTOM_EDGE = 4;
    /**
     * @deprecated use {@link #CHECKBOX_WIDTH}, {@link #CHECKBOX_HEIGHT}, {@link #CHECKBOX_LABEL_X} and
     *             {@link #CHECKBOX_LABEL_Y}; the on/off box is called a check box everywhere, after AE2's
     *             {@code AECheckbox}
     */
    @Deprecated
    public static final int SWITCH_WIDTH = CHECKBOX_WIDTH, SWITCH_HEIGHT = CHECKBOX_HEIGHT,
        SWITCH_LABEL_X = CHECKBOX_LABEL_X, SWITCH_LABEL_Y = CHECKBOX_LABEL_Y;
    /**
     * @deprecated use {@link #SMALL_SCROLLER_WIDTH}; this name reads like the width of a whole scroll bar
     */
    @Deprecated
    public static final int SCROLLBAR_WIDTH = SMALL_SCROLLER_WIDTH;
    /**
     * @deprecated use {@link #UPGRADE_SLOTS_PER_COLUMN}; the slots run down a column, not across a row
     */
    @Deprecated
    public static final int UPGRADE_ROWS = UPGRADE_SLOTS_PER_COLUMN;
    /**
     * AE2 page tabs: size, distance between tab tops (neighbours share an outline row) and how far a tab starts inside
     * the panel's right edge, which its left columns cover so the selected tab joins the panel.
     */
    public static final int PAGE_TAB_SIZE = 22, PAGE_TAB_PITCH = 21, PAGE_TAB_INSET = 3;
    /**
     * How far an upgrade panel starts inside the right edge of the panel it belongs to, as AE2 places it.
     */
    public static final int UPGRADE_OVERLAP = 2;
    /**
     * Size of the dropdown triangle drawn by {@link AE2PanelPainter#drawCaret}.
     */
    public static final int CARET_WIDTH = 5, CARET_HEIGHT = 3;
    /**
     * Size of AE2's square sprite sheets, the 18-pixel item slot, the space kept left and right of a button label and
     * the height of one line of label text, in GUI pixels.
     */
    public static final int SHEET = 256, SLOT_SIZE = 18, LABEL_PADDING = 2, LABEL_HEIGHT = 8;
    /**
     * ARGB text color of disabled buttons and unavailable text outside the AE2 themes, MUI2's disabled grey.
     */
    public static final int DISABLED_TEXT = 0xFFA0A0A0;
    /**
     * Alpha of sprites and icons drawn for something that cannot be used.
     */
    public static final float INACTIVE_ALPHA = 0.5F;
    /**
     * Folder names of the three AE2 skins below {@code textures/gui/skin/}.
     */
    public static final String LIGHT_FOLDER = "ae2_light", DARK_FOLDER = "ae2_dark", PURPLE_FOLDER = "ae2_dark_purple";
    /**
     * Textures the purple dark skin recolours; it takes the others from {@link #DARK_FOLDER}.
     */
    public static final Set<String> PURPLE_TEXTURES = Set
        .of("button_highlighted", "checkbox", "legacy_states", "list_row", "slider_fill", "slider_tag", "states");
    private static final String ROOT = "textures/gui/skin/";
    /**
     * Shared sizing and spacing used by all three AE2 skins.
     */
    public static final ThemeStyle STYLE = new ThemeStyle(
        ThemeStyle.DEFAULT.cornerLength(),
        ThemeStyle.DEFAULT.cornerThickness(),
        ThemeStyle.DEFAULT.cardAccentTop(),
        ThemeStyle.DEFAULT.cardAccentWidth(),
        ThemeStyle.DEFAULT.cardAccentHeight(),
        2,
        4,
        8,
        8,
        BUTTON_HEIGHT,
        SLOT_SIZE,
        1F);

    /**
     * Registers fresh definitions for built-in and resource pack panel and menu themes.
     * <p>
     * GTNLLib installs the themes during client initialization and invokes this method on theme reload. Consumers
     * select a theme identifier without registering these definitions themselves.
     */
    @SideOnly(Side.CLIENT)
    public static void register() {
        List<AE2Variant> variants = AE2ThemeLoader.load(
            Minecraft.getMinecraft()
                .getResourceManager());
        for (ThemeBuilder<?> definition : Variants.REGISTERED) IThemeApi.get()
            .getJavaDefaultThemes(definition.getId())
            .removeIf(registered -> registered == definition);
        Variants.REGISTERED.clear();
        Variants.ALL = List.copyOf(variants);
        Variants.BY_ID = index(variants);
        for (AE2Variant variant : variants) {
            install(AE2ThemeDefinitions.panelTheme(variant));
            install(AE2ThemeDefinitions.menuTheme(variant));
        }
    }

    private static void install(ThemeBuilder<?> theme) {
        // ThemeManager mutates registered JSON while parsing; every reload needs a fresh definition.
        IThemeApi.get()
            .registerTheme(theme);
        Variants.REGISTERED.add(theme);
    }

    /**
     * Resolves a texture below GTNLLib's GUI skin directory. The purple dark skin ships only the
     * {@link #PURPLE_TEXTURES} it recolours and takes every other texture from the dark skin's folder.
     *
     * @param folder skin folder, such as {@code ae2_light}
     * @param name   texture filename without the {@code .png} extension
     * @return texture resource location
     */
    public static ResourceLocation texture(String folder, String name) {
        String source = PURPLE_FOLDER.equals(folder) && !PURPLE_TEXTURES.contains(name) ? DARK_FOLDER : folder;
        return new ResourceLocation(GTNLLib.MODID, ROOT + source + "/" + name + ".png");
    }

    /**
     * Finds the variant associated with a panel or menu theme identifier.
     *
     * @param themeId theme identifier, or {@code null} for the default theme
     * @return the matching variant, or {@code null} for an unrecognized identifier
     */
    public static @Nullable AE2Variant variant(@Nullable String themeId) {
        return themeId == null ? null : Variants.BY_ID.get(themeId);
    }

    /**
     * Returns the active skins, with built-ins first and custom skins in parent-before-child order.
     *
     * @return an immutable snapshot replaced on theme reload
     */
    public static Collection<AE2Variant> variants() {
        return Variants.ALL;
    }

    private static Map<String, AE2Variant> index(Collection<AE2Variant> variants) {
        Map<String, AE2Variant> indexed = new HashMap<>();
        for (AE2Variant variant : variants) {
            indexed.put(variant.id, variant);
            indexed.put(variant.menuId, variant);
        }
        return Map.copyOf(indexed);
    }

    // The holder avoids initializing the skin registry while the built-in skins initialize this class.
    private static class Variants {

        private static List<AE2Variant> ALL = List.of(AE2Variant.values());
        private static Map<String, AE2Variant> BY_ID = index(ALL);
        private static final List<ThemeBuilder<?>> REGISTERED = new ArrayList<>();
    }

    /**
     * Reports whether an identifier names one of the AE2 panel or menu themes.
     *
     * @param themeId theme identifier, possibly {@code null}
     * @return {@code true} for an AE2 theme identifier
     */
    public static boolean isAE2(@Nullable String themeId) {
        return variant(themeId) != null;
    }

    /**
     * Resolves the variant currently used by a modular GUI context, as {@link AE2Look#of(GuiContext)} does. A panel can
     * use a theme other than its screen's; {@link AE2Look#of(IWidget)} resolves the panel's.
     *
     * @return the active variant, or {@code null} for another theme or a non-modular context
     */
    public static @Nullable AE2Variant active(GuiContext context) {
        return AE2Look.of(context)
            .variant();
    }

    /**
     * Maps named child menu panels to the menu theme matching their parent screen.
     *
     * @param screenTheme parent theme identifier, or {@code null} for the default theme
     * @param owner       owning mod identifier
     * @param screen      registered screen name
     * @param menus       child panel names within the screen
     */
    public static void useMenuTheme(@Nullable String screenTheme, String owner, String screen, String... menus) {
        AE2Look look = AE2Look.ofTheme(screenTheme);
        for (String menu : menus) MUI2Fixes.registerMenuTheme(owner, screen, menu, look);
    }

    /**
     * @deprecated use {@link #polish(ModularScreen)}, which also gives sliders AE2's handle
     */
    @Deprecated
    public static void sliderHandles(IWidget root, @Nullable String themeId) {
        AE2Variant variant = AE2Look.ofTheme(themeId)
            .variant();
        if (variant != null) sliderHandles(root, variant);
    }

    private static void sliderHandles(IWidget widget, AE2Variant variant) {
        if (widget instanceof SliderWidget slider) MUI2Fixes.sliderHandle(slider, variant);
        for (IWidget child : widget.getChildren()) sliderHandles(child, variant);
    }

    /**
     * Gives the MUI2 widgets of a screen what its AE2 theme cannot give them, when the screen's theme is an AE2 skin or
     * a theme based on one: scroll areas other than text fields lose MUI2's edge shadows and get the skin's track
     * color, sliders get AE2's handle, and menu buttons such as MUI2's {@code ContextMenuButton} open their menus in
     * the skin's menu theme. Under any other theme it only sends those menus back to MUI2's context-menu theme, since
     * MUI2 keeps the menu theme of a screen name from one opening to the next.
     * <p>
     * Call it once the screen's theme is set with {@code ModularScreen.useTheme} and its main panel is built. A theme
     * set through {@code UISettings.useTheme} for a GUI the server opens is applied only after {@code createScreen}, so
     * calling it there sees the default theme. Name each menu button once, when it is created, and give the menus of a
     * screen different names: the menu theme is kept by that name.
     * <p>
     * Widgets added later, panels opened later and menus built when they open are not reached: give a later panel
     * {@link #polish(IWidget, AE2Look, String, String)}, build a context menu's contents with
     * {@link #menu(ListWidget, String)}, and use {@code CycleDropdown} and {@code GTNLListWidget}, which look after
     * themselves. Shadows a scroll area should keep are turned on again after this call. A screen that changes its
     * theme is built again and polished again, as a skin's handles and track colors are not taken back.
     *
     * @param screen screen whose theme is set and whose main panel is built
     */
    @SideOnly(Side.CLIENT)
    public static void polish(ModularScreen screen) {
        polish(screen.getMainPanel(), AE2Look.of(screen.getCurrentTheme()), screen.getOwner(), screen.getName());
    }

    /**
     * Does what {@link #polish(ModularScreen)} does for one widget tree, such as a panel the screen opens later.
     *
     * @param root   root widget to visit, including its descendants
     * @param look   look of the screen
     * @param owner  identifier of the mod that owns the screen
     * @param screen screen name, which is the name of its main panel
     */
    public static void polish(IWidget root, AE2Look look, String owner, String screen) {
        polishOne(root, look, owner, screen);
        for (IWidget child : root.getChildren()) polish(child, look, owner, screen);
    }

    // Text fields are scroll widgets too. Dropdowns of GTNLLib pick their menu theme themselves, and a registered theme
    // would win over theirs.
    private static void polishOne(IWidget widget, AE2Look look, String owner, String screen) {
        String menu = widget.getName();
        if (widget instanceof AbstractMenuButton<?> && !(widget instanceof CycleDropdown<?>) && menu != null)
            MUI2Fixes.registerMenuTheme(owner, screen, menu, look);
        AE2Variant variant = look.variant();
        if (variant == null) return;
        if (widget instanceof SliderWidget slider) MUI2Fixes.sliderHandle(slider, variant);
        if (widget instanceof AbstractScrollWidget<?, ?>scroll && !(widget instanceof BaseTextFieldWidget<?>)) {
            MUI2Fixes.noScrollShadows(scroll);
            MUI2Fixes.scrollTrack(scroll, variant.colors.line());
        }
    }

    /**
     * Builds the menu of a MUI2 {@code ContextMenuButton} ({@code menu(...)}) around a list of options, laid out like a
     * {@code CycleDropdown}'s menu: as wide as the button, as high as the list, and kept inside the menu panel's
     * border.
     * The list is set to the menu's full width; under an AE2 skin it also loses MUI2's edge shadows and gets the skin's
     * track color. Options that are {@code ButtonWidget}s light up under the pointer in the menu theme; the menu itself
     * follows the screen's skin after {@link #polish(ModularScreen)}.
     *
     * @param options list of options; limit its height with {@code maxSize}
     * @param themeId identifier of the screen's theme, or {@code null} for the default theme
     * @return the menu, ready for {@code menu(...)}
     */
    public static Menu<?> menu(ListWidget<?, ?> options, @Nullable String themeId) {
        AE2Look look = AE2Look.ofTheme(themeId);
        AE2Variant variant = look.variant();
        if (variant != null) {
            MUI2Fixes.noScrollShadows(options);
            MUI2Fixes.scrollTrack(options, variant.colors.line());
        }
        Menu<?> menu = new Menu<>().widthRel(1f)
            .coverChildrenHeight()
            .child(options.widthRel(1f));
        MUI2Fixes.menuPadding(menu, look);
        return menu;
    }

    /**
     * @deprecated use {@link AE2Legacy#legacyStates}
     */
    @Deprecated
    public static ResourceLocation legacyStates(@Nullable AE2Variant variant) {
        return AE2Legacy.legacyStates(AE2Look.of(variant));
    }

    /**
     * @deprecated use {@link AE2Legacy#legacyTerminal}
     */
    @Deprecated
    public static ResourceLocation legacyTerminal(@Nullable AE2Variant variant) {
        return AE2Legacy.legacyTerminal(AE2Look.of(variant));
    }

    /**
     * @deprecated use {@link AE2Look#scrollbarWidth} of the panel's look
     */
    @Deprecated
    public static int scrollbarWidth(@Nullable AE2Variant variant) {
        return AE2Look.of(variant)
            .scrollbarWidth();
    }

    /**
     * @deprecated use {@link AE2Look#drawScrollbar} of the panel's look
     */
    @Deprecated
    public static void drawScrollbar(@Nullable AE2Variant variant, int x, int y, int height, int handleY,
        boolean enabled) {
        AE2Look.of(variant)
            .drawScrollbar(x, y, height, handleY, enabled);
    }

    /**
     * @deprecated use {@link AE2ScrollbarPainter#drawSmallScrollbar}
     */
    @Deprecated
    public static void drawSmallScrollbar(AE2Variant variant, int x, int y, int height, int handleY, boolean enabled) {
        AE2ScrollbarPainter.drawSmallScrollbar(variant, x, y, height, handleY, enabled);
    }

    /**
     * @deprecated use {@link AE2PanelPainter#drawButtonBar}
     */
    @Deprecated
    public static void drawButtonBar(AE2Variant variant, int x, int y, int width, int height) {
        AE2PanelPainter.drawButtonBar(variant, x, y, width, height);
    }

    /**
     * @deprecated use {@link AE2UpgradePainter#upgradePanelWidth}
     */
    @Deprecated
    public static int upgradePanelWidth(int slots) {
        return AE2UpgradePainter.upgradePanelWidth(slots);
    }

    /**
     * @deprecated use {@link AE2UpgradePainter#upgradePanelHeight}
     */
    @Deprecated
    public static int upgradePanelHeight(int slots) {
        return AE2UpgradePainter.upgradePanelHeight(slots);
    }

    /**
     * @deprecated use {@link AE2UpgradePainter#drawUpgradePanel}
     */
    @Deprecated
    public static void drawUpgradePanel(AE2Variant variant, int x, int y, int slots) {
        AE2UpgradePainter.drawUpgradePanel(variant, x, y, slots);
    }

    /**
     * @deprecated use {@link AE2PanelPainter#tableSize}
     */
    @Deprecated
    public static int tableSize(int cells, int cellSize) {
        return AE2PanelPainter.tableSize(cells, cellSize);
    }

    /**
     * @deprecated use {@link AE2PanelPainter#drawTable}
     */
    @Deprecated
    public static void drawTable(AE2Variant variant, int x, int y, int columns, int rows, int cellWidth,
        int cellHeight) {
        AE2PanelPainter.drawTable(variant, x, y, columns, rows, cellWidth, cellHeight);
    }

    /**
     * @deprecated use {@link AE2PanelPainter#listRow}, which follows the theme of the row's own panel
     */
    @Deprecated
    public static IDrawable listRow(BooleanSupplier selected) {
        return (context, x, y, width, height, theme) -> AE2PanelPainter
            .drawListRow(AE2Look.of(context), selected.getAsBoolean(), x, y, width, height);
    }

    /**
     * @deprecated use {@link AE2CheckboxPainter#radio}
     */
    @Deprecated
    public static IDrawable radio(Widget<?> widget, BooleanSupplier selected, Supplier<String> label,
        BooleanSupplier available) {
        return AE2CheckboxPainter.radio(widget, selected, label, available);
    }

    /**
     * @deprecated use {@link AE2CheckboxPainter#radioWidth}
     */
    @Deprecated
    public static int radioWidth(String label) {
        return AE2CheckboxPainter.radioWidth(label);
    }

    /**
     * @deprecated use {@link AE2Legacy#drawLegacyState}
     */
    @Deprecated
    public static void drawLegacyState(@Nullable AE2Variant variant, int index, float x, float y, float size) {
        AE2Legacy.drawLegacyState(AE2Look.of(variant), index, x, y, size);
    }

    /**
     * @deprecated use {@link AE2ButtonPainter#button}
     */
    @Deprecated
    public static IDrawable button(Widget<?> widget, BooleanSupplier available, BooleanSupplier selected) {
        return AE2ButtonPainter.button(widget, available, selected);
    }

    /**
     * @deprecated use {@link AE2Look#buttonText} of the look of the button's own panel
     */
    @Deprecated
    public static ButtonText buttonText(GuiContext context, State state, int height) {
        return AE2Look.of(context)
            .buttonText(state, height);
    }

    /**
     * @deprecated use {@link AE2ButtonPainter#label}
     */
    @Deprecated
    public static IDrawable label(Widget<?> widget, Supplier<String> text, BooleanSupplier available,
        BooleanSupplier selected) {
        return AE2ButtonPainter.label(widget, text, available, selected);
    }

    /**
     * @deprecated use {@link AE2ButtonPainter#iconButton}
     */
    @Deprecated
    public static IDrawable iconButton(Widget<?> widget, Supplier<? extends IconSource> icon,
        BooleanSupplier available) {
        return AE2ButtonPainter.iconButton(widget, icon, available);
    }

    /**
     * @deprecated use {@link AE2ButtonPainter#tabButton}
     */
    @Deprecated
    public static IDrawable tabButton(Widget<?> widget, Supplier<? extends IconSource> icon,
        BooleanSupplier available) {
        return AE2ButtonPainter.tabButton(widget, icon, available);
    }

    /**
     * @deprecated use {@link AE2CheckboxPainter#checkbox}
     */
    @Deprecated
    public static IDrawable toggleSwitch(Widget<?> widget, BooleanSupplier on, Supplier<String> label,
        BooleanSupplier available) {
        return AE2CheckboxPainter.checkbox(widget, on, label, available);
    }

    /**
     * @deprecated use {@link AE2CheckboxPainter#checkboxWidth}
     */
    @Deprecated
    public static int switchWidth(String label) {
        return AE2CheckboxPainter.checkboxWidth(label);
    }

    /**
     * @deprecated use {@link AE2Draw#text}
     */
    @Deprecated
    public static void text(String text, int x, int y, int color, boolean shadow) {
        AE2Draw.text(text, x, y, color, shadow);
    }

    @SideOnly(Side.CLIENT)
    public static class Reloader {

        /**
         * Refreshes the registered theme definitions.
         *
         * @param event theme reload event
         */
        @SubscribeEvent
        public void onThemeReload(ReloadThemeEvent.Pre event) {
            register();
        }
    }
}

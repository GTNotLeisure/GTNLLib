package com.gtnllib.gui.theme;

import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

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
import com.gtnllib.gui.theme.ae2.AE2Icon;
import com.gtnllib.gui.theme.ae2.AE2Legacy;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.AE2PanelPainter;
import com.gtnllib.gui.theme.ae2.AE2ScrollbarPainter;
import com.gtnllib.gui.theme.ae2.AE2ThemeDefinitions;
import com.gtnllib.gui.theme.ae2.AE2UpgradePainter;
import com.gtnllib.gui.theme.ae2.AE2Variant;
import com.gtnllib.gui.theme.ae2.ButtonText;
import com.gtnllib.gui.theme.ae2.IconSource;
import com.gtnllib.gui.widget.CycleDropdown;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The AE2 light, dark and purple dark themes for ModularUI 2: their identifiers, AE2's widget dimensions, the three
 * skins with their colors and textures ({@link AE2Variant}), the built-in toolbar icons ({@link AE2Icon}) and the theme
 * registration.
 * <p>
 * Select a panel theme through {@link ScreenTheme}. Panel backgrounds, fields, slots, switches and button styles are
 * supplied by the registered theme. What a theme cannot reach in MUI2's own widgets is added in one call once the
 * screen is built, {@link #polish(ModularScreen)}: scroll areas without MUI2's edge shadows and with the skin's track
 * color, AE2 slider handles, and menus in the skin's menu theme. {@link #menu(ListWidget, String)} builds a context
 * menu's contents. {@code CycleDropdown} and {@code GTNLListWidget} need neither. The MUI2 workarounds themselves are
 * in {@link MUI2Fixes}.
 * <p>
 * Drawing lives in the {@code ae2} package. Widgets ask {@link AE2Look#of(IWidget)} for the look of the panel they are
 * in, and the look answers everything that exists in every look. The painters draw AE2's parts, one class per part:
 * {@link AE2ButtonPainter}, {@link AE2CheckboxPainter}, {@link AE2ScrollbarPainter}, {@link AE2PanelPainter},
 * {@link AE2UpgradePainter}, {@code AE2ListRowPainter}, {@link AE2Legacy} and {@link AE2Draw}; {@code PlainLook} holds
 * the look outside AE2 themes. The drawing methods still declared here forward to those classes and are deprecated.
 * <p>
 * The class loads on both sides: constants, {@link AE2Variant} lookups and drawable factories may be used while a GUI
 * is built on the server, as ModularUI 2 builds GUIs opened by the server on both sides. Drawing, measuring text and
 * theme registration run on the client only.
 *
 * @see AE2Variant
 * @see AE2Look
 */
public class AE2Theme {

    /** Light panel theme identifier. */
    public static final String LIGHT_ID = "gtnllib:ae2_light";
    /** Light context-menu theme identifier. */
    public static final String LIGHT_MENU_ID = "gtnllib:ae2_light_menu";
    /** Dark panel theme identifier. */
    public static final String DARK_ID = "gtnllib:ae2_dark";
    /** Dark context-menu theme identifier. */
    public static final String DARK_MENU_ID = "gtnllib:ae2_dark_menu";
    /** Dark panel theme identifier with purple selection highlights. */
    public static final String DARK_PURPLE_ID = "gtnllib:ae2_dark_purple";
    /** Dark context-menu theme identifier with purple selection highlights. */
    public static final String DARK_PURPLE_MENU_ID = "gtnllib:ae2_dark_purple_menu";
    /** MUI2 context-menu theme used outside AE2 panels. */
    public static final String CONTEXT_MENU_ID = "modularui.context_menu";
    /** ARGB light-blue selection fill, top edge and shadow of the light and dark variants. */
    public static final int HIGHLIGHT = 0xFF9CD3FF, HIGHLIGHT_TOP = 0xFFDAFFFF, HIGHLIGHT_SHADOW = 0xFF708CBA,
        FIELD_SELECTION = 0xFF0000FF;
    /** ARGB fluix-purple selection fill, top edge and shadow of the purple dark variant. */
    public static final int PURPLE_HIGHLIGHT = 0xFF644595, PURPLE_HIGHLIGHT_TOP = 0xFFE2A3E3,
        PURPLE_HIGHLIGHT_SHADOW = 0xFF212040;
    /** ARGB text color of a text field that cannot be edited, as Minecraft draws uneditable fields. */
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
     * Size and label offsets of AE2's check box under their older names.
     *
     * @deprecated use {@link #CHECKBOX_WIDTH}, {@link #CHECKBOX_HEIGHT}, {@link #CHECKBOX_LABEL_X} and
     *             {@link #CHECKBOX_LABEL_Y}; the on/off box is called a check box everywhere, after AE2's
     *             {@code AECheckbox}
     */
    @Deprecated
    public static final int SWITCH_WIDTH = CHECKBOX_WIDTH, SWITCH_HEIGHT = CHECKBOX_HEIGHT,
        SWITCH_LABEL_X = CHECKBOX_LABEL_X, SWITCH_LABEL_Y = CHECKBOX_LABEL_Y;
    /**
     * Width of the small scroll-bar handle.
     *
     * @deprecated use {@link #SMALL_SCROLLER_WIDTH}; this name reads like the width of a whole scroll bar
     */
    @Deprecated
    public static final int SCROLLBAR_WIDTH = SMALL_SCROLLER_WIDTH;
    /**
     * Upgrade slots in a full column.
     *
     * @deprecated use {@link #UPGRADE_SLOTS_PER_COLUMN}; the slots run down a column, not across a row
     */
    @Deprecated
    public static final int UPGRADE_ROWS = UPGRADE_SLOTS_PER_COLUMN;
    /**
     * AE2 page tabs: size, distance between tab tops (neighbours share an outline row) and how far a tab starts inside
     * the panel's right edge, which its left columns cover so the selected tab joins the panel.
     */
    public static final int PAGE_TAB_SIZE = 22, PAGE_TAB_PITCH = 21, PAGE_TAB_INSET = 3;
    /** How far an upgrade panel starts inside the right edge of the panel it belongs to, as AE2 places it. */
    public static final int UPGRADE_OVERLAP = 2;
    /** Size of the dropdown triangle drawn by {@link AE2PanelPainter#drawCaret}. */
    public static final int CARET_WIDTH = 5, CARET_HEIGHT = 3;
    /**
     * Size of AE2's square sprite sheets, the 18-pixel item slot, the space kept left and right of a button label and
     * the height of one line of label text, in GUI pixels.
     */
    public static final int SHEET = 256, SLOT_SIZE = 18, LABEL_PADDING = 2, LABEL_HEIGHT = 8;
    /** ARGB text color of disabled buttons and unavailable text outside the AE2 themes, MUI2's disabled grey. */
    public static final int DISABLED_TEXT = 0xFFA0A0A0;
    /** Alpha of sprites and icons drawn for something that cannot be used. */
    public static final float INACTIVE_ALPHA = 0.5F;
    /** Folder names of the three AE2 skins below {@code textures/gui/skin/}. */
    public static final String LIGHT_FOLDER = "ae2_light", DARK_FOLDER = "ae2_dark", PURPLE_FOLDER = "ae2_dark_purple";
    /** Textures the purple dark skin recolours; it takes the others from {@link #DARK_FOLDER}. */
    public static final Set<String> PURPLE_TEXTURES = Set
        .of("button_highlighted", "checkbox", "legacy_states", "list_row", "slider_fill", "slider_tag", "states");
    private static final String ROOT = "textures/gui/skin/";
    /** Shared sizing and spacing used by all three AE2 skins. */
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
     * Registers fresh definitions for the three panel themes and their menu themes.
     * <p>
     * GTNLLib installs the themes during client initialization and invokes this method on theme reload. Consumers
     * select a theme identifier without registering these definitions themselves.
     */
    @SideOnly(Side.CLIENT)
    public static void register() {
        for (AE2Variant variant : AE2Variant.values()) {
            replace(variant.id, AE2ThemeDefinitions.panelTheme(variant));
            replace(variant.menuId, AE2ThemeDefinitions.menuTheme(variant));
        }
    }

    private static void replace(String id, ThemeBuilder<?> theme) {
        // ThemeManager writes "background": "none" into the registered json on every reload, so register a fresh copy.
        IThemeApi.get()
            .getJavaDefaultThemes(id)
            .clear();
        IThemeApi.get()
            .registerTheme(theme);
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
        for (AE2Variant variant : Variants.ALL)
            if (variant.id.equals(themeId) || variant.menuId.equals(themeId)) return variant;
        return null;
    }

    // Widgets in plain panels look this up every frame; a holder keeps the array from being copied each time and is
    // only read once the skins exist, since the skins themselves load this class.
    private static class Variants {

        private static final AE2Variant[] ALL = AE2Variant.values();
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
     * @param context current drawing context
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
     * Applies an AE2 handle texture to sliders in a widget tree.
     * <p>
     * Only children already present in the tree are visited. A theme that is not an AE2 skin leaves handles unchanged.
     *
     * @param root    root widget to visit, including its descendants
     * @param themeId panel or menu theme identifier, possibly {@code null}
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
     * Same as {@link AE2Legacy#legacyStates}.
     *
     * @param variant recolored variant, or {@code null} for the resource supplied by AE2
     * @return state sheet resource location
     * @deprecated use {@link AE2Legacy#legacyStates}
     */
    @Deprecated
    public static ResourceLocation legacyStates(@Nullable AE2Variant variant) {
        return AE2Legacy.legacyStates(AE2Look.of(variant));
    }

    /**
     * Same as {@link AE2Legacy#legacyTerminal}.
     *
     * @param variant recolored variant, or {@code null} for the resource supplied by AE2
     * @return terminal sheet resource location
     * @deprecated use {@link AE2Legacy#legacyTerminal}
     */
    @Deprecated
    public static ResourceLocation legacyTerminal(@Nullable AE2Variant variant) {
        return AE2Legacy.legacyTerminal(AE2Look.of(variant));
    }

    /**
     * Same as {@link AE2Look#scrollbarWidth}.
     *
     * @param variant AE2 variant, or {@code null} for the vanilla creative-inventory style
     * @return 12 pixels for AE2 or 14 pixels for vanilla
     * @deprecated use {@link AE2Look#scrollbarWidth} of the panel's look
     */
    @Deprecated
    public static int scrollbarWidth(@Nullable AE2Variant variant) {
        return AE2Look.of(variant)
            .scrollbarWidth();
    }

    /**
     * Same as {@link AE2Look#drawScrollbar}.
     *
     * @param variant AE2 variant, or {@code null} for the vanilla creative-inventory style
     * @param x       left edge of the track
     * @param y       top edge of the track, including its one-pixel border
     * @param height  total track height, at least {@code SCROLLER_HEIGHT + 2}
     * @param handleY handle offset from the inner track top, from zero to {@code height - SCROLLER_HEIGHT - 2}
     * @param enabled whether to draw the enabled handle
     * @deprecated use {@link AE2Look#drawScrollbar} of the panel's look
     */
    @Deprecated
    public static void drawScrollbar(@Nullable AE2Variant variant, int x, int y, int height, int handleY,
        boolean enabled) {
        AE2Look.of(variant)
            .drawScrollbar(x, y, height, handleY, enabled);
    }

    /**
     * Same as {@link AE2ScrollbarPainter#drawSmallScrollbar}.
     *
     * @param variant non-null AE2 variant
     * @param x       left edge of the handle; the groove starts one pixel to its right
     * @param y       top edge of the groove, including its one-pixel border
     * @param height  total groove height, at least {@code SCROLLER_HEIGHT + 2}
     * @param handleY handle offset from the inner groove top, from zero to {@code height - SCROLLER_HEIGHT - 2}
     * @param enabled whether to draw the enabled handle
     * @deprecated use {@link AE2ScrollbarPainter#drawSmallScrollbar}
     */
    @Deprecated
    public static void drawSmallScrollbar(AE2Variant variant, int x, int y, int height, int handleY, boolean enabled) {
        AE2ScrollbarPainter.drawSmallScrollbar(variant, x, y, height, handleY, enabled);
    }

    /**
     * Same as {@link AE2PanelPainter#drawButtonBar}.
     *
     * @param variant non-null AE2 variant
     * @param x       left edge of the background
     * @param y       top edge of the background
     * @param width   background width, including button margins
     * @param height  background height, including button margins and spacing
     * @deprecated use {@link AE2PanelPainter#drawButtonBar}
     */
    @Deprecated
    public static void drawButtonBar(AE2Variant variant, int x, int y, int width, int height) {
        AE2PanelPainter.drawButtonBar(variant, x, y, width, height);
    }

    /**
     * Same as {@link AE2UpgradePainter#upgradePanelWidth}.
     *
     * @param slots nonnegative slot count; zero still reserves one column
     * @return panel width in pixels, 23 for one column
     * @deprecated use {@link AE2UpgradePainter#upgradePanelWidth}
     */
    @Deprecated
    public static int upgradePanelWidth(int slots) {
        return AE2UpgradePainter.upgradePanelWidth(slots);
    }

    /**
     * Same as {@link AE2UpgradePainter#upgradePanelHeight}.
     *
     * @param slots nonnegative slot count; zero still reserves one row
     * @return panel height in pixels
     * @deprecated use {@link AE2UpgradePainter#upgradePanelHeight}
     */
    @Deprecated
    public static int upgradePanelHeight(int slots) {
        return AE2UpgradePainter.upgradePanelHeight(slots);
    }

    /**
     * Same as {@link AE2UpgradePainter#drawUpgradePanel}.
     *
     * @param variant non-null AE2 variant
     * @param x       panel left edge
     * @param y       panel top edge
     * @param slots   nonnegative number of slots to draw
     * @deprecated use {@link AE2UpgradePainter#drawUpgradePanel}
     */
    @Deprecated
    public static void drawUpgradePanel(AE2Variant variant, int x, int y, int slots) {
        AE2UpgradePainter.drawUpgradePanel(variant, x, y, slots);
    }

    /**
     * Same as {@link AE2PanelPainter#tableSize}.
     *
     * @param cells    positive number of cells along the axis
     * @param cellSize positive inner size of each cell in pixels
     * @return total dimension in pixels
     * @deprecated use {@link AE2PanelPainter#tableSize}
     */
    @Deprecated
    public static int tableSize(int cells, int cellSize) {
        return AE2PanelPainter.tableSize(cells, cellSize);
    }

    /**
     * Same as {@link AE2PanelPainter#drawTable}.
     *
     * @param variant    non-null AE2 variant
     * @param x          table left edge
     * @param y          table top edge
     * @param columns    positive column count
     * @param rows       positive row count
     * @param cellWidth  positive inner cell width
     * @param cellHeight positive inner cell height
     * @deprecated use {@link AE2PanelPainter#drawTable}
     */
    @Deprecated
    public static void drawTable(AE2Variant variant, int x, int y, int columns, int rows, int cellWidth,
        int cellHeight) {
        AE2PanelPainter.drawTable(variant, x, y, columns, rows, cellWidth, cellHeight);
    }

    /**
     * Creates a selectable list-row background in the look of the whole screen.
     *
     * @param selected selection supplier; a hover supplier can be used for hover highlighting
     * @return the row background drawable
     * @deprecated use {@link AE2PanelPainter#listRow}, which follows the theme of the row's own panel
     */
    @Deprecated
    public static IDrawable listRow(BooleanSupplier selected) {
        return (context, x, y, width, height, theme) -> AE2PanelPainter
            .drawListRow(AE2Look.of(context), selected.getAsBoolean(), x, y, width, height);
    }

    /**
     * Same as {@link AE2CheckboxPainter#radio}.
     *
     * @param widget    widget supplying hover state
     * @param selected  supplier of the selected state
     * @param label     supplier of non-null display text
     * @param available supplier of the visual enabled state
     * @return the indicator and label drawable
     * @deprecated use {@link AE2CheckboxPainter#radio}
     */
    @Deprecated
    public static IDrawable radio(Widget<?> widget, BooleanSupplier selected, Supplier<String> label,
        BooleanSupplier available) {
        return AE2CheckboxPainter.radio(widget, selected, label, available);
    }

    /**
     * Same as {@link AE2CheckboxPainter#radioWidth}.
     *
     * @param label display text
     * @return required width in pixels
     * @deprecated use {@link AE2CheckboxPainter#radioWidth}
     */
    @Deprecated
    public static int radioWidth(String label) {
        return AE2CheckboxPainter.radioWidth(label);
    }

    /**
     * Same as {@link AE2Legacy#drawLegacyState}.
     *
     * @param variant recolored variant, or {@code null} for the sheet supplied by AE2
     * @param index   zero-based cell index, from zero through 255
     * @param x       destination left edge
     * @param y       destination top edge
     * @param size    destination width and height in pixels
     * @deprecated use {@link AE2Legacy#drawLegacyState}
     */
    @Deprecated
    public static void drawLegacyState(@Nullable AE2Variant variant, int index, float x, float y, float size) {
        AE2Legacy.drawLegacyState(AE2Look.of(variant), index, x, y, size);
    }

    /**
     * Same as {@link AE2ButtonPainter#button}.
     *
     * @param widget    widget supplying hover state
     * @param available supplier of the visual enabled state; input handling remains the caller's responsibility
     * @param selected  supplier of the selected state, which uses the highlighted background
     * @return background drawable suitable for both normal and hover backgrounds
     * @deprecated use {@link AE2ButtonPainter#button}
     */
    @Deprecated
    public static IDrawable button(Widget<?> widget, BooleanSupplier available, BooleanSupplier selected) {
        return AE2ButtonPainter.button(widget, available, selected);
    }

    /**
     * Resolves the text style for a button in the look of the whole screen.
     *
     * @param context current drawing context
     * @param state   visual button state
     * @param height  button height in pixels
     * @return text color, vertical offset and shadow setting
     * @deprecated use {@link AE2Look#buttonText} of the look of the button's own panel
     */
    @Deprecated
    public static ButtonText buttonText(GuiContext context, State state, int height) {
        return AE2Look.of(context)
            .buttonText(state, height);
    }

    /**
     * Same as {@link AE2ButtonPainter#label}.
     *
     * @param widget    widget supplying hover state
     * @param text      supplier of non-null display text
     * @param available supplier of the visual enabled state
     * @param selected  supplier of the selected state
     * @return label drawable suitable for a button overlay
     * @deprecated use {@link AE2ButtonPainter#label}
     */
    @Deprecated
    public static IDrawable label(Widget<?> widget, Supplier<String> text, BooleanSupplier available,
        BooleanSupplier selected) {
        return AE2ButtonPainter.label(widget, text, available, selected);
    }

    /**
     * Same as {@link AE2ButtonPainter#iconButton}.
     *
     * @param widget    widget supplying hover state
     * @param icon      supplier of the displayed icon
     * @param available supplier of the visual enabled state
     * @return combined background and icon drawable; the standard size is 18 by 20 pixels
     * @deprecated use {@link AE2ButtonPainter#iconButton}
     */
    @Deprecated
    public static IDrawable iconButton(Widget<?> widget, Supplier<? extends IconSource> icon,
        BooleanSupplier available) {
        return AE2ButtonPainter.iconButton(widget, icon, available);
    }

    /**
     * Same as {@link AE2ButtonPainter#tabButton}.
     *
     * @param widget    widget supplying hover state
     * @param icon      supplier of the displayed icon
     * @param available supplier of the visual enabled state
     * @return combined tab background and icon drawable
     * @deprecated use {@link AE2ButtonPainter#tabButton}
     */
    @Deprecated
    public static IDrawable tabButton(Widget<?> widget, Supplier<? extends IconSource> icon,
        BooleanSupplier available) {
        return AE2ButtonPainter.tabButton(widget, icon, available);
    }

    /**
     * Same as {@link AE2CheckboxPainter#checkbox}.
     *
     * @param widget    widget supplying hover state
     * @param on        supplier of the switch value
     * @param label     supplier of non-null display text
     * @param available supplier of the visual enabled state
     * @return combined switch and label drawable
     * @deprecated use {@link AE2CheckboxPainter#checkbox}
     */
    @Deprecated
    public static IDrawable toggleSwitch(Widget<?> widget, BooleanSupplier on, Supplier<String> label,
        BooleanSupplier available) {
        return AE2CheckboxPainter.checkbox(widget, on, label, available);
    }

    /**
     * Same as {@link AE2CheckboxPainter#checkboxWidth}.
     *
     * @param label display text
     * @return required width in pixels
     * @deprecated use {@link AE2CheckboxPainter#checkboxWidth}
     */
    @Deprecated
    public static int switchWidth(String label) {
        return AE2CheckboxPainter.checkboxWidth(label);
    }

    /**
     * Same as {@link AE2Draw#text}.
     *
     * @param text   display text
     * @param x      text left edge
     * @param y      text top edge
     * @param color  ARGB text color
     * @param shadow whether to draw a drop shadow
     * @deprecated use {@link AE2Draw#text}
     */
    @Deprecated
    public static void text(String text, int x, int y, int color, boolean shadow) {
        AE2Draw.text(text, x, y, color, shadow);
    }

    /** Client event listener that reinstalls the theme definitions before a MUI2 theme reload. */
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

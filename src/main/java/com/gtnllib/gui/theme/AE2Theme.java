package com.gtnllib.gui.theme;

import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.ReloadThemeEvent;
import com.cleanroommc.modularui.theme.ThemeBuilder;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.gtnllib.GTNLLib;
import com.gtnllib.gui.theme.ae2.AE2ButtonDrawable;
import com.gtnllib.gui.theme.ae2.AE2ButtonDrawable.State;
import com.gtnllib.gui.theme.ae2.AE2Buttons;
import com.gtnllib.gui.theme.ae2.AE2Checkboxes;
import com.gtnllib.gui.theme.ae2.AE2Draw;
import com.gtnllib.gui.theme.ae2.AE2Icon;
import com.gtnllib.gui.theme.ae2.AE2Legacy;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.AE2Panels;
import com.gtnllib.gui.theme.ae2.AE2Scrollbars;
import com.gtnllib.gui.theme.ae2.AE2Upgrades;
import com.gtnllib.gui.theme.ae2.AE2Variant;
import com.gtnllib.gui.theme.ae2.ButtonText;
import com.gtnllib.gui.theme.ae2.IconSource;
import com.gtnllib.gui.widget.HandleScrollData;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The AE2 light, dark and purple dark themes for ModularUI 2: their identifiers, AE2's widget dimensions, the three
 * skins with their colors and textures ({@link AE2Variant}), the built-in toolbar icons ({@link AE2Icon}) and the theme
 * registration.
 * <p>
 * Select a panel theme through {@link ScreenTheme}. Panel backgrounds, fields, slots, switches and button styles
 * are supplied by the registered theme. Use {@link #useMenuTheme(String, String, String, String...)} to map child
 * menus and {@link #sliderHandles(IWidget, String)} to style slider handles.
 * <p>
 * The drawing helpers live in the {@code ae2} package, one class per part: {@link AE2Buttons}, {@link AE2Checkboxes},
 * {@link AE2Scrollbars}, {@link AE2Panels}, {@link AE2Upgrades}, {@link AE2Legacy} and {@link AE2Draw}. Widgets ask
 * {@link AE2Look#of(IWidget)} for the skin of the panel they are in. The drawing methods still declared here forward
 * to those classes and are deprecated.
 * <p>
 * The class loads on both sides: constants, {@link AE2Variant} lookups and drawable factories may be used while a GUI
 * is
 * built on the server, as ModularUI 2 builds GUIs opened by the server on both sides. Drawing, measuring text and
 * theme registration run on the client only.
 *
 * @see AE2Variant
 * @see HandleScrollData
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
     */
    public static final int BUTTON_HEIGHT = 20, TEXT_FIELD_HEIGHT = 12, ICON_SIZE = 16, ICON_BUTTON_WIDTH = 18,
        ICON_BUTTON_HEIGHT = 20, TAB_BUTTON_SIZE = 20, SWITCH_WIDTH = 22, SWITCH_HEIGHT = 12, SWITCH_LABEL_X = 26,
        SWITCH_LABEL_Y = 4, SMALL_SCROLLER_WIDTH = 7, INSET_TOP = 3, INSET_EDGE = 1, BIG_SCROLLER_WIDTH = 12,
        SCROLLER_HEIGHT = 15, RADIO_SIZE = 14, RADIO_LABEL_X = 16, RADIO_LABEL_Y = 4, LIST_ROW_HEIGHT = 22,
        PROGRESS_WIDTH = 6, PROGRESS_HEIGHT = 18, BUTTON_BAR_MARGIN = 2, BUTTON_BAR_SPACING = 6, UPGRADE_PADDING = 5,
        UPGRADE_SLOTS_PER_COLUMN = 8, VALIDATION_ICON_SIZE = 8, PANEL_EDGE = 2, PANEL_BOTTOM_EDGE = 4;
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
    /** Size of the dropdown triangle drawn by {@link AE2Panels#drawCaret}. */
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
            replace(variant.id, variant.panelTheme());
            replace(variant.menuId, variant.menuTheme());
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
        for (AE2Variant variant : AE2Variant.values())
            if (variant.id.equals(themeId) || variant.menuId.equals(themeId)) return variant;
        return null;
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
        AE2Variant variant = variant(screenTheme);
        String menuTheme = variant == null ? CONTEXT_MENU_ID : variant.menuId;
        for (String menu : menus) IThemeApi.get()
            .registerThemeForScreen(owner + ":" + screen + ":" + menu, menuTheme);
    }

    /**
     * Applies an AE2 handle texture to sliders in a widget tree.
     * <p>
     * Only children already present in the tree are visited. An unrecognized theme identifier leaves handles
     * unchanged.
     *
     * @param root    root widget to visit, including its descendants
     * @param themeId panel or menu theme identifier, possibly {@code null}
     */
    public static void sliderHandles(IWidget root, @Nullable String themeId) {
        AE2Variant variant = variant(themeId);
        if (variant != null) sliderHandles(root, new AE2ButtonDrawable(State.NORMAL, variant.folder));
    }

    private static void sliderHandles(IWidget widget, IDrawable handle) {
        if (widget instanceof SliderWidget slider) slider.sliderTexture(handle);
        for (IWidget child : widget.getChildren()) sliderHandles(child, handle);
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
        return AE2Legacy.legacyStates(variant);
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
        return AE2Legacy.legacyTerminal(variant);
    }

    /**
     * Same as {@link AE2Scrollbars#scrollbarWidth}.
     *
     * @param variant AE2 variant, or {@code null} for the vanilla creative-inventory style
     * @return 12 pixels for AE2 or 14 pixels for vanilla
     * @deprecated use {@link AE2Scrollbars#scrollbarWidth}
     */
    @Deprecated
    public static int scrollbarWidth(@Nullable AE2Variant variant) {
        return AE2Scrollbars.scrollbarWidth(variant);
    }

    /**
     * Same as {@link AE2Scrollbars#drawScrollbar}.
     *
     * @param variant AE2 variant, or {@code null} for the vanilla creative-inventory style
     * @param x       left edge of the track
     * @param y       top edge of the track, including its one-pixel border
     * @param height  total track height, at least {@code SCROLLER_HEIGHT + 2}
     * @param handleY handle offset from the inner track top, from zero to {@code height - SCROLLER_HEIGHT - 2}
     * @param enabled whether to draw the enabled handle
     * @deprecated use {@link AE2Scrollbars#drawScrollbar}
     */
    @Deprecated
    public static void drawScrollbar(@Nullable AE2Variant variant, int x, int y, int height, int handleY,
        boolean enabled) {
        AE2Scrollbars.drawScrollbar(variant, x, y, height, handleY, enabled);
    }

    /**
     * Same as {@link AE2Scrollbars#drawSmallScrollbar}.
     *
     * @param variant non-null AE2 variant
     * @param x       left edge of the handle; the groove starts one pixel to its right
     * @param y       top edge of the groove, including its one-pixel border
     * @param height  total groove height, at least {@code SCROLLER_HEIGHT + 2}
     * @param handleY handle offset from the inner groove top, from zero to {@code height - SCROLLER_HEIGHT - 2}
     * @param enabled whether to draw the enabled handle
     * @deprecated use {@link AE2Scrollbars#drawSmallScrollbar}
     */
    @Deprecated
    public static void drawSmallScrollbar(AE2Variant variant, int x, int y, int height, int handleY, boolean enabled) {
        AE2Scrollbars.drawSmallScrollbar(variant, x, y, height, handleY, enabled);
    }

    /**
     * Same as {@link AE2Panels#drawButtonBar}.
     *
     * @param variant non-null AE2 variant
     * @param x       left edge of the background
     * @param y       top edge of the background
     * @param width   background width, including button margins
     * @param height  background height, including button margins and spacing
     * @deprecated use {@link AE2Panels#drawButtonBar}
     */
    @Deprecated
    public static void drawButtonBar(AE2Variant variant, int x, int y, int width, int height) {
        AE2Panels.drawButtonBar(variant, x, y, width, height);
    }

    /**
     * Same as {@link AE2Upgrades#upgradePanelWidth}.
     *
     * @param slots nonnegative slot count; zero still reserves one column
     * @return panel width in pixels, 23 for one column
     * @deprecated use {@link AE2Upgrades#upgradePanelWidth}
     */
    @Deprecated
    public static int upgradePanelWidth(int slots) {
        return AE2Upgrades.upgradePanelWidth(slots);
    }

    /**
     * Same as {@link AE2Upgrades#upgradePanelHeight}.
     *
     * @param slots nonnegative slot count; zero still reserves one row
     * @return panel height in pixels
     * @deprecated use {@link AE2Upgrades#upgradePanelHeight}
     */
    @Deprecated
    public static int upgradePanelHeight(int slots) {
        return AE2Upgrades.upgradePanelHeight(slots);
    }

    /**
     * Same as {@link AE2Upgrades#drawUpgradePanel}.
     *
     * @param variant non-null AE2 variant
     * @param x       panel left edge
     * @param y       panel top edge
     * @param slots   nonnegative number of slots to draw
     * @deprecated use {@link AE2Upgrades#drawUpgradePanel}
     */
    @Deprecated
    public static void drawUpgradePanel(AE2Variant variant, int x, int y, int slots) {
        AE2Upgrades.drawUpgradePanel(variant, x, y, slots);
    }

    /**
     * Same as {@link AE2Panels#tableSize}.
     *
     * @param cells    positive number of cells along the axis
     * @param cellSize positive inner size of each cell in pixels
     * @return total dimension in pixels
     * @deprecated use {@link AE2Panels#tableSize}
     */
    @Deprecated
    public static int tableSize(int cells, int cellSize) {
        return AE2Panels.tableSize(cells, cellSize);
    }

    /**
     * Same as {@link AE2Panels#drawTable}.
     *
     * @param variant    non-null AE2 variant
     * @param x          table left edge
     * @param y          table top edge
     * @param columns    positive column count
     * @param rows       positive row count
     * @param cellWidth  positive inner cell width
     * @param cellHeight positive inner cell height
     * @deprecated use {@link AE2Panels#drawTable}
     */
    @Deprecated
    public static void drawTable(AE2Variant variant, int x, int y, int columns, int rows, int cellWidth,
        int cellHeight) {
        AE2Panels.drawTable(variant, x, y, columns, rows, cellWidth, cellHeight);
    }

    /**
     * Same as {@link AE2Panels#listRow}.
     *
     * @param selected selection supplier; a hover supplier can be used for hover highlighting
     * @return the row background drawable
     * @deprecated use {@link AE2Panels#listRow}
     */
    @Deprecated
    public static IDrawable listRow(BooleanSupplier selected) {
        return AE2Panels.listRow(selected);
    }

    /**
     * Same as {@link AE2Checkboxes#radio}.
     *
     * @param widget    widget supplying hover state
     * @param selected  supplier of the selected state
     * @param label     supplier of non-null display text
     * @param available supplier of the visual enabled state
     * @return the indicator and label drawable
     * @deprecated use {@link AE2Checkboxes#radio}
     */
    @Deprecated
    public static IDrawable radio(Widget<?> widget, BooleanSupplier selected, Supplier<String> label,
        BooleanSupplier available) {
        return AE2Checkboxes.radio(widget, selected, label, available);
    }

    /**
     * Same as {@link AE2Checkboxes#radioWidth}.
     *
     * @param label display text
     * @return required width in pixels
     * @deprecated use {@link AE2Checkboxes#radioWidth}
     */
    @Deprecated
    public static int radioWidth(String label) {
        return AE2Checkboxes.radioWidth(label);
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
        AE2Legacy.drawLegacyState(variant, index, x, y, size);
    }

    /**
     * Same as {@link AE2Buttons#button}.
     *
     * @param widget    widget supplying hover state
     * @param available supplier of the visual enabled state; input handling remains the caller's responsibility
     * @param selected  supplier of the selected state, which uses the highlighted background
     * @return background drawable suitable for both normal and hover backgrounds
     * @deprecated use {@link AE2Buttons#button}
     */
    @Deprecated
    public static IDrawable button(Widget<?> widget, BooleanSupplier available, BooleanSupplier selected) {
        return AE2Buttons.button(widget, available, selected);
    }

    /**
     * Same as {@link AE2Buttons#buttonText}.
     *
     * @param context current drawing context
     * @param state   visual button state
     * @param height  button height in pixels
     * @return text color, vertical offset and shadow setting
     * @deprecated use {@link AE2Buttons#buttonText}
     */
    @Deprecated
    public static ButtonText buttonText(GuiContext context, State state, int height) {
        return AE2Buttons.buttonText(context, state, height);
    }

    /**
     * Same as {@link AE2Buttons#label}.
     *
     * @param widget    widget supplying hover state
     * @param text      supplier of non-null display text
     * @param available supplier of the visual enabled state
     * @param selected  supplier of the selected state
     * @return label drawable suitable for a button overlay
     * @deprecated use {@link AE2Buttons#label}
     */
    @Deprecated
    public static IDrawable label(Widget<?> widget, Supplier<String> text, BooleanSupplier available,
        BooleanSupplier selected) {
        return AE2Buttons.label(widget, text, available, selected);
    }

    /**
     * Same as {@link AE2Buttons#iconButton}.
     *
     * @param widget    widget supplying hover state
     * @param icon      supplier of the displayed icon
     * @param available supplier of the visual enabled state
     * @return combined background and icon drawable; the standard size is 18 by 20 pixels
     * @deprecated use {@link AE2Buttons#iconButton}
     */
    @Deprecated
    public static IDrawable iconButton(Widget<?> widget, Supplier<? extends IconSource> icon,
        BooleanSupplier available) {
        return AE2Buttons.iconButton(widget, icon, available);
    }

    /**
     * Same as {@link AE2Buttons#tabButton}.
     *
     * @param widget    widget supplying hover state
     * @param icon      supplier of the displayed icon
     * @param available supplier of the visual enabled state
     * @return combined tab background and icon drawable
     * @deprecated use {@link AE2Buttons#tabButton}
     */
    @Deprecated
    public static IDrawable tabButton(Widget<?> widget, Supplier<? extends IconSource> icon,
        BooleanSupplier available) {
        return AE2Buttons.tabButton(widget, icon, available);
    }

    /**
     * Same as {@link AE2Checkboxes#checkbox}.
     *
     * @param widget    widget supplying hover state
     * @param on        supplier of the switch value
     * @param label     supplier of non-null display text
     * @param available supplier of the visual enabled state
     * @return combined switch and label drawable
     * @deprecated use {@link AE2Checkboxes#checkbox}
     */
    @Deprecated
    public static IDrawable toggleSwitch(Widget<?> widget, BooleanSupplier on, Supplier<String> label,
        BooleanSupplier available) {
        return AE2Checkboxes.checkbox(widget, on, label, available);
    }

    /**
     * Same as {@link AE2Checkboxes#checkboxWidth}.
     *
     * @param label display text
     * @return required width in pixels
     * @deprecated use {@link AE2Checkboxes#checkboxWidth}
     */
    @Deprecated
    public static int switchWidth(String label) {
        return AE2Checkboxes.checkboxWidth(label);
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

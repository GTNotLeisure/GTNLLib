package com.gtnllib.gui.theme.ae2;

import static com.gtnllib.gui.theme.AE2Theme.BIG_SCROLLER_WIDTH;
import static com.gtnllib.gui.theme.AE2Theme.DARK_FOLDER;
import static com.gtnllib.gui.theme.AE2Theme.DARK_ID;
import static com.gtnllib.gui.theme.AE2Theme.DARK_MENU_ID;
import static com.gtnllib.gui.theme.AE2Theme.DARK_PURPLE_ID;
import static com.gtnllib.gui.theme.AE2Theme.DARK_PURPLE_MENU_ID;
import static com.gtnllib.gui.theme.AE2Theme.FIELD_SELECTION;
import static com.gtnllib.gui.theme.AE2Theme.HIGHLIGHT;
import static com.gtnllib.gui.theme.AE2Theme.HIGHLIGHT_SHADOW;
import static com.gtnllib.gui.theme.AE2Theme.HIGHLIGHT_TOP;
import static com.gtnllib.gui.theme.AE2Theme.ICON_BUTTON_HEIGHT;
import static com.gtnllib.gui.theme.AE2Theme.ICON_BUTTON_WIDTH;
import static com.gtnllib.gui.theme.AE2Theme.ICON_SIZE;
import static com.gtnllib.gui.theme.AE2Theme.INSET_EDGE;
import static com.gtnllib.gui.theme.AE2Theme.INSET_TOP;
import static com.gtnllib.gui.theme.AE2Theme.LIGHT_FOLDER;
import static com.gtnllib.gui.theme.AE2Theme.LIGHT_ID;
import static com.gtnllib.gui.theme.AE2Theme.LIGHT_MENU_ID;
import static com.gtnllib.gui.theme.AE2Theme.LIST_ROW_HEIGHT;
import static com.gtnllib.gui.theme.AE2Theme.PAGE_TAB_SIZE;
import static com.gtnllib.gui.theme.AE2Theme.PANEL_BOTTOM_EDGE;
import static com.gtnllib.gui.theme.AE2Theme.PANEL_EDGE;
import static com.gtnllib.gui.theme.AE2Theme.PROGRESS_HEIGHT;
import static com.gtnllib.gui.theme.AE2Theme.PROGRESS_WIDTH;
import static com.gtnllib.gui.theme.AE2Theme.PURPLE_FOLDER;
import static com.gtnllib.gui.theme.AE2Theme.PURPLE_HIGHLIGHT;
import static com.gtnllib.gui.theme.AE2Theme.PURPLE_HIGHLIGHT_SHADOW;
import static com.gtnllib.gui.theme.AE2Theme.PURPLE_HIGHLIGHT_TOP;
import static com.gtnllib.gui.theme.AE2Theme.RADIO_SIZE;
import static com.gtnllib.gui.theme.AE2Theme.SCROLLER_HEIGHT;
import static com.gtnllib.gui.theme.AE2Theme.SHEET;
import static com.gtnllib.gui.theme.AE2Theme.SLOT_SIZE;
import static com.gtnllib.gui.theme.AE2Theme.SMALL_SCROLLER_WIDTH;
import static com.gtnllib.gui.theme.AE2Theme.STYLE;
import static com.gtnllib.gui.theme.AE2Theme.SWITCH_HEIGHT;
import static com.gtnllib.gui.theme.AE2Theme.SWITCH_LABEL_X;
import static com.gtnllib.gui.theme.AE2Theme.SWITCH_WIDTH;
import static com.gtnllib.gui.theme.AE2Theme.TAB_BUTTON_SIZE;
import static com.gtnllib.gui.theme.AE2Theme.TEXT_FIELD_HEIGHT;
import static com.gtnllib.gui.theme.AE2Theme.texture;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.theme.SelectableTheme;
import com.cleanroommc.modularui.theme.ThemeAPI;
import com.cleanroommc.modularui.theme.ThemeBuilder;
import com.cleanroommc.modularui.utils.JsonBuilder;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ButtonPurpose;
import com.gtnllib.gui.theme.LayoutTheme;
import com.gtnllib.gui.theme.PaletteThemes;
import com.gtnllib.gui.theme.SkinDrawable;
import com.gtnllib.gui.theme.ThemePalette;
import com.gtnllib.gui.theme.ThemeSkin;
import com.gtnllib.gui.theme.ThemeSkin.Part;
import com.gtnllib.gui.theme.ae2.AE2ButtonDrawable.State;
import com.gtnllib.gui.wheel.WheelStyle;
import com.gtnllib.gui.widget.SlideSwitch;

/**
 * AE2 panel and menu variants with their palettes and textures.
 * <p>
 * Colors and resource names are shared by the registered themes and direct drawing helpers. Raw textures do
 * not select a variant from the drawing context. Each variant's selection colors match the selected sprites of
 * its skin.
 */
public enum AE2Variant {

    /** Light AE2 palette. */
    LIGHT(LIGHT_ID, LIGHT_MENU_ID, LIGHT_FOLDER, false, AE2Colors.builder()
        .text(0xFF413F54)
        .muted(0xFF878FA5)
        .secondary(0xFF4A5068)
        .buttonText(0xFFF2F2F2)
        .hoverText(0xFF517497)
        .disabledText(0xFF413F54)
        .fieldText(0xFFF2F2F2)
        .placeholder(0xFFDEDFE3)
        .line(0xFF696D88)
        .edge(0xFFF2F2F2)
        .panel(0xFFCBCCD4)
        .well(0xFF9A9FB4)
        .outline(0xFF413F54)
        .raised(0xFFADB0C4)
        .good(0xFF1C6B34)
        .bad(0xFF8F2B23)
        .info(0xFF144673)
        .warning(0xFF7A4A00)
        .error(0xFFFF1900)
        .build(), HIGHLIGHT, HIGHLIGHT_TOP, HIGHLIGHT_SHADOW),
    /** Dark AE2 palette. */
    DARK(DARK_ID, DARK_MENU_ID, DARK_FOLDER, true, darkColors(0xFF517497), HIGHLIGHT, HIGHLIGHT_TOP, HIGHLIGHT_SHADOW),
    /** Dark AE2 palette with fluix-purple selection highlights. */
    DARK_PURPLE(DARK_PURPLE_ID, DARK_PURPLE_MENU_ID, PURPLE_FOLDER, true, darkColors(0xFFFFFFFF), PURPLE_HIGHLIGHT,
        PURPLE_HIGHLIGHT_TOP, PURPLE_HIGHLIGHT_SHADOW, 0xFFFFFFFF);

    private static final int PAGE_TAB_U = 128, PAGE_TAB_V = 128;
    private static final int PANEL_SIZE = 256, INSET_WIDTH = 8, INSET_HEIGHT = 9, FIELD_WIDTH = 128, SWITCH_SHEET = 64,
        TAB_BUTTON_U = 160, TAB_BUTTON_V = 192, TAB_HOVER_V = 224, TAB_BORDER_LEFT = 2, PAGE_TAB_BORDER_LEFT = 3,
        TRACK_WIDTH = 8, TRACK_HEIGHT = 6, FILL_SIZE = 4, TAG_WIDTH = 6, TAG_HEIGHT = 9, DIVIDER_WIDTH = 4,
        DIVIDER_HEIGHT = 2, WHEEL_DISC_ALPHA = 0xE6000000, LIST_ROW_WIDTH = 67, BUTTON_BAR_WIDTH = 21,
        BUTTON_BAR_HEIGHT = 26, RADIO_X = 28, INVALID_Y = 128;
    // IDrawable.EMPTY is saved as JSON null, which the theme loader reads as unset and replaces with the parent
    // theme's framed button; a transparent rectangle keeps menu options flat.
    private static final IDrawable FLAT_OPTION = new Rectangle().color(0);

    /** Registered panel theme identifier. */
    public final String id;
    /** Registered context-menu theme identifier. */
    public final String menuId;
    /** Folder name below {@code textures/gui/skin/}. */
    public final String folder;
    /** Whether the panel is dark, so overlays such as a hovered row lighten it rather than darken it. */
    public final boolean dark;
    /** Semantic ARGB colors. */
    public final AE2Colors colors;
    /** ARGB selection fill, top edge and bottom shadow matching this skin's selected sprites. */
    public final int highlight, highlightTop, highlightShadow;
    /** ARGB text color drawn on the selection fill, such as slider value tags. */
    public final int highlightText;
    /** Palette used by GTNLLib's general theme helpers. */
    public final ThemePalette palette;
    /** Skin used by GTNLLib's general theme helpers. */
    public final ThemeSkin skin;
    /** Recolored AE2 rv3 state and terminal sheets. */
    public final ResourceLocation legacyStates, legacyTerminal;
    /** Resizable panel background and inset list well. */
    public final UITexture panel, inset;
    /** The panel texture without its left border, for upgrade panels; see {@link AE2Upgrades#drawUpgradePanel}. */
    public final UITexture upgradeFrame;
    /** Empty item and fluid slot backgrounds, natively 18 by 18 pixels. */
    public final UITexture slotItem, slotFluid;
    /** Small scroll-bar handle, natively 7 by 15 pixels and resizable by MUI2. */
    public final UITexture scroller;
    /** Disabled small scroll-bar handle, 7 by 15 pixels. */
    public final UITexture scrollerDisabled;
    /** Enabled and disabled large scroll-bar handles, 12 by 15 pixels. */
    public final UITexture bigScroller, bigScrollerDisabled;
    /** Resizable text-field background with a native height of 12 pixels. */
    public final UITexture textField;
    /** Dark background of a text field that cannot be edited, with the same borders as {@link #textField}. */
    public final UITexture textFieldDisabled;
    /** Normal, hovered and selected toolbar-button frames, natively 18 by 20 pixels. */
    public final UITexture iconButton, iconButtonHover, iconButtonSelected;
    /** Normal 20-by-20 and hovered 22-by-22 tab-button backgrounds. */
    public final UITexture tabButton, tabButtonHover;
    /** Normal, selected and hovered 22-by-22 page tabs that stick out of a panel's right edge. */
    public final UITexture pageTab, pageTabSelected, pageTabHover;
    /** Unchecked switch sprites in normal and hovered states, 22 by 12 pixels. */
    public final UITexture switchOff, switchOffHover;
    /** Checked switch sprites in normal and hovered states, 22 by 12 pixels. */
    public final UITexture switchOn, switchOnHover;
    /** Unselected radio sprites in normal and hovered states, 14 by 14 pixels. */
    public final UITexture radioOff, radioOffHover;
    /** Selected radio sprites in normal and hovered states, 14 by 14 pixels. */
    public final UITexture radioOn, radioOnHover;
    /** Resizable normal and selected list rows with a native height of 22 pixels. */
    public final UITexture listRow, listRowSelected;
    /** Resizable vertical button-bar background. */
    public final UITexture buttonBar;
    /** Empty and filled inscriber progress textures, 6 by 18 pixels, intended for upward filling. */
    public final UITexture progressEmpty, progressFull;
    /**
     * Validation warning sprite, 16 by 16 pixels and drawn at that size like AE2's; number entries use
     * {@link AE2Theme#VALIDATION_ICON_SIZE} only for its hover area.
     */
    public final UITexture invalid;

    AE2Variant(String id, String menuId, String folder, boolean dark, AE2Colors colors, int highlight, int highlightTop,
        int highlightShadow) {
        this(id, menuId, folder, dark, colors, highlight, highlightTop, highlightShadow, colors.outline());
    }

    AE2Variant(String id, String menuId, String folder, boolean dark, AE2Colors colors, int highlight, int highlightTop,
        int highlightShadow, int highlightText) {
        this.id = id;
        this.menuId = menuId;
        this.folder = folder;
        this.dark = dark;
        this.colors = colors;
        this.highlight = highlight;
        this.highlightTop = highlightTop;
        this.highlightShadow = highlightShadow;
        this.highlightText = highlightText;
        palette = palette(colors, highlight, highlightShadow, highlightText);
        legacyStates = texture(folder, "legacy_states");
        legacyTerminal = texture(folder, "legacy_terminal");
        skin = ThemeSkin.NONE.with(Part.PANEL, NineSlice.PANEL.skin(folder))
            .with(Part.CARD, NineSlice.INSET.skin(folder))
            .with(Part.SLOT, NineSlice.SLOT.skin(folder))
            .with(Part.SLIDER_TRACK, skin(folder, "slider_track", TRACK_WIDTH, TRACK_HEIGHT).border(1, 2, 1, 1))
            .with(Part.SLIDER_FILL, skin(folder, "slider_fill", FILL_SIZE, FILL_SIZE).border(0, 1, 0, 0))
            .with(Part.SLIDER_TAG, skin(folder, "slider_tag", TAG_WIDTH, TAG_HEIGHT).border(1, 2, 1, 2))
            .with(Part.DIVIDER, skin(folder, "divider", DIVIDER_WIDTH, DIVIDER_HEIGHT));
        panel = NineSlice.PANEL.adaptable(folder);
        upgradeFrame = UITexture.builder()
            .location(texture(folder, "panel"))
            .imageSize(PANEL_SIZE, PANEL_SIZE)
            .subAreaXYWH(PANEL_EDGE, 0, PANEL_SIZE - PANEL_EDGE, PANEL_SIZE)
            .adaptable(0, PANEL_EDGE, PANEL_EDGE, PANEL_BOTTOM_EDGE)
            .name("gtnllib_" + folder + "_upgrade_frame")
            .build();
        inset = NineSlice.INSET.adaptable(folder);
        slotItem = NineSlice.SLOT.adaptable(folder);
        slotFluid = adaptable(folder, "slot_fluid", SLOT_SIZE, SLOT_SIZE, 1, 1, 1, 1);
        scroller = adaptable(folder, "scroller", SMALL_SCROLLER_WIDTH, SCROLLER_HEIGHT, 2, 2, 2, 4);
        scrollerDisabled = image(folder, "scroller_disabled", SMALL_SCROLLER_WIDTH, SCROLLER_HEIGHT);
        bigScroller = image(folder, "big_scroller", BIG_SCROLLER_WIDTH, SCROLLER_HEIGHT);
        bigScrollerDisabled = image(folder, "big_scroller_disabled", BIG_SCROLLER_WIDTH, SCROLLER_HEIGHT);
        textField = fieldSprite(folder, "text_field", 0);
        textFieldDisabled = fieldSprite(folder, "text_field_disabled", TEXT_FIELD_HEIGHT);
        iconButton = frame(folder, "icon_button", 176, 128);
        iconButtonHover = frame(folder, "icon_button_hover", 212, 128);
        iconButtonSelected = frame(folder, "icon_button_selected", 194, 128);
        tabButton = tabSprite(folder, "tab_button", TAB_BUTTON_U, TAB_BUTTON_V, TAB_BUTTON_SIZE, TAB_BORDER_LEFT);
        tabButtonHover = tabSprite(
            folder,
            "tab_button_hover",
            TAB_BUTTON_U,
            TAB_HOVER_V,
            TAB_BUTTON_SIZE,
            TAB_BORDER_LEFT);
        pageTab = tabSprite(folder, "page_tab", PAGE_TAB_U, PAGE_TAB_V, PAGE_TAB_SIZE, PAGE_TAB_BORDER_LEFT);
        pageTabSelected = tabSprite(
            folder,
            "page_tab_selected",
            PAGE_TAB_U,
            PAGE_TAB_V + PAGE_TAB_SIZE,
            PAGE_TAB_SIZE,
            PAGE_TAB_BORDER_LEFT);
        pageTabHover = tabSprite(
            folder,
            "page_tab_hover",
            PAGE_TAB_U + PAGE_TAB_SIZE,
            PAGE_TAB_V,
            PAGE_TAB_SIZE,
            PAGE_TAB_BORDER_LEFT);
        switchOff = checkbox(folder, "switch_off", 0, 28);
        switchOffHover = checkbox(folder, "switch_off_hover", 22, 28);
        switchOn = checkbox(folder, "switch_on", 0, 40);
        switchOnHover = checkbox(folder, "switch_on_hover", 22, 40);
        radioOff = sprite(folder, "checkbox", SWITCH_SHEET, RADIO_X, 0, RADIO_SIZE, RADIO_SIZE);
        radioOffHover = sprite(folder, "checkbox", SWITCH_SHEET, RADIO_X + RADIO_SIZE, 0, RADIO_SIZE, RADIO_SIZE);
        radioOn = sprite(folder, "checkbox", SWITCH_SHEET, RADIO_X, RADIO_SIZE, RADIO_SIZE, RADIO_SIZE);
        radioOnHover = sprite(
            folder,
            "checkbox",
            SWITCH_SHEET,
            RADIO_X + RADIO_SIZE,
            RADIO_SIZE,
            RADIO_SIZE,
            RADIO_SIZE);
        listRow = rowSprite(folder, "list_row", 0);
        listRowSelected = rowSprite(folder, "list_row_selected", LIST_ROW_HEIGHT);
        buttonBar = adaptable(folder, "button_bar", BUTTON_BAR_WIDTH, BUTTON_BAR_HEIGHT, 2, 2, 0, 4);
        progressEmpty = progressSprite(folder, "progress_empty", 0);
        progressFull = progressSprite(folder, "progress_full", PROGRESS_WIDTH);
        invalid = sprite(folder, "states", SHEET, 0, INVALID_Y, ICON_SIZE, ICON_SIZE);
    }

    /**
     * Selects the switch sprite for the supplied state.
     *
     * @param on      switch value
     * @param hovered whether the switch is hovered and available
     * @return a 22-by-12-pixel switch sprite
     */
    public UITexture switchSprite(boolean on, boolean hovered) {
        return on ? hovered ? switchOnHover : switchOn : hovered ? switchOffHover : switchOff;
    }

    /**
     * Selects the radio sprite for the supplied state.
     *
     * @param on      whether the radio option is selected
     * @param hovered whether the option is hovered and available
     * @return a 14-by-14-pixel radio sprite
     */
    public UITexture radioSprite(boolean on, boolean hovered) {
        return on ? hovered ? radioOnHover : radioOn : hovered ? radioOffHover : radioOff;
    }

    private static UITexture rowSprite(String folder, String name, int y) {
        return UITexture.builder()
            .location(texture(folder, "list_row"))
            .imageSize(LIST_ROW_WIDTH, LIST_ROW_HEIGHT * 2)
            .subAreaXYWH(0, y, LIST_ROW_WIDTH, LIST_ROW_HEIGHT)
            .adaptable(1, 1, 1, 2)
            .name("gtnllib_" + folder + "_" + name)
            .build();
    }

    private static UITexture fieldSprite(String folder, String name, int y) {
        return UITexture.builder()
            .location(texture(folder, "text_field"))
            .imageSize(FIELD_WIDTH, FIELD_WIDTH)
            .subAreaXYWH(0, y, FIELD_WIDTH, TEXT_FIELD_HEIGHT)
            .adaptable(1, 3, 1, 1)
            .name("gtnllib_" + folder + "_" + name)
            .build();
    }

    private static UITexture progressSprite(String folder, String name, int x) {
        return UITexture.builder()
            .location(texture(folder, "progress"))
            .imageSize(PROGRESS_WIDTH * 2, PROGRESS_HEIGHT)
            .subAreaXYWH(x, 0, PROGRESS_WIDTH, PROGRESS_HEIGHT)
            .name("gtnllib_" + folder + "_" + name)
            .build();
    }

    /**
     * Applies this variant's disc, line, text and highlight colors to a wheel style.
     *
     * @param style builder to modify
     * @return the supplied builder
     */
    public WheelStyle.Builder wheel(WheelStyle.Builder style) {
        return style.discColor(WHEEL_DISC_ALPHA | colors.panel() & 0xFFFFFF)
            .lineColor(colors.line())
            .textColor(colors.text())
            .textShadow(false)
            .highlightColor(highlight);
    }

    private static UITexture checkbox(String folder, String name, int x, int y) {
        return UITexture.builder()
            .location(texture(folder, "checkbox"))
            .imageSize(SWITCH_SHEET, SWITCH_SHEET)
            .subAreaXYWH(x, y, SWITCH_WIDTH, SWITCH_HEIGHT)
            .nonOpaque()
            .name("gtnllib_" + folder + "_" + name)
            .build();
    }

    private static ThemePalette palette(AE2Colors colors, int highlight, int highlightShadow, int highlightText) {
        return new ThemePalette(
            highlight,
            highlightShadow,
            0x40FFFFFF,
            colors.text(),
            colors.muted(),
            colors.panel(),
            colors.panel(),
            colors.well(),
            colors.edge(),
            colors.well(),
            highlight,
            0x80000000 | highlight & 0xFFFFFF,
            colors.well(),
            colors.edge(),
            0x80FFFFFF,
            colors.line(),
            colors.muted(),
            colors.edge(),
            colors.raised(),
            colors.text(),
            colors.muted(),
            colors.secondary(),
            colors.hoverText(),
            highlightText,
            0x40000000 | colors.text() & 0xFFFFFF,
            0x99000000 | colors.text() & 0xFFFFFF,
            colors.error());
    }

    public ThemeBuilder<?> menuTheme() {
        return new ThemeBuilder<>(menuId).parent(id)
            .background(IThemeApi.PANEL, panel)
            .background(IThemeApi.BUTTON, FLAT_OPTION)
            .hoverBackground(IThemeApi.BUTTON, listRowSelected)
            .textColor(IThemeApi.BUTTON, colors.text())
            .textHoverColor(IThemeApi.BUTTON, colors.hoverText())
            .widgetTheme(
                IThemeApi.TOGGLE_BUTTON,
                new SelectableTheme.Builder<>().background(FLAT_OPTION)
                    .textColor(colors.text())
                    .selectedBackground(listRowSelected)
                    .selectedTextColor(colors.hoverText()))
            .widgetThemeHover(
                IThemeApi.TOGGLE_BUTTON,
                new SelectableTheme.Builder<>().background(listRowSelected)
                    .textColor(colors.hoverText())
                    .selectedBackground(listRowSelected)
                    .selectedTextColor(colors.hoverText()));
    }

    public ThemeBuilder<?> panelTheme() {
        IDrawable button = new AE2ButtonDrawable(State.NORMAL, folder),
            lit = new AE2ButtonDrawable(State.HOVER, folder);
        int normal = colors.buttonText(), hover = colors.hoverText();
        ThemeBuilder<?> theme = PaletteThemes.build(id, ThemeAPI.DEFAULT_ID, palette, STYLE, skin)
            .defaultTextShadow(false)
            .background(IThemeApi.BUTTON, button)
            .hoverBackground(IThemeApi.BUTTON, lit)
            .textColor(IThemeApi.BUTTON, normal)
            .textHoverColor(IThemeApi.BUTTON, hover)
            .textShadow(IThemeApi.BUTTON, false)
            .textHoverShadow(IThemeApi.BUTTON, false)
            .widgetTheme(
                IThemeApi.TOGGLE_BUTTON,
                new SelectableTheme.Builder<>().background(button)
                    .textColor(normal)
                    .selectedBackground(lit)
                    .selectedTextColor(hover))
            .widgetThemeHover(
                IThemeApi.TOGGLE_BUTTON,
                new SelectableTheme.Builder<>().background(lit)
                    .textColor(hover)
                    .selectedBackground(lit)
                    .selectedTextColor(hover))
            .background(IThemeApi.CLOSE_BUTTON, button)
            .hoverBackground(IThemeApi.CLOSE_BUTTON, lit)
            .textColor(IThemeApi.CLOSE_BUTTON, normal)
            .textHoverColor(IThemeApi.CLOSE_BUTTON, hover)
            .textShadow(IThemeApi.CLOSE_BUTTON, false)
            .textHoverShadow(IThemeApi.CLOSE_BUTTON, false)
            .background(IThemeApi.FLUID_SLOT, slotFluid)
            .background(IThemeApi.TEXT_FIELD, textField)
            .textColor(IThemeApi.TEXT_FIELD, colors.fieldText())
            .textFieldHintColor(colors.placeholder())
            .textFieldMarkedColor(FIELD_SELECTION)
            .background(IThemeApi.SCROLLBAR, scroller)
            .widgetTheme(
                SlideSwitch.Theme.KEY,
                new SlideSwitch.Theme.Builder().text(colors.text(), colors.muted(), colors.text())
                    .sprites(switchOff, switchOffHover, switchOn, switchOnHover))
            .hoverBackground(SlideSwitch.Theme.KEY, IDrawable.EMPTY)
            .widgetTheme(AE2Look.KEY, new AE2Look.Marker.Builder().skin(folder))
            .widgetTheme(
                LayoutTheme.KEY,
                new LayoutTheme.Builder().values(STYLE.layout())
                    .switches(SWITCH_WIDTH, SWITCH_HEIGHT, SWITCH_LABEL_X - SWITCH_WIDTH))
            .background(ButtonPurpose.PRIMARY.key, lit)
            .hoverBackground(ButtonPurpose.PRIMARY.key, lit)
            .textColor(ButtonPurpose.PRIMARY.key, hover)
            .textHoverColor(ButtonPurpose.PRIMARY.key, hover)
            .textShadow(ButtonPurpose.PRIMARY.key, false)
            .textHoverShadow(ButtonPurpose.PRIMARY.key, false)
            .background(ButtonPurpose.DANGER.key, button)
            .hoverBackground(ButtonPurpose.DANGER.key, lit)
            .textColor(ButtonPurpose.DANGER.key, colors.bad())
            .textHoverColor(ButtonPurpose.DANGER.key, colors.bad())
            .background(ButtonPurpose.ICON.key, iconButton)
            .hoverBackground(ButtonPurpose.ICON.key, iconButtonHover);
        theme.mergeAdd(
            IThemeApi.TOGGLE_BUTTON.getFullName(),
            new JsonBuilder().add(IThemeApi.SELECTED_TEXT_SHADOW, false));
        PaletteThemes.decorate(theme, palette);
        return theme;
    }

    private static SkinDrawable skin(String folder, String name, int width, int height) {
        return new SkinDrawable().texture(texture(folder, name), width, height);
    }

    /**
     * A texture used both by GTNLLib's skin parts and by MUI2 directly, so its size and borders are written once.
     *
     * @param name   texture filename without the {@code .png} extension
     * @param width  texture width
     * @param height texture height
     * @param left   left border kept unstretched
     * @param top    top border kept unstretched
     * @param right  right border kept unstretched
     * @param bottom bottom border kept unstretched
     */
    private record NineSlice(String name, int width, int height, int left, int top, int right, int bottom) {

        private static final NineSlice PANEL = new NineSlice(
            "panel",
            PANEL_SIZE,
            PANEL_SIZE,
            PANEL_EDGE,
            PANEL_EDGE,
            PANEL_EDGE,
            PANEL_BOTTOM_EDGE),
            INSET = new NineSlice("inset", INSET_WIDTH, INSET_HEIGHT, INSET_EDGE, INSET_TOP, INSET_EDGE, INSET_EDGE),
            SLOT = new NineSlice("slot_item", SLOT_SIZE, SLOT_SIZE, 1, 1, 1, 1);

        private SkinDrawable skin(String folder) {
            return AE2Variant.skin(folder, name, width, height)
                .border(left, top, right, bottom);
        }

        private UITexture adaptable(String folder) {
            return AE2Variant.adaptable(folder, name, width, height, left, top, right, bottom);
        }
    }

    private static UITexture adaptable(String folder, String name, int width, int height, int left, int top, int right,
        int bottom) {
        return UITexture.builder()
            .location(texture(folder, name))
            .imageSize(width, height)
            .adaptable(left, top, right, bottom)
            .name("gtnllib_" + folder + "_" + name)
            .build();
    }

    private static AE2Colors darkColors(int hoverText) {
        return AE2Colors.builder()
            .text(0xFFAAAAAA)
            .muted(0xFF7E808E)
            .secondary(0xFF9496A4)
            .buttonText(0xFFE0E0E0)
            .hoverText(hoverText)
            .disabledText(0xFF5E6070)
            .fieldText(0xFFE0E0E0)
            .placeholder(0xFF6B6E7E)
            .line(0xFF14151D)
            .edge(0xFF8E8E8E)
            .panel(0xFF42434B)
            .well(0xFF242631)
            .outline(0xFF0A090E)
            .raised(0xFF2D2F3C)
            .good(0xFF5FCB79)
            .bad(0xFFE06A60)
            .info(0xFF6CB4F0)
            .warning(0xFFE0A54A)
            .error(0xFFFF5A3C)
            .build();
    }

    private static UITexture frame(String folder, String name, int x, int y) {
        return UITexture.builder()
            .location(texture(folder, "states"))
            .imageSize(SHEET, SHEET)
            .subAreaXYWH(x, y, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT)
            .adaptable(2, 2, 2, 1)
            .name("gtnllib_" + folder + "_" + name)
            .build();
    }

    private static UITexture image(String folder, String name, int width, int height) {
        return UITexture.builder()
            .location(texture(folder, name))
            .imageSize(width, height)
            .nonOpaque()
            .name("gtnllib_" + folder + "_" + name)
            .build();
    }

    // Why: tab sprites are a frame around a flat fill, two pixels at the top and four at the bottom with the shadow, so
    // they stretch to any size; the page tab's three left columns are the panel border it covers.
    private static UITexture tabSprite(String folder, String name, int x, int y, int size, int left) {
        return UITexture.builder()
            .location(texture(folder, "states"))
            .imageSize(SHEET, SHEET)
            .subAreaXYWH(x, y, size, size)
            .adaptable(left, 2, 2, 4)
            .nonOpaque()
            .name("gtnllib_" + folder + "_" + name)
            .build();
    }

    /**
     * Cuts a sprite out of one of a skin's square sheets.
     *
     * @param folder skin folder, such as {@code ae2_light}
     * @param sheet  sheet filename without the {@code .png} extension
     * @param size   width and height of the sheet in pixels
     * @param x      left edge of the sprite in the sheet
     * @param y      top edge of the sprite in the sheet
     * @param width  sprite width
     * @param height sprite height
     * @return the sprite, drawn with transparency
     */
    public static UITexture sprite(String folder, String sheet, int size, int x, int y, int width, int height) {
        return UITexture.builder()
            .location(texture(folder, sheet))
            .imageSize(size, size)
            .subAreaXYWH(x, y, width, height)
            .nonOpaque()
            .build();
    }
}

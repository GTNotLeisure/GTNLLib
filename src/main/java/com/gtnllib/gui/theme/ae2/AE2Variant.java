package com.gtnllib.gui.theme.ae2;

import static com.gtnllib.gui.theme.AE2Theme.BIG_SCROLLER_WIDTH;
import static com.gtnllib.gui.theme.AE2Theme.CHECKBOX_HEIGHT;
import static com.gtnllib.gui.theme.AE2Theme.CHECKBOX_WIDTH;
import static com.gtnllib.gui.theme.AE2Theme.DARK_FOLDER;
import static com.gtnllib.gui.theme.AE2Theme.DARK_ID;
import static com.gtnllib.gui.theme.AE2Theme.DARK_MENU_ID;
import static com.gtnllib.gui.theme.AE2Theme.DARK_PURPLE_ID;
import static com.gtnllib.gui.theme.AE2Theme.DARK_PURPLE_MENU_ID;
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
import static com.gtnllib.gui.theme.AE2Theme.TAB_BUTTON_SIZE;
import static com.gtnllib.gui.theme.AE2Theme.TEXT_FIELD_HEIGHT;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.drawable.UITexture;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.SkinDrawable;
import com.gtnllib.gui.theme.ThemePalette;
import com.gtnllib.gui.theme.ThemeSkin;
import com.gtnllib.gui.theme.ThemeSkin.Part;

public class AE2Variant {

    /**
     * Light AE2 palette.
     */
    public static final AE2Variant LIGHT = new AE2Variant(
        LIGHT_ID,
        LIGHT_MENU_ID,
        LIGHT_FOLDER,
        false,
        AE2Colors.builder()
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
            .build(),
        HIGHLIGHT,
        HIGHLIGHT_TOP,
        HIGHLIGHT_SHADOW);
    /**
     * Dark AE2 palette.
     */
    public static final AE2Variant DARK = new AE2Variant(
        DARK_ID,
        DARK_MENU_ID,
        DARK_FOLDER,
        true,
        darkColors(0xFF517497),
        HIGHLIGHT,
        HIGHLIGHT_TOP,
        HIGHLIGHT_SHADOW);
    /**
     * Dark AE2 palette with fluix-purple selection highlights.
     */
    public static final AE2Variant DARK_PURPLE = new AE2Variant(
        DARK_PURPLE_ID,
        DARK_PURPLE_MENU_ID,
        PURPLE_FOLDER,
        true,
        darkColors(0xFFFFFFFF),
        PURPLE_HIGHLIGHT,
        PURPLE_HIGHLIGHT_TOP,
        PURPLE_HIGHLIGHT_SHADOW,
        0xFFFFFFFF);

    private static final AE2Variant[] BUILT_INS = { LIGHT, DARK, DARK_PURPLE };

    private static final int PAGE_TAB_U = 128, PAGE_TAB_V = 128;
    private static final int PANEL_SIZE = 256, INSET_WIDTH = 8, INSET_HEIGHT = 9, FIELD_WIDTH = 128,
        CHECKBOX_SHEET = 64, TAB_BUTTON_U = 160, TAB_BUTTON_V = 192, TAB_HOVER_V = 224, TAB_BORDER_LEFT = 2,
        PAGE_TAB_BORDER_LEFT = 3, TRACK_WIDTH = 8, TRACK_HEIGHT = 6, FILL_SIZE = 4, TAG_WIDTH = 6, TAG_HEIGHT = 9,
        DIVIDER_WIDTH = 4, DIVIDER_HEIGHT = 2, LIST_ROW_WIDTH = 67, BUTTON_BAR_WIDTH = 21, BUTTON_BAR_HEIGHT = 26,
        RADIO_X = 28, INVALID_Y = 128;

    /**
     * Registered panel theme identifier.
     */
    public final String id;
    /**
     * Registered context-menu theme identifier.
     */
    public final String menuId;
    /**
     * Inherited fallback folder below {@code textures/gui/skin/}. Use {@link #texture(String)} for custom textures.
     */
    public final String folder;
    /**
     * Whether the panel is dark, so overlays such as a hovered row lighten it rather than darken it.
     */
    public final boolean dark;
    /**
     * Semantic ARGB colors.
     */
    public final AE2Colors colors;
    /**
     * ARGB selection fill, top edge and bottom shadow matching this skin's selected sprites.
     */
    public final int highlight, highlightTop, highlightShadow;
    /**
     * ARGB text color drawn on the selection fill, such as slider value tags.
     */
    public final int highlightText;
    /**
     * Palette used by GTNLLib's general theme helpers.
     */
    public final ThemePalette palette;
    /**
     * Skin used by GTNLLib's general theme helpers.
     */
    public final ThemeSkin skin;
    /**
     * Recolored AE2 rv3 state and terminal sheets.
     */
    public final ResourceLocation legacyStates, legacyTerminal;
    /**
     * Resizable panel background and inset list well.
     */
    public final UITexture panel, inset;
    /**
     * The panel texture without its left border, for upgrade panels; see {@link AE2UpgradePainter#drawUpgradePanel}.
     */
    public final UITexture upgradeFrame;
    /**
     * Empty item and fluid slot backgrounds, natively 18 by 18 pixels.
     */
    public final UITexture slotItem, slotFluid;
    /**
     * Small scroll-bar handle, natively 7 by 15 pixels and resizable by MUI2.
     */
    public final UITexture scroller;
    /**
     * Disabled small scroll-bar handle, 7 by 15 pixels.
     */
    public final UITexture scrollerDisabled;
    /**
     * Enabled and disabled large scroll-bar handles, 12 by 15 pixels.
     */
    public final UITexture bigScroller, bigScrollerDisabled;
    /**
     * Resizable text-field background with a native height of 12 pixels.
     */
    public final UITexture textField;
    /**
     * Dark background of a text field that cannot be edited, with the same borders as {@link #textField}.
     */
    public final UITexture textFieldDisabled;
    /**
     * Normal, hovered and selected toolbar-button frames, natively 18 by 20 pixels.
     */
    public final UITexture iconButton, iconButtonHover, iconButtonSelected;
    /**
     * Normal 20-by-20 and hovered 22-by-22 tab-button backgrounds.
     */
    public final UITexture tabButton, tabButtonHover;
    /**
     * Normal, selected and hovered 22-by-22 page tabs that stick out of a panel's right edge.
     */
    public final UITexture pageTab, pageTabSelected, pageTabHover;
    /**
     * Unchecked check box sprites in normal and hovered states, 22 by 12 pixels.
     */
    public final UITexture checkboxOff, checkboxOffHover;
    /**
     * Checked check box sprites in normal and hovered states, 22 by 12 pixels.
     */
    public final UITexture checkboxOn, checkboxOnHover;
    /**
     * Unselected radio sprites in normal and hovered states, 14 by 14 pixels.
     */
    public final UITexture radioOff, radioOffHover;
    /**
     * Selected radio sprites in normal and hovered states, 14 by 14 pixels.
     */
    public final UITexture radioOn, radioOnHover;
    /**
     * Resizable normal and selected list rows with a native height of 22 pixels.
     */
    public final UITexture listRow, listRowSelected;
    /**
     * Resizable vertical button-bar background.
     */
    public final UITexture buttonBar;
    /**
     * Empty and filled inscriber progress textures, 6 by 18 pixels, intended for upward filling.
     */
    public final UITexture progressEmpty, progressFull;
    /**
     * Validation warning sprite, 16 by 16 pixels and drawn at that size like AE2's; number entries use
     * {@link AE2Theme#VALIDATION_ICON_SIZE} only for its hover area.
     */
    public final UITexture invalid;

    private final Map<String, ResourceLocation> textures;
    private final Map<Sprite, UITexture> sprites = new HashMap<>();
    private @Nullable AE2Look look;

    private AE2Variant(String id, String menuId, String folder, boolean dark, AE2Colors colors, int highlight,
        int highlightTop, int highlightShadow) {
        this(id, menuId, folder, dark, colors, highlight, highlightTop, highlightShadow, colors.outline());
    }

    private AE2Variant(String id, String menuId, String folder, boolean dark, AE2Colors colors, int highlight,
        int highlightTop, int highlightShadow, int highlightText) {
        this(id, menuId, folder, dark, colors, highlight, highlightTop, highlightShadow, highlightText, Map.of());
    }

    private AE2Variant(String id, String menuId, String folder, boolean dark, AE2Colors colors, int highlight,
        int highlightTop, int highlightShadow, int highlightText, Map<String, ResourceLocation> textures) {
        this.id = id;
        this.menuId = menuId;
        this.folder = folder;
        this.dark = dark;
        this.colors = colors;
        this.highlight = highlight;
        this.highlightTop = highlightTop;
        this.highlightShadow = highlightShadow;
        this.highlightText = highlightText;
        this.textures = new HashMap<>(textures);
        palette = palette(colors, highlight, highlightShadow, highlightText);
        legacyStates = texture("legacy_states");
        legacyTerminal = texture("legacy_terminal");
        skin = ThemeSkin.NONE.with(Part.PANEL, NineSlice.PANEL.skin(this))
            .with(Part.CARD, NineSlice.INSET.skin(this))
            .with(Part.SLOT, NineSlice.SLOT.skin(this))
            .with(Part.SLIDER_TRACK, skin("slider_track", TRACK_WIDTH, TRACK_HEIGHT).border(1, 2, 1, 1))
            .with(Part.SLIDER_FILL, skin("slider_fill", FILL_SIZE, FILL_SIZE).border(0, 1, 0, 0))
            .with(Part.SLIDER_TAG, skin("slider_tag", TAG_WIDTH, TAG_HEIGHT).border(1, 2, 1, 2))
            .with(Part.DIVIDER, skin("divider", DIVIDER_WIDTH, DIVIDER_HEIGHT));
        panel = NineSlice.PANEL.adaptable(this);
        upgradeFrame = UITexture.builder()
            .location(texture("panel"))
            .imageSize(PANEL_SIZE, PANEL_SIZE)
            .subAreaXYWH(PANEL_EDGE, 0, PANEL_SIZE - PANEL_EDGE, PANEL_SIZE)
            .adaptable(0, PANEL_EDGE, PANEL_EDGE, PANEL_BOTTOM_EDGE)
            .name(textureName("upgrade_frame"))
            .build();
        inset = NineSlice.INSET.adaptable(this);
        slotItem = NineSlice.SLOT.adaptable(this);
        slotFluid = adaptable("slot_fluid", SLOT_SIZE, SLOT_SIZE, 1, 1, 1, 1);
        scroller = adaptable("scroller", SMALL_SCROLLER_WIDTH, SCROLLER_HEIGHT, 2, 2, 2, 4);
        scrollerDisabled = image("scroller_disabled", SMALL_SCROLLER_WIDTH, SCROLLER_HEIGHT);
        bigScroller = image("big_scroller", BIG_SCROLLER_WIDTH, SCROLLER_HEIGHT);
        bigScrollerDisabled = image("big_scroller_disabled", BIG_SCROLLER_WIDTH, SCROLLER_HEIGHT);
        textField = fieldSprite("text_field", 0);
        textFieldDisabled = fieldSprite("text_field_disabled", TEXT_FIELD_HEIGHT);
        iconButton = frame("icon_button", 176, 128);
        iconButtonHover = frame("icon_button_hover", 212, 128);
        iconButtonSelected = frame("icon_button_selected", 194, 128);
        tabButton = tabSprite("tab_button", TAB_BUTTON_U, TAB_BUTTON_V, TAB_BUTTON_SIZE, TAB_BORDER_LEFT);
        tabButtonHover = tabSprite("tab_button_hover", TAB_BUTTON_U, TAB_HOVER_V, TAB_BUTTON_SIZE, TAB_BORDER_LEFT);
        pageTab = tabSprite("page_tab", PAGE_TAB_U, PAGE_TAB_V, PAGE_TAB_SIZE, PAGE_TAB_BORDER_LEFT);
        pageTabSelected = tabSprite(
            "page_tab_selected",
            PAGE_TAB_U,
            PAGE_TAB_V + PAGE_TAB_SIZE,
            PAGE_TAB_SIZE,
            PAGE_TAB_BORDER_LEFT);
        pageTabHover = tabSprite(
            "page_tab_hover",
            PAGE_TAB_U + PAGE_TAB_SIZE,
            PAGE_TAB_V,
            PAGE_TAB_SIZE,
            PAGE_TAB_BORDER_LEFT);
        checkboxOff = checkbox("switch_off", 0, 28);
        checkboxOffHover = checkbox("switch_off_hover", 22, 28);
        checkboxOn = checkbox("switch_on", 0, 40);
        checkboxOnHover = checkbox("switch_on_hover", 22, 40);
        radioOff = sprite("checkbox", CHECKBOX_SHEET, RADIO_X, 0, RADIO_SIZE, RADIO_SIZE);
        radioOffHover = sprite("checkbox", CHECKBOX_SHEET, RADIO_X + RADIO_SIZE, 0, RADIO_SIZE, RADIO_SIZE);
        radioOn = sprite("checkbox", CHECKBOX_SHEET, RADIO_X, RADIO_SIZE, RADIO_SIZE, RADIO_SIZE);
        radioOnHover = sprite("checkbox", CHECKBOX_SHEET, RADIO_X + RADIO_SIZE, RADIO_SIZE, RADIO_SIZE, RADIO_SIZE);
        listRow = rowSprite("list_row", 0);
        listRowSelected = rowSprite("list_row_selected", LIST_ROW_HEIGHT);
        buttonBar = adaptable("button_bar", BUTTON_BAR_WIDTH, BUTTON_BAR_HEIGHT, 2, 2, 0, 4);
        progressEmpty = progressSprite("progress_empty", 0);
        progressFull = progressSprite("progress_full", PROGRESS_WIDTH);
        invalid = sprite("states", SHEET, 0, INVALID_Y, ICON_SIZE, ICON_SIZE);
    }

    /**
     * Returns the three built-in skins. Resource pack skins are available from {@link AE2Theme#variants()}.
     *
     * @return a fresh array in light, dark, purple-dark order
     */
    public static AE2Variant[] values() {
        return BUILT_INS.clone();
    }

    /**
     * Starts a skin definition that inherits all colors and texture sheets from its parent.
     *
     * @param id     registered panel theme identifier
     * @param parent inherited skin
     * @return a new definition builder
     */
    public static Builder builder(String id, AE2Variant parent) {
        return new Builder(id, parent);
    }

    /**
     * Resolves a texture sheet, including inherited and resource pack overrides.
     *
     * @param name sheet filename without the extension
     * @return the resolved texture location
     */
    public ResourceLocation texture(String name) {
        ResourceLocation location = textures.get(name);
        if (location == null) {
            location = AE2Theme.texture(folder, name);
            textures.put(name, location);
        }
        return location;
    }

    /**
     * Returns this skin's cached drawing look. The cache belongs to the skin and expires with it.
     *
     * @return the drawing look
     */
    public AE2Look look() {
        if (look == null) look = new AE2Look(this);
        return look;
    }

    /**
     * Cuts and caches a sprite from this skin's resolved texture sheet.
     *
     * @param sheet  sheet filename without the extension
     * @param size   square sheet size in pixels
     * @param x      sprite left edge
     * @param y      sprite top edge
     * @param width  sprite width
     * @param height sprite height
     * @return the cached transparent sprite
     */
    public UITexture sprite(String sheet, int size, int x, int y, int width, int height) {
        Sprite key = new Sprite(sheet, size, x, y, width, height);
        UITexture sprite = sprites.get(key);
        if (sprite == null) {
            sprite = UITexture.builder()
                .location(texture(sheet))
                .imageSize(size, size)
                .subAreaXYWH(x, y, width, height)
                .nonOpaque()
                .build();
            sprites.put(key, sprite);
        }
        return sprite;
    }

    private @Nullable String textureName(String name) {
        // Reusing inherited artwork must not rename MUI2's already registered built-in textures.
        return id.equals(LIGHT_ID) || id.equals(DARK_ID) || id.equals(DARK_PURPLE_ID) ? "gtnllib_" + folder + "_" + name
            : null;
    }

    private record Sprite(String sheet, int size, int x, int y, int width, int height) {}

    public static class Builder {

        private final String id;
        private final String folder;
        private final Map<String, ResourceLocation> textures;
        private String menuId;
        private boolean dark;
        private AE2Colors colors;
        private int highlight, highlightTop, highlightShadow, highlightText;

        private Builder(String id, AE2Variant parent) {
            this.id = Objects.requireNonNull(id);
            menuId = id + "_menu";
            folder = parent.folder;
            dark = parent.dark;
            colors = parent.colors;
            highlight = parent.highlight;
            highlightTop = parent.highlightTop;
            highlightShadow = parent.highlightShadow;
            highlightText = parent.highlightText;
            textures = new HashMap<>(parent.textures);
        }

        /** @param menuId context-menu theme identifier */
        public Builder menu(String menuId) {
            this.menuId = Objects.requireNonNull(menuId);
            return this;
        }

        /** @param dark whether overlays should lighten the panel */
        public Builder dark(boolean dark) {
            this.dark = dark;
            return this;
        }

        /** @param colors semantic ARGB colors */
        public Builder colors(AE2Colors colors) {
            this.colors = Objects.requireNonNull(colors);
            return this;
        }

        /**
         * @param fill   selection fill
         * @param top    top selection edge
         * @param shadow bottom selection edge
         * @param text   text drawn on the selection
         */
        public Builder highlight(int fill, int top, int shadow, int text) {
            highlight = fill;
            highlightTop = top;
            highlightShadow = shadow;
            highlightText = text;
            return this;
        }

        /**
         * @param name     sheet filename without the extension
         * @param location replacement sheet with the inherited dimensions and sprite layout
         */
        public Builder texture(String name, ResourceLocation location) {
            textures.put(Objects.requireNonNull(name), Objects.requireNonNull(location));
            return this;
        }

        /** @return a new skin with independent texture, sprite and look caches */
        public AE2Variant build() {
            return new AE2Variant(
                id,
                menuId,
                folder,
                dark,
                colors,
                highlight,
                highlightTop,
                highlightShadow,
                highlightText,
                textures);
        }
    }

    /**
     * Selects the check box sprite for the supplied state.
     *
     * @param on      whether the box is checked
     * @param hovered whether the check box is hovered and available
     * @return a 22-by-12-pixel check box sprite
     */
    public UITexture checkboxSprite(boolean on, boolean hovered) {
        return on ? hovered ? checkboxOnHover : checkboxOn : hovered ? checkboxOffHover : checkboxOff;
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

    private UITexture rowSprite(String name, int y) {
        return UITexture.builder()
            .location(texture("list_row"))
            .imageSize(LIST_ROW_WIDTH, LIST_ROW_HEIGHT * 2)
            .subAreaXYWH(0, y, LIST_ROW_WIDTH, LIST_ROW_HEIGHT)
            .adaptable(1, 1, 1, 2)
            .name(textureName(name))
            .build();
    }

    private UITexture fieldSprite(String name, int y) {
        return UITexture.builder()
            .location(texture("text_field"))
            .imageSize(FIELD_WIDTH, FIELD_WIDTH)
            .subAreaXYWH(0, y, FIELD_WIDTH, TEXT_FIELD_HEIGHT)
            .adaptable(1, 3, 1, 1)
            .name(textureName(name))
            .build();
    }

    private UITexture progressSprite(String name, int x) {
        return UITexture.builder()
            .location(texture("progress"))
            .imageSize(PROGRESS_WIDTH * 2, PROGRESS_HEIGHT)
            .subAreaXYWH(x, 0, PROGRESS_WIDTH, PROGRESS_HEIGHT)
            .name(textureName(name))
            .build();
    }

    private UITexture checkbox(String name, int x, int y) {
        return UITexture.builder()
            .location(texture("checkbox"))
            .imageSize(CHECKBOX_SHEET, CHECKBOX_SHEET)
            .subAreaXYWH(x, y, CHECKBOX_WIDTH, CHECKBOX_HEIGHT)
            .nonOpaque()
            .name(textureName(name))
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

    private SkinDrawable skin(String name, int width, int height) {
        return new SkinDrawable().texture(texture(name), width, height);
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

        private SkinDrawable skin(AE2Variant variant) {
            return variant.skin(name, width, height)
                .border(left, top, right, bottom);
        }

        private UITexture adaptable(AE2Variant variant) {
            return variant.adaptable(name, width, height, left, top, right, bottom);
        }
    }

    private UITexture adaptable(String name, int width, int height, int left, int top, int right, int bottom) {
        return UITexture.builder()
            .location(texture(name))
            .imageSize(width, height)
            .adaptable(left, top, right, bottom)
            .name(textureName(name))
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

    private UITexture frame(String name, int x, int y) {
        return UITexture.builder()
            .location(texture("states"))
            .imageSize(SHEET, SHEET)
            .subAreaXYWH(x, y, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT)
            .adaptable(2, 2, 2, 1)
            .name(textureName(name))
            .build();
    }

    private UITexture image(String name, int width, int height) {
        return UITexture.builder()
            .location(texture(name))
            .imageSize(width, height)
            .nonOpaque()
            .name(textureName(name))
            .build();
    }

    // Tab sprites are a frame around a flat fill, two pixels at the top and four at the bottom with the shadow, so
    // they stretch to any size; the page tab's three left columns are the panel border it covers.
    private UITexture tabSprite(String name, int x, int y, int size, int left) {
        return UITexture.builder()
            .location(texture("states"))
            .imageSize(SHEET, SHEET)
            .subAreaXYWH(x, y, size, size)
            .adaptable(left, 2, 2, 4)
            .nonOpaque()
            .name(textureName(name))
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
            .location(AE2Theme.texture(folder, sheet))
            .imageSize(size, size)
            .subAreaXYWH(x, y, width, height)
            .nonOpaque()
            .build();
    }
}

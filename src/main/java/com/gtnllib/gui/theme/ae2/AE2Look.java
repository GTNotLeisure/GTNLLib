package com.gtnllib.gui.theme.ae2;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeBuilder;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.theme.WidgetThemeKey;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonObject;
import com.gtnllib.gui.theme.AE2Theme;

/**
 * The look GTNLLib's AE2-styled widgets take in one panel: one of the AE2 skins, or the plain look every other theme
 * gets. Widgets ask {@link #of(IWidget)} while they draw, so a panel with a theme of its own, such as a dialog over an
 * AE2 screen, gets the look of its own theme rather than the screen's.
 * <p>
 * A theme names its AE2 skin in the {@link #KEY} entry of its theme data. The AE2 themes write that entry when they
 * are registered, and themes whose parent is an AE2 theme inherit it. A theme without the entry is recognised by its
 * identifier, so the AE2 themes keep their look even when the entry is missing.
 * <p>
 * The class loads on both sides. Resolving a look from a widget, a context or a theme is meant for drawing on the
 * client, while the widget is part of an open panel.
 *
 * @param variant AE2 skin, or {@code null} for the plain look
 */
public record AE2Look(@Nullable AE2Variant variant) {

    private static final AE2Variant[] VARIANTS = AE2Variant.values();
    /** Theme data entry that names the AE2 skin of a theme; themes without it get the plain look. */
    public static final WidgetThemeKey<Marker> KEY = Marker.register();
    /** Pixels a list well's border takes on each side, under every look. */
    public static final int WELL_EDGE = 1;
    private static final int PLAIN_WELL = 0xFF8B8B8B, PLAIN_WELL_SHADOW = 0xFF373737, PLAIN_WELL_LIGHT = 0xFFFFFFFF,
        PLAIN_TEXT = 0xFFFFFFFF, PLAIN_MUTED = 0xFF404040, PLAIN_SELECTED = 0xFF5A5A5A, PLAIN_HOVER = 0x30FFFFFF,
        PLAIN_NESTED = 0x20000000, PLAIN_GOOD = 0xFF60E060, PLAIN_BAD = 0xFFFF6060, PLAIN_SCROLL_BAR = 0xFFC6C6C6,
        LIGHT_SHADE = 0x18000000, DARK_SHADE = 0x18FFFFFF;
    private static final AE2Look PLAIN = new AE2Look(null);
    private static final AE2Look[] SKINNED = skinned();

    /**
     * Returns the plain look, used under every theme that is not an AE2 theme.
     *
     * @return the plain look
     */
    public static AE2Look plain() {
        return PLAIN;
    }

    /**
     * Returns the look of an AE2 skin.
     *
     * @param variant AE2 skin, or {@code null} for the plain look
     * @return the shared look of that skin
     */
    public static AE2Look of(@Nullable AE2Variant variant) {
        return variant == null ? PLAIN : SKINNED[variant.ordinal()];
    }

    /**
     * Resolves the look of the panel a widget belongs to, from that panel's theme.
     *
     * @param widget widget that is part of an open panel
     * @return the panel's look, or the plain look for a widget that is not in a panel yet
     */
    public static AE2Look of(IWidget widget) {
        return widget.isValid() ? of(
            widget.getPanel()
                .getTheme())
            : PLAIN;
    }

    /**
     * Resolves the look of the screen being drawn, for drawing code that has no widget at hand. Prefer
     * {@link #of(IWidget)}: a panel can use a theme other than its screen's.
     *
     * @param context current drawing context
     * @return the screen's look, or the plain look outside modular screens
     */
    public static AE2Look of(GuiContext context) {
        return context instanceof ModularGuiContext modular ? of(modular.getTheme()) : PLAIN;
    }

    /**
     * Resolves the look a theme gives: the skin named in its {@link #KEY} entry, else the AE2 skin registered under its
     * identifier, else the plain look.
     *
     * @param theme theme to read, possibly {@code null}
     * @return the theme's look
     */
    public static AE2Look of(@Nullable ITheme theme) {
        if (theme == null) return PLAIN;
        WidgetThemeEntry<Marker> entry = theme.getWidgetTheme(KEY);
        AE2Variant marked = entry == null ? null
            : entry.getTheme()
                .variant();
        return of(marked != null ? marked : AE2Theme.variant(theme.getId()));
    }

    /**
     * Finds the AE2 skin stored under a texture folder name.
     *
     * @param folder skin folder name below {@code textures/gui/skin/}, such as {@code ae2_dark}
     * @return the matching skin, or {@code null} for an empty or unknown name
     */
    public static @Nullable AE2Variant skin(@Nullable String folder) {
        for (AE2Variant variant : VARIANTS) if (variant.folder.equals(folder)) return variant;
        return null;
    }

    /**
     * Reports whether this is one of the AE2 skins rather than the plain look.
     *
     * @return {@code true} under an AE2 skin
     */
    public boolean isAE2() {
        return variant != null;
    }

    /**
     * Draws a list well: AE2's inset under an AE2 skin, Minecraft's sunken slot box (grey fill, dark top and left
     * edges, white bottom and right edges) in the plain look. Rows inside start {@link #WELL_EDGE} pixels in. Client
     * only.
     *
     * @param x      left edge
     * @param y      top edge
     * @param width  well width in pixels
     * @param height well height in pixels
     */
    public void drawWell(int x, int y, int width, int height) {
        if (variant != null) {
            variant.inset.draw(x, y, width, height);
            return;
        }
        GuiDraw.drawRect(x, y, width, height, PLAIN_WELL);
        GuiDraw.drawRect(x, y, width - 1, 1, PLAIN_WELL_SHADOW);
        GuiDraw.drawRect(x, y, 1, height - 1, PLAIN_WELL_SHADOW);
        GuiDraw.drawRect(x + width - 1, y + 1, 1, height - 1, PLAIN_WELL_LIGHT);
        GuiDraw.drawRect(x + 1, y + height - 1, width - 1, 1, PLAIN_WELL_LIGHT);
    }

    /**
     * Returns the color of text in a list row: the skin's text color, or white in the plain look.
     *
     * @return ARGB color
     */
    public int listText() {
        return variant == null ? PLAIN_TEXT : variant.colors.text();
    }

    /**
     * Returns the color of a list row's detail on its right: the skin's secondary text color, or white in the plain
     * look.
     *
     * @return ARGB color
     */
    public int listDetail() {
        return variant == null ? PLAIN_TEXT : variant.colors.secondary();
    }

    /**
     * Returns the color of section headers and of rows that cannot be used, drawn without a shadow: the skin's muted
     * color, or Minecraft's dark grey in the plain look.
     *
     * @return ARGB color
     */
    public int listMuted() {
        return variant == null ? PLAIN_MUTED : variant.colors.muted();
    }

    /**
     * Reports whether list text other than headers and unavailable rows is drawn with a shadow, as Minecraft draws
     * light text on its grey slots; only the plain look does.
     *
     * @return {@code true} in the plain look
     */
    public boolean listShadow() {
        return variant == null;
    }

    /**
     * Draws the background of a selected list row: AE2's selected row under an AE2 skin, a dark grey bar in the plain
     * look. Client only.
     *
     * @param x      left edge
     * @param y      top edge
     * @param width  row width in pixels
     * @param height row height in pixels
     */
    public void drawSelectedRow(int x, int y, int width, int height) {
        if (variant != null) variant.listRowSelected.draw(x, y, width, height);
        else GuiDraw.drawRect(x, y, width, height, PLAIN_SELECTED);
    }

    /**
     * Returns the color of text on a selected row: the skin's text-on-selection color, or white in the plain look.
     *
     * @return ARGB color
     */
    public int selectedText() {
        return variant == null ? PLAIN_TEXT : variant.highlightText;
    }

    /**
     * Returns the color laid over a list row under the mouse: a faint shade that darkens a light skin and lightens a
     * dark one, or a faint white in the plain look.
     *
     * @return ARGB color
     */
    public int hover() {
        if (variant == null) return PLAIN_HOVER;
        return variant.dark ? DARK_SHADE : LIGHT_SHADE;
    }

    /**
     * Returns the color laid under nested rows, such as the options a list dropdown opens.
     *
     * @return ARGB color
     */
    public int nested() {
        return variant == null ? PLAIN_NESTED : hover();
    }

    /**
     * Returns the color of a row marked good, such as enough of a material.
     *
     * @return ARGB color
     */
    public int good() {
        return variant == null ? PLAIN_GOOD : variant.colors.good();
    }

    /**
     * Returns the color of a row marked bad, such as too little of a material.
     *
     * @return ARGB color
     */
    public int bad() {
        return variant == null ? PLAIN_BAD : variant.colors.bad();
    }

    /**
     * Returns the color of the thin scroll bar that lists with a look of their own draw in the plain look, the face
     * color of Minecraft's scroll handle.
     *
     * @return ARGB color
     */
    public static int plainScrollBar() {
        return PLAIN_SCROLL_BAR;
    }

    private static AE2Look[] skinned() {
        AE2Variant[] variants = AE2Variant.values();
        AE2Look[] looks = new AE2Look[variants.length];
        for (AE2Variant variant : variants) looks[variant.ordinal()] = new AE2Look(variant);
        return looks;
    }

    /** Theme data naming the AE2 skin a theme uses. */
    public static class Marker extends WidgetTheme {

        /** JSON key holding the skin folder name. */
        public static final String SKIN = "skin";
        private final String skin;
        private final @Nullable AE2Variant variant;

        /**
         * Creates the entry of a theme.
         *
         * @param skin skin folder name, such as {@code ae2_dark}, or an empty string for the plain look
         */
        public Marker(String skin) {
            super(0, 0, null, Color.WHITE.main, Color.WHITE.main, false, Color.WHITE.main);
            this.skin = skin;
            variant = AE2Look.skin(skin);
        }

        /**
         * Reads the entry of a theme from its theme data, keeping the parent's skin when the data names none.
         *
         * @param parent   entry of the parent theme
         * @param json     this entry's theme data
         * @param fallback the theme's top-level data, or {@code null}
         */
        public Marker(Marker parent, JsonObject json, JsonObject fallback) {
            super(parent, json, fallback);
            skin = JsonHelper.getString(json, parent.skin, SKIN);
            variant = AE2Look.skin(skin);
        }

        /**
         * Returns the skin folder name.
         *
         * @return skin folder name, or an empty string for the plain look
         */
        public String skin() {
            return skin;
        }

        /**
         * Returns the AE2 skin the folder name stands for, found once when the theme is read.
         *
         * @return the skin, or {@code null} for the plain look or an unknown name
         */
        public @Nullable AE2Variant variant() {
            return variant;
        }

        private static WidgetThemeKey<Marker> register() {
            Marker plain = new Marker("");
            return IThemeApi.get()
                .widgetThemeKeyBuilder("gtnllibAE2Look", Marker.class)
                .defaultTheme(plain)
                .defaultHoverTheme(plain)
                .parser(Marker::new)
                .register();
        }

        /** Writes the entry into a theme definition. */
        public static class Builder extends WidgetThemeBuilder<Marker, Builder> {

            /**
             * Names the skin.
             *
             * @param folder skin folder name, such as {@code ae2_dark}
             * @return this builder
             */
            public Builder skin(String folder) {
                add(SKIN, folder);
                return this;
            }
        }
    }
}

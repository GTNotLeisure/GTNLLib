package com.gtnllib.gui.theme;

import org.jetbrains.annotations.Nullable;

public enum ScreenTheme {

    /**
     * Uses the screen's default MUI2 theme.
     */
    DEFAULT(null),
    /**
     * Uses the light AE2 panel theme.
     */
    AE2_LIGHT(AE2Theme.LIGHT_ID),
    /**
     * Uses the dark AE2 panel theme.
     */
    AE2_DARK(AE2Theme.DARK_ID),
    /**
     * Uses the dark AE2 panel theme with purple selection highlights.
     */
    AE2_DARK_PURPLE(AE2Theme.DARK_PURPLE_ID);

    /**
     * Registered theme identifier, or {@code null} when no theme override is requested.
     */
    public final @Nullable String themeId;

    ScreenTheme(@Nullable String themeId) {
        this.themeId = themeId;
    }
}

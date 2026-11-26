package com.gtnllib.gui.theme.ae2;

import static com.gtnllib.gui.theme.AE2Theme.ICON_SIZE;
import static com.gtnllib.gui.theme.AE2Theme.SHEET;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

public enum AE2Icon implements IconSource {

    /**
     * Copy action.
     */
    COPY(192, 0, GuiTextures.COPY),
    /**
     * Additional actions.
     */
    MORE(16, 0, GuiTextures.MORE),
    /**
     * Refresh action.
     */
    REFRESH(240, 0, GuiTextures.REFRESH),
    /**
     * Visibility control.
     */
    VISIBLE(16, 32, GuiTextures.VISIBLE),
    /**
     * Start or resume action.
     */
    PLAY(48, 32, GuiTextures.PLAY),
    /**
     * Pause action.
     */
    PAUSE(64, 32, GuiTextures.PAUSE),
    /**
     * Move-to action.
     */
    MOVE_TO(224, 32, GuiTextures.MOVE_TO),
    /**
     * Search action.
     */
    SEARCH(160, 48, GuiTextures.SEARCH),
    /**
     * Confirmation action.
     */
    CHECKMARK(176, 48, GuiTextures.CHECKMARK),
    /**
     * Warning, such as a problem to look at.
     */
    EXCLAMATION(16, 48, GuiTextures.EXCLAMATION),
    /**
     * Settings action.
     */
    GEAR(0, 0, GuiTextures.GEAR),
    /**
     * Add action.
     */
    ADD(64, 0, GuiTextures.ADD),
    /**
     * Remove action.
     */
    REMOVE(96, 0, GuiTextures.REMOVE),
    /**
     * Close action.
     */
    CLOSE(112, 16, GuiTextures.CLOSE),
    /**
     * Edit action.
     */
    EDIT(80, 16, GuiTextures.EDIT),
    /**
     * Filter control.
     */
    FILTER(128, 0, GuiTextures.FILTER),
    /**
     * Hidden state of a visibility control.
     */
    INVISIBLE(32, 32, GuiTextures.INVISIBLE),
    /**
     * Upward arrow from a bar, such as raising something.
     */
    UPLOAD(16, 16, GuiTextures.UPLOAD),
    /**
     * Downward arrow onto a bar, such as lowering something.
     */
    DOWNLOAD(0, 16, GuiTextures.DOWNLOAD),
    /**
     * Two opposite horizontal arrows, such as mirroring.
     */
    REVERSE(224, 16, GuiTextures.REVERSE),
    /**
     * A block or cube.
     */
    BLOCK(240, 16, GuiTextures.BLOCK),
    /**
     * Turning arrow, such as a quarter turn.
     */
    SHIFT_BACKWARD(208, 32, GuiTextures.SHIFT_BACKWARD),
    /**
     * A page of text, such as a list.
     */
    FILE(80, 48, GuiTextures.FILE),
    /**
     * A chip, such as a machine part.
     */
    PROCESSOR(96, 48, GuiTextures.PROCESSOR),
    /**
     * Arrows pointing outwards, such as fitting a view to its content.
     */
    MAXIMIZE(80, 32, GuiTextures.MAXIMIZE),
    /**
     * Arrows pointing inwards, such as turning around a centre.
     */
    MINIMIZE(96, 32, GuiTextures.MINIMIZE),
    /**
     * Plain upward arrow, such as raising by one step.
     */
    ARROW_UP(0, 64, GuiTextures.ARROW_UP),
    /**
     * Plain downward arrow, such as lowering by one step.
     */
    ARROW_DOWN(16, 64, GuiTextures.ARROW_DOWN),
    /**
     * Two opposite vertical arrows, such as flipping upside down.
     */
    FLIP_VERTICAL(32, 64, null),
    /**
     * {@link #MOVE_TO} pointing up, such as a face turned upwards.
     */
    MOVE_TO_UP(48, 64, null),
    /**
     * {@link #MOVE_TO} pointing down, such as a face turned downwards.
     */
    MOVE_TO_DOWN(64, 64, null);

    /**
     * Source coordinates within the AE2 icon sheet, in pixels.
     */
    public final int x, y;
    /**
     * MUI2 fallback texture used outside AE2 themes, or {@code null} to use the light AE2 sprite there.
     */
    public final @Nullable UITexture vanilla;

    AE2Icon(int x, int y, @Nullable UITexture vanilla) {
        this.x = x;
        this.y = y;
        this.vanilla = vanilla;
    }

    /**
     * Resolves this icon's texture in a look, caching the AE2 sprite for each skin.
     *
     * @param look look to draw in; the plain look gives the MUI2 fallback, or for an icon MUI2 has no equivalent for
     *             the light AE2 sprite, which is white like MUI2's icons
     * @return icon texture
     */
    public UITexture texture(AE2Look look) {
        AE2Variant variant = look.variant();
        if (variant == null) {
            if (vanilla != null) return vanilla;
            variant = AE2Variant.LIGHT;
        }
        return variant.sprite("icons", SHEET, x, y, ICON_SIZE, ICON_SIZE);
    }

    @Override
    public void drawIcon(GuiContext context, WidgetTheme theme, AE2Look look, int x, int y, boolean enabled) {
        AE2Draw.drawSprite(texture(look), x, y, enabled);
    }
}

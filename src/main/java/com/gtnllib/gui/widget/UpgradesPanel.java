package com.gtnllib.gui.widget;

import static com.gtnllib.gui.theme.AE2Theme.SLOT_SIZE;
import static com.gtnllib.gui.theme.AE2Theme.UPGRADE_OVERLAP;
import static com.gtnllib.gui.theme.AE2Theme.UPGRADE_SLOTS_PER_COLUMN;

import java.util.ArrayList;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Unit.Measure;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.AE2PanelPainter;
import com.gtnllib.gui.theme.ae2.AE2UpgradePainter;
import com.gtnllib.gui.theme.ae2.PlainLook;

/**
 * AE2's upgrade panel ({@code UpgradesPanel}): a strip of upgrade slots attached to the right of a panel. Add the slots
 * with {@link #slot(Widget)}, usually MUI2 {@code ItemSlot}s on the machine's upgrade inventory; the panel places them,
 * sizes itself to them and draws its background, in columns of {@link AE2Theme#UPGRADE_SLOTS_PER_COLUMN} slots from the
 * top. Place it at {@code panelRight - AE2Theme.UPGRADE_OVERLAP} and add it after the panel's other children.
 * <p>
 * Under AE2 themes it is AE2's panel ({@link AE2UpgradePainter}): it has no left border and covers the panel's right
 * border, and it draws the slot wells itself, so it turns off the slots' own theme frames. Other themes draw the panel
 * background of the theme without its left border, joined to the panel the same way, with the slots' own frames; the
 * slots then start at the panel's right edge, two pixels further right than under AE2 themes. With no slots nothing is
 * drawn, as in AE2. The panel stands outside its panel, so it tells NEI to keep its panels clear of it. It can be built
 * on either side, so it also works in GUIs the server opens.
 */
public class UpgradesPanel extends ParentWidget<UpgradesPanel> {

    /** Pixels the slots and the right edge move right outside AE2 themes, where the background hides a wider border. */
    public static final int PLAIN_SHIFT = 2;
    private final IDrawable plainPanel = AE2PanelPainter.panelBackground(this, 0, 0);
    private final List<Widget<?>> slots = new ArrayList<>();
    private int perColumn = UPGRADE_SLOTS_PER_COLUMN;

    /**
     * Creates an empty upgrade panel; add slots with {@link #slot(Widget)}.
     */
    public UpgradesPanel() {
        width(() -> AE2UpgradePainter.upgradePanelWidth(slots.size(), perColumn) + shift(), Measure.PIXEL)
            .height(() -> AE2UpgradePainter.upgradePanelHeight(slots.size(), perColumn), Measure.PIXEL)
            .excludeAreaInRecipeViewer();
    }

    /**
     * Adds a slot below the existing ones, starting a new column after every {@link #perColumn(int) perColumn} slots.
     * The slot is sized 18 by 18 pixels.
     *
     * @param slot slot widget, not added anywhere else
     * @return this panel
     */
    public UpgradesPanel slot(Widget<?> slot) {
        int index = slots.size();
        slots.add(slot);
        child(
            slot.left(() -> AE2UpgradePainter.upgradeSlotX(index, perColumn) + shift(), Measure.PIXEL)
                .top(() -> AE2UpgradePainter.upgradeSlotY(index, perColumn), Measure.PIXEL)
                .size(SLOT_SIZE, SLOT_SIZE));
        return this;
    }

    /**
     * Sets how many slots a column holds before the next column starts; AE2's panels hold
     * {@link AE2Theme#UPGRADE_SLOTS_PER_COLUMN}. Use 1 for a single row.
     *
     * @param perColumn slots per column, at least one
     * @return this panel
     */
    public UpgradesPanel perColumn(int perColumn) {
        this.perColumn = Math.max(1, perColumn);
        if (isValid()) scheduleResize();
        return this;
    }

    private int shift() {
        return AE2Look.of(this)
            .isAE2() ? 0 : PLAIN_SHIFT;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        AE2Look look = AE2Look.of(this);
        for (Widget<?> slot : slots) slot.disableThemeBackground(look.isAE2());
        if (slots.isEmpty()) return;
        if (look.isAE2()) {
            AE2UpgradePainter.drawUpgradePanel(look.variant(), 0, 0, slots.size(), perColumn);
            return;
        }
        int width = getArea().width, height = getArea().height;
        Stencil.apply(-UPGRADE_OVERLAP, 0, width + UPGRADE_OVERLAP, height, context);
        plainPanel.draw(
            context,
            -UPGRADE_OVERLAP - PlainLook.PANEL_BORDER,
            0,
            width + UPGRADE_OVERLAP + PlainLook.PANEL_BORDER,
            height,
            widgetTheme.getTheme());
        Stencil.remove();
    }
}

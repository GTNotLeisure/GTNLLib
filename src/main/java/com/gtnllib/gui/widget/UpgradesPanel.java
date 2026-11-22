package com.gtnllib.gui.widget;

import static com.gtnllib.gui.theme.AE2Theme.SLOT_SIZE;
import static com.gtnllib.gui.theme.AE2Theme.UPGRADE_OVERLAP;
import static com.gtnllib.gui.theme.AE2Theme.UPGRADE_SLOTS_PER_COLUMN;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.INotifyEnabled;
import com.cleanroommc.modularui.api.widget.IWidget;
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

public class UpgradesPanel extends ParentWidget<UpgradesPanel> implements INotifyEnabled {

    /**
     * Pixels the slots and the right edge move right outside AE2 themes, where the background hides a wider border.
     */
    public static final int PLAIN_SHIFT = 2;
    private final IDrawable plainPanel = AE2PanelPainter.panelBackground(this, 0, 0);
    private int perColumn = UPGRADE_SLOTS_PER_COLUMN, shown;

    /**
     * Creates an empty upgrade panel; add slots with {@link #slot(Widget)}.
     */
    public UpgradesPanel() {
        width(() -> shown == 0 ? 0 : AE2UpgradePainter.upgradePanelWidth(shown, perColumn) + shift(), Measure.PIXEL)
            .height(() -> shown == 0 ? 0 : AE2UpgradePainter.upgradePanelHeight(shown, perColumn), Measure.PIXEL);
    }

    /**
     * Adds a slot below the existing ones, starting a new column after every {@link #perColumn(int) perColumn} slots.
     * The slot is sized 18 by 18 pixels.
     *
     * @param slot slot widget, not added anywhere else
     */
    public UpgradesPanel slot(Widget<?> slot) {
        child(slot.size(SLOT_SIZE, SLOT_SIZE));
        return this;
    }

    @Override
    public boolean addChild(IWidget child, int index) {
        boolean changed = super.addChild(child, index);
        if (changed) layoutSlots();
        return changed;
    }

    @Override
    public boolean remove(IWidget child) {
        boolean changed = super.remove(child);
        if (changed) layoutSlots();
        return changed;
    }

    @Override
    public boolean remove(int index) {
        boolean changed = super.remove(index);
        if (changed) layoutSlots();
        return changed;
    }

    @Override
    public boolean removeAll() {
        boolean changed = super.removeAll();
        if (changed) layoutSlots();
        return changed;
    }

    @Override
    public void onInit() {
        super.onInit();
        layoutSlots();
    }

    @Override
    public void onChildChangeEnabled(IWidget child, boolean enabled) {
        layoutSlots();
    }

    private void layoutSlots() {
        int index = 0;
        for (IWidget slot : getChildren()) {
            if (!slot.isEnabled()) continue;
            int place = index++;
            slot.resizer()
                .left(() -> AE2UpgradePainter.upgradeSlotX(place, perColumn) + shift(), Measure.PIXEL)
                .top(() -> AE2UpgradePainter.upgradeSlotY(place, perColumn), Measure.PIXEL);
        }
        shown = index;
        updateExclusion();
        if (isValid()) scheduleResize();
    }

    private void updateExclusion() {
        boolean exclude = shown > 0;
        if (exclude == isExcludeAreaInRecipeViewer()) return;
        excludeAreaInRecipeViewer(exclude);
        // MUI2 adds a live widget to NEI's list whenever this flag is set, even to false.
        if (!exclude && isValid()) getContext().getRecipeViewerSettings()
            .removeExclusionArea(this);
    }

    /**
     * Sets how many slots a column holds before the next column starts; AE2's panels hold
     * {@link AE2Theme#UPGRADE_SLOTS_PER_COLUMN}. Use 1 for a single row.
     *
     * @param perColumn slots per column, at least one
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
        for (IWidget child : getChildren()) {
            if (child instanceof Widget<?>slot) slot.disableThemeBackground(look.isAE2())
                .disableHoverThemeBackground(look.isAE2());
        }
        if (shown == 0) return;
        if (look.isAE2()) {
            AE2UpgradePainter.drawUpgradePanel(look.variant(), 0, 0, shown, perColumn);
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

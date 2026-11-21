package com.gtnllib.client.gallery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.LongValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.gtnllib.GTNLLib;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2Icon;
import com.gtnllib.gui.theme.ae2.AE2SettingIcon;
import com.gtnllib.gui.widget.AE2Button;
import com.gtnllib.gui.widget.AECheckbox;
import com.gtnllib.gui.widget.CloseButton;
import com.gtnllib.gui.widget.CycleDropdown;
import com.gtnllib.gui.widget.Divider;
import com.gtnllib.gui.widget.FadingNotice;
import com.gtnllib.gui.widget.FillBar;
import com.gtnllib.gui.widget.GTNLListWidget;
import com.gtnllib.gui.widget.GhostItemSlot;
import com.gtnllib.gui.widget.HandleScrollData;
import com.gtnllib.gui.widget.IconButton;
import com.gtnllib.gui.widget.IconRowList;
import com.gtnllib.gui.widget.ListRow;
import com.gtnllib.gui.widget.NumberEntryWidget;
import com.gtnllib.gui.widget.PageTabs;
import com.gtnllib.gui.widget.ProgressBar;
import com.gtnllib.gui.widget.Scrollbar;
import com.gtnllib.gui.widget.SettingToggleButton;
import com.gtnllib.gui.widget.SlideSwitch;
import com.gtnllib.gui.widget.StepSlider;
import com.gtnllib.gui.widget.TabButton;
import com.gtnllib.gui.widget.UpgradesPanel;
import com.gtnllib.gui.widget.ValueStepper;
import com.gtnllib.gui.widget.VerticalButtonBar;

/**
 * A client-only screen with every AE2-styled widget of GTNLLib, all working, opened by {@code /gtnl_widgets}. The
 * buttons on top reopen it in each AE2 skin or the plain look, keeping the widgets' values; the dialog button opens a
 * panel in another theme. A check box hides one toolbar button and all upgrade slots, so the toolbar shortens and the
 * empty upgrade panel no longer asks NEI to keep clear. Client only.
 */
public class WidgetGallery {

    private static final String LANG = "gtnllib.widget_gallery.";
    private static final int WIDTH = 300, HEIGHT = 214, PAGE_X = 8, PAGE_Y = 50, PAGE_WIDTH = 284, PAGE_HEIGHT = 156,
        NOTICE_RGB = 0x55FF55, DIALOG_WIDTH = 180;
    private static final String[] TIERS = { "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV" };
    private static final String[] THEMES = { AE2Theme.LIGHT_ID, AE2Theme.DARK_ID, AE2Theme.DARK_PURPLE_ID, null };
    private static final String[] THEME_KEYS = { "theme.light", "theme.dark", "theme.purple", "theme.plain" };

    private final State state;
    private final @Nullable String themeId;
    private final FadingNotice notice = new FadingNotice();

    private WidgetGallery(State state, @Nullable String themeId) {
        this.state = state;
        this.themeId = themeId;
    }

    /**
     * Opens the gallery in the light AE2 theme.
     */
    public static void open() {
        open(new State(), AE2Theme.LIGHT_ID);
    }

    private static void open(State state, @Nullable String themeId) {
        ModularScreen screen = new ModularScreen(GTNLLib.MODID, new WidgetGallery(state, themeId).panel());
        GhostItemSlot.open(themeId == null ? screen : screen.useTheme(themeId));
    }

    private ModularPanel panel() {
        ModularPanel panel = ModularPanel.defaultPanel("gtnllib_widget_gallery", WIDTH, HEIGHT);
        for (int i = 0; i < THEMES.length; i++) {
            String id = THEMES[i], key = THEME_KEYS[i];
            panel.child(
                new AE2Button(() -> tr(key), () -> open(state, id)).selected(() -> Objects.equals(themeId, id))
                    .pos(8 + i * 58, 6)
                    .size(56, 18));
        }
        IPanelHandler dialog = IPanelHandler.simple(panel, (parent, player) -> dialog(), true);
        panel.child(
            new AE2Button(() -> tr("dialog"), dialog::openPanel).pos(242, 6)
                .size(50, 18))
            .child(
                new AECheckbox(
                    () -> tr("available"),
                    new BoolValue.Dynamic(() -> state.usable, on -> state.usable = on)).pos(8, 30))
            .child(
                new AECheckbox(() -> tr("hide"), new BoolValue.Dynamic(() -> state.hide, on -> state.hide = on))
                    .pos(120, 30))
            .child(
                new Divider().pos(8, 44)
                    .size(PAGE_WIDTH, Divider.HEIGHT))
            .child(
                notice.pos(PAGE_X, HEIGHT - 18)
                    .size(PAGE_WIDTH, 12));
        PageTabs tabs = new PageTabs(() -> state.page, page -> state.page = page)
            .tab(() -> AE2Icon.GEAR, () -> tr("page.buttons"))
            .tab(() -> AE2SettingIcon.PRIORITY, () -> tr("page.inputs"))
            .tab(() -> AE2Icon.FILE, () -> tr("page.lists"));
        return panel.child(tabs.page(0, buttonsPage()))
            .child(tabs.page(1, inputsPage()))
            .child(tabs.page(2, listsPage()))
            .child(toolbar())
            .child(upgrades())
            .child(tabs.pos(WIDTH - AE2Theme.PAGE_TAB_INSET, 80));
    }

    private ParentWidget<?> page() {
        return new ParentWidget<>().pos(PAGE_X, PAGE_Y)
            .size(PAGE_WIDTH, PAGE_HEIGHT - 20);
    }

    private ParentWidget<?> buttonsPage() {
        ParentWidget<?> page = page();
        page.child(
            new AE2Button(() -> "AE2Button", () -> show("AE2Button")).available(() -> state.usable)
                .pos(0, 0)
                .size(70, 20))
            .child(
                new AE2Button(() -> "selected", () -> state.selected = !state.selected).selected(() -> state.selected)
                    .available(() -> state.usable)
                    .pos(74, 0)
                    .size(70, 20))
            .child(
                new IconButton(() -> AE2Icon.ADD, () -> show("IconButton")).available(() -> state.usable)
                    .pos(150, 0))
            .child(
                new IconButton(() -> AE2Icon.REFRESH, () -> show("IconButton")).available(() -> state.usable)
                    .pos(170, 0))
            .child(
                new IconButton(() -> AE2SettingIcon.CRAFT_HAMMER, () -> show("IconButton"))
                    .selected(() -> state.selected)
                    .available(() -> state.usable)
                    .pos(190, 0))
            .child(
                new TabButton(() -> AE2SettingIcon.BACK, () -> show("TabButton")).available(() -> state.usable)
                    .pos(214, 0))
            .child(
                new SettingToggleButton().value(new IntValue.Dynamic(() -> state.redstone, v -> state.redstone = v))
                    .stateCount(3)
                    .title(IKey.str("SettingToggleButton"))
                    .state(0, AE2SettingIcon.REDSTONE_IGNORE, IKey.str("REDSTONE_IGNORE"))
                    .state(1, AE2SettingIcon.REDSTONE_LOW, IKey.str("REDSTONE_LOW"))
                    .state(2, AE2SettingIcon.REDSTONE_HIGH, IKey.str("REDSTONE_HIGH"))
                    .available(() -> state.usable)
                    .pos(0, 28))
            .child(
                new SettingToggleButton().value(new BoolValue.Dynamic(() -> state.fuzzy, on -> state.fuzzy = on))
                    .title(IKey.str("SettingToggleButton"))
                    .state(true, AE2SettingIcon.FUZZY_PERCENT_99, IKey.str("FUZZY_PERCENT_99"))
                    .state(false, AE2SettingIcon.FUZZY_IGNORE, IKey.str("FUZZY_IGNORE"))
                    .available(() -> state.usable)
                    .pos(22, 28))
            .child(
                new SlideSwitch().value(() -> state.slide)
                    .label(IKey.str("SlideSwitch"))
                    .onMousePressed(mouse -> {
                        state.slide = !state.slide;
                        return true;
                    })
                    .available(() -> state.usable)
                    .pos(50, 30)
                    .size(100, 14))
            .child(
                new AECheckbox(() -> "AECheckbox", new BoolValue.Dynamic(() -> state.checked, on -> state.checked = on))
                    .available(() -> state.usable)
                    .pos(0, 56));
        IntValue.Dynamic radio = new IntValue.Dynamic(() -> state.radio, v -> state.radio = v);
        for (int i = 0; i < 3; i++) page.child(
            AECheckbox.radio(() -> "AECheckbox.radio", radio, i)
                .available(() -> state.usable)
                .pos(0, 74 + i * 16));
        return page;
    }

    private ParentWidget<?> inputsPage() {
        CycleDropdown<Integer> dropdown = new CycleDropdown<>("gtnllib_gallery_dropdown", Integer.class).wheel(true)
            .available(() -> state.usable);
        dropdown.optionToWidget(
            (value, selected) -> IKey.str(TIERS[value])
                .asWidget())
            .value(new IntValue.Dynamic(() -> state.tier, v -> state.tier = v));
        dropdown.choices(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7));
        DoubleValue.Dynamic progress = new DoubleValue.Dynamic(() -> state.progress, v -> state.progress = v);
        return page().child(
            new NumberEntryWidget().bounds(0, 1_000_000)
                .value(new LongValue.Dynamic(() -> state.amount, v -> state.amount = v))
                .available(() -> state.usable)
                .pos(0, 0))
            .child(
                new NumberEntryWidget().bounds(0, 64)
                    .value(new LongValue.Dynamic(() -> state.small, v -> state.small = v))
                    .wheel(true)
                    .available(() -> state.usable)
                    .pos(150, 0)
                    .size(64, 16))
            .child(
                new ValueStepper(
                    () -> TIERS[state.tier],
                    step -> state.tier = Math.floorMod(state.tier + step, TIERS.length)).wheel(true)
                        .available(() -> state.usable)
                        .pos(150, 22)
                        .size(84, 16))
            .child(
                dropdown.pos(150, 44)
                    .size(84, 18))
            .child(
                new StepSlider().value(progress)
                    .range(0, 1, 10)
                    .wheel(true)
                    .available(() -> state.usable)
                    .pos(0, 70)
                    .size(138, 16))
            .child(
                new ProgressBar().value(progress)
                    .pos(150, 68))
            .child(
                new FillBar().value(() -> state.progress)
                    .pos(162, 74)
                    .size(72, 6))
            .child(
                new Divider().pos(0, 94)
                    .size(PAGE_WIDTH - 12, Divider.HEIGHT))
            .child(
                new Scrollbar(() -> state.scroll, () -> 9, row -> state.scroll = row).pos(0, 100)
                    .height(36))
            .child(
                new Scrollbar(() -> state.scroll, () -> 9, row -> state.scroll = row).small(true)
                    .pos(18, 100)
                    .height(36))
            .child(
                IKey.dynamic(() -> "Scrollbar " + state.scroll)
                    .asWidget()
                    .pos(32, 112));
    }

    private ParentWidget<?> listsPage() {
        ListWidget<IWidget, ?> list = new GTNLListWidget<>().scrollDirection(new HandleScrollData(true))
            .pos(156, 0)
            .size(116, 110);
        list.child(
            ListRow.header(() -> "ListRow")
                .widthRel(1f));
        for (int i = 0; i < 6; i++) {
            int index = i;
            list.child(
                new ListRow(() -> tr("option", index + 1))
                    .checkbox(() -> state.rows[index], () -> state.rows[index] = !state.rows[index])
                    .available(() -> state.usable || index % 2 == 0)
                    .widthRel(1f));
        }
        list.child(
            new ListRow(() -> "ValueStepper")
                .control(
                    new ValueStepper(
                        () -> TIERS[state.tier],
                        step -> state.tier = Math.floorMod(state.tier + step, TIERS.length)),
                    60,
                    16)
                .widthRel(1f));
        return page().child(
            new IconRowList(this::rows).pos(0, 0)
                .size(150, 110))
            .child(list);
    }

    private List<IconRowList.Row> rows() {
        List<IconRowList.Row> rows = new ArrayList<>();
        rows.add(IconRowList.Row.header("IconRowList"));
        rows.add(
            IconRowList.Row.item(
                new ItemStack(Items.iron_ingot),
                new ItemStack(Items.iron_ingot).getDisplayName(),
                "x64",
                state.selectedRow == 0,
                () -> state.selectedRow = 0));
        rows.add(
            IconRowList.Row.item(
                new ItemStack(Items.gold_ingot),
                new ItemStack(Items.gold_ingot).getDisplayName(),
                "x16",
                state.selectedRow == 1,
                () -> state.selectedRow = 1));
        rows.add(
            IconRowList.Row.colored(new ItemStack(Items.diamond), "GOOD", "64 / 64", IconRowList.GOOD)
                .withAvailable(state.usable));
        rows.add(IconRowList.Row.colored(new ItemStack(Items.redstone), "BAD", "3 / 64", IconRowList.BAD));
        rows.add(IconRowList.Row.checkbox("checkbox", state.rowChecked, () -> state.rowChecked = !state.rowChecked));
        List<IconRowList.Row> options = new ArrayList<>();
        for (int i = 0; i < TIERS.length; i++) {
            int tier = i;
            options.add(IconRowList.Row.item(null, TIERS[i], "", state.tier == i, () -> state.tier = tier));
        }
        rows.add(
            IconRowList.Row.dropdown(null, "dropdown", TIERS[state.tier], false, options)
                .withKey("tier"));
        rows.add(
            IconRowList.Row.number("number", state.small, 0, 64, 0, v -> state.small = v)
                .withAvailable(state.usable));
        return rows;
    }

    private VerticalButtonBar toolbar() {
        VerticalButtonBar bar = new VerticalButtonBar()
            .button(() -> AE2Icon.REFRESH, () -> "VerticalButtonBar.button", () -> show("VerticalButtonBar"))
            .toggle(
                () -> state.visible ? AE2Icon.VISIBLE : AE2Icon.INVISIBLE,
                () -> "VerticalButtonBar.toggle",
                () -> state.visible,
                () -> state.visible = !state.visible)
            .add(
                new SettingToggleButton().value(new IntValue.Dynamic(() -> state.access, v -> state.access = v))
                    .stateCount(3)
                    .title(IKey.str("VerticalButtonBar.add"))
                    .state(0, AE2SettingIcon.ACCESS_READ_WRITE, IKey.str("ACCESS_READ_WRITE"))
                    .state(1, AE2SettingIcon.ACCESS_READ, IKey.str("ACCESS_READ"))
                    .state(2, AE2SettingIcon.ACCESS_WRITE, IKey.str("ACCESS_WRITE")))
            .available(() -> state.usable);
        IconButton hidden = new IconButton(() -> AE2Icon.GEAR, () -> show(tr("hidden")));
        hidden.setEnabledIf(button -> !state.hide);
        return bar.add(hidden)
            .button(() -> AE2SettingIcon.HELP, () -> "VerticalButtonBar.button", () -> show("VerticalButtonBar"))
            .pos(-VerticalButtonBar.WIDTH + VerticalButtonBar.OVERLAP, 8);
    }

    private UpgradesPanel upgrades() {
        UpgradesPanel panel = new UpgradesPanel().perColumn(2);
        for (int i = 0; i < 3; i++) panel.slot(
            new GhostItemSlot().clearable(true)
                .setEnabledIf(slot -> !state.hide));
        return panel.pos(WIDTH - AE2Theme.UPGRADE_OVERLAP, 6);
    }

    private ModularPanel dialog() {
        String other = themeId == null ? AE2Theme.DARK_ID : "vanilla";
        CycleDropdown<Integer> dropdown = new CycleDropdown<>("gtnllib_gallery_dialog_dropdown", Integer.class);
        dropdown.optionToWidget(
            (value, selected) -> IKey.str(TIERS[value])
                .asWidget())
            .value(new IntValue.Dynamic(() -> state.tier, v -> state.tier = v));
        dropdown.choices(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7));
        ModularPanel dialog = ModularPanel.defaultPanel("gtnllib_widget_gallery_dialog", DIALOG_WIDTH, 100);
        dialog.themeOverride(other);
        return dialog.child(
            IKey.str(tr("dialog.title"))
                .asWidget()
                .pos(8, 8))
            .child(new CloseButton().pos(DIALOG_WIDTH - 4 - CloseButton.SIZE, 4))
            .child(
                TabButton.back(dialog::closeIfOpen)
                    .pos(DIALOG_WIDTH - 56, TabButton.BACK_TOP))
            .child(
                dropdown.pos(8, 24)
                    .size(80, 18))
            .child(
                new AECheckbox(() -> "AECheckbox", new BoolValue.Dynamic(() -> state.checked, on -> state.checked = on))
                    .pos(8, 50))
            .child(new IconButton(() -> AE2Icon.ADD, () -> show("IconButton")).pos(96, 24))
            .child(
                new NumberEntryWidget().bounds(0, 64)
                    .value(new LongValue.Dynamic(() -> state.small, v -> state.small = v))
                    .pos(8, 70)
                    .size(64, 16));
    }

    private void show(String text) {
        notice.show(text, NOTICE_RGB);
    }

    private static String tr(String key, Object... args) {
        return StatCollector.translateToLocalFormatted(LANG + key, args);
    }

    private static class State {

        private boolean usable = true, hide, selected, fuzzy, slide, checked, rowChecked, visible = true;
        private int page, redstone, radio, access, tier, scroll, selectedRow;
        private long amount = 64, small = 8;
        private double progress = 0.4;
        private final boolean[] rows = new boolean[6];
    }
}

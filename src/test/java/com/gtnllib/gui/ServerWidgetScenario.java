package com.gtnllib.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.value.EnumValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.LongValue;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnllib.gui.layout.Layout;
import com.gtnllib.gui.settings.BlockPosFields;
import com.gtnllib.gui.settings.SettingControls;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.LayoutTheme;
import com.gtnllib.gui.theme.ScreenTheme;
import com.gtnllib.gui.theme.ae2.AE2ButtonPainter;
import com.gtnllib.gui.theme.ae2.AE2CheckboxPainter;
import com.gtnllib.gui.theme.ae2.AE2Icon;
import com.gtnllib.gui.theme.ae2.AE2Legacy;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.AE2PanelPainter;
import com.gtnllib.gui.theme.ae2.AE2SettingIcon;
import com.gtnllib.gui.theme.ae2.AE2Variant;
import com.gtnllib.gui.theme.ae2.IconSource;
import com.gtnllib.gui.wheel.WheelStyle;
import com.gtnllib.gui.widget.AE2Button;
import com.gtnllib.gui.widget.AECheckbox;
import com.gtnllib.gui.widget.Breadcrumb;
import com.gtnllib.gui.widget.CloseButton;
import com.gtnllib.gui.widget.CycleDropdown;
import com.gtnllib.gui.widget.Divider;
import com.gtnllib.gui.widget.FadingNotice;
import com.gtnllib.gui.widget.FillBar;
import com.gtnllib.gui.widget.GTNLListWidget;
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
import com.gtnllib.gui.widget.ThemedButton;
import com.gtnllib.gui.widget.UpgradesPanel;
import com.gtnllib.gui.widget.ValueStepper;
import com.gtnllib.gui.widget.VerticalButtonBar;

/**
 * Builds the AE2 widgets the way a GUI opened by the server builds them on the server, without drawing anything.
 * {@link ServerSideLoadingTest} runs it inside a {@link ServerClassLoader}.
 */
public class ServerWidgetScenario implements Runnable {

    @Override
    public void run() {
        for (ScreenTheme theme : ScreenTheme.values()) AE2Look.of(AE2Theme.variant(theme.themeId));
        AE2Look.skin(AE2Look.KEY.getFullName());
        new AE2Look.Marker.Builder().skin(AE2Variant.DARK.folder);
        AE2Variant light = AE2Theme.variant(AE2Theme.LIGHT_ID);
        ButtonWidget<?> plain = new ButtonWidget<>();
        plain.background(AE2ButtonPainter.button(plain, () -> true, () -> false))
            .overlay(AE2ButtonPainter.label(plain, () -> "Apply", () -> true, () -> false));
        AE2ButtonPainter.iconButton(plain, () -> AE2Icon.ADD, () -> true, () -> false);
        AE2ButtonPainter.tabButton(plain, () -> AE2Icon.GEAR, () -> true);
        AE2ButtonPainter.pageTab(plain, () -> AE2Icon.FILE, () -> true);
        AE2CheckboxPainter.checkbox(plain, () -> true, () -> "Auto", () -> true);
        AE2CheckboxPainter.radio(plain, () -> true, () -> "Tier", () -> true);
        AE2PanelPainter.listRow(plain, () -> false);
        AE2PanelPainter.well(plain, 60);
        AE2PanelPainter.panelBackground(plain, 0, 0);
        new AE2Button(() -> "Apply", () -> {}).available(() -> false)
            .selected(() -> false);
        new ThemedButton<>().available(() -> true);
        new IconButton(() -> AE2Icon.REFRESH, () -> {});
        new NumberEntryWidget().bounds(1, 64)
            .value(3)
            .available(() -> false);
        new NumberEntryWidget().size(64, 16);
        new ListRow(() -> "Row").checkbox(() -> true, () -> {})
            .available(() -> false);
        ListRow.header(() -> "Header");
        new VerticalButtonBar().button(() -> AE2Icon.GEAR, () -> "Settings", () -> {})
            .toggle(() -> AE2Icon.VISIBLE, () -> "Show", () -> true, () -> {});
        new ValueStepper(() -> "LV", step -> {});
        new GTNLListWidget<>().scrollDirection(
            new HandleScrollData(true).look(AE2Look.of(light))
                .rows(IconRowList.ROW));
        new HandleScrollData(null).look(AE2Look.plain());
        new CycleDropdown<>("menu", String.class).choices(Arrays.asList("a", "b"));
        List<IconRowList.Row> rows = Collections.singletonList(
            IconRowList.Row.checkbox("Auto", true, () -> {})
                .withAvailable(false));
        new IconRowList(() -> rows);
        new SlideSwitch().value(() -> true)
            .available(() -> false);
        new StepSlider().range(0, 1, 10)
            .vertical(true)
            .available(() -> false);
        new ValueStepper(() -> "MV", step -> {}).available(() -> false);
        new CycleDropdown<>("menu_off", String.class).available(() -> false);
        new VerticalButtonBar().button(() -> IconSource.of(IDrawable.EMPTY), () -> "Custom", () -> {})
            .available(() -> false);
        int[] openPage = { 0 };
        PageTabs tabs = new PageTabs(() -> openPage[0], page -> openPage[0] = page)
            .tab(() -> AE2Icon.GEAR, () -> "Settings")
            .tab(() -> AE2Icon.FILE, () -> "Plan")
            .available(() -> false);
        tabs.page(1, new ParentWidget<>());
        tabs.tabSize(30, 26);
        new VerticalButtonBar().button(() -> AE2Icon.ADD, () -> "Add", () -> {})
            .buttonSize(24, 24);
        new IconRowList(() -> rows).rowHeight(24)
            .numberWidth(48);
        new ListRow(() -> "Tall row").height(26)
            .control(new NumberEntryWidget(), 64, 16);
        int[] firstRow = { 0 };
        new Scrollbar(() -> firstRow[0], () -> 5, row -> firstRow[0] = row).step(2)
            .small(true)
            .plainBar(0xFF808090, () -> 3);
        new TabButton(() -> AE2Icon.GEAR, () -> {});
        new SettingToggleButton().value(new EnumValue<>(ScreenTheme.class, ScreenTheme.AE2_LIGHT))
            .title(IKey.str("Theme"))
            .state(ScreenTheme.AE2_LIGHT, AE2Icon.VISIBLE, IKey.str("Light"))
            .available(() -> false);
        new SettingToggleButton().value(new BoolValue(true))
            .state(true, AE2Icon.VISIBLE, IKey.str("Shown"))
            .state(false, AE2Icon.INVISIBLE, IKey.str("Hidden"));
        new SettingToggleButton().value(new IntValue(0))
            .stateCount(3)
            .state(2, AE2Icon.ADD);
        new ProgressBar().value(new DoubleValue(0.4));
        AE2Theme.sliderHandles(new ParentWidget<>().child(new SliderWidget()), AE2Theme.LIGHT_ID);
        AE2Theme.useMenuTheme(AE2Theme.DARK_ID, "gtnllib", "server_scenario", "menu");
        AE2Theme.polish(
            new ParentWidget<>().child(new ListWidget<>())
                .child(new SliderWidget()),
            AE2Look.of(AE2Variant.DARK),
            "gtnllib",
            "server_scenario");
        AE2Theme.menu(new ListWidget<>(), AE2Theme.DARK_ID);
        AE2Legacy.legacyStates(AE2Look.of(light));
        AE2Legacy.legacyTerminal(AE2Look.plain());
        WheelStyle.builder()
            .skin(light);
        AE2Look.plain()
            .scrollbarWidth();
        new ListRow(() -> "Stepper row").control(new ValueStepper(() -> "HV", step -> {}), 84, 16);
        new FillBar().value(() -> 0.5);
        new Divider().color(0xFF808080);
        new CloseButton();
        new Breadcrumb().segments(() -> Arrays.asList("Root", "Page"))
            .onSelect(depth -> {})
            .colors(0xFF404040, 0xFF1C4CA6, 0xFF777777);
        new FadingNotice().duration(2000)
            .background(0x80000000);
        Layout settings = Layout.rows(LayoutTheme.DEFAULT, 200);
        SettingControls.slider(null, Options.class, "volume", 0.1, volume -> String.valueOf(volume))
            .widget(settings, LayoutTheme.DEFAULT, 80, 24, () -> {});
        SettingControls.toggle(null, Options.class, "shown")
            .widget(settings, LayoutTheme.DEFAULT, 80, 24, () -> {});
        new BlockPosFields(0.5, 64, -3.5, 1000, 0, 255).x();
        new VerticalButtonBar().add(
            new SettingToggleButton().value(new BoolValue(true))
                .state(true, AE2SettingIcon.AUTO_EXPORT_ON, IKey.str("On"))
                .state(false, AE2SettingIcon.AUTO_EXPORT_OFF, IKey.str("Off")));
        new NumberEntryWidget().value(new LongValue(3));
        new AECheckbox(() -> "Auto", new BoolValue(true)).available(() -> false);
        AECheckbox.radio(() -> "First", new IntValue(0), 0);
        new Scrollbar(() -> 0, () -> 4, row -> {}).height(40);
        TabButton.back(() -> {})
            .pos(0, TabButton.BACK_TOP);
        new CloseButton().action(() -> {});
        new UpgradesPanel().slot(new ItemSlot())
            .slot(new ItemSlot())
            .perColumn(1);

        ModularPanel panel = ModularPanel.defaultPanel("server_scenario");
        panel.child(new AE2Button(() -> "Apply", () -> {}).size(40, 20))
            .child(new NumberEntryWidget())
            .child(new ListRow(() -> "Row").checkbox(() -> false, () -> {}))
            .child(new VerticalButtonBar().button(() -> AE2Icon.ADD, () -> "Add", () -> {}))
            .child(new ValueStepper(() -> "LV", step -> {}).size(84, 16))
            .child(new CycleDropdown<>("server_scenario_menu", String.class).choices(Arrays.asList("a", "b")))
            .child(new IconRowList(() -> rows))
            .child(new Scrollbar(() -> 0, () -> 0, row -> {}).size(AE2Theme.BIG_SCROLLER_WIDTH, 40))
            .child(new PageTabs(() -> 0, page -> {}).tab(() -> AE2Icon.GEAR, () -> "Settings"))
            .child(new StepSlider().size(60, 12))
            .child(new GTNLListWidget<>().scrollDirection(new HandleScrollData()));
        WidgetTree.collectSyncValues(new PanelSyncManager(new ModularSyncManager(false), true), panel);
    }

    public static class Options {

        @Config.LangKey("gtnllib.test.volume")
        @Config.RangeDouble(min = 0, max = 1)
        @Config.DefaultDouble(0.5)
        public static double volume = 0.5;

        @Config.LangKey("gtnllib.test.shown")
        @Config.DefaultBoolean(true)
        public static boolean shown = true;
    }
}

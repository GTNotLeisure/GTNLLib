package com.gtnllib.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.AE2Theme.Icon;
import com.gtnllib.gui.theme.AE2Theme.Variant;
import com.gtnllib.gui.theme.ScreenTheme;
import com.gtnllib.gui.wheel.WheelStyle;
import com.gtnllib.gui.widget.ActionButton;
import com.gtnllib.gui.widget.CycleDropdown;
import com.gtnllib.gui.widget.GTNLListWidget;
import com.gtnllib.gui.widget.HandleScrollData;
import com.gtnllib.gui.widget.IconRowList;
import com.gtnllib.gui.widget.ListRow;
import com.gtnllib.gui.widget.NumberEntry;
import com.gtnllib.gui.widget.SlideSwitch;
import com.gtnllib.gui.widget.ValueStepper;
import com.gtnllib.gui.widget.VerticalButtonBar;

/**
 * Builds the AE2 widgets the way a GUI opened by the server builds them on the server, without drawing anything.
 * {@link ServerSideLoadingTest} runs it inside a {@link ServerClassLoader}.
 */
public class ServerWidgetScenario implements Runnable {

    @Override
    public void run() {
        for (ScreenTheme theme : ScreenTheme.values()) AE2Theme.variant(theme.themeId);
        Variant light = AE2Theme.variant(AE2Theme.LIGHT_ID);
        ButtonWidget<?> plain = new ButtonWidget<>();
        plain.background(AE2Theme.button(plain, () -> true, () -> false))
            .overlay(AE2Theme.label(plain, () -> "Apply", () -> true, () -> false));
        AE2Theme.iconButton(plain, () -> Icon.ADD, () -> true, () -> false);
        AE2Theme.tabButton(plain, () -> Icon.GEAR, () -> true);
        AE2Theme.pageTab(plain, () -> Icon.FILE, () -> true);
        AE2Theme.toggleSwitch(plain, () -> true, () -> "Auto", () -> true);
        AE2Theme.radio(plain, () -> true, () -> "Tier", () -> true);
        AE2Theme.listRow(() -> false);
        AE2Theme.well(60, 0);
        AE2Theme.panelBackground(0, 0);
        ActionButton.text(() -> "Apply", () -> {})
            .available(() -> false)
            .selected(() -> false);
        ActionButton.icon(() -> Icon.REFRESH, () -> {});
        new NumberEntry(light).bounds(1, 64)
            .value(3)
            .available(() -> false);
        new NumberEntry(null).size(64, 16);
        new ListRow(() -> "Row").toggle(() -> true, () -> {})
            .available(() -> false);
        ListRow.header(() -> "Header");
        new VerticalButtonBar().button(() -> Icon.GEAR, () -> "Settings", () -> {})
            .toggle(() -> Icon.VISIBLE, () -> "Show", () -> true, () -> {});
        new ValueStepper(() -> "LV", step -> {});
        new GTNLListWidget<>().scrollDirection(new HandleScrollData(light, true).rows(IconRowList.ROW));
        new HandleScrollData(null);
        new CycleDropdown<>("menu", String.class).choices(Arrays.asList("a", "b"));
        List<IconRowList.Row> rows = Collections.singletonList(
            IconRowList.Row.toggle("Auto", true, () -> {})
                .withAvailable(false));
        new IconRowList(() -> rows);
        new SlideSwitch().value(() -> true);
        AE2Theme.sliderHandles(new ParentWidget<>().child(new SliderWidget()), AE2Theme.LIGHT_ID);
        AE2Theme.useMenuTheme(AE2Theme.DARK_ID, "gtnllib", "server_scenario", "menu");
        AE2Theme.legacyStates(light);
        AE2Theme.legacyTerminal(null);
        light.wheel(WheelStyle.builder());

        ModularPanel panel = ModularPanel.defaultPanel("server_scenario");
        panel.child(
            ActionButton.text(() -> "Apply", () -> {})
                .size(40, 20))
            .child(new NumberEntry(light))
            .child(new ListRow(() -> "Row").toggle(() -> false, () -> {}))
            .child(new VerticalButtonBar().button(() -> Icon.ADD, () -> "Add", () -> {}))
            .child(new ValueStepper(() -> "LV", step -> {}).size(84, 16))
            .child(new CycleDropdown<>("server_scenario_menu", String.class).choices(Arrays.asList("a", "b")))
            .child(new IconRowList(() -> rows))
            .child(new GTNLListWidget<>().scrollDirection(new HandleScrollData(light)));
        WidgetTree.collectSyncValues(new PanelSyncManager(new ModularSyncManager(false), true), panel);
    }
}

package com.gtnllib.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.AE2Theme.Icon;
import com.gtnllib.gui.theme.AE2Theme.Variant;
import com.gtnllib.gui.theme.ScreenTheme;
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
    }
}

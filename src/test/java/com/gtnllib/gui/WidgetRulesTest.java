package com.gtnllib.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.AbstractParentWidget;
import com.cleanroommc.modularui.widget.AbstractScrollWidget;
import com.cleanroommc.modularui.widget.AbstractWidget;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.gtnllib.gui.widget.CycleDropdown;
import com.gtnllib.gui.widget.IWheelStep;
import com.gtnllib.gui.widget.NumberEntryWidget;
import com.gtnllib.gui.widget.StepSlider;
import com.gtnllib.gui.widget.ValueStepper;

/**
 * Checks the rules every GTNLLib widget follows: build on MUI2's matching widget or be listed, keep availability, wheel
 * and held-press behaviour in their shared pieces, never add an "is it AE2" check, build on a dedicated server, and
 * leave the wheel off by default. The widget rules cover the {@code gui.widget} package; the AE2 check count covers all
 * main sources.
 */
public class WidgetRulesTest {

    private static final Path WIDGETS = Paths.get("src/main/java/com/gtnllib/gui/widget"),
        SCENARIO = Paths.get("src/test/java/com/gtnllib/gui/ServerWidgetScenario.java"),
        SOURCES = Paths.get("src/main/java");
    private static final String PACKAGE = "com.gtnllib.gui.widget.";
    // MUI2 has no widget like these, so they build on its Widget or ParentWidget.
    private static final Set<String> GAP_FILLERS = names(
        "Breadcrumb",
        "Divider",
        "FadingNotice",
        "IconRowList",
        "ListRow",
        "NumberEntryWidget",
        "PageTabs",
        "UpgradesPanel",
        "ValueStepper",
        "VerticalButtonBar");
    // MUI2 has a similar widget, and the class documentation says why it is not extended.
    private static final Set<String> EXCEPTIONS = names(
        "AECheckbox",
        "FillBar",
        "GhostItemSlot",
        "Scrollbar",
        "SlideSwitch",
        "StepSlider");
    private static final Set<String> CLIENT_ONLY = names("GhostItemSlot"),
        LIST_SCROLLERS = names("IconRowList", "Scrollbar"), LAST_CHILD_SETTERS = names("PageTabs", "VerticalButtonBar");
    private static final Set<Class<?>> BARE_BASES = new HashSet<>(
        Arrays.asList(
            AbstractWidget.class,
            Widget.class,
            SingleChildWidget.class,
            AbstractParentWidget.class,
            ParentWidget.class,
            AbstractScrollWidget.class));
    private static final Pattern AVAILABLE_SETTER = Pattern.compile("public \\S+ available\\(BooleanSupplier"),
        WHEEL_SETTER = Pattern.compile("public \\S+ wheel\\(boolean"),
        AE2_CHECK = Pattern.compile("(?<!boolean )\\bisAE2\\(|::isAE2\\b"),
        VARIANT_READ = Pattern.compile("\\.variant\\(\\)"),
        MUI2_FIX = Pattern
            .compile("\\.(showScrollShadows|setScrollBarBackgroundColor|registerThemeForScreen|sliderTexture)\\("),
        MENU_ID = Pattern.compile("\\.menuId\\b"), CONTEXT_MENU = Pattern.compile("\\bCONTEXT_MENU_ID\\b");
    // Menu theme identifiers belong to the skins and their registration; only MUI2Fixes hands them to menus.
    private static final Set<String> MENU_ID_FILES = names(
        "MUI2Fixes",
        "AE2Variant",
        "AE2Theme",
        "AE2ThemeDefinitions"), CONTEXT_MENU_FILES = names("MUI2Fixes", "AE2Theme");
    private static final int AE2_CHECKS = 14, VARIANT_READS = 4;

    @Test
    public void mui2First() throws IOException {
        Set<String> names = new HashSet<>();
        for (Class<?> type : widgets()) {
            String name = type.getSimpleName();
            names.add(name);
            Class<?> parent = type.getSuperclass();
            boolean bare = BARE_BASES.contains(parent);
            if (bare) assertTrue(
                GAP_FILLERS.contains(name) || EXCEPTIONS.contains(name),
                name + " builds on MUI2's "
                    + parent.getSimpleName()
                    + ": extend the MUI2 widget it resembles, or list it as a gap filler or an exception");
            else assertFalse(GAP_FILLERS.contains(name), name + " extends a MUI2 widget, so it is no gap filler");
        }
        for (String name : GAP_FILLERS) assertTrue(names.contains(name), "no widget named " + name + " any more");
        for (String name : EXCEPTIONS) {
            assertTrue(names.contains(name), "no widget named " + name + " any more");
            assertTrue(
                classDoc(name).contains("does not extend"),
                name + "'s class documentation must say why it does not extend the MUI2 widget it resembles");
        }
    }

    @Test
    public void ae2ChecksOnlyShrink() throws IOException {
        int checks = 0;
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path file : files.filter(
                path -> path.toString()
                    .endsWith(".java"))
                .collect(Collectors.toList())) {
                Matcher matcher = AE2_CHECK.matcher(read(file));
                while (matcher.find()) checks++;
            }
        }
        assertEquals(
            AE2_CHECKS,
            checks,
            "\"is it AE2\" checks may only go away: lower AE2_CHECKS when one does, never raise it; "
                + "ask AE2Look to draw the part instead");
        int reads = 0;
        for (Path file : widgetFiles()) {
            Matcher matcher = VARIANT_READ.matcher(read(file));
            while (matcher.find()) reads++;
        }
        assertEquals(
            VARIANT_READS,
            reads,
            "widgets may only read the AE2 skin less often: lower VARIANT_READS when a read goes away, never raise it; "
                + "ask AE2Look to draw the part instead");
    }

    @Test
    public void behaviourInPieces() throws IOException {
        for (Path file : widgetFiles()) {
            String name = simpleName(file), text = read(file);
            if (!name.equals("Availability")) assertFalse(
                text.contains("Result.STOP"),
                name + " swallows a press itself: route it through Availability "
                    + "(for another use, such as a swallowed key, allow the file here)");
            if (!name.equals("PressRepeat")) assertFalse(
                text.contains("Mouse.isButtonDown") || text.contains("System.currentTimeMillis"),
                name + " times a held press itself: use PressRepeat "
                    + "(for a timer that has nothing to do with a held button, allow the file here)");
            if (text.contains("boolean onMouseScroll(") && !LIST_SCROLLERS.contains(name)) assertTrue(
                text.contains("wheelStep.scroll("),
                name + " handles the wheel itself: route onMouseScroll through WheelStep.scroll");
            if (!LAST_CHILD_SETTERS.contains(name)) assertFalse(
                AVAILABLE_SETTER.matcher(text)
                    .find(),
                name + " writes its own available setter: implement IAvailable");
            assertFalse(
                WHEEL_SETTER.matcher(text)
                    .find(),
                name + " writes its own wheel setter: implement IWheelStep");
        }
    }

    @Test
    public void fixesInOnePlace() throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path file : files.filter(
                path -> path.toString()
                    .endsWith(".java"))
                .collect(Collectors.toList())) {
                String name = simpleName(file), text = read(file);
                if (name.equals("MUI2Fixes")) continue;
                Matcher matcher = MUI2_FIX.matcher(text);
                assertFalse(
                    matcher.find(),
                    name + " works around MUI2 itself ("
                        + (matcher.find(0) ? matcher.group() : "")
                        + "): call the method of MUI2Fixes instead");
                if (!MENU_ID_FILES.contains(name)) assertFalse(
                    MENU_ID.matcher(text)
                        .find(),
                    name + " picks a skin's menu theme itself: let MUI2Fixes give menus their theme");
                if (!CONTEXT_MENU_FILES.contains(name)) assertFalse(
                    CONTEXT_MENU.matcher(text)
                        .find(),
                    name + " picks MUI2's context-menu theme itself: let MUI2Fixes give menus their theme");
            }
        }
    }

    @Test
    public void serverScenario() throws IOException {
        String scenario = read(SCENARIO);
        for (Class<?> type : widgets()) {
            String name = type.getSimpleName();
            if (CLIENT_ONLY.contains(name)) continue;
            assertTrue(
                Pattern.compile("new " + name + "[<(]|\\b" + name + "\\.[a-z]\\w*\\(")
                    .matcher(scenario)
                    .find(),
                name + " must be built in ServerWidgetScenario, with new or a factory method, so a GUI the server "
                    + "opens can use it");
        }
    }

    @Test
    public void wheelOff() throws IOException {
        List<IWheelStep<?>> wheels = Arrays.asList(
            new CycleDropdown<>("wheel_rule", String.class),
            new NumberEntryWidget(),
            new StepSlider(),
            new ValueStepper(() -> "LV", step -> {}));
        Set<String> checked = new TreeSet<>(), declared = new TreeSet<>();
        for (IWheelStep<?> wheel : wheels) {
            String name = wheel.getClass()
                .getSimpleName();
            checked.add(name);
            assertFalse(
                wheel.wheelStep()
                    .isOn(),
                name + " must leave the wheel off until wheel(true), so a list scrolled past it keeps scrolling");
        }
        for (Class<?> type : widgets()) if (IWheelStep.class.isAssignableFrom(type)) declared.add(type.getSimpleName());
        assertEquals(declared, checked, "every widget the wheel can change must be checked here");
    }

    private static Set<String> names(String... names) {
        return new HashSet<>(Arrays.asList(names));
    }

    private static List<Path> widgetFiles() throws IOException {
        try (Stream<Path> files = Files.list(WIDGETS)) {
            return files.filter(
                path -> path.toString()
                    .endsWith(".java"))
                .sorted()
                .collect(Collectors.toList());
        }
    }

    private static List<Class<?>> widgets() throws IOException {
        List<Class<?>> widgets = new ArrayList<>();
        for (Path file : widgetFiles()) {
            Class<?> type;
            try {
                type = Class.forName(PACKAGE + simpleName(file), false, WidgetRulesTest.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new AssertionError("no class for " + file, e);
            }
            int modifiers = type.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers)
                && !type.isInterface()
                && IWidget.class.isAssignableFrom(type)) widgets.add(type);
        }
        return widgets;
    }

    private static String classDoc(String name) throws IOException {
        String text = read(WIDGETS.resolve(name + ".java"));
        int declaration = text.indexOf("\npublic class " + name), doc = text.lastIndexOf("/**", declaration);
        return declaration < 0 || doc < 0 ? "" : text.substring(doc, declaration);
    }

    private static String simpleName(Path file) {
        String name = file.getFileName()
            .toString();
        return name.substring(0, name.length() - ".java".length());
    }

    private static String read(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }
}

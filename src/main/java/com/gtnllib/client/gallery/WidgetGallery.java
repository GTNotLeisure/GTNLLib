package com.gtnllib.client.gallery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.LongValue;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.CategoryList;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.Dialog;
import com.cleanroommc.modularui.widgets.Expandable;
import com.cleanroommc.modularui.widgets.FluidDisplayWidget;
import com.cleanroommc.modularui.widgets.ItemDisplayWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.ScrollingTextWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.SortableListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.menu.ContextMenuButton;
import com.cleanroommc.modularui.widgets.menu.DropdownWidget;
import com.cleanroommc.modularui.widgets.textfield.TextEditorWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
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
 * empty upgrade panel no longer asks NEI to keep clear. Two pages show MUI2's own widgets under the theme, polished by
 * the one {@code AE2Theme.polish} call an AE2 screen adds, its context menu built by {@code AE2Theme.menu}, and the
 * dropdown's menu laid out as MUI2 lays it out, which only a subclass such as {@code CycleDropdown} changes; the last
 * page compares MUI2 and GTNLLib
 * buttons at several heights and the three ways to change a number with the mouse wheel. Client only.
 */
public class WidgetGallery {

    private static final String LANG = "gtnllib.widget_gallery.", PANEL_NAME = "gtnllib_widget_gallery",
        NATIVE_DROPDOWN = "gtnllib_gallery_native_dropdown", NATIVE_MENU = "gtnllib_gallery_native_menu";
    private static final int[] BUTTON_HEIGHTS = { 12, 16, 20, 28 };
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
        if (themeId != null) screen.useTheme(themeId);
        AE2Theme.polish(screen);
        GhostItemSlot.open(screen);
    }

    private ModularPanel panel() {
        ModularPanel panel = ModularPanel.defaultPanel(PANEL_NAME, WIDTH, HEIGHT);
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
            .tab(() -> AE2Icon.FILE, () -> tr("page.lists"))
            .tab(() -> AE2Icon.PROCESSOR, () -> tr("page.native"))
            .tab(() -> AE2Icon.MORE, () -> tr("page.native_more"))
            .tab(() -> AE2Icon.FILTER, () -> tr("page.compare"));
        IPanelHandler nativeDialog = IPanelHandler.simple(panel, (parent, player) -> nativeDialog(), true);
        return panel.child(tabs.page(0, buttonsPage()))
            .child(tabs.page(1, inputsPage()))
            .child(tabs.page(2, listsPage()))
            .child(tabs.page(3, nativePage()))
            .child(tabs.page(4, nativeMorePage(nativeDialog)))
            .child(tabs.page(5, comparePage()))
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

    private ParentWidget<?> nativePage() {
        DropdownWidget<Integer, ?> dropdown = new DropdownWidget<>(NATIVE_DROPDOWN, Integer.class);
        dropdown.optionToWidget((value, selected) -> {
            TextWidget<?> text = IKey.str(TIERS[value])
                .asWidget()
                .paddingLeft(4)
                .widthRel(1f);
            return selected ? text.heightRel(1f) : text.height(12);
        })
            .value(new IntValue.Dynamic(() -> state.tier, v -> state.tier = v));
        dropdown.options(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7));
        CycleButtonWidget cycle = new CycleButtonWidget()
            .value(new IntValue.Dynamic(() -> state.nativeCycle, v -> state.nativeCycle = v))
            .stateCount(3);
        for (int i = 0; i < 3; i++) cycle.stateOverlay(i, IKey.str("CycleButtonWidget " + (i + 1)));
        PagedWidget.Controller controller = new PagedWidget.Controller();
        PagedWidget<?> paged = new PagedWidget<>().controller(controller)
            .initialPage(state.nativePage)
            .onPageChange(page -> state.nativePage = page);
        ParentWidget<?> page = page();
        for (int i = 0; i < 3; i++) {
            paged.addPage(
                IKey.str("PagedWidget " + (i + 1))
                    .asWidget());
            page.child(
                new PageButton(i, controller).overlay(IKey.str(String.valueOf(i + 1)))
                    .pos(i * 22, 100)
                    .size(20, 20));
        }
        ListWidget<IWidget, ?> list = new ListWidget<>().pos(196, 48)
            .size(76, 88);
        for (int i = 0; i < 8; i++) {
            String name = "ListWidget " + (i + 1);
            list.child(
                new ButtonWidget<>().overlay(IKey.str(name))
                    .onMousePressed(mouse -> {
                        show(name);
                        return true;
                    })
                    .widthRel(1f)
                    .height(16));
        }
        page.child(
            new ButtonWidget<>().overlay(IKey.str("ButtonWidget"))
                .onMousePressed(mouse -> {
                    show("ButtonWidget");
                    return true;
                })
                .pos(0, 0)
                .size(84, 20))
            .child(
                new ToggleButton().value(new BoolValue.Dynamic(() -> state.nativeToggle, on -> state.nativeToggle = on))
                    .overlay(IKey.str("ToggleButton"))
                    .pos(88, 0)
                    .size(84, 20))
            .child(
                cycle.pos(176, 0)
                    .size(96, 20))
            .child(
                new TextFieldWidget().value(new StringValue.Dynamic(() -> state.nativeText, v -> state.nativeText = v))
                    .pos(0, 26)
                    .size(120, 18))
            .child(
                dropdown.pos(126, 26)
                    .size(64, 18))
            .child(
                new ButtonWidget<>().widgetTheme(IThemeApi.CLOSE_BUTTON)
                    .overlay(GuiTextures.CROSS_TINY)
                    .onMousePressed(mouse -> {
                        show("closeButton");
                        return true;
                    })
                    .pos(196, 28))
            .child(
                new Widget<>().widgetTheme(IThemeApi.ITEM_SLOT)
                    .overlay(
                        new ItemDrawable(new ItemStack(Items.diamond)).asIcon()
                            .size(16))
                    .pos(0, 50)
                    .size(18, 18))
            .child(
                new Widget<>().widgetTheme(IThemeApi.FLUID_SLOT)
                    .pos(22, 50)
                    .size(18, 18))
            .child(
                IKey.str("itemSlot / fluidSlot")
                    .asWidget()
                    .pos(46, 55))
            .child(
                new SliderWidget().value(new DoubleValue.Dynamic(() -> state.nativeSlider, v -> state.nativeSlider = v))
                    .pos(0, 76)
                    .size(120, 14))
            .child(
                new ProgressWidget().value(new DoubleValue.Dynamic(() -> state.progress, v -> state.progress = v))
                    .texture(GuiTextures.PROGRESS_ARROW, 20)
                    .pos(126, 75)
                    .size(20, 16))
            .child(
                paged.pos(70, 105)
                    .size(120, 12))
            .child(list);
        return page;
    }

    private ParentWidget<?> nativeMorePage(IPanelHandler dialog) {
        VerticalScrollData vertical = new VerticalScrollData();
        vertical.setScrollSize(8 * 12);
        ScrollWidget<?> scroll = new ScrollWidget<>(vertical).pos(144, 0)
            .size(64, 60);
        for (int i = 0; i < 8; i++) scroll.child(
            IKey.str("ScrollWidget " + (i + 1))
                .asWidget()
                .pos(2, i * 12 + 2));
        Map<String, SortableListWidget.Item<String>> items = new HashMap<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String name = "Sortable " + (i + 1);
            names.add(name);
            items.put(
                name,
                new SortableListWidget.Item<>(name).child(
                    item -> Flow.row()
                        .child(
                            new Widget<>().widgetTheme(IThemeApi.BUTTON)
                                .overlay(IKey.str(name))
                                .expanded()
                                .heightRel(1f))
                        .child(
                            new ButtonWidget<>().onMousePressed(mouse -> item.removeSelfFromList())
                                .overlay(
                                    GuiTextures.CROSS_TINY.asIcon()
                                        .size(10))
                                .width(10)
                                .heightRel(1f)))
                    .height(14));
        }
        CategoryList.Root categories = new CategoryList.Root().pos(144, 66)
            .size(128, 70);
        SortableListWidget<String> sortable = new SortableListWidget<String>().children(names, items::get)
            .pos(212, 0)
            .size(60, 60);
        for (int i = 0; i < 2; i++) {
            Category category = new Category();
            category.widthRel(1f)
                .height(14)
                .widgetTheme(IThemeApi.BUTTON)
                .overlay(IKey.str("CategoryList " + (i + 1)));
            for (int j = 0; j < 3; j++) category.item(
                IKey.str("Entry " + (j + 1))
                    .asWidget()
                    .paddingLeft(8)
                    .widthRel(1f)
                    .height(12));
            categories.child(category);
        }
        return page().child(
            new TextEditorWidget().hintText("TextEditorWidget")
                .pos(0, 0)
                .size(138, 40))
            .child(
                new ItemDisplayWidget().item(new ItemStack(Items.diamond, 64))
                    .displayAmount(true)
                    .pos(0, 46)
                    .size(18, 18))
            .child(
                new FluidDisplayWidget().value(new FluidStack(FluidRegistry.WATER, 8000))
                    .capacity(16000)
                    .pos(22, 46)
                    .size(18, 18))
            .child(
                new ScrollingTextWidget(IKey.str("ScrollingTextWidget: a long line that scrolls")).pos(46, 51)
                    .size(92, 10))
            .child(
                nativeContextMenu().pos(0, 70)
                    .size(96, 18))
            .child(
                new ButtonWidget<>().overlay(IKey.str("Dialog"))
                    .onMousePressed(mouse -> {
                        dialog.openPanel();
                        return true;
                    })
                    .pos(100, 70)
                    .size(38, 18))
            .child(
                new Expandable().animationDuration(0)
                    .widgetTheme(IThemeApi.PANEL)
                    .collapsedView(expandableTitle("Expandable", 80))
                    .expandedView(
                        new ParentWidget<>().size(120, 40)
                            .child(expandableTitle("Expandable: open", 120))
                            .child(
                                new ItemDisplayWidget().item(new ItemStack(Items.redstone))
                                    .pos(4, 18)
                                    .size(18, 18)))
                    .pos(0, 96))
            .child(scroll)
            .child(sortable)
            .child(categories);
    }

    private ParentWidget<?> comparePage() {
        ParentWidget<?> page = page();
        page.child(
            IKey.str("ButtonWidget (MUI2)")
                .asWidget()
                .pos(0, 0))
            .child(
                IKey.str("AE2Button (GTNLLib)")
                    .asWidget()
                    .pos(0, 46));
        for (int i = 0; i < BUTTON_HEIGHTS.length; i++) {
            String label = String.valueOf(BUTTON_HEIGHTS[i]);
            page.child(
                new ButtonWidget<>().overlay(IKey.str(label))
                    .onMousePressed(mouse -> {
                        show("ButtonWidget " + label);
                        return true;
                    })
                    .pos(i * 70, 12)
                    .size(64, BUTTON_HEIGHTS[i]))
                .child(
                    new AE2Button(() -> label, () -> show("AE2Button " + label)).available(() -> state.usable)
                        .pos(i * 70, 58)
                        .size(64, BUTTON_HEIGHTS[i]));
        }
        page.child(
            IKey.str("TextFieldWidget")
                .asWidget()
                .pos(0, 92))
            .child(
                new TextFieldWidget().setNumbers(0, 64)
                    .scrollValues(1, 10, 100, 1000)
                    .value(new StringValue.Dynamic(() -> state.compareText, v -> state.compareText = v))
                    .pos(0, 104)
                    .size(86, 16))
            .child(
                IKey.dynamic(() -> "SliderWidget " + Math.round(state.compareSlider))
                    .asWidget()
                    .pos(92, 92))
            .child(
                new SliderWidget().bounds(0, 64)
                    .value(new DoubleValue.Dynamic(() -> state.compareSlider, v -> state.compareSlider = v))
                    .scrollValues(1, 10, 1, 1)
                    .pos(92, 104)
                    .size(86, 16))
            .child(
                IKey.str("NumberEntryWidget")
                    .asWidget()
                    .pos(184, 92))
            .child(
                new NumberEntryWidget().bounds(0, 64)
                    .value(new LongValue.Dynamic(() -> state.compareEntry, v -> state.compareEntry = v))
                    .wheel(true)
                    .available(() -> state.usable)
                    .pos(184, 104)
                    .size(90, 16));
        return page;
    }

    private ContextMenuButton<?> nativeContextMenu() {
        ContextMenuButton<?> button = new ContextMenuButton<>(NATIVE_MENU);
        ListWidget<IWidget, ?> options = new ListWidget<>().maxSize(70);
        for (String tier : TIERS) options.child(
            new ButtonWidget<>().overlay(IKey.str(tier))
                .onMousePressed(mouse -> {
                    show(tier);
                    button.closeMenu(false);
                    return true;
                })
                .widthRel(1f)
                .height(12));
        return button.overlay(IKey.str("ContextMenuButton"))
            .menu(AE2Theme.menu(options, themeId));
    }

    private static TextWidget<?> expandableTitle(String text, int width) {
        return IKey.str(text)
            .asWidget()
            .padding(4, 0, AE2Theme.PANEL_EDGE, AE2Theme.PANEL_BOTTOM_EDGE)
            .size(width, 18);
    }

    private ModularPanel nativeDialog() {
        return new Dialog<>("gtnllib_gallery_native_dialog").size(120, 50)
            .child(
                IKey.str("Dialog")
                    .asWidget()
                    .pos(8, 8))
            .child(ButtonWidget.panelCloseButton());
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

    // MUI2 2.3.94's CategoryList has no public way to add entries.
    private static class Category extends CategoryList {

        private void item(IWidget entry) {
            addChild(entry, -1);
        }
    }

    private static class State {

        private boolean usable = true, hide, selected, fuzzy, slide, checked, rowChecked, visible = true, nativeToggle;
        private int page, redstone, radio, access, tier, scroll, selectedRow, nativeCycle, nativePage;
        private long amount = 64, small = 8;
        private double progress = 0.4, nativeSlider = 40, compareSlider = 8;
        private long compareEntry = 8;
        private String nativeText = "", compareText = "8";
        private final boolean[] rows = new boolean[6];
    }
}

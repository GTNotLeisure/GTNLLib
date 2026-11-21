# AE2 Theme API

GTNLLib provides light, dark and purple-highlighted dark AE2 themes for ModularUI 2. Their source Javadoc defines parameter units, return values, null handling and lifecycle requirements.

Every AE2 widget decides its look while drawing, from the theme of the panel it is in (`AE2Look.of(widget)`), so a dialog that uses a theme of its own is drawn in that theme's look, not the screen's. A theme names its AE2 skin in its `gtnllibAE2Look` theme data entry, which the AE2 themes write and themes based on them inherit; a theme without the entry is recognised by its identifier. Nothing has to be passed to a widget when it is created.

ModularUI 2 builds a GUI that the server opens (a machine, a held item) on both sides. `AE2Theme`, the drawing classes of the `ae2` package and every widget below load on both sides, so they can be used in such GUIs; they draw only on the client. `ServerSideLoadingTest` builds them in a class loader that behaves like a dedicated server. Theme registration (`AE2Theme.register`) is client-only and is done by GTNLLib.

## API Reference

| Type | Responsibility |
| --- | --- |
| [AE2Theme](src/main/java/com/gtnllib/gui/theme/AE2Theme.java) | Theme identifiers, AE2's widget dimensions, the skin texture folders, theme registration and lookup. Its drawing methods are deprecated forwarders to the classes below. |
| [AE2Variant](src/main/java/com/gtnllib/gui/theme/ae2/AE2Variant.java) | The three AE2 skins (light, dark, purple dark): their palette (`AE2Colors`, built by name with `AE2Colors.builder()`), selection colors and textures. The purple dark skin ships only the textures it recolours (`AE2Theme.PURPLE_TEXTURES`) and takes the rest from the dark skin. |
| [AE2Icon](src/main/java/com/gtnllib/gui/theme/ae2/AE2Icon.java) | The built-in toolbar icons, drawn from the AE2 skin's sheet or MUI2's icons outside AE2 themes. |
| [ae2 package](src/main/java/com/gtnllib/gui/theme/ae2) | One class per part: `AE2Buttons` (text buttons, toolbar buttons, tabs, page tabs, `buttonText`), `AE2Switches` (switches, radio buttons, check-box labels, `SwitchHover`), `AE2Scrollbars`, `AE2Panels` (panel backgrounds, list wells and rows, the toolbar strip, tables, the dropdown caret), `AE2Upgrades`, `AE2Legacy` (GTNH AE2's rv3 sheets) and `AE2Draw` (text and helpers shared by the others). `IconSource` is what a toolbar button or tab shows. |
| [AE2Look](src/main/java/com/gtnllib/gui/theme/ae2/AE2Look.java) | The look a panel gives GTNLLib's AE2 widgets: one of the three AE2 skins or the plain look. `AE2Look.of(widget)` reads the theme of the widget's panel; every AE2 widget asks it while drawing. |
| [ScreenTheme](src/main/java/com/gtnllib/gui/theme/ScreenTheme.java) | Persistable selection of the default, light, dark or purple dark panel theme. |
| [HandleScrollData](src/main/java/com/gtnllib/gui/widget/HandleScrollData.java) | Vertical scrolling with a fixed-size handle and optional row snapping; a press on the track beside the handle moves the handle's centre to the pointer and keeps dragging, or pages like AE2 with `pageOnTrackPress(true)`. |
| [GTNLListWidget](src/main/java/com/gtnllib/gui/widget/GTNLListWidget.java) | List layout, initial scroll position and persistent scroll-bar integration; with paging turned on, keeps paging while a track press is held. |
| [RowScrollbar](src/main/java/com/gtnllib/gui/widget/RowScrollbar.java) | Stand-alone AE2 scroll bar over whole positions (usually rows) for widgets that draw their own content, like AE2's `Scrollbar`: the owner keeps the position, and the bar drags, jumps to a track press (or pages like AE2) and takes the wheel. |
| [IPersistentScrollbar](src/main/java/com/gtnllib/gui/widget/IPersistentScrollbar.java) | Extension contract for scroll data that retains an inactive track. |
| [NumberEntry](src/main/java/com/gtnllib/gui/widget/NumberEntry.java) | Bounded integer input of any size, expression parsing, step buttons or `<` `>` arrows, optional wheel stepping (`wheel(true)`), right-click reset and validation feedback; `available` gives it AE2's disabled look, and `textAlignment(Alignment.Center)` centres the number in its field. |
| [VerticalButtonBar](src/main/java/com/gtnllib/gui/widget/VerticalButtonBar.java) | AE2's vertical toolbar left of a panel: 18×20 icon buttons 22 pixels apart on a strip that covers the panel's left border; on/off buttons show AE2's selected frame while on, `available` disables the button added last, and `buttonSize(width, height)` changes every button's size. |
| [PageTabs](src/main/java/com/gtnllib/gui/widget/PageTabs.java) | AE2's page tabs down a panel's right edge as one widget: the owner keeps the open page, `tab(icon, tooltip)` adds a tab, `page(index, widget)` shows a page only while its tab is open, `available` disables the tab added last, and `tabSize(width, height)` changes every tab's size. |
| [IconRowList](src/main/java/com/gtnllib/gui/widget/IconRowList.java) | Fast drawn list rows with icons, details (optionally in their own color), dropdowns, switches and `NumberEntry` fields; AE2 inset, highlights and scroll bar under AE2 themes. `Row.withAvailable(false)` draws a row muted and ignores its clicks. Its scroll bar is a `RowScrollbar`: one row per wheel notch, and a track press moves the handle to the pointer. `rowHeight` and `numberWidth` change the row height (18 by default) and the number fields' width (64), and `numberWheel(true)` lets the wheel step the number fields. Put a `ValueStepper` in a `ListRow` for a stepper row. |
| [ListRow](src/main/java/com/gtnllib/gui/widget/ListRow.java) | A widget row drawn like an `IconRowList` row: label, then a control widget or switch against the right end; clickable rows highlight, and `available` mutes the row. It is 18 pixels high unless its height is set; the label, switch and control stay centred in any height. For lists that mix MUI2 controls with rows. |
| [ActionButton](src/main/java/com/gtnllib/gui/widget/ActionButton.java) | Themed text button, 18×20 toolbar icon button or 20×20 tab button (`ActionButton.tab`) with a left-click action, optional available and selected states; an unavailable button swallows every mouse button. |
| [SettingButton](src/main/java/com/gtnllib/gui/widget/SettingButton.java) | AE2's setting button on MUI2's cycle button: an 18×20 toolbar button with one icon and tooltip description per value; left click steps forward, right click back. With a boolean value it is AE2's two-icon toggle. Takes an integer, enum, boolean or synced value; `available` disables it. |
| [ProgressBar](src/main/java/com/gtnllib/gui/widget/ProgressBar.java) | AE2's 6×18 inscriber progress bar on MUI2's `ProgressWidget`, filling upwards in whole pixels; outside AE2 themes a slot-style well that fills with green. Takes a 0–1 or synced value. |
| [ValueStepper](src/main/java/com/gtnllib/gui/widget/ValueStepper.java) | `<` value `>` stepper over named values such as voltage tiers; `wheel(true)` lets the mouse wheel step it; `available` greys it out. |
| [CycleDropdown](src/main/java/com/gtnllib/gui/widget/CycleDropdown.java) | Dropdown that keeps its menu inside the panel (`menuWidth`); `iconOption` builds an item-icon option row with a scrolling name, placed by an optional `Alignment` (left by default) and keeping formatting codes such as GT's tier colors. Under AE2 themes the shown value and its caret use the button text colors and sink on hover like a button label. `available` draws it as a disabled button and closes its menu; `wheel(true)` cycles the choices with the mouse wheel. |
| [SlideSwitch](src/main/java/com/gtnllib/gui/widget/SlideSwitch.java) | Theme-driven on/off switch with a label; AE2 themes give it AE2's switch sprites, centred on its height, with the label wrapped and placed as in AE2's check boxes and AE2's hover rule after a click. `available` fades it; `glowWhen` only chooses whether an on switch glows. |
| [StepSlider](src/main/java/com/gtnllib/gui/widget/StepSlider.java) | Slider over a fixed number of steps with ticks, milestones and a value tag in a small built-in font (digits, letters shown as capitals, space and `% . - + × / : ( ) = < > ! ?`). Under AE2 themes it uses the slider skins, also when vertical; `available` greys it out, and under AE2 themes darkens its skins; `wheel(true)` lets the mouse wheel move it. |
| [GhostItemSlot](src/main/java/com/gtnllib/gui/widget/GhostItemSlot.java) | Item slot for client-only screens that takes an item dragged from NEI, with an optional filter and change callback; it lights up under the pointer, and with `clearable(true)` a right-click empties it and runs `onClear`. |

Every widget that can be unavailable takes `available(BooleanSupplier)`: `ActionButton`, `NumberEntry`, `ListRow`, `ValueStepper`, `CycleDropdown`, `SlideSwitch`, `StepSlider`, and the last button or tab added to a `VerticalButtonBar` or `PageTabs`; an `IconRowList` row uses `Row.withAvailable(false)`. An unavailable widget looks disabled (AE2's disabled sprites, a darkened skin, a faded icon or muted text), shows no hover, swallows clicks so they never reach what lies below (a `ListRow` does when it has a click action), and leaves the mouse wheel to the widgets around it. `SlideSwitch.availableWhen`, `SlideSwitch.liveWhen` and `StepSlider.activeWhen` are the older names and still work.

Toolbar buttons, tabs and page tabs show an `IconSource`: one of the built-in `AE2Icon`s, which follow the AE2 skin, or `IconSource.of(texture)` for a mod's own 16×16 texture and `IconSource.of(itemStack)` for an item. Built-in icons and textures fade on an unavailable button; items do not.

Every widget takes a custom size, and AE2's sizes are the defaults. Frames and fills stretch while their borders keep their width: buttons, icon buttons, tabs (`AE2Buttons.tabButton` and `pageTab` draw at the widget's size), panels, insets, list rows and the upgrade panel. Sprites that AE2 never stretches keep their size and stay centred or anchored: switches, radio buttons, scroll-bar handles and 16×16 icons. Widgets made of several parts take their part sizes through their own methods: `VerticalButtonBar.buttonSize`, `PageTabs.tabSize`, `IconRowList.rowHeight` and `numberWidth`, and the `perColumn` overloads of `AE2Upgrades.drawUpgradePanel`, `upgradePanelWidth`, `upgradePanelHeight`, `upgradeSlotX` and `upgradeSlotY` (AE2 fills columns of `UPGRADE_SLOTS_PER_COLUMN`, 8).

AE2 draws its toolbar strip after the panel, anchored three pixels inside the panel's left edge, so the strip's right two pixels hide the panel border and the two read as one piece. Place `VerticalButtonBar` at `panelLeft - WIDTH + OVERLAP` and add it after the panel's other children. A panel that also holds the bar starts its background `WIDTH - OVERLAP` pixels right of its own left edge; `AE2Panels.panelBackground(left, right)` draws the panel texture between such insets.

Pages are switched with AE2's page tabs (`PageTabs`, or `AE2Buttons.pageTab` to draw one on a button of your own), the mode tabs of the pattern encoding terminal: 22×22 tabs stacked `PAGE_TAB_PITCH` apart on the panel's right edge, starting `PAGE_TAB_INSET` pixels inside it and added after the panel. The selected tab covers the panel border and joins the panel; the others stand out. Toolbar buttons (`AE2Buttons.iconButton`) are actions, not pages; their `selected` overload marks a toggled action.

AE2 lists keep their scroll bar beside the list inset, on the panel background, with eight pixels from the inset edge to the groove (as in AE2's network status and crafting CPU screens); the groove is never drawn inside the inset. `IconRowList` reserves `AE2_SCROLL_COLUMN` for it, so align controls above a list to `width - AE2_SCROLL_COLUMN` under AE2 themes. `AE2Panels.well(width, plainColor)` draws such an inset for a page of your own widgets (`drawWell` for code that draws directly). Switches in list rows and `AE2Switches.toggleSwitch` share `AE2Switches.drawSwitch`: AE2's switch sprite under AE2 themes, MUI2's check box otherwise. `ListRow`s as wide as the inset put their text and controls where `IconRowList`'s own rows put them, so pages built from either line up. A scrolling list of `ListRow`s placed one pixel inside its inset, top and bottom, clips its rows inside the inset border while `HandleScrollData` still draws the groove over the inset's full height.

`NumberEntry`, `CycleDropdown`, `ValueStepper` and `StepSlider` ignore the mouse wheel by default, so the wheel scrolls a list around them even while the pointer is over one, and scrolling a page never changes a value by accident. `wheel(true)` lets one step its value while the pointer is over it (for `NumberEntry`, over its text field), also inside a scrolling list; anywhere else in the list the wheel still scrolls the list. `IconRowList.numberWheel(true)` does the same for the number fields of its rows. Lists and scroll bars always take the wheel. MUI2 offers the wheel to a text field with keyboard focus first, wherever the pointer is; `NumberEntry` passes it on unless the pointer is over the field, so clicking into a number field and moving away never turns the wheel into a hidden value change.

AE2 buttons are 20 pixels high, and at that height `AE2ButtonDrawable` matches AE2 row for row. Lower buttons, such as the 16-pixel arrows of a compact `NumberEntry` or a `ValueStepper`, follow a GTNLLib rule rather than AE2's, on purpose. The normal shadow is `height - 13` pixels, clamped to 1–3: 3 from 16 pixels up, 2 at 15 and 1 at 14 and below. A hovered button's shadow is one pixel thinner and a disabled button's two, never below zero. The face sinks by the pixels the shadow lost, and the label stays centred on the face. AE2 1.21 itself nine-slices its button sprite with a 3-pixel border. Below 20 pixels that leaves a 2-pixel shadow, and from 18 pixels down the face also loses its bottom highlight row, so a 16-pixel GTNLLib button has a thicker shadow than AE2's. Above 20 pixels GTNLLib stretches the face, while AE2 repeats a light and dark stripe across the middle. `AE2ButtonDrawable.shadow(state, height)` and `textY(state, height)` return the shadow and the label offset for custom drawing.

`AE2Buttons.label` places button text as AE2 does: normal, hovered and disabled labels move with the face, and under AE2 themes a label wider than the button scrolls back and forth inside it. A scrolling label does not move with the face: as in AE2, it stays at the hovered label's height in every state, so on a normal button it sits one pixel lower than a centred label, and on a disabled one one pixel higher. MUI2's own buttons (close buttons, toggle buttons, dialog buttons) get the AE2 button background from the theme, but MUI2 places their text itself: centred, one pixel lower than AE2's normal label and without sinking on hover. Use `ActionButton` or `AE2Buttons.button` with `AE2Buttons.label` where the text should behave like AE2's.

Switch and radio labels wrap at the widget's width as in AE2's `AECheckbox` (`AE2Switches.checkboxLabel`): one line starts four pixels below the sprite's top, several lines start one pixel below it. After a switch flips under the pointer it shows its plain sprite until the pointer leaves, as AE2's does (`AE2Switches.SwitchHover`); radio buttons keep their hover look. AE2 wraps at the widget width minus 22, which lets a switch label run four pixels past the widget; GTNLLib wraps inside the widget.

AE2's toolbar icon buttons are 16×16 widgets inside a drawn 18×20 frame. `ActionButton.icon` and the `VerticalButtonBar` buttons take the mouse on the whole 18×20 frame, on purpose. AE2's text fields draw their text two pixels from the background's edge; GTNLLib's text fields, `NumberEntry` included, keep MUI2's four pixels.

AE2 attaches the upgrade panel to the right of a screen's panel, `UPGRADE_OVERLAP` pixels inside its right edge: the upgrade panel has no left border, so it covers the main panel's border and the two read as one piece. `AE2Upgrades.drawUpgradePanel` draws it like that, with a light frame around a block of 16×16 slot wells 18 pixels apart; draw it after the main panel. Put 18×18 item slots at `upgradeSlotX(i)`, `upgradeSlotY(i)` with `disableThemeBackground(true)`, so their theme frame does not cover the wells.

AE2's scroll bars also take the mouse wheel anywhere on the screen. MUI2 offers the wheel to screen-level listeners before any widget and lets none of them keep it, so GTNLLib does not do this by default; a screen that wants it calls `RowScrollbar.scroll(direction)` from its panel's `onMouseScroll` when `super.onMouseScroll` returns false.

The small scroll bar (`HandleScrollData(true)`) draws the groove AE2 uses for its small scroll bar in the processing pattern panel: a one-pixel line-colored outline around a raised fill. Scroll-bar handles never stretch in AE2; MUI2 scroll widgets that do not use `HandleScrollData` stretch the theme's small handle to their own length.

A background set with `background(...)` on a MUI2 `ItemSlot` disappears while the slot holds an item (`ItemSlot.getCurrentBackground` in MUI2 2.3.94). The theme's slot frame is drawn separately and stays, so leave slot frames to the theme or draw them in the panel background.

## Differences From AE2

These differ from AE2 1.21 on purpose; the paragraphs above give the details.

- Buttons lower than 20 pixels use GTNLLib's shadow rule, and taller ones stretch the face.
- A press on a scroll bar's track moves the handle's centre to the pointer and keeps dragging; AE2 pages. `pageOnTrackPress(true)` restores AE2's paging.
- Toolbar icon buttons take the mouse on their whole 18×20 frame; AE2's only on the 16×16 icon.
- Text fields keep MUI2's four-pixel text inset; AE2's is two pixels.
- Value controls ignore the wheel unless `wheel(true)` is set; with it they step while the pointer is over them, also in a scrolling list, where AE2 has no such controls.
- The wheel does not scroll a list from anywhere on the screen by default.
- Switch and radio labels wrap inside the widget; AE2's switch labels can run four pixels past it.
- Unavailable toolbar buttons, tabs and page tabs fade their built-in or texture icon; AE2's full-size icon buttons keep their icon and are usually hidden instead.
- `VerticalButtonBar.toggle` shows the selected frame while on and keeps its icon; AE2's `ToggleButton` switches between an on and an off icon.
- `NumberEntry` arrows and wheel step by 10 while Shift is held, and a step from invalid input starts at zero; AE2's wheel steps by one and only on valid input.
- `IconRowList` and clickable `ListRow` rows highlight under the pointer; AE2's crafting CPU rows do not.

AE2 1.21 has no dropdown, stepper, slider, fill bar, divider, close button, breadcrumb or toast. `CycleDropdown`, `ValueStepper`, the single-row `NumberEntry`, `StepSlider`, `FillBar`, `Divider`, `CloseButton`, `Breadcrumb`, `FadingNotice`, `IconRowList`, `ListRow` and `GhostItemSlot` are GTNLLib's own controls, built from AE2's parts and colors where the AE2 themes apply.

Two things cannot match AE2: Minecraft 1.7.10 has no keyboard focus for buttons, so AE2's focus look has no counterpart, and MUI2's own buttons place their text themselves.

## Theme Selection

| Selection | Panel theme | Menu theme |
| --- | --- | --- |
| `ScreenTheme.DEFAULT` | Screen default (`themeId == null`) | `modularui.context_menu` |
| `ScreenTheme.AE2_LIGHT` | `gtnllib:ae2_light` | `gtnllib:ae2_light_menu` |
| `ScreenTheme.AE2_DARK` | `gtnllib:ae2_dark` | `gtnllib:ae2_dark_menu` |
| `ScreenTheme.AE2_DARK_PURPLE` | `gtnllib:ae2_dark_purple` | `gtnllib:ae2_dark_purple_menu` |

GTNLLib registers these themes during client initialization. Apply the selected identifier through MUI2's screen theme registration. `AE2Theme.useMenuTheme` maps named child menu panels to the matching menu theme.

The purple dark theme shares the dark palette and replaces AE2's light-blue selection with fluix purple. Custom selection drawing should read `AE2Variant.highlight`, `highlightTop`, `highlightShadow` and `highlightText` instead of the `AE2Theme.HIGHLIGHT` constants, so it matches every variant.

![AE2 dark and purple dark themes](.github/images/ae2-dark-purple-preview.png)

## Usage Examples

### Row-Based List

```java
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.gtnllib.gui.widget.GTNLListWidget;
import com.gtnllib.gui.widget.HandleScrollData;

HandleScrollData scrolling = new HandleScrollData().rows(22);
ListWidget<IWidget, ?> list = new GTNLListWidget<>().scrollDirection(scrolling)
    .size(180, 110);
```

`GTNLListWidget` hands `HandleScrollData` the look of its panel before every layout, so the handle and the track width (12 pixels for AE2, 14 for vanilla) follow the panel's theme; another scroll widget calls `look(AE2Look.of(widget))` itself. The older constructors that take an `AE2Variant` still work: that skin is used until a `GTNLListWidget` hands over its panel's look. `GTNLListWidget` reserves the scroll-bar width and draws its disabled handle when the content fits. Row snapping includes both scroll endpoints. See `HandleScrollData.rows(int)` for layout alignment requirements. Dragging the handle is anchored to the offset at the press, so holding the handle still never moves long lists. A press on the track beside the handle moves the handle's centre to the pointer at once, and the same press keeps dragging from there. This is a deliberate GTNLLib choice: AE2 pages instead, and `pageOnTrackPress(true)` gives AE2's behaviour, moving one wheel step towards the pointer (one row after `rows(int)`) and, while the button is held, again after 250 ms and then every 150 ms. AE2's scroll bars move by a page size each screen chooses, for the wheel and the track alike: one row in the network status and crafting screens, a sixth of the rows in terminals and a third in the crafting CPU list; for a larger step call `setScrollSpeed(rows * rowHeight)` after `rows(int)`. Only the left button scrolls a `GTNLListWidget`'s scroll bar.

### Stand-alone Scroll Bar

```java
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.widget.RowScrollbar;

RowScrollbar bar = new RowScrollbar(() -> firstRow, () -> rowCount - visibleRows, row -> firstRow = row)
    .size(AE2Theme.BIG_SCROLLER_WIDTH, visibleRows * 18 + 2);
```

`RowScrollbar` is AE2's scroll bar for content a widget draws or places itself, such as a grid, a table or a list of drawn rows. It keeps no position of its own: it reads the first visible row and the last reachable one from the owner and passes every new row back. The wheel over the bar moves `step(int)` rows (default 1). A press beside the handle moves the handle's centre to the pointer and keeps dragging; `pageOnTrackPress(true)` makes it page `step(int)` rows towards the pointer and repeat while held, like AE2. Forward the wheel over the content with `bar.scroll(direction)`. Under AE2 themes it draws AE2's groove and 12×15 handle (`small(true)` for the 7×15 one, `AE2Theme.SMALL_SCROLLER_WIDTH` wide); other themes get the vanilla scroll bar, 14 pixels wide, or a thin bar with `plainBar(color, visibleRows)`. The bar needs `AE2Theme.SCROLLER_HEIGHT + 2` pixels of height at least.

### Integer Input

```java
import java.util.OptionalLong;

import com.gtnllib.gui.widget.NumberEntry;

NumberEntry amount = new NumberEntry().bounds(1, 1_000_000)
    .value(64)
    .available(() -> canEdit);
OptionalLong requested = amount.value();
```

The entry follows the theme it is drawn with, validation feedback included: invalid text is AE2's input error color under AE2 themes and a plain red otherwise. The older constructor that takes an `AE2Variant` still works and ignores it. While `available` reports false the entry looks like AE2's disabled amount entry: disabled buttons, the dark uneditable field background and grey text; input is ignored and the field gives up keyboard focus. Invalid input turns the text red and lists the problems in the field's tooltip; the tall layout also shows AE2's 16×16 warning icon to the right of the field (its tooltip area is the top-left 8×8 pixels, as in AE2). `value()` returns an empty result for invalid input. `follow(LongSupplier)` keeps the field showing a value that other controls can change too, except while the player is typing in it. `isInputValid()` checks the input; inherited `isValid()` reports the MUI2 widget lifecycle. Callback semantics are defined by `NumberEntry.onChange(LongConsumer)`.

The layout follows the size: from 62 pixels high it shows the AE2 step buttons stretched to the width, and lower entries are one row of `<`, the text field and `>`, such as `.size(64, 16)`. The arrows step by 1, or 10 with Shift, and so does the mouse wheel over the field after `wheel(true)`. The wheel steps only while the pointer is over the text field, even after the player has clicked into it, as in AE2. Right-clicking the field applies `resetValue(long)` (default 0) clamped to the bounds.

## Moved And Renamed After 1.1.8

These types moved without a forwarder, so code built against 1.1.8 has to be rebuilt; update the imports:

| 1.1.8 | Now |
| --- | --- |
| `AE2Theme.Variant` | `ae2.AE2Variant` (`theme()` and `menu()` are now `panelTheme()` and `menuTheme()`) |
| `AE2Theme.Colors` | `ae2.AE2Colors`, with `AE2Colors.builder()` |
| `AE2Theme.Icon` | `ae2.AE2Icon` |
| `AE2Theme.ButtonText` | `ae2.ButtonText` |
| `widget.AE2ButtonDrawable` | `theme.ae2.AE2ButtonDrawable` |
| `widget.SkinDrawable`, `CardDrawable`, `FrameDrawable`, `LayoutTheme` | the same names in `theme` |

These names still work and are deprecated: `AE2Theme.SCROLLBAR_WIDTH` (now `SMALL_SCROLLER_WIDTH`), `AE2Theme.UPGRADE_ROWS` (now `UPGRADE_SLOTS_PER_COLUMN`) and `SlideSwitch.liveWhen` (now `glowWhen`).

## Texture Attribution

Texture origins, transformations and licensing are specified in [AE2-gui-theme-NOTICE.txt](src/main/resources/META-INF/licenses/AE2-gui-theme-NOTICE.txt).

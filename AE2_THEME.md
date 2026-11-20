# AE2 Theme API

GTNLLib provides light, dark and purple-highlighted dark AE2 themes for ModularUI 2. Their source Javadoc defines parameter units, return values, null handling and lifecycle requirements.

ModularUI 2 builds a GUI that the server opens (a machine, a held item) on both sides. `AE2Theme`, its drawable factories and every widget below load on both sides, so they can be used in such GUIs; they draw only on the client. `ServerSideLoadingTest` builds them in a class loader that behaves like a dedicated server. Theme registration (`AE2Theme.register`) is client-only and is done by GTNLLib.

## API Reference

| Type | Responsibility |
| --- | --- |
| [AE2Theme](src/main/java/com/gtnllib/gui/theme/AE2Theme.java) | Theme registration, variant lookup, drawable factories and direct drawing helpers. Includes the `Variant`, `Colors`, `ButtonText` and `Icon` types. |
| [ScreenTheme](src/main/java/com/gtnllib/gui/theme/ScreenTheme.java) | Persistable selection of the default, light, dark or purple dark panel theme. |
| [HandleScrollData](src/main/java/com/gtnllib/gui/widget/HandleScrollData.java) | Vertical scrolling with a fixed-size handle and optional row snapping; a press on the track beside the handle pages towards the pointer, as in AE2. |
| [GTNLListWidget](src/main/java/com/gtnllib/gui/widget/GTNLListWidget.java) | List layout, initial scroll position and persistent scroll-bar integration; keeps paging while a track press is held. |
| [RowScrollbar](src/main/java/com/gtnllib/gui/widget/RowScrollbar.java) | Stand-alone AE2 scroll bar over whole positions (usually rows) for widgets that draw their own content, like AE2's `Scrollbar`: the owner keeps the position, and the bar drags, pages, repeats and takes the wheel. |
| [IPersistentScrollbar](src/main/java/com/gtnllib/gui/widget/IPersistentScrollbar.java) | Extension contract for scroll data that retains an inactive track. |
| [NumberEntry](src/main/java/com/gtnllib/gui/widget/NumberEntry.java) | Bounded integer input of any size, expression parsing, step buttons or `<` `>` arrows, wheel stepping, right-click reset and validation feedback; `available` gives it AE2's disabled look. |
| [VerticalButtonBar](src/main/java/com/gtnllib/gui/widget/VerticalButtonBar.java) | AE2's vertical toolbar left of a panel: 18×20 icon buttons 22 pixels apart on a strip that covers the panel's left border; on/off buttons show AE2's selected frame while on. |
| [IconRowList](src/main/java/com/gtnllib/gui/widget/IconRowList.java) | Fast drawn list rows with icons, details (optionally in their own color), dropdowns, switches and `NumberEntry` fields; AE2 inset, highlights and scroll bar under AE2 themes. `Row.withAvailable(false)` draws a row muted and ignores its clicks. Its scroll bar is a `RowScrollbar` moving one row per notch or track press. Put a `ValueStepper` in a `ListRow` for a stepper row. |
| [ListRow](src/main/java/com/gtnllib/gui/widget/ListRow.java) | A widget row drawn like an `IconRowList` row: label, then a control widget or switch against the right end; clickable rows highlight, and `available` mutes the row. For lists that mix MUI2 controls with rows. |
| [ActionButton](src/main/java/com/gtnllib/gui/widget/ActionButton.java) | Themed text button or 18×20 toolbar icon button with a left-click action, optional available and selected states; an unavailable button swallows left clicks. |
| [ValueStepper](src/main/java/com/gtnllib/gui/widget/ValueStepper.java) | `<` value `>` stepper over named values such as voltage tiers; the mouse wheel steps too. |
| [CycleDropdown](src/main/java/com/gtnllib/gui/widget/CycleDropdown.java) | Dropdown that keeps its menu inside the panel (`menuWidth`); `iconOption` builds an item-icon option row with a scrolling name. Under AE2 themes the shown value and its caret use the button text colors and sink on hover like a button label. |
| [SlideSwitch](src/main/java/com/gtnllib/gui/widget/SlideSwitch.java) | Theme-driven on/off switch with a label; AE2 themes give it AE2's switch sprites, with the label four pixels below the sprite top as in AE2. |
| [GhostItemSlot](src/main/java/com/gtnllib/gui/widget/GhostItemSlot.java) | Item slot for client-only screens that takes an item dragged from NEI, with an optional filter and change callback. |

AE2 draws its toolbar strip after the panel, anchored three pixels inside the panel's left edge, so the strip's right two pixels hide the panel border and the two read as one piece. Place `VerticalButtonBar` at `panelLeft - WIDTH + OVERLAP` and add it after the panel's other children. A panel that also holds the bar starts its background `WIDTH - OVERLAP` pixels right of its own left edge; `AE2Theme.panelBackground(left, right)` draws the panel texture between such insets.

Pages are switched with AE2's page tabs (`AE2Theme.pageTab`), the mode tabs of the pattern encoding terminal: 22×22 tabs stacked `PAGE_TAB_PITCH` apart on the panel's right edge, starting `PAGE_TAB_INSET` pixels inside it and added after the panel. The selected tab covers the panel border and joins the panel; the others stand out. Toolbar buttons (`AE2Theme.iconButton`) are actions, not pages; their `selected` overload marks a toggled action.

AE2 lists keep their scroll bar beside the list inset, on the panel background, with eight pixels from the inset edge to the groove (as in AE2's network status and crafting CPU screens); the groove is never drawn inside the inset. `IconRowList` reserves `AE2_SCROLL_COLUMN` for it, so align controls above a list to `width - AE2_SCROLL_COLUMN` under AE2 themes. `AE2Theme.well(width, plainColor)` draws such an inset for a page of your own widgets (`drawWell` for code that draws directly). Switches in list rows and `AE2Theme.toggleSwitch` share `AE2Theme.drawSwitch`: AE2's switch sprite under AE2 themes, MUI2's check box otherwise. `ListRow`s as wide as the inset put their text and controls where `IconRowList`'s own rows put them, so pages built from either line up. A scrolling list of `ListRow`s placed one pixel inside its inset, top and bottom, clips its rows inside the inset border while `HandleScrollData` still draws the groove over the inset's full height.

`NumberEntry`, `CycleDropdown`, `ValueStepper` and `StepSlider` step their value with the mouse wheel while the pointer is over them (for `NumberEntry`, over its text field), also inside a scrolling list. Anywhere else in the list the wheel scrolls the list. MUI2 offers the wheel to a text field with keyboard focus first, wherever the pointer is; `NumberEntry` passes it on unless the pointer is over the field, so clicking into a number field and moving away never turns the wheel into a hidden value change.

AE2 buttons are 20 pixels high, and at that height `AE2ButtonDrawable` matches AE2 row for row. Lower buttons, such as the 16-pixel arrows of a compact `NumberEntry` or a `ValueStepper`, follow a GTNLLib rule rather than AE2's, on purpose. The normal shadow is `height - 13` pixels, clamped to 1–3: 3 from 16 pixels up, 2 at 15 and 1 at 14 and below. A hovered button's shadow is one pixel thinner and a disabled button's two, never below zero. The face sinks by the pixels the shadow lost, and the label stays centred on the face. AE2 1.21 itself nine-slices its button sprite with a 3-pixel border. Below 20 pixels that leaves a 2-pixel shadow, and from 18 pixels down the face also loses its bottom highlight row, so a 16-pixel GTNLLib button has a thicker shadow than AE2's. Above 20 pixels GTNLLib stretches the face, while AE2 repeats a light and dark stripe across the middle. `AE2ButtonDrawable.shadow(state, height)` and `textY(state, height)` return the shadow and the label offset for custom drawing.

`AE2Theme.label` places button text as AE2 does: normal, hovered and disabled labels move with the face, and under AE2 themes a label wider than the button scrolls back and forth inside it. MUI2's own buttons (close buttons, toggle buttons, dialog buttons) get the AE2 button background from the theme, but MUI2 places their text itself: centred, one pixel lower than AE2's normal label and without sinking on hover. Use `ActionButton` or `AE2Theme.button` with `AE2Theme.label` where the text should behave like AE2's.

Switch and radio labels start four pixels below the sprite's top, as in AE2's `AECheckbox`. The small scroll bar (`HandleScrollData(variant, true)`) draws the groove AE2 uses for its small scroll bar in the processing pattern panel: a one-pixel line-colored outline around a raised fill. Scroll-bar handles never stretch in AE2; MUI2 scroll widgets that do not use `HandleScrollData` stretch the theme's small handle to their own length.

A background set with `background(...)` on a MUI2 `ItemSlot` disappears while the slot holds an item (`ItemSlot.getCurrentBackground` in MUI2 2.3.94). The theme's slot frame is drawn separately and stays, so leave slot frames to the theme or draw them in the panel background.

## Theme Selection

| Selection | Panel theme | Menu theme |
| --- | --- | --- |
| `ScreenTheme.DEFAULT` | Screen default (`themeId == null`) | `modularui.context_menu` |
| `ScreenTheme.AE2_LIGHT` | `gtnllib:ae2_light` | `gtnllib:ae2_light_menu` |
| `ScreenTheme.AE2_DARK` | `gtnllib:ae2_dark` | `gtnllib:ae2_dark_menu` |
| `ScreenTheme.AE2_DARK_PURPLE` | `gtnllib:ae2_dark_purple` | `gtnllib:ae2_dark_purple_menu` |

GTNLLib registers these themes during client initialization. Apply the selected identifier through MUI2's screen theme registration. `AE2Theme.useMenuTheme` maps named child menu panels to the matching menu theme.

The purple dark theme shares the dark palette and replaces AE2's light-blue selection with fluix purple. Custom selection drawing should read `Variant.highlight`, `highlightTop`, `highlightShadow` and `highlightText` instead of the `AE2Theme.HIGHLIGHT` constants, so it matches every variant.

![AE2 dark and purple dark themes](.github/images/ae2-dark-purple-preview.png)

## Usage Examples

### Row-Based List

```java
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.AE2Theme.Variant;
import com.gtnllib.gui.widget.GTNLListWidget;
import com.gtnllib.gui.widget.HandleScrollData;

Variant variant = AE2Theme.variant(themeId); // the panel's theme; null outside the AE2 themes
HandleScrollData scrolling = new HandleScrollData(variant).rows(22);
ListWidget<IWidget, ?> list = new GTNLListWidget<>().scrollDirection(scrolling)
    .size(180, 110);
```

`HandleScrollData` keeps the variant it was created with, so pass the variant of the panel's theme rather than a fixed one; a fixed `Variant.LIGHT` stays light when the player picks the dark theme. `GTNLListWidget` reserves the scroll-bar width and draws its disabled handle when the content fits. Row snapping includes both scroll endpoints. See `HandleScrollData.rows(int)` for layout alignment requirements. Dragging the handle is anchored to the offset at the press, so holding the handle still never moves long lists. A press on the track beside the handle pages a third of the view towards the pointer, as AE2's crafting CPU list does, and holding it keeps paging, first after 250 ms and then every 150 ms, like AE2. (AE2's scroll bars move by a page size each screen chooses, for the wheel and the track alike: one row in the network status and crafting screens, a sixth of the rows in terminals.) Only the left button scrolls a `GTNLListWidget`'s scroll bar. The press changes the scroll data's offset at once and starts no drag, so a widget that calls `ScrollArea.mouseClicked` itself and expects a drag there should call `pageOnTrackPress(false)`, which restores the old jump-and-drag behaviour.

### Stand-alone Scroll Bar

```java
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.widget.RowScrollbar;

RowScrollbar bar = new RowScrollbar(() -> firstRow, () -> rowCount - visibleRows, row -> firstRow = row)
    .size(AE2Theme.BIG_SCROLLER_WIDTH, visibleRows * 18 + 2);
```

`RowScrollbar` is AE2's scroll bar for content a widget draws or places itself, such as a grid, a table or a list of drawn rows. It keeps no position of its own: it reads the first visible row and the last reachable one from the owner and passes every new row back. The wheel over the bar and a press beside its handle move `step(int)` rows (default 1); hold the press and it repeats like AE2. Forward the wheel over the content with `bar.scroll(direction)`. Under AE2 themes it draws AE2's groove and 12×15 handle (`small(true)` for the 7×15 one, `AE2Theme.SCROLLBAR_WIDTH` wide); other themes get the vanilla scroll bar, 14 pixels wide, or a thin bar with `plainBar(color, visibleRows)`. The bar needs `AE2Theme.SCROLLER_HEIGHT + 2` pixels of height at least.

### Integer Input

```java
import java.util.OptionalLong;

import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.widget.NumberEntry;

NumberEntry amount = new NumberEntry(AE2Theme.variant(themeId)).bounds(1, 1_000_000)
    .value(64)
    .available(() -> canEdit);
OptionalLong requested = amount.value();
```

The variant passed to the constructor colors the validation feedback, so pass the panel's variant here too. While `available` reports false the entry looks like AE2's disabled amount entry: disabled buttons, the dark uneditable field background and grey text; input is ignored and the field gives up keyboard focus. Invalid input turns the text red and shows AE2's 16×16 warning icon to the right of the field (its tooltip area is the top-left 8×8 pixels, as in AE2). `value()` returns an empty result for invalid input. `follow(LongSupplier)` keeps the field showing a value that other controls can change too, except while the player is typing in it. `isInputValid()` checks the input; inherited `isValid()` reports the MUI2 widget lifecycle. Callback semantics are defined by `NumberEntry.onChange(LongConsumer)`.

The layout follows the size: from 62 pixels high it shows the AE2 step buttons stretched to the width, and lower entries are one row of `<`, the text field and `>`, such as `.size(64, 16)`. The arrows and the mouse wheel over the field step by 1, or 10 with Shift. The wheel steps only while the pointer is over the text field, even after the player has clicked into it, as in AE2. Right-clicking the field applies `resetValue(long)` (default 0) clamped to the bounds.

## Texture Attribution

Texture origins, transformations and licensing are specified in [AE2-gui-theme-NOTICE.txt](src/main/resources/META-INF/licenses/AE2-gui-theme-NOTICE.txt).

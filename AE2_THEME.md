# AE2 Theme API

GTNLLib provides light, dark and purple-highlighted dark AE2 themes for ModularUI 2. Rendering helpers and themed widgets are client-side APIs. Their source Javadoc defines parameter units, return values, null handling and lifecycle requirements.

## API Reference

| Type | Responsibility |
| --- | --- |
| [AE2Theme](src/main/java/com/gtnllib/gui/theme/AE2Theme.java) | Theme registration, variant lookup, drawable factories and direct drawing helpers. Includes the `Variant`, `Colors`, `ButtonText` and `Icon` types. |
| [ScreenTheme](src/main/java/com/gtnllib/gui/theme/ScreenTheme.java) | Persistable selection of the default, light, dark or purple dark panel theme. |
| [HandleScrollData](src/main/java/com/gtnllib/gui/widget/HandleScrollData.java) | Vertical scrolling with a fixed-size handle and optional row snapping. |
| [GTNLListWidget](src/main/java/com/gtnllib/gui/widget/GTNLListWidget.java) | List layout, initial scroll position and persistent scroll-bar integration. |
| [IPersistentScrollbar](src/main/java/com/gtnllib/gui/widget/IPersistentScrollbar.java) | Extension contract for scroll data that retains an inactive track. |
| [NumberEntry](src/main/java/com/gtnllib/gui/widget/NumberEntry.java) | Bounded integer input of any size, expression parsing, step buttons or `<` `>` arrows, wheel stepping, right-click reset and validation feedback. |
| [VerticalButtonBar](src/main/java/com/gtnllib/gui/widget/VerticalButtonBar.java) | AE2's vertical toolbar left of a panel: 18×20 icon buttons 22 pixels apart on a strip that covers the panel's left border; on/off buttons show AE2's selected frame while on. |
| [IconRowList](src/main/java/com/gtnllib/gui/widget/IconRowList.java) | Fast drawn list rows with icons, details (optionally in their own color), dropdowns, switches and `NumberEntry` fields; AE2 inset, highlights and scroll bar under AE2 themes. Put a `ValueStepper` in a `ListRow` for a stepper row. |
| [ListRow](src/main/java/com/gtnllib/gui/widget/ListRow.java) | A widget row drawn like an `IconRowList` row: label, then a control widget or switch against the right end; clickable rows highlight. For lists that mix MUI2 controls with rows. |
| [ActionButton](src/main/java/com/gtnllib/gui/widget/ActionButton.java) | Themed text button or 18×20 toolbar icon button with a left-click action, optional available and selected states; an unavailable button swallows left clicks. |
| [ValueStepper](src/main/java/com/gtnllib/gui/widget/ValueStepper.java) | `<` value `>` stepper over named values such as voltage tiers; the mouse wheel steps too. |
| [CycleDropdown](src/main/java/com/gtnllib/gui/widget/CycleDropdown.java) | Dropdown that keeps its menu inside the panel (`menuWidth`); `iconOption` builds an item-icon option row with a scrolling name. |

AE2 draws its toolbar strip after the panel, anchored three pixels inside the panel's left edge, so the strip's right two pixels hide the panel border and the two read as one piece. Place `VerticalButtonBar` at `panelLeft - WIDTH + OVERLAP` and add it after the panel's other children. A panel that also holds the bar starts its background `WIDTH - OVERLAP` pixels right of its own left edge; `AE2Theme.panelBackground(left, right)` draws the panel texture between such insets.

Pages are switched with AE2's page tabs (`AE2Theme.pageTab`), the mode tabs of the pattern encoding terminal: 22×22 tabs stacked `PAGE_TAB_PITCH` apart on the panel's right edge, starting `PAGE_TAB_INSET` pixels inside it and added after the panel. The selected tab covers the panel border and joins the panel; the others stand out. Toolbar buttons (`AE2Theme.iconButton`) are actions, not pages; their `selected` overload marks a toggled action.

AE2 lists keep their scroll bar beside the list inset, on the panel background, with seven pixels from the inset edge to the groove; the groove is never drawn inside the inset. `IconRowList` reserves `AE2_SCROLL_COLUMN` for it, so align controls above a list to `width - AE2_SCROLL_COLUMN` under AE2 themes. `AE2Theme.well(width, plainColor)` draws such an inset for a page of your own widgets (`drawWell` for code that draws directly). Switches in list rows and `AE2Theme.toggleSwitch` share `AE2Theme.drawSwitch`: AE2's switch sprite under AE2 themes, MUI2's check box otherwise. `ListRow`s as wide as the inset put their text and controls where `IconRowList`'s own rows put them, so pages built from either line up. A scrolling list of `ListRow`s placed one pixel inside its inset, top and bottom, clips its rows inside the inset border while `HandleScrollData` still draws the groove over the inset's full height.

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
import com.gtnllib.gui.theme.AE2Theme.Variant;
import com.gtnllib.gui.widget.GTNLListWidget;
import com.gtnllib.gui.widget.HandleScrollData;

HandleScrollData scrolling = new HandleScrollData(Variant.LIGHT).rows(22);
ListWidget<IWidget, ?> list = new GTNLListWidget<>().scrollDirection(scrolling)
    .size(180, 110);
```

`GTNLListWidget` reserves the scroll-bar width and draws its disabled handle when the content fits. Row snapping includes both scroll endpoints. See `HandleScrollData.rows(int)` for layout alignment requirements. Dragging the handle is anchored to the offset at the press, so holding the handle still never moves long lists.

### Integer Input

```java
import java.util.OptionalLong;

import com.gtnllib.gui.theme.AE2Theme.Variant;
import com.gtnllib.gui.widget.NumberEntry;

NumberEntry amount = new NumberEntry(Variant.LIGHT).bounds(1, 1_000_000)
    .value(64);
OptionalLong requested = amount.value();
```

`value()` returns an empty result for invalid input. `follow(LongSupplier)` keeps the field showing a value that other controls can change too, except while the player is typing in it. `isInputValid()` checks the input; inherited `isValid()` reports the MUI2 widget lifecycle. Callback semantics are defined by `NumberEntry.onChange(LongConsumer)`.

The layout follows the size: from 62 pixels high it shows the AE2 step buttons stretched to the width, and lower entries are one row of `<`, the text field and `>`, such as `.size(64, 16)`. The arrows and the mouse wheel over the field step by 1, or 10 with Shift. Right-clicking the field applies `resetValue(long)` (default 0) clamped to the bounds.

## Texture Attribution

Texture origins, transformations and licensing are specified in [AE2-gui-theme-NOTICE.txt](src/main/resources/META-INF/licenses/AE2-gui-theme-NOTICE.txt).

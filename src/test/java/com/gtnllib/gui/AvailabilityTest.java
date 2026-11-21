package com.gtnllib.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.cleanroommc.modularui.api.widget.Interactable.Result;
import com.gtnllib.gui.widget.AE2Button;
import com.gtnllib.gui.widget.Availability;
import com.gtnllib.gui.widget.CycleDropdown;
import com.gtnllib.gui.widget.ListRow;
import com.gtnllib.gui.widget.SlideSwitch;
import com.gtnllib.gui.widget.StepSlider;
import com.gtnllib.gui.widget.ValueStepper;

public class AvailabilityTest {

    @Test
    public void swallowsPresses() {
        Availability availability = new Availability();
        int[] handled = { 0 };
        Result open = availability.press(() -> {
            handled[0]++;
            return Result.SUCCESS;
        });
        assertEquals(Result.SUCCESS, open, "an available widget answers the press itself");
        availability.set(() -> false);
        Result shut = availability.press(() -> {
            handled[0]++;
            return Result.SUCCESS;
        });
        assertEquals(Result.STOP, shut, "an unavailable widget stops the press");
        assertEquals(1, handled[0], "an unavailable widget does not run its own handling");
    }

    @Test
    public void widgetsSwallow() {
        int[] clicks = { 0 };
        Result button = new AE2Button(() -> "Apply", () -> clicks[0]++).available(() -> false)
            .onMousePressed(0);
        assertEquals(Result.STOP, button, "an unavailable button stops the press");
        Result row = new ListRow(() -> "Row").checkbox(() -> true, () -> clicks[0]++)
            .available(() -> false)
            .onMousePressed(0);
        assertEquals(Result.STOP, row, "an unavailable clickable row stops the press");
        Result stepper = new ValueStepper(() -> "LV", step -> clicks[0]++).available(() -> false)
            .onMousePressed(0);
        assertEquals(Result.STOP, stepper, "an unavailable stepper stops the press");
        Result slider = new StepSlider().value(() -> 0, value -> clicks[0]++)
            .available(() -> false)
            .onMousePressed(0);
        assertEquals(Result.STOP, slider, "an unavailable slider stops the press");
        Result slideSwitch = new SlideSwitch().onMousePressed(mouse -> clicks[0]++ >= 0)
            .available(() -> false)
            .onMousePressed(0);
        assertEquals(Result.STOP, slideSwitch, "an unavailable switch stops the press");
        Result dropdown = new CycleDropdown<>("availability_menu", String.class).available(() -> false)
            .onMousePressed(0);
        assertEquals(Result.STOP, dropdown, "an unavailable dropdown stops the press");
        assertEquals(0, clicks[0], "no unavailable widget ran its action");
        assertEquals(
            Result.IGNORE,
            new ListRow(() -> "Plain row").available(() -> false)
                .onMousePressed(0),
            "a row without a click action lets the press through, available or not");
        assertEquals(
            Result.IGNORE,
            new ValueStepper(() -> "LV", step -> {}).onMousePressed(0),
            "an available stepper leaves the press to its arrow buttons");
    }
}

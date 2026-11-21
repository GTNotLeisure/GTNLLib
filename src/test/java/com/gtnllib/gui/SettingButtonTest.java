package com.gtnllib.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.IntValue;
import com.gtnllib.gui.widget.SettingButton;

public class SettingButtonTest {

    @Test
    public void cyclesBothWays() {
        IntValue value = new IntValue(0);
        SettingButton button = new SettingButton().value(value)
            .stateCount(3)
            .playClickSound(false);
        button.onMousePressed(0);
        assertEquals(1, value.getIntValue(), "a left click steps to the next value");
        button.onMousePressed(1);
        button.onMousePressed(1);
        assertEquals(2, value.getIntValue(), "a right click steps back and wraps to the last value");
        button.onMousePressed(0);
        assertEquals(0, value.getIntValue(), "a left click on the last value wraps to the first");
    }

    @Test
    public void flipsBoolean() {
        BoolValue value = new BoolValue(false);
        SettingButton button = new SettingButton().value(value)
            .playClickSound(false);
        button.onMousePressed(1);
        assertEquals(true, value.getBoolValue(), "either click flips a two-icon toggle");
    }

    @Test
    public void unavailableIgnoresClicks() {
        IntValue value = new IntValue(1);
        SettingButton button = new SettingButton().value(value)
            .stateCount(3)
            .playClickSound(false)
            .available(() -> false);
        button.onMousePressed(0);
        button.onMousePressed(1);
        assertEquals(1, value.getIntValue(), "an unavailable button keeps its value");
    }
}

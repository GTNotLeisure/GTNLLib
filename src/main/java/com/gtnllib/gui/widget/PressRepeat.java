package com.gtnllib.gui.widget;

import org.lwjgl.input.Mouse;

/**
 * Repeats a held left-button press the way AE2's scroll bars page: the owner acts once on the press, again after
 * {@value #FIRST_DELAY_MILLIS} ms and then every {@value #INTERVAL_MILLIS} ms until the button is released. The owner
 * calls {@link #start(int)} on the press, acts once, and then acts once more each time {@link #due()}, called every
 * client tick, returns true.
 */
public class PressRepeat {

    /** Delay before a held press acts again, and the interval between later repeats, in milliseconds. */
    public static final int FIRST_DELAY_MILLIS = 250, INTERVAL_MILLIS = 150;

    private int direction;
    private long next;

    /**
     * Starts repeating a press.
     *
     * @param direction which way the press moves things, such as {@code -1} or {@code 1}; must not be zero
     */
    public void start(int direction) {
        this.direction = direction;
        next = System.currentTimeMillis() + FIRST_DELAY_MILLIS;
    }

    /** Stops repeating, such as when the owner sees the button released. */
    public void stop() {
        direction = 0;
    }

    /**
     * Returns which way the held press moves things.
     *
     * @return the direction given to {@link #start(int)}, or zero while no press repeats
     */
    public int direction() {
        return direction;
    }

    /**
     * Reports whether a press is repeating.
     *
     * @return true between {@link #start(int)} and the release
     */
    public boolean isActive() {
        return direction != 0;
    }

    /**
     * Reports whether the held press should act again now, and stops once the button is released.
     *
     * @return true at most once per interval while the press is held
     */
    public boolean due() {
        if (direction == 0) return false;
        // MUI2 sends the release only to widgets under the pointer, so a press held off the widget never ends there.
        if (!Mouse.isButtonDown(0)) {
            direction = 0;
            return false;
        }
        long now = System.currentTimeMillis();
        if (now < next) return false;
        next = now + INTERVAL_MILLIS;
        return true;
    }
}

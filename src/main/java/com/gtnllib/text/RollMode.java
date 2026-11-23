package com.gtnllib.text;

public enum RollMode {

    AUTO(""),
    CUT("cut"),
    FADE("fade"),
    JUMP("jump");

    public final String id;

    RollMode(String id) {
        this.id = id;
    }

    /**
     * Returns the mode written as {@code id}, or null when the name is unknown or empty.
     */
    public static RollMode byId(String id) {
        for (RollMode mode : values()) {
            if (mode != AUTO && mode.id.equals(id)) return mode;
        }
        return null;
    }
}

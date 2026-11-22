package com.gtnllib.text;

public enum ShakeMotion {

    SMOOTH("smooth"),
    JUMP("jump");

    public final String id;

    ShakeMotion(String id) {
        this.id = id;
    }

    /**
     * Returns the motion written as {@code id}, or null when the name is unknown.
     */
    public static ShakeMotion byId(String id) {
        for (ShakeMotion motion : values()) {
            if (motion.id.equals(id)) return motion;
        }
        return null;
    }
}

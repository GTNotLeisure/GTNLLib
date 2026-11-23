package com.gtnllib.text;

public enum TextEntrance {

    NONE("", 0),
    DECODE("decode", 0.18f),
    FADE("fade", 0.25f),
    DROP("drop", 0.2f),
    SLAM("slam", 0.3f);

    public final String id;
    public final float defaultSeconds;

    TextEntrance(String id, float defaultSeconds) {
        this.id = id;
        this.defaultSeconds = defaultSeconds;
    }

    /**
     * Returns the entrance written as {@code id}, or null when the name is unknown or empty.
     */
    public static TextEntrance byId(String id) {
        for (TextEntrance entrance : values()) {
            if (entrance != NONE && entrance.id.equals(id)) return entrance;
        }
        return null;
    }
}

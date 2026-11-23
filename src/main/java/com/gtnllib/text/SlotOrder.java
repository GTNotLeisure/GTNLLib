package com.gtnllib.text;

public enum SlotOrder {

    NONE(""),
    RIGHT_FIRST("right"),
    LEFT_FIRST("left");

    public final String id;

    SlotOrder(String id) {
        this.id = id;
    }

    /**
     * Returns the order written as {@code id}, or null when the name is unknown or empty.
     */
    public static SlotOrder byId(String id) {
        for (SlotOrder order : values()) {
            if (order != NONE && order.id.equals(id)) return order;
        }
        return null;
    }
}

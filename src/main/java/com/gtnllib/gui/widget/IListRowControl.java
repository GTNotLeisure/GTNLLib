package com.gtnllib.gui.widget;

public interface IListRowControl {

    /**
     * Tells the control that it is the control of a list row. {@link ListRow#control} calls it once, when the control
     * is placed in the row.
     */
    void placedInListRow();
}

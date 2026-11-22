package com.gtnllib.gui.theme.ae2;

/**
 * Text style resolved for a button state.
 *
 * @param color  ARGB text color
 * @param y      vertical offset from the button's top edge in pixels
 * @param shadow whether to draw a drop shadow
 */
public record ButtonText(int color, int y, boolean shadow) {}

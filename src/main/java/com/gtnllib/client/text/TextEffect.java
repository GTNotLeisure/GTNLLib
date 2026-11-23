package com.gtnllib.client.text;

import com.gtnllib.text.TextEffectColors;

public interface TextEffect extends AutoCloseable {

    void render(TextRenderContext context);

    default float padding(float height) {
        return height;
    }

    default float padding(float width, float height) {
        return padding(height);
    }

    default int fallbackColor() {
        return TextEffectColors.rgb(TextEffectColors.effectWhite);
    }

    @Override
    default void close() {}
}

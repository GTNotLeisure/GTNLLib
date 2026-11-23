package com.gtnllib.client.text.effect;

import net.minecraft.util.ResourceLocation;

import com.gtnllib.client.text.TextRenderContext;
import com.gtnllib.text.TextEffectFormat;

public class NeonFlickerTextEffect extends ShaderTextEffect {

    public static final int DEFAULT_FAULT = 20;

    public NeonFlickerTextEffect(ResourceLocation fragment, int... colors) {
        super(fragment, colors);
    }

    @Override
    protected void configureUniforms(TextShader shader, TextRenderContext context) {
        shader.uniform1f(
            "faultShare",
            context.style()
                .option(TextEffectFormat.FAULT, DEFAULT_FAULT) / 100f);
    }
}

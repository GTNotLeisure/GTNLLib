#version 120

uniform sampler2D sceneTex;
uniform vec2 viewOrigin;
uniform vec2 viewSize;
uniform float time;
uniform float mode;
uniform float strength;
uniform float holeRatio;
uniform float fillMix;
uniform float flashAmount;
uniform float lensScale;
uniform vec2 copyMin;
uniform vec2 copyMax;
uniform vec3 voidColor;
uniform vec3 primaryColor;
uniform vec3 highlightColor;

varying vec2 vUv;

#include "noise.glsl"

void main() {
    vec2 p = vUv * 2.0 - 1.0;
    float r = length(p);
    if (r >= 1.0) discard;

    vec2 dir = r > 1.0e-4 ? p / r : vec2(0.0, 1.0);
    vec2 tang = vec2(-dir.y, dir.x);
    float aspect = viewSize.x / max(1.0, viewSize.y);
    vec2 baseUv = (gl_FragCoord.xy - viewOrigin) / max(vec2(1.0), viewSize);

    // Mode 1: Pinpoint vertical needle spike inside wide misty radial vapor cloud.
    if (mode >= 0.5 && mode < 1.5) {
        float radialFade = 1.0 - smoothstep(0.58, 1.0, r);
        float mistN = fbm3(vec3(p * 3.0, time * 1.4), 2.08);
        float vaporCloud = exp(-r * 2.1) * (0.55 + 0.55 * mistN) * flashAmount * 0.78;
        float star = exp(-r * 19.0) * 1.65 + exp(-r * 6.5) * 0.45;
        float needle = exp(-abs(p.x) * 86.0) * exp(-abs(p.y) * 3.2) * 2.1 * flashAmount;
        float horiz = exp(-abs(p.y) * 52.0) * exp(-abs(p.x) * 4.2) * 0.48 * flashAmount;
        float softCore = exp(-r * 4.2) * 0.36;
        float intensity = (star + needle + horiz + softCore + vaporCloud) * radialFade * strength;
        vec3 vaporCol = mix(primaryColor, highlightColor, clamp(mistN * 1.15, 0.0, 1.0));
        vec3 col = mix(vaporCol, vec3(1.0, 1.0, 1.0), clamp((star + needle) * 0.82, 0.0, 1.0));
        gl_FragColor = vec4(col, clamp(intensity, 0.0, 0.96));
        return;
    }

    vec2 uvOffset = vec2(0.0);
    float holeMask = 0.0;
    vec3 overlayCol = vec3(0.0);
    float overlayAlpha = 0.0;
    float alpha = 0.0;

    if (mode < 0.5) {
        // Mode 0: Sharp void wormhole + thin white rim + lumpy pale mist band + mild gravitational lensing.
        if (r >= 0.98) discard;
        float hr = max(0.02, holeRatio);
        float rn = r / hr;
        float quadFade = 1.0 - smoothstep(0.62, 0.98, r);

        float spin = 0.9 * log(max(rn, 0.05)) - time * 0.9;
        float cs = cos(spin);
        float sn = sin(spin);
        vec2 spiralDir = vec2(dir.x * cs - dir.y * sn, dir.x * sn + dir.y * cs);
        float nSwirl = fbm3(vec3(spiralDir * 3.2, rn * 1.4 - time * 0.5), 2.08);
        float nFine = fbm3(vec3(spiralDir * 9.0, rn * 3.0 - time * 1.2), 2.08);
        float nCloud = fbm3(vec3(p * 3.4 - dir * time * 0.65, time * 0.85), 2.08);

        float outside = smoothstep(0.90, 1.05, rn);
        float lensFalloff = 1.0 / (0.16 + (rn - 0.94) * (rn - 0.94) * 3.4);
        float lensEnv = outside * clamp(lensFalloff, 0.0, 2.0) * quadFade;

        float radialBend = (0.05 * lensEnv + 0.015 * (nSwirl - 0.5) * lensEnv) * strength;
        float swirlBend = (0.02 * lensEnv * (0.65 + 0.7 * nFine)) * strength;
        uvOffset = -(dir * radialBend + tang * swirlBend) / vec2(aspect, 1.0);

        float edge = rn + (nFine - 0.5) * 0.012;
        holeMask = (1.0 - smoothstep(0.96, 1.0, edge)) * clamp(strength * 1.5, 0.0, 1.0);

        float lumps = fbm3(vec3(spiralDir * 1.5, time * 0.35), 2.08) * 0.7 + nCloud * 0.3;
        float dMist = (rn - 1.08 - 0.20 * (lumps - 0.42)) / 0.17;
        float puffs = smoothstep(0.20, 0.62, lumps);
        float mist = exp(-dMist * dMist) * (0.5 + 0.6 * puffs) * quadFade;
        float dRim = (rn - 1.012) / 0.022;
        float rim = exp(-dRim * dRim);

        vec3 pureWhite = vec3(0.98, 0.99, 1.0);
        vec3 pale = mix(highlightColor, pureWhite, clamp(puffs * 0.45, 0.0, 0.7));
        overlayCol = mix(primaryColor, pale, 1.0 - smoothstep(1.08, 1.32, rn - 0.15 * (lumps - 0.42)));
        overlayCol = mix(overlayCol, pureWhite, clamp(rim * 1.2, 0.0, 1.0));
        overlayAlpha = clamp((mist * 0.95 + rim * 1.2) * strength, 0.0, 0.98);

        if (flashAmount > 0.01) {
            float fl = (0.38 + exp(-r * 2.2) * 0.85) * (0.72 + 0.28 * nCloud) * flashAmount * quadFade;
            overlayCol = mix(overlayCol, mix(highlightColor, pureWhite, 0.45), clamp(fl, 0.0, 1.0));
            overlayAlpha = max(overlayAlpha, clamp(fl * 0.90, 0.0, 0.94));
        }

        alpha = clamp(max(holeMask, max(quadFade * clamp(lensEnv * 0.95, 0.0, 1.0), overlayAlpha)), 0.0, 1.0);
        alpha *= clamp(strength * 1.65, 0.0, 1.0);
    } else {
        // Mode 2: Turbulent transparent air-refraction shockwave shell.
        float ringR = clamp(holeRatio, 0.06, 0.92);
        if (abs(r - ringR) > 0.0275 + 3.5 * (0.042 + ringR * 0.055)) discard;
        float turb = fbm3(vec3(dir * 5.2, time * 2.8), 2.08) - 0.5;
        float sd = (r - ringR + turb * 0.055) / (0.042 + ringR * 0.055);
        float ring = exp(-sd * sd) * (1.0 - smoothstep(0.84, 1.0, r));
        uvOffset = dir * (0.125 * ring * (1.0 + 0.50 * turb) * strength) / vec2(aspect, 1.0);

        float crest = ring * ring * smoothstep(-0.32, 0.42, turb);
        overlayCol = mix(primaryColor, highlightColor, clamp(crest, 0.0, 1.0));
        overlayAlpha = clamp(crest * 0.32 * strength, 0.0, 0.42);
        alpha = clamp(ring * min(1.0, strength * 1.85), 0.0, 1.0);
    }

    uvOffset *= lensScale;
    vec3 refracted = texture2D(sceneTex, clamp(baseUv + uvOffset, copyMin, copyMax)).rgb;

    if (holeMask > 0.001) {
        float rn = r / max(0.02, holeRatio);
        vec3 darkInterior = mix(voidColor, primaryColor * 0.30, smoothstep(0.55, 1.0, rn) * 0.6);
        vec3 openInterior = mix(primaryColor, highlightColor, smoothstep(0.72, 1.0, rn));
        refracted = mix(refracted, mix(darkInterior, openInterior, fillMix), holeMask);
    }

    vec3 finalCol = mix(refracted, overlayCol, overlayAlpha);
    gl_FragColor = vec4(finalCol, alpha);
}

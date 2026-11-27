#version 120
#include "noise.glsl"

uniform float time;
uniform float progress;
uniform float strength;
uniform float mode;
uniform vec3 darkColor;
uniform vec3 midColor;
uniform vec3 crestColor;

varying vec2 vUv;

void main() {
    // Mode 1: Individual 3D billowing smoky vapor puff billboard.
    if (mode >= 0.5) {
        vec2 p = vUv * 2.0 - 1.0;
        float r = length(p);
        if (r >= 1.0) discard;
        vec3 wp = vec3(p * 2.4 + vec2(0.0, -time * 0.9), progress * 3.5 + time * 0.6);
        float n1 = fbm3(wp);
        float n2 = fbm3(wp * 2.2 + vec3(n1 * 1.4, -n1 * 1.2, 0.8));
        float cloud = smoothstep(0.24, 0.82, n1 * 0.55 + n2 * 0.50);
        float softBall = 1.0 - smoothstep(0.18, 1.0, r + (n1 - 0.5) * 0.35);
        float fade = (1.0 - smoothstep(0.32, 1.0, progress)) * smoothstep(0.0, 0.12, progress);
        float density = cloud * softBall * fade * strength;
        if (density < 0.015) discard;
        vec3 col = mix(darkColor, midColor, clamp(n2 * 1.25 + (p.y * 0.25 + 0.25), 0.0, 1.0));
        col = mix(col, crestColor, clamp(cloud * (0.5 + 0.5 * p.y) * 0.72, 0.0, 1.0));
        gl_FragColor = vec4(col, clamp(density * 0.78, 0.0, 0.78));
        return;
    }

    // Mode 0: Irregular ragged ground-hugging mist carpet.
    vec2 p = vUv * 2.0 - 1.0;
    float r = length(p);
    if (r > 1.0) discard;

    float frontR = 0.08 + progress * 0.90;
    float bandWidth = 0.11 + progress * 0.14;
    float trail = smoothstep(0.0, frontR, r) * (1.0 - smoothstep(frontR, frontR + 0.22, r));
    float fadeOut = (1.0 - smoothstep(0.32, 1.0, progress)) * (1.0 - smoothstep(0.68, 1.0, r));
    // fbm3 lies in [0, 0.91] and puffs in [0, 1], so this bounds density from above without any noise.
    float minFront = max(abs(r - frontR) - 0.08, 0.0) / bandWidth;
    float maxDensity = (exp(-minFront * minFront) * 1.27 + trail * 0.45) * fadeOut * strength;
    if (maxDensity < 0.015) discard;

    vec2 dir = r > 1.0e-4 ? p / r : vec2(1.0, 0.0);
    vec3 baseP = vec3(p * 4.4 - dir * progress * 5.2, time * 0.82);
    vec3 warp = vec3(fbm3(baseP), fbm3(baseP + vec3(3.7, -2.1, 1.4)), 0.0);
    float n1 = fbm3(baseP + warp * 1.05);
    float n2 = fbm3(baseP * 2.7 + warp * 0.65 + vec3(dir.x, dir.y, time * 0.5));
    float puffs = smoothstep(0.28, 0.82, n1 * 0.56 + n2 * 0.50);

    float dFront = (r - frontR + (n1 - 0.5) * 0.16) / bandWidth;
    float frontBand = exp(-dFront * dFront);

    float density = (frontBand * (0.32 + 0.95 * puffs) + trail * puffs * 0.45) * fadeOut * strength;
    if (density < 0.015) discard;

    vec3 col = mix(darkColor, midColor, clamp(puffs * 1.15, 0.0, 1.0));
    col = mix(col, crestColor, clamp(frontBand * puffs * 0.78, 0.0, 1.0));
    gl_FragColor = vec4(col, clamp(density * 0.68, 0.0, 0.72));
}

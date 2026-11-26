#version 120

uniform float time;
uniform float strength;
uniform vec3 primaryColor;
uniform vec3 secondaryColor;
uniform vec3 coreColor;

varying vec2 vUv;

float hash21(vec2 p) {
    p = fract(p * vec2(127.1, 311.7));
    p += dot(p, p + 19.19);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

void main() {
    float across = abs(vUv.y * 2.0 - 1.0);
    float along = vUv.x;

    float surge = 0.80 + 0.30 * noise(vec2(along * 22.0 - time * 30.0, time * 14.0));
    float endFade = smoothstep(0.0, 0.03, along) * (1.0 - smoothstep(0.84, 1.0, along));

    float core = exp(-across * across * 32.0);
    float mid = exp(-across * 4.6);
    float aura = exp(-across * 2.2) * (1.0 - across);

    vec3 col = mix(primaryColor, secondaryColor, clamp(mid * 1.15, 0.0, 1.0));
    col = mix(col, coreColor, clamp(core * 1.35, 0.0, 1.0));

    float alpha = clamp((core * 1.15 + mid * 0.78 + aura * 0.42) * surge * endFade * strength, 0.0, 1.0);
    if (alpha < 0.015) discard;
    gl_FragColor = vec4(col, alpha);
}

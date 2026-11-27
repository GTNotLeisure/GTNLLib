#version 120
#include "noise.glsl"

uniform float time;
uniform float strength;
uniform float whiteCore;
uniform vec2 centerXZ;
uniform float centerY;
uniform vec2 sizeXZ;
uniform float sizeY;
uniform vec3 primaryColor;
uniform vec3 highlightColor;

varying vec3 vLocal;

void main() {
    vec2 column = floor(vLocal.xz + 0.5);
    float colSeed = hash31(vec3(column.x, 0.0, column.y));
    float stream = 0.5 + 0.5 * sin(vLocal.y * 3.8 - time * 16.0 + colSeed * 12.56);
    float fine = 0.5 + 0.5 * sin(vLocal.y * 11.0 + time * 9.0 + colSeed * 6.28);
    float pulse = stream * 0.72 + fine * 0.28;

    vec3 halfSize = max(vec3(1.0), vec3(sizeXZ.x, sizeY, sizeXZ.y) * 0.5);
    vec3 normDelta = (vLocal - vec3(centerXZ.x, centerY, centerXZ.y)) / halfSize;
    float coreGlow = exp(-dot(normDelta, normDelta) * 2.5);

    vec3 pureWhite = vec3(1.0, 1.0, 1.0);
    vec3 col = mix(primaryColor, highlightColor, clamp(pulse * 0.92, 0.0, 1.0));
    float p2 = pulse * pulse;
    col = mix(col, pureWhite, clamp(coreGlow * whiteCore * 0.85 + p2 * p2 * 0.72, 0.0, 0.98));
    float lineGain = 0.4 + 0.6 * hash31(vec3(column.x, 5.0, column.y) + 0.37);
    float alpha = clamp((0.28 + 0.68 * pulse) * strength * lineGain, 0.0, 0.95);
    gl_FragColor = vec4(col, alpha);
}

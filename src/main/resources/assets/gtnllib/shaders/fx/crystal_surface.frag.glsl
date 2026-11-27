#version 120
#include "noise.glsl"

uniform sampler2D blockTex;
uniform float time;
uniform float fill;
uniform float whiteout;
uniform float sheen;
uniform float centerY;
uniform float sizeY;
uniform float reveal;
uniform vec3 revealCenter;
uniform vec3 revealHalf;
uniform vec3 shadowColor;
uniform vec3 primaryColor;
uniform vec3 highlightColor;

varying vec2 vTexCoord;
varying vec4 vColor;
varying vec3 vLocal;
varying vec3 vViewPos;

void main() {
    vec3 dpdx = dFdx(vViewPos);
    vec3 dpdy = dFdy(vViewPos);
    vec4 tex = texture2D(blockTex, vTexCoord);
    if (tex.a < 0.1) discard;

    vec3 rawN = cross(dpdx, dpdy);
    float nLen = length(rawN);
    vec3 N = nLen > 1.0e-5 ? rawN / nLen : vec3(0.0, 0.0, 1.0);
    vec3 V = normalize(-vViewPos);
    float ndotv = abs(dot(N, V));
    float fresnel = pow(clamp(1.0 - ndotv, 0.0, 1.0), 1.9);

    float revealMask = 1.0;
    float front = 0.0;
    if (reveal < 1.0) {
        float dist = length((vLocal - revealCenter) / max(vec3(1.0), revealHalf));
        float radius = reveal * 1.85;
        revealMask = 1.0 - smoothstep(radius - 0.06, radius, dist);
        float band = (dist - radius) / 0.10;
        front = reveal > 0.001 ? exp(-band * band) * (1.0 - smoothstep(0.85, 1.0, reveal)) : 0.0;
    }

    vec3 solidCol = tex.rgb * vColor.rgb;
    float texLum = dot(solidCol, vec3(0.299, 0.587, 0.114));

    // Full strength on any face lying on an integer coordinate, which is every face of a block mesh.
    vec3 fracP = abs(fract(vLocal) - 0.5);
    float gridDist = max(max(fracP.x, fracP.y), fracP.z);
    float sheenMask = smoothstep(0.45, 0.495, gridDist) * clamp(sheen, 0.0, 1.0);

    float nFlow = fbm3(vec3(vLocal.x * 0.78, vLocal.y * 0.42 - time * 2.8, vLocal.z * 0.78));
    float nFacet = fbm3(vLocal * 3.1 + vec3(0.0, -time * 1.4, time * 0.5));
    float crackle = pow(clamp(1.0 - abs(nFacet * 2.0 - 1.0), 0.0, 1.0), 3.4);

    float normY = clamp((vLocal.y - (centerY - sizeY * 0.5)) / max(1.0, sizeY), 0.0, 1.0);
    float bodySurge = 0.65 + 0.35 * sin(normY * 3.14159);

    float threshold = 1.06 - fill * 1.32;
    float weaveMask = smoothstep(threshold, threshold + 0.18, nFlow * 0.62 + nFacet * 0.38 + 0.14);
    float surfaceAlpha = clamp(weaveMask * fill + sheenMask * 0.35 * smoothstep(0.0, 0.22, fill), 0.0, 1.0);

    vec3 pureWhite = vec3(1.0, 1.0, 1.0);
    float relief = clamp(texLum * 0.75 + nFacet * 0.55 - 0.15, 0.0, 1.0);
    vec3 crystal = mix(shadowColor, primaryColor, smoothstep(0.08, 0.52, relief));
    crystal = mix(crystal, highlightColor, smoothstep(0.44, 0.90, relief + fresnel * 0.65));
    crystal = mix(crystal, pureWhite, clamp(fresnel * 0.62 + sheenMask * 0.45 + crackle * 0.85, 0.0, 0.94));

    float earlyReveal = smoothstep(0.48, 0.88, fill) * 0.28 * (1.0 - whiteout);
    vec3 col = mix(crystal, solidCol * mix(vec3(1.0), highlightColor, 0.35) + crystal * 0.48, earlyReveal);
    col = mix(col, pureWhite, clamp(whiteout * (0.78 + 0.32 * bodySurge + fresnel * 0.25), 0.0, 1.0));
    float alpha = clamp(surfaceAlpha * (0.46 + 0.50 * fill + whiteout * 0.35), 0.0, 0.98) * revealMask;

    float frontMix = clamp(front, 0.0, 1.0) * 0.9;
    col = mix(col, mix(highlightColor, pureWhite, 0.55), frontMix);
    alpha = max(alpha, frontMix * 0.85);

    if (alpha < 0.04) discard;
    gl_FragColor = vec4(col, alpha);
}

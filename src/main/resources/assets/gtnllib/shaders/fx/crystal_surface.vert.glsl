#version 120

varying vec2 vTexCoord;
varying vec4 vColor;
varying vec3 vLocal;
varying vec3 vViewPos;

void main() {
    vTexCoord = gl_MultiTexCoord0.xy;
    vColor = gl_Color;
    vLocal = gl_Vertex.xyz;
    vec4 eyePos = gl_ModelViewMatrix * gl_Vertex;
    vViewPos = eyePos.xyz;
    gl_Position = gl_ProjectionMatrix * eyePos;
}

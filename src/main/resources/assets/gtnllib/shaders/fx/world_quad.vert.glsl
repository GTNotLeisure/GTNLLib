#version 120

varying vec2 vUv;

void main() {
    vUv = gl_MultiTexCoord0.xy;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}

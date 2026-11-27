#version 120

varying vec3 vLocal;

void main() {
    vLocal = gl_Vertex.xyz;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}

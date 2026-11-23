#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 bandColor;
out float bandHeight;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    bandColor = Color;
    bandHeight = UV0.y;
}

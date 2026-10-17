#version 150
in vec4 vertexColor;
uniform vec4 ColorModulator;
out vec4 fragColor;
uniform sampler2D Sampler0;
in vec2 texCoord;
void main() { fragColor = vertexColor * ColorModulator * texture(Sampler0, texCoord); }

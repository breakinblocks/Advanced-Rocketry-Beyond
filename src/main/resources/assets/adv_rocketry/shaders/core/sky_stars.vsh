#version 150
in vec3 Position;
in vec2 UV0;
in vec4 Color;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float Time;
uniform float Distortion;
out vec2 uv;
out vec4 tint;
out float phase;
void main() {
    phase = dot(Color.rgb, vec3(91.7, 173.3, 57.1)) + Color.a * 819.0;
    vec4 p = ModelViewMat * vec4(Position, 1.0);
    p.xy += Distortion * .012 * vec2(sin(Time*2.7+phase), cos(Time*3.1+phase));
    gl_Position = ProjMat * p;
    uv = UV0; tint = Color;
}

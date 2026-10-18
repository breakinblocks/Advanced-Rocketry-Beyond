#version 150
uniform vec4 ColorModulator;
uniform float Time;
uniform float Twinkle;
uniform float Atmosphere;
in vec2 uv;
in vec4 tint;
in float phase;
out vec4 fragColor;
void main() {
    vec2 p = uv * 2.0 - 1.0;
    float r = length(p);
    float edge = 1.0 - smoothstep(max(0.0, .8-fwidth(r)), 1.0, r);
    float core = exp(-r*r*7.0) + .18*exp(-r*r*1.7);
    float scintillation = 1.0 + Twinkle*(.08+.25*Atmosphere)*sin(Time*(1.1+fract(phase)*2.0)+phase)*sin(Time*.73+phase*2.0);
    vec3 color = mix(vec3(1.0), tint.rgb, .55);
    color += Atmosphere*Twinkle*.06*vec3(sin(Time*2.0+phase),0,cos(Time*2.0+phase));
    fragColor = vec4(color, edge*core*tint.a*scintillation) * ColorModulator;
}

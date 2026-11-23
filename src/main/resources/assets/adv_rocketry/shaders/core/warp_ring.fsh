#version 150

in vec4 bandColor;
in float bandHeight;
out vec4 fragColor;

void main() {
    float d = abs(bandHeight * 2.0 - 1.0);
    float halo = 0.5 * (1.0 - smoothstep(0.0, 1.0, d));
    float crest = 0.45 * (1.0 - smoothstep(0.015, 0.19, d));
    vec3 tint = mix(bandColor.rgb, vec3(0.66, 0.9, 1.0), crest);
    fragColor = vec4(tint, (halo + crest) * bandColor.a);
}

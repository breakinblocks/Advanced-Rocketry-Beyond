#version 150
uniform float GameTime;
uniform float Motion;
uniform vec3 CloudColor;
uniform float Coverage;
uniform float Seed;
uniform vec3 LightDirection;
uniform vec4 ColorModulator;
in vec2 texCoord;
out vec4 fragColor;
float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }
float noise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1, 0)), f.x),
               mix(hash(i + vec2(0, 1)), hash(i + vec2(1, 1)), f.x), f.y);
}
void main() {
    vec2 p = texCoord * 2.0 - 1.0;
    float r2 = dot(p, p);
    if (r2 >= 0.98) discard;
    float z = sqrt(max(0.001, 1.0 - r2));
    vec2 sphere = vec2(atan(p.x, z), asin(p.y));
    vec2 uv = sphere * vec2(5.0, 12.0);
    uv.x += GameTime * 12.0 * Motion + Seed;
    float density = noise(uv) * .55 + noise(uv * 2.1) * .3 + noise(uv * 4.3) * .15;
    float cloud = smoothstep(.75 - Coverage * .5, .93 - Coverage * .5, density);
    float limb = smoothstep(0.0, .35, z);
    fragColor = vec4(CloudColor * ColorModulator.rgb * (.15 + .85 * max(0.0, dot(vec3(p.x, p.y, z), LightDirection))),
        cloud * limb * ColorModulator.a);
}

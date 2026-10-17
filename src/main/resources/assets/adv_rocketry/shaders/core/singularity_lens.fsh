#version 150
uniform sampler2D SceneSampler;
uniform sampler2D DepthSampler;
uniform vec2 ScreenSize;
uniform vec4 Lens0;
uniform vec4 Lens1;
uniform vec4 Lens2;
uniform vec4 Lens3;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec4 lenses[4] = vec4[4](Lens0, Lens1, Lens2, Lens3);
    vec2 pixel = texCoord * ScreenSize;
    float depth = texture(DepthSampler, texCoord).r;
    vec2 offset = vec2(0.0);
    float horizon = 0.0;
    vec3 glow = vec3(0.0);
    for (int i = 0; i < 4; i++) {
        vec4 lens = lenses[i];
        float rs = lens.z * ScreenSize.x;
        if (rs < 0.75 || depth < lens.w) continue;
        vec2 delta = pixel - lens.xy * ScreenSize;
        float r = length(delta);
        float reach = 1.0 - smoothstep(rs * 4.0, rs * 8.0, r);
        vec2 direction = delta / max(r, 0.001);
        offset -= direction * 1.8 * rs * rs / max(r, rs) * reach;
        horizon = max(horizon, 1.0 - smoothstep(rs * 0.92, rs * 1.04, r));
        float ring = exp(-pow((r - sqrt(1.8) * rs) / (rs * 0.16), 2.0));
        glow += vec3(1.0, 0.72, 0.38) * ring * 0.4 * reach;
    }
    vec2 sampleUv = clamp((pixel + offset) / ScreenSize, vec2(0.001), vec2(0.999));
    // Preserve foreground geometry even if a displaced sample crosses its silhouette.
    if (texture(DepthSampler, sampleUv).r < depth - 0.0005) sampleUv = texCoord;
    vec3 color = texture(SceneSampler, sampleUv).rgb + glow;
    fragColor = vec4(mix(color, vec3(0.0), clamp(horizon, 0.0, 1.0)), 1.0);
}

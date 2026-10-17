#version 150
uniform sampler2D SceneSampler;
uniform sampler2D DepthSampler;
uniform vec2 ScreenSize;
uniform vec4 Lens0;
uniform vec4 Lens1;
uniform vec4 Lens2;
uniform vec4 Lens3;
uniform float HeatTime;
uniform float HeatStrength;
uniform vec4 HeatStart0;
uniform vec4 HeatEnd0;
uniform vec4 HeatStart1;
uniform vec4 HeatEnd1;
uniform vec4 HeatStart2;
uniform vec4 HeatEnd2;
uniform vec4 HeatStart3;
uniform vec4 HeatEnd3;
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
    vec4 starts[4] = vec4[4](HeatStart0, HeatStart1, HeatStart2, HeatStart3);
    vec4 ends[4] = vec4[4](HeatEnd0, HeatEnd1, HeatEnd2, HeatEnd3);
    vec2 shimmer = vec2(0.0);
    for (int i = 0; i < 4; i++) {
        vec4 a = starts[i], b = ends[i];
        if (a.z <= 0.0 || b.z <= 0.0) continue;
        vec2 axis = (b.xy-a.xy)*ScreenSize;
        vec2 delta = pixel-a.xy*ScreenSize;
        float t = clamp(dot(delta,axis)/max(dot(axis,axis),1.0),0.0,1.0);
        if (depth < mix(a.w,b.w,t)) continue;
        float radius = mix(a.z,b.z,t)*ScreenSize.x*(.4+.6*t);
        float distance = length(delta-axis*t)/max(radius,1.0);
        float envelope = (1.0-smoothstep(.45,1.0,distance))*smoothstep(0.0,.07,t)*(1.0-smoothstep(.6,1.0,t));
        vec2 waves = vec2(sin(pixel.y*.13+HeatTime*1.1+sin(pixel.x*.06)),
                          cos(pixel.x*.11-HeatTime*.8+sin(pixel.y*.09)));
        shimmer += waves*envelope*HeatStrength*2.2;
    }
    offset += clamp(shimmer,vec2(-3.0),vec2(3.0));
    vec2 sampleUv = clamp((pixel + offset) / ScreenSize, vec2(0.001), vec2(0.999));
    // Preserve foreground geometry even if a displaced sample crosses its silhouette.
    if (texture(DepthSampler, sampleUv).r < depth - 0.0005) sampleUv = texCoord;
    vec3 color = texture(SceneSampler, sampleUv).rgb + glow;
    fragColor = vec4(mix(color, vec3(0.0), clamp(horizon, 0.0, 1.0)), 1.0);
}

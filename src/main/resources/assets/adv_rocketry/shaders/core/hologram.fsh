#version 150
uniform float GameTime;
uniform float Motion;
in vec4 vertexColor;
in vec2 texCoord;
in vec3 viewNormal;
in vec3 viewPosition;
out vec4 fragColor;
void main() {
    float rim = pow(1.0 - abs(dot(normalize(viewNormal), normalize(-viewPosition))), 2.2);
    vec2 grid = abs(fract(texCoord * vec2(16.0, 12.0) + 0.5) - 0.5);
    vec2 aa = max(fwidth(texCoord) * vec2(16.0, 12.0), vec2(0.006));
    float meridian = 1.0 - smoothstep(aa.x, aa.x * 2.0, grid.x);
    float latitude = 1.0 - smoothstep(aa.y, aa.y * 2.0, grid.y);
    float scan = 0.65 + 0.35 * sin(texCoord.y * 220.0 - GameTime * 3000.0 * Motion);
    float sweep = exp(-pow((fract(texCoord.y - GameTime * 160.0 * Motion) - 0.5) * 20.0, 2.0));
    float lines = max(meridian, latitude);
    float alpha = (0.10 + rim * 0.65 + lines * 0.25 + sweep * 0.25) * vertexColor.a;
    vec3 light = vertexColor.rgb * (0.6 + rim * 0.5 + lines * 0.4 + sweep * 0.5);
    fragColor = vec4(light, alpha * scan);
}

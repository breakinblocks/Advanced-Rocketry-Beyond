#version 150
uniform float GameTime;
uniform float Motion;
in vec4 vertexColor;
in vec2 texCoord;
in vec3 viewNormal;
in vec3 viewPosition;
out vec4 fragColor;
void main() {
    float facing = abs(dot(normalize(viewNormal), normalize(-viewPosition)));
    float rim = pow(1.0 - facing, 8.0);
    vec2 coordinates = texCoord * vec2(16.0, 12.0);
    vec2 distanceToLine = abs(fract(coordinates + 0.5) - 0.5);
    // Measure coverage in screen pixels, keeping magnified lines thin and stable.
    vec2 pixelWidth = max(fwidth(coordinates), vec2(0.00001));
    vec2 coverage = 1.0 - smoothstep(vec2(0.35), vec2(1.05), distanceToLine / pixelWidth);
    // Fade unresolved cells near the poles and silhouette rather than merging into haze.
    coverage *= 1.0 - smoothstep(vec2(0.2), vec2(0.5), pixelWidth);
    float lines = max(coverage.x, coverage.y);
    float sweepDistance = abs(fract(texCoord.y - GameTime * 160.0 * Motion) - 0.5);
    float sweep = 1.0 - smoothstep(0.004, 0.012, sweepDistance);
    float surface = 0.025 + rim * 0.75 + lines * 0.9 + sweep * 0.3;
    float rear = gl_FrontFacing ? 1.0 : 0.12;
    float alpha = clamp(surface * vertexColor.a * rear, 0.0, 1.0);
    vec3 light = vertexColor.rgb * (0.85 + lines * 0.3 + rim * 0.2);
    fragColor = vec4(light, alpha);
}

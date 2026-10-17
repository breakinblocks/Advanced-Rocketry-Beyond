#version 150
in vec2 uv;
in vec4 tint;
out vec4 fragColor;
float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1,311.7))) * 43758.5453); }
float noise(vec2 p) { vec2 i=floor(p), f=fract(p); f=f*f*(3.0-2.0*f); return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+vec2(1,1)),f.x),f.y); }
float fbm(vec2 p) { return .57*noise(p)+.28*noise(p*2.03)+.15*noise(p*4.07); }
void main() {
 vec2 p=uv*2.0-1.0;
 float n=fbm(p*2.5+tint.r*19.0);
 float edge=1.0-smoothstep(.48,1.0,length(p)+(n-.5)*.35);
 float density=edge*(.78+.22*n)*tint.a;
 if(density<.005) discard;
 float light=.43+.38*n-.12*p.y;
 fragColor=vec4(vec3(light)*vec3(1.0,.98,.94),density);
}

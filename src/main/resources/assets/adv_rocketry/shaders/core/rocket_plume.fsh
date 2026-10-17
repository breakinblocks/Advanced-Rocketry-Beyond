#version 150
in vec2 uv;
in vec4 tint;
out vec4 fragColor;
uniform vec3 PlumeColor;
float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1,311.7))) * 43758.5453); }
float noise(vec2 p) { vec2 i=floor(p), f=fract(p); f=f*f*(3.0-2.0*f); return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+vec2(1,1)),f.x),f.y); }
float fbm(vec2 p) { return .57*noise(p)+.28*noise(p*2.03)+.15*noise(p*4.07); }
void main() {
 float y=uv.y;
 float width=.10+.75*pow(y,.7);
 float turbulence=fbm(vec2(uv.x*12.0,y*24.0+tint.r*20.0));
 float x=abs(uv.x*2.0-1.0);
 float shape=1.0-smoothstep(width*.45,width+(turbulence-.5)*.13,x);
 float tail=1.0-smoothstep(.62,1.0,y);
 vec3 heat=mix(PlumeColor * vec3(1.0,.55,.25),vec3(1.0,.97,.78),pow(shape,2.0));
 // Repeating compression cells inside the expanding supersonic jet.
 float cell=fract(y*6.0+.1);
 float diamond=max(0.0,1.0-abs(cell-.5)*2.0);
 float core=1.0-smoothstep(width*(.12+.38*diamond),width*(.24+.42*diamond),x);
 float shock=core*pow(diamond,3.0)*(1.0-smoothstep(.45,.85,y));
 heat=mix(heat,vec3(.78,.90,1.0),shock*.65);
 float base=mix(.08,.35,smoothstep(.35,.7,y));
 float alpha=shape*tail*tint.a*(base+(1.0-base)*shock);
 if(alpha<.005) discard;
 fragColor=vec4(heat,alpha);
}

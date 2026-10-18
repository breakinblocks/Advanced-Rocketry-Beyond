#version 150
uniform vec4 ColorModulator;
uniform vec3 Center;
uniform vec3 Strength;
in vec3 direction;
out vec4 fragColor;
float hash(vec3 p) { p=fract(p*.3183099+vec3(.13,.71,.37)); p*=17.0; return fract(p.x*p.y*p.z*(p.x+p.y+p.z)); }
float noise(vec3 p) {
    vec3 i=floor(p), f=fract(p); f=f*f*(3.0-2.0*f);
    return mix(mix(mix(hash(i),hash(i+vec3(1,0,0)),f.x),mix(hash(i+vec3(0,1,0)),hash(i+vec3(1,1,0)),f.x),f.y),mix(mix(hash(i+vec3(0,0,1)),hash(i+vec3(1,0,1)),f.x),mix(hash(i+vec3(0,1,1)),hash(i+vec3(1,1,1)),f.x),f.y),f.z);
}
float fbm(vec3 p) { float n=0.0,a=.5; for(int i=0;i<5;i++){n+=a*noise(p);p=p*2.03+vec3(3.1,7.3,1.9);a*=.5;} return n; }
void main() {
    vec3 d=normalize(direction);
    float broad=fbm(d*7.0);
    float fine=fbm(d*48.0+vec3(broad*4.0));
    float plane=d.y+(broad-.5)*.18;
    float band=exp(-plane*plane*48.0);
    float center=pow(max(0.0,dot(d,Center)),8.0);
    float dust=smoothstep(.32,.65,fbm(d*22.0+vec3(11.0)));
    float lane=exp(-pow((plane+.025+(fine-.5)*.06)*24.0,2.0));
    float light=band*(.2+fine*1.4)*(1.0-dust*.72)*(1.0-lane*.65);
    vec3 color=mix(vec3(.12,.22,.53),vec3(.65,.38,.21),center*.8);
    vec3 galaxy=color*light*(.5+center)*Strength.x;
    float cloud=pow(max(0.0,broad-.32)*2.0,2.0)*( .35+fine);
    vec3 nebula=mix(vec3(.08,.19,.4),vec3(.32,.08,.44),fine)*cloud*Strength.y;
    fragColor=vec4((galaxy+nebula)*Strength.z*2.2,1.0)*ColorModulator;
}

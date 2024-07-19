#version 330 core

const float OPTIMAL_COLOR_DISTANCE = 10.;

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

in DATA {
	vec3 vertColor;
}data[];

out vec3 geomColor;

uniform mat4 modelMatr;
uniform mat4 viewMatr;
uniform mat4 projMatr;
uniform vec4 camPos;

uniform vec3 lightIntensity;
uniform vec4 lightPos;
uniform float ambientIntensity;

vec4 getCenter() {
	vec4 center = vec4(0.0, 0.0, 0.0, 0.0);
	for (int i = 0; i < gl_in.length(); ++i) {
		center += gl_in[i].gl_Position;
	}
	center /= gl_in.length();
    center.w = 1.0;
	return center;
}

vec3 getNormal(vec3 A, vec3 B, vec3 C){
    return normalize(cross(B - A, C - A));
}

vec3 getNormal()
{
   vec3 a = vec3(gl_in[1].gl_Position) - vec3(gl_in[0].gl_Position);
   vec3 b = vec3(gl_in[2].gl_Position) - vec3(gl_in[0].gl_Position);
   return normalize(cross(a, b));
}  

void main()
{
    /*
    vec4 arr[gl_in.length()];
    for (int i = 0; i < gl_in.length(); ++i) {
		arr[i] = projMatr * gl_in[i].gl_Position;
	}
    /**/
   
    float multiplier = length(abs(camPos.xyz - getCenter().xyz))/OPTIMAL_COLOR_DISTANCE;
    geomColor = vec3(1,0,1) * (1-multiplier);

    vec4 center = getCenter();
    vec4 e = camPos - center;
    e.w = 1.0;

    //ambient
    vec3 ambient = ambientIntensity * lightIntensity;

    //diffuse
    vec3 n = getNormal();
    vec3 l = normalize(vec3(lightPos - center));
    vec3 diffuse = lightIntensity * max(dot(n, l), 0.0);

    //specular
    vec3 r = reflect(-l, n);
    vec3 v = normalize(-e.xyz);
    float spec = max(dot(r, v), 0.0);
    vec3 specular = lightIntensity * pow(spec, 32);

    geomColor = ambient + diffuse + specular;

    for (int i = 0; i < gl_in.length(); ++i) {
        gl_Position = projMatr * gl_in[i].gl_Position;
        EmitVertex();
    }
    EndPrimitive();
}
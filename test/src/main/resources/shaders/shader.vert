#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aUvCoords;


out DATA {
	vec3 vertColor;
} data;

uniform mat4 modelMatr;
uniform mat4 viewMatr;
uniform mat4 projMatr;

uniform mat3 normalMatr;

uniform vec4 cameraPos;

uniform vec3 lightIntensity;
uniform vec4 lightPos;
uniform float ambientIntensity;

uniform vec3 objectColor;

void main()
{
	vec3 color;
	if (objectColor.x == 0 && objectColor.y == 0 && objectColor.z == 0 ) {
		color = vec3(1.0, 0, 1.0);
	} else {
		color = objectColor;
	}

	vec3 fragPos = vec3(modelMatr * vec4(aPos, 1.0));
	vec3 norm = normalize(normalMatr * aNormal);

	vec3 lightDir = normalize(lightPos.xyz - fragPos);
	float diff = max(dot(norm, lightDir), 0.0);
	vec3 diffuse = lightIntensity * diff;

	vec3 viewDir = normalize(cameraPos.xyz - fragPos);
	vec3 reflectDir = reflect(-lightDir, norm);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
	vec3 specular = lightIntensity * spec;

	vec3 ambient = ambientIntensity * lightIntensity;

	data.vertColor = (ambient + diffuse + specular) * color;
	
    gl_Position = projMatr * viewMatr * modelMatr * vec4(aPos, 1.0);

}


#version 330 core

out vec4 FragColor;

in DATA {
	vec3 vertColor;
} data;

void main()
{
	FragColor = vec4(data.vertColor, 1);
} 
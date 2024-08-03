#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;

uniform mat4 model;

out VS_OUT {
    vec3 FragPos;
    vec3 Normal;
} vs_out;

void main()
{
    vs_out.FragPos = vec3(model * vec4(aPos, 1.0));
    vs_out.Normal = mat3(transpose(inverse(model))) * aNormal;
    gl_Position = vec4(aPos, 1.0);
}
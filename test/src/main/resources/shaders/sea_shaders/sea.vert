#version 330 core

layout (location = 0) in vec3 aPos;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform float time;

out vec3 FragPos;

void main()
{
    vec3 pos = aPos;
    pos.y = sin(time + aPos.y) * 0.4; // Adjust 0.1 for wave height
    gl_Position = projection * view * model * vec4(pos, 1.0);
    FragPos = vec3(model * vec4(pos, 1.0));
}
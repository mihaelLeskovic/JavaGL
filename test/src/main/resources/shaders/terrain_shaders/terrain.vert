#version 330 core

layout(location = 0) in vec3 aPos;

uniform mat4 projection;
uniform mat4 model;
uniform mat4 view;

out vec3 FragPos;
out vec3 Position;

void main()
{
    FragPos = vec3(model * vec4(aPos, 1.0));
    Position = aPos;

    gl_Position = projection * view * vec4(FragPos, 1.0);
}

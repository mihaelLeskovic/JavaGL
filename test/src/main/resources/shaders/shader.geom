#version 330 core

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

in VS_OUT {
    vec3 FragPos;
    vec3 Normal;
} gs_in[];

out GS_OUT {
    vec3 FragPos;
    vec3 Normal;
} gs_out;

uniform mat4 view;
uniform mat4 projection;

void main()
{
    for(int i = 0; i < 3; i++)
    {
        gl_Position = projection * view * vec4(gs_in[i].FragPos, 1.0);
        gs_out.FragPos = gs_in[i].FragPos;
        gs_out.Normal = gs_in[i].Normal;
        EmitVertex();
    }
    EndPrimitive();
}
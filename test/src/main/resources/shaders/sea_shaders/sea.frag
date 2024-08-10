#version 330 core

in vec3 FragPos;

out vec4 FragColor;

uniform vec3 viewPos;

void main()
{
    float depth = (FragPos.y + 0.1) / 0.2; // Normalize depth
    vec3 shallowColor = vec3(0.0, 0.2, 0.5);
    vec3 deepColor = vec3(0.0, 0.0, 0.3);

    float distance = length(FragPos - viewPos);
    vec3 finalColor = mix(deepColor, shallowColor, depth);

    finalColor = mix(deepColor, finalColor, 10/distance);

    FragColor = vec4(finalColor, 1.0);
}
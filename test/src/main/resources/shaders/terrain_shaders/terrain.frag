#version 330 core

in vec3 FragPos;
in vec3 Normal;

out vec4 FragColor;

uniform vec3 viewPos;
uniform float height;

// New uniforms
uniform vec3 lightColor;
uniform vec3 lightDirection;
uniform float ambientIntensity;

void main()
{
    float depth = (FragPos.y) / (height);
    vec3 deepColor = vec3(0.87, 0.8, 0.33);
    vec3 shallowColor = vec3(0.48, 0.48, 0.48);

    vec3 baseColor = mix(deepColor, shallowColor, depth);

    vec3 ambient = ambientIntensity * lightColor;

    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(-lightDirection);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    vec3 finalColor = (ambient + diffuse) * baseColor;

    FragColor = vec4(finalColor, 1.0);
}
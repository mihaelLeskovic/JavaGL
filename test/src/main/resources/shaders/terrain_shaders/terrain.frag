#version 330 core

in vec3 FragPos;
in vec3 Position;

uniform vec3 viewPos;         // Camera position
uniform vec3 lightColor;      // Light color
uniform vec3 lightDirection;  // Directional light direction
uniform float ambientIntensity; // Ambient light intensity
uniform float maxHeight;      // Maximum height of the terrain

// Define sand and rock colors
vec3 sandColor = vec3(0.95, 0.85, 0.55); // Typical sand color
vec3 rockColor = vec3(0.5, 0.5, 0.5);    // Typical rock color (gray)

out vec4 FragColor;

void main()
{
    // Calculate approximate normal by using position derivatives
    vec3 dX = dFdx(Position);
    vec3 dY = dFdy(Position);
    vec3 norm = normalize(cross(dX, dY));

    vec3 lightDir = normalize(-lightDirection);
    vec3 viewDir = normalize(viewPos - FragPos);

    // Calculate blending factor based on height
    float blendFactor = clamp((FragPos.y / maxHeight), 0.0, 1.0);

    // Blend between sand and rock colors
    vec3 terrainColor = mix(sandColor, rockColor, blendFactor);

    // Ambient
    vec3 ambient = ambientIntensity * terrainColor;

    // Diffuse
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * terrainColor;

    // Specular
    float specularStrength = 0.2;  // Reduced shininess for sand/rock
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 16);  // Shininess of 16
    vec3 specular = specularStrength * spec * lightColor;

    // Combine results
    vec3 result = ambient + diffuse + specular;
    FragColor = vec4(result, 1.0);
}

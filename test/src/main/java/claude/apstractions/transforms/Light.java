package claude.apstractions.transforms;

import org.joml.Vector3f;

public class Light extends Transform{
    private Vector3f color;
    private float ambientIntensity;

    public Light() {
        super();
        this.color = new Vector3f(1, 1, 1);
        this.ambientIntensity = 0.4f;
    }

    public Light(Vector3f intensity, float ambientIntensity) {
        this.color = intensity;
        this.ambientIntensity = ambientIntensity;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public float getAmbientIntensity() {
        return ambientIntensity;
    }

    public void setAmbientIntensity(float ambientIntensity) {
        this.ambientIntensity = ambientIntensity;
    }
}
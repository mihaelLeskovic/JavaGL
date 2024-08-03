package claude.apstractions.transforms;

import org.joml.Vector3f;

public class Light extends Transform{
    private Vector3f intensity;
    private float ambientIntensity;

    public Light() {
        super();
        this.intensity = new Vector3f(1, 1, 1);
        this.ambientIntensity = 0.1f;
    }

    public Light(Vector3f intensity, float ambientIntensity) {
        this.intensity = intensity;
        this.ambientIntensity = ambientIntensity;
    }

    public Vector3f getIntensity() {
        return intensity;
    }

    public void setIntensity(Vector3f intensity) {
        this.intensity = intensity;
    }

    public float getAmbientIntensity() {
        return ambientIntensity;
    }

    public void setAmbientIntensity(float ambientIntensity) {
        this.ambientIntensity = ambientIntensity;
    }
}
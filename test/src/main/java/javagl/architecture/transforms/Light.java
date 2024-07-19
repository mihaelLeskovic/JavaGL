package javagl.architecture.transforms;

import javagl.architecture.transforms.Transform;
import org.joml.Vector3f;

public class Light extends Transform {
    public Vector3f intensity;
    public float ambientIntensity;

    public Light() {
        super();
        intensity = new Vector3f(1, 1, 1);
    }
}

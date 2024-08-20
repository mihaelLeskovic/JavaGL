package javagl.architecture.transforms;

import org.joml.Matrix4f;

public class Camera extends Transform {
    float l, r, b, t, n, f;
    Matrix4f projectionMatrix = null;

    public Camera() {
        super();
    }

    public Camera(float l, float r, float b, float t, float n, float f) {
        super();
        this.l = l;
        this.r = r;
        this.b = b;
        this.t = t;
        this.n = n;
        this.f = f;
    }

    public Matrix4f getProjectionMatrix() {
        if(projectionMatrix == null) {
            projectionMatrix = new Matrix4f().set(new float[]{
                    2 * n / (r - l), 0, 0, 0,
                    0, 2 * n / (t - b), 0, 0,
                    (r + l) / (r - l), (t + b) / (t - b), -(f + n) / (f - n), -1,
                    0, 0, -2 * f * n / (f - n), 0
            });
            return projectionMatrix;
        }

        return projectionMatrix;
    }
}

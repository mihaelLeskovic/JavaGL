package claude.apstractions.transforms;

import org.joml.Matrix4f;

public class Camera extends Transform {
    private float l;
    private float r;
    private float b;
    private float t;
    private float n;
    private float f;
    private Matrix4f projectionMatrix;

    public Camera() {
        this.l = -0.5f;
        this.r = 0.5f;
        this.b = -0.5f;
        this.t = 0.5f;
        this.n = 1.0f;
        this.f = 100f;
        projectionMatrix = new Matrix4f().frustum(l, r, b, t, n, f);
    }

    public Camera(float l, float r, float b, float t, float n, float f) {
        this.l = l;
        this.r = r;
        this.b = b;
        this.t = t;
        this.n = n;
        this.f = f;
        projectionMatrix = new Matrix4f().frustum(l, r, b, t, n, f);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
}

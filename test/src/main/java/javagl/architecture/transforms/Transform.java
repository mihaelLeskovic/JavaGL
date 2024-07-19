package javagl.architecture.transforms;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Transform {
    public Vector3f position;
    public Vector3f scale;
    public Vector3f up;
    public Vector3f right;
    public Vector3f front;

    public Transform() {
        position = new Vector3f(0,0,0);
        scale = new Vector3f(1, 1, 1);
        up = new Vector3f(0, 1, 0);
        right = new Vector3f(1, 0, 0);
        front = new Vector3f(0, 0, -1);
    }

    public Matrix4f getModelMatrix() {
        return getViewMatrix().invert();
    }

    public Matrix4f getViewMatrix() {
        Vector3f vecTarget = new Vector3f(front.x + position.x, front.y + position.y, front.z + position.z);

        return new Matrix4f().scale(scale).lookAt(position, vecTarget, up);
    }

    public Matrix4f getNormalMatrix() {
        return getModelMatrix().invert().transpose();
    }

    public void rotate(Vector3f axis, float angle) {
        Matrix3f rotation = new Matrix3f().rotate(angle, axis);

        //joml syntax: rotation matrix operating on (transforming) front vector, storing the result in that vector
        rotation.transform(front);
        rotation.transform(up);
        rotation.transform(right);
    }

    // lookAt vector will be mutated after this call
    public void setLookDirection(Vector3f lookAt, Vector3f up) {
        this.front = lookAt.add(position.negate(new Vector3f())).normalize();

        //joml syntax: up x front, stored into right
        up.cross(front, right);

        right.cross(front, up);
    }
}

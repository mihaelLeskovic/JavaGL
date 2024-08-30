package simulator.transforms;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Transform {

    private Vector3f position;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;
    private Vector3f scale;
    private Matrix4f modelMatrix;
    private Matrix4f viewMatrix;

    public Transform() {
        position = new Vector3f(0,0,0);
        front = new Vector3f(0, 0, -1);
        up = new Vector3f(0, 1, 0);
        right = front.cross(up, new Vector3f());
        scale = new Vector3f(1, 1, 1);
        modelMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        updateMatrices();
    }

//    private Vector3f applyAdjustment(Vector3f vec) {
//        Vector4f adjusted = (new Vector4f(vec, 1).mul(adjustmentMatrix));
//        return new Vector3f(vec.x, vec.y, vec.z);
//    }

    public Transform setScale(float universal) {
        return setScale(universal, universal, universal);
    }

//    private Matrix4f applyAdjustment(Matrix4f mat) {
//        return new Matrix4f(mat).mul(adjustmentMatrix);
//    }

    private void updateMatrices() {
        normalizeVectors();

        Matrix4f rotationMatrix = new Matrix4f(
                right.x, right.y, right.z, 0,
                up.x, up.y, up.z, 0,
                front.x, front.y, front.z, 0,
                0, 0, 0, 1
        );

        modelMatrix.identity()
                .translate(position)
                .mul(rotationMatrix)
                .scale(scale);

        Vector3f target = position.add(front, new Vector3f());

        viewMatrix.identity()
                .lookAt(position, target, up)
//                .scale(1/scale.x, 1/scale.y, 1/scale.z);
        ;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
//        return applyAdjustment(modelMatrix);
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getNormalMatrix(){
        return new Matrix4f(getModelMatrix()).invert(new Matrix4f()).transpose();
    }

    public Transform setPosition(Vector3f newPos) {
        setPosition(newPos.x, newPos.y, newPos.z);
        updateMatrices();
        return this;
    }

    public Transform setPosition(float x, float y, float z) {
        position.set(x, y, z);
        updateMatrices();
        return this;
    }

    public Transform setScale(Vector3f newScale) {
        scale.set(scale);
        updateMatrices();
        return this;
    }

    public Vector3f getFront() {
        return front;
    }

    public Vector3f getUp() {
        return up;
    }

    public Vector3f getRight() {
        return right;
    }

    public Vector3f getScale() {
        return scale;
    }

    public Transform rotate(Vector3f axis, float angle) {
        Matrix4f rotationMat = new Matrix4f().rotate(angle, axis);
        up.rotateAxis(angle, axis.x, axis.y, axis.z);
        right.rotateAxis(angle, axis.x, axis.y, axis.z);
        front.rotateAxis(angle, axis.x, axis.y, axis.z);
        updateMatrices();
        normalizeVectors();
        return this;
    }

    public Transform setLookDirection(Vector3f lookAt, Vector3f upVector) {
        front = new Vector3f(lookAt);
        right = front.cross(upVector, new Vector3f());
        up = right.cross(front, new Vector3f());
        normalizeVectors();
        updateMatrices();
        return this;
    }

    private void normalizeVectors() {
        this.up.normalize();
        this.front.normalize();
        this.right.normalize();
    }

    public Transform translateLocal(Vector3f translation) {
//        translation = applyAdjustment(translation);
        position.add(front.mul(-translation.z, new Vector3f()))
                .add(right.mul(translation.x, new Vector3f()))
                .add(up.mul(translation.y, new Vector3f()));
        updateMatrices();
        return this;
    }

    public Transform translateGlobal(float x, float y, float z) {
        return translateGlobal(new Vector3f(x,y,z));
    }

    public Transform translateGlobal(Vector3f translation) {
        position.add(translation);
        updateMatrices();
        return this;
    }

    public Transform setScale(float x, float y, float z) {
        scale.set(x, y, z);
        updateMatrices();
        return this;
    }

    public Transform applyRotationMatrix4f(Matrix4f mat) {
        mat.transformPosition(this.up);
        mat.transformPosition(this.front);
        mat.transformPosition(this.right);
        updateMatrices();
        return this;
    }
}

package simulator.physics;

import org.joml.Matrix4f;
import simulator.transforms.*;
import org.joml.Vector3f;

public class PhysicalObject implements Forceable, Updateable {

    //in kg
    float mass;

    //in m/s
    //global
    Vector3f velocity;

    //rotation multipliers
    float globalAngularSensitivity = 0.5f;

    float drag = 0.1f;

    Vector3f lastForce;
    Matrix4f globalTorqueMatrix;
    Transform transform;

    public Transform getTransform() {
        return transform;
    }

    public PhysicalObject(float mass, Transform transform) {
        this.mass = mass;
        this.transform = transform;
        this.velocity = new Vector3f(0);
        this.lastForce = new Vector3f(0);
        this.globalTorqueMatrix = new Matrix4f().identity();
    }

    public float getGlobalAngularSensitivity() {
        return globalAngularSensitivity;
    }

    public void setGlobalAngularSensitivity(float globalAngularSensitivity) {
        this.globalAngularSensitivity = globalAngularSensitivity;
    }

    @Override
    public void applyForce(Vector3f force) {
        lastForce.add(force);
    }

    @Override
    public void applyForceLocal(Vector3f force) {
        lastForce.add(
                new Vector3f(transform.getRight()).mul(force.x)
                        .add(new Vector3f(transform.getUp()).mul(force.y))
                        .add(new Vector3f(transform.getFront()).mul(-force.z))
        );
    }

    @Override
    public void applyAcceleration(Vector3f acceleration) {
        lastForce.add(acceleration.mul(mass));
    }

    @Override
    public void applyGlobalTorque(float aroundX, float aroundY, float aroundZ) {
        if(aroundX!=0) globalTorqueMatrix.rotate(aroundX, new Vector3f(1,0,0));
        if(aroundY!=0) globalTorqueMatrix.rotate(aroundY, new Vector3f(0,1,0));
        if(aroundZ!=0) globalTorqueMatrix.rotate(aroundZ, new Vector3f(0,0,1));
    }

    @Override
    public void applyTorqueAroundAxis(Vector3f axis, float amount) {
        globalTorqueMatrix.rotate(amount, axis);
    }

    // returns a delta velocity which is gained because of the force
    private Vector3f consumeLastForce(float deltaT) {
        if(lastForce.lengthSquared() == 0) return new Vector3f(velocity).mul(deltaT);

        Vector3f deltaV = new Vector3f(lastForce).div(mass).mul(deltaT);
        lastForce.set(0);
        velocity.add(deltaV);
        return new Vector3f(velocity).mul(deltaT);
    }

    private void consumeGlobalTorque(float deltaT) {
        if(globalTorqueMatrix.equals(new Matrix4f().identity(), 0.0001f)) return;

        globalTorqueMatrix.scale(deltaT * globalAngularSensitivity);
        transform.applyRotationMatrix4f(globalTorqueMatrix);
        globalTorqueMatrix.identity();
    }

    public float getMass() {
        return mass;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity = velocity;
    }

    @Override
    public void update(float deltaT) {
        Vector3f deltaX = consumeLastForce(deltaT);
        transform.translateGlobal(deltaX);
        consumeGlobalTorque(deltaT);
    }
}

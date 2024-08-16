package simulator.physics;

import org.joml.Matrix4f;
import simulator.transforms.*;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class PhysicalObject implements Forceable, Updateable {

    //in kg
    float mass;

    //in m/s
    //global
    Vector3f velocity;

    //rotation multipliers
    float angularSensitivity = 0.5f;

    Vector3f angularVelocity;

    float drag = 0.9f;
    float angularDrag = 2f;

    Vector3f lastForce;
    Transform transform;


    public Transform getTransform() {
        return transform;
    }

    public PhysicalObject(float mass, Transform transform) {
        this.mass = mass;
        this.transform = transform;
        this.velocity = new Vector3f(0);
        this.lastForce = new Vector3f(0);
        this.angularVelocity = new Vector3f(0);
    }

    public void setAngularSensitivity(float angularSensitivity) {
        this.angularSensitivity = angularSensitivity;
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
        if(aroundX!=0) applyTorqueAroundAxis(new Vector3f(1, 0, 0), aroundX);
        if(aroundY!=0) applyTorqueAroundAxis(new Vector3f(0, 1, 0), aroundY);
        if(aroundZ!=0) applyTorqueAroundAxis(new Vector3f(0, 0, 1), aroundZ);
    }

    @Override
    public void applyTorqueAroundAxis(Vector3f axis, float amount) {
        angularVelocity.add(axis.normalize().mul(amount));
    }

    @Override
    public void applyTorqueAroundLocalAxis(Vector3f axis, float amount) {
        Vector3f globalAxis = new Vector3f(
                new Vector3f(transform.getRight()).mul(axis.x)
                        .add(new Vector3f(transform.getUp()).mul(axis.y))
                        .add(new Vector3f(transform.getFront()).mul(axis.z))
        );
        applyTorqueAroundAxis(globalAxis.normalize(), amount);
    }

    // returns a delta velocity which is gained because of the force
    private void consumeLastForce(float deltaT) {
        Vector3f deltaX;
        if(lastForce.length() > 0.001f) {
            Vector3f deltaV = new Vector3f(lastForce).div(mass).mul(deltaT);
            lastForce.set(0);
            velocity.add(deltaV);
        } else if(angularVelocity.length() < 0.001f) return;

        deltaX = new Vector3f(velocity).mul(deltaT);

        transform.translateGlobal(deltaX);
        velocity.mul(1-deltaT*drag);
        if(velocity.length() < 0.001f) velocity.set(0);
    }

    private void consumeGlobalTorque(float deltaT) {
        if(angularVelocity.length() < 0.001f) return;

        float angle = angularVelocity.length() * deltaT * angularSensitivity;
        transform.rotate(new Vector3f(angularVelocity).normalize(), angle);
        angularVelocity.mul(1 - deltaT*angularDrag);
        if(angularVelocity.length() < 0.001f) angularVelocity.set(0);
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
        consumeLastForce(deltaT);
        consumeGlobalTorque(deltaT);
    }
}

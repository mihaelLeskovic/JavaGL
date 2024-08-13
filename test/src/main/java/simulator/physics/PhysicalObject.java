package simulator.physics;

import simulator.transforms.Camera;
import simulator.transforms.Light;
import simulator.transforms.ObjectInstance;
import simulator.transforms.Renderable;
import org.joml.Vector3f;

public class PhysicalObject implements Forceable, Updateable, Renderable {
    //in kg
    float mass;
    //in m/s
    //global
    Vector3f lastForce;
    Vector3f velocity;
    ObjectInstance objectInstance;

    public ObjectInstance getObjectInstance() {
        return objectInstance;
    }

    public PhysicalObject(float mass, ObjectInstance objectInstance) {
        this.mass = mass;
        this.objectInstance = objectInstance;
        this.velocity = new Vector3f(0);
        this.lastForce = new Vector3f(0);
    }

    @Override
    public void applyForce(Vector3f force) {
        lastForce.add(force);
    }

    @Override
    public void applyAcceleration(Vector3f acceleration) {
        lastForce.add(acceleration.mul(mass));
    }

    @Override
    public void applyGlobalRotation(Vector3f selfMovement) {

    }

    @Override
    public void applyLocalRotation(Vector3f selfMovement) {

    }

    // returns a delta velocity which is gained because of the force
    private Vector3f consumeLastForce(float deltaT) {
        Vector3f deltaV = new Vector3f(lastForce).div(mass).mul(deltaT);
        lastForce.set(0);
        return deltaV;
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
        velocity.add(consumeLastForce(deltaT));

        objectInstance.translateGlobal(new Vector3f(velocity).mul(deltaT));
    }

    @Override
    public void render(Camera camera, Light light) {
        objectInstance.render(camera, light);
    }
}

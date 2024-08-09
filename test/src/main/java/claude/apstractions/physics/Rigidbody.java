package claude.apstractions.physics;

import org.joml.Vector3f;

public abstract class Rigidbody {
    private float mass;
    private Vector3f force;

    public void setForce(Vector3f force) {
        this.force = force;
    }

    public float getMass() {
        return mass;
    }

    public Vector3f getForce() {
        return force;
    }
}

package simulator.physics;

import org.joml.Vector3f;

public interface Forceable {
    void applyForce(Vector3f force);

    void applyAcceleration(Vector3f acceleration);

    // component-wise rotation
    // x is rotation around x-axis, y around y-axis, z around z-axis
    void applyGlobalRotation(Vector3f selfMovement);

    // x is rotation around local x-axis, y around local y-axis, z around local z-axis
    void applyLocalRotation(Vector3f selfMovement);
}

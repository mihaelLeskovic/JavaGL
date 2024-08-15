package simulator.physics;

import org.joml.Vector3f;

public interface Forceable {
    void applyForce(Vector3f force);

    void applyForceLocal(Vector3f force);

    void applyAcceleration(Vector3f acceleration);

    void applyGlobalTorque(float aroundX, float aroundY, float aroundZ);

    void applyTorqueAroundAxis(Vector3f axis, float amount);
    void applyTorqueAroundLocalAxis(Vector3f axis, float amount);
}

package simulator.physics;

import org.joml.Vector3f;
import simulator.physics.hitboxes.HitboxVisitor;
import simulator.physics.hitboxes.PlaneHitbox;
import simulator.physics.hitboxes.VisitableHitbox;
import simulator.transforms.Transform;

import java.util.ArrayList;
import java.util.List;

public class Plane implements Updateable, CollisionEventListener {
    List<HitboxVisitor> hitboxVisitorList;
    PhysicalObject planePhysicalObject;
    PlaneFollower planeFollower;

    float throttleLevel = 0f;
    float throttlePower = 1f;
    boolean shouldApplyThrottle = false;
    float pitch = 0f;
    float pitchSensitivity = 0.01f;
    float yaw = 0f;
    float yawSensitivity = 0.01f;
    float roll = 0f;
    float rollSensitivity = 0.01f;
    float liftCoefficient = 1f;
    boolean isGrounded = true;

    public Plane initializeDefaultValues(float maxSpeed, float maxAcceleration) {
        liftCoefficient = planePhysicalObject.getMass() * PhysicalObject.GRAVITY / maxSpeed;
        throttlePower = maxAcceleration * planePhysicalObject.getMass();
        planePhysicalObject.drag = throttlePower / maxSpeed;

        return this;
    }

    public void setPlaneFollower(PlaneFollower planeFollower) {
        this.planeFollower = planeFollower;
        planeFollower.setPlane(this);
    }

    public void applyAllForces() {
        if(shouldApplyThrottle) applyThrottle();
//        if(!isGrounded)
            applyRollAndPitch();
        applyYaw();
        applyLift();
//        applyGravity();
//        applyDrag();
    }

    float drag = 1f;
    private void applyDrag() {
        planePhysicalObject.applyForceLocal(new Vector3f(planePhysicalObject.velocity).mul(-drag));
    }

    private void applyLift() {
        float dotProd = new Vector3f(getTransform().getFront()).dot(new Vector3f(planePhysicalObject.getVelocity()).normalize());
        planePhysicalObject.applyForceLocal(new Vector3f(0, 1, 0).mul(liftCoefficient * dotProd * planePhysicalObject.getVelocity().length()));
    }

    private void applyGravity() {
        planePhysicalObject.applyAcceleration(new Vector3f(0, PhysicalObject.GRAVITY, 0));
    }

    private void applyYaw() {
        planePhysicalObject.applyTorqueAroundLocalAxis(new Vector3f(0, 1, 0), yaw * yawSensitivity);
    }

    private void applyRollAndPitch() {
        planePhysicalObject.applyTorqueAroundLocalAxis(new Vector3f(1, 0, 0), pitch * pitchSensitivity);
        planePhysicalObject.applyTorqueAroundLocalAxis(new Vector3f(0, 0, 1), -roll * rollSensitivity);
    }

    private void applyThrottle() {
        planePhysicalObject.applyForceLocal(new Vector3f(0, 0, -1).mul(throttleLevel * throttlePower));
    }

    public Transform getTransform() {
        return planePhysicalObject.getTransform();
    }

    public float getThrottleLevel() {
        return throttleLevel;
    }

    public void setThrottleLevel(float throttleLevel) {
        this.throttleLevel = throttleLevel;
    }

    public boolean isShouldApplyThrottle() {
        return shouldApplyThrottle;
    }

    public boolean getShouldApplyThrottle() {
        return shouldApplyThrottle;
    }

    public void setShouldApplyThrottle(boolean shouldApplyThrottle) {
        this.shouldApplyThrottle = shouldApplyThrottle;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public Plane(PhysicalObject planePhysicalObject, String pathToHitboxModel) {
        this.planePhysicalObject = planePhysicalObject;
        hitboxVisitorList = new ArrayList<>();
    }

    void updateHitboxes() {
        for(HitboxVisitor visitor : hitboxVisitorList) {
            visitor.update(0);
        }
    }

    @Override
    public void update(float deltaT) {
        planePhysicalObject.update(deltaT);

        planeFollower.update(deltaT);
        updateHitboxes();
        applyAllForces();
    }

    public Plane addHitboxVisitor(HitboxVisitor hitboxVisitor) {
        hitboxVisitorList.add(hitboxVisitor);
        return this;
    }

    public void performCollisionsOn(VisitableHitbox visitableHitbox) {
        for(HitboxVisitor hitboxVisitor : hitboxVisitorList){
            visitableHitbox.accept(hitboxVisitor);
        }
    }

    public Plane setShouldRender(boolean shouldRender) {
        for(HitboxVisitor hitboxVisitor : hitboxVisitorList){
            hitboxVisitor.setShouldRender(shouldRender);
        }
        return this;
    }

    @Override
    public void notifyCollision() {
        planePhysicalObject.notifyCollision();
        isGrounded = true;
        updateHitboxes();
    }
}

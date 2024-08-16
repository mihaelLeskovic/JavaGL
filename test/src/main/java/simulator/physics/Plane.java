package simulator.physics;

import org.joml.Vector3f;
import simulator.drawables.TerrainObject;
import simulator.transforms.SeaObject;
import simulator.transforms.Transform;

import java.util.ArrayList;
import java.util.List;

public class Plane implements Updateable{
    List<HitboxVisitor> hitboxVisitorList;
    PhysicalObject planePhysicalObject;

    float throttle = 0f;
    boolean shouldApplyThrottle = false;
    float pitch = 0f;
    float yaw = 0f;
    float roll = 0f;
    boolean isGrounded = true;

    public Transform getMainTransform() {
        return planePhysicalObject.getTransform();
    }

    public float getThrottle() {
        return throttle;
    }

    public void setThrottle(float throttle) {
        this.throttle = throttle;
    }

    public boolean isShouldApplyThrottle() {
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

    public Plane(PhysicalObject planePhysicalObject) {
        this.planePhysicalObject = planePhysicalObject;
        hitboxVisitorList = new ArrayList<>();
    }

    @Override
    public void update(float deltaT) {
        for(HitboxVisitor visitor : hitboxVisitorList) {
            visitor.update(deltaT);
        }
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
}

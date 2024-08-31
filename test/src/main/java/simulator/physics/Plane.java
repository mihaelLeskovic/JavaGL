package simulator.physics;

import org.joml.Vector3f;
import simulator.drawables.Drawable;
import simulator.transforms.TerrainObject;
import simulator.physics.hitboxes.HitboxVisitor;
import simulator.physics.hitboxes.PlaneHitbox;
import simulator.physics.hitboxes.VisitableHitbox;
import simulator.physics.hitboxes.WheelHitbox;
import simulator.shaders.Shader;
import simulator.shaders.UniformManager;
import simulator.transforms.*;

import java.util.ArrayList;
import java.util.List;

public class Plane implements Updateable, CollisionEventListener, Renderable {
    List<HitboxVisitor> hitboxVisitorList;
    PhysicalObject planePhysicalObject;
    PlaneFollower planeFollower;
    List<Renderable> hitboxRenderables;
    Renderable planeRenderable;

    float throttleLevel = 0f;
    float throttlePower = 100000f;
    boolean shouldApplyThrottle = false;
    float pitch = 0f;
    float pitchSensitivity = 12f;
    float yaw = 0f;
    float yawSensitivity = 12f;
    float roll = 0f;
    float rollSensitivity = 12f;
    float liftCoefficient = 5f;
    boolean isGrounded = true;
    boolean shouldApplyGravity = true;

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

    public void applyAllForces(float deltaT) {
        if(shouldApplyThrottle) applyThrottle();
        if(planePhysicalObject.velocity.length()>4f) applyRollAndPitch(deltaT);
        if(planePhysicalObject.velocity.length()>1f) applyYaw(deltaT);
        applyLift();
        applyGravity();
//        applyDrag();
    }

    float drag = 1f;
    private void applyDrag() {
        if(planePhysicalObject.velocity.length() < 0.001) return;
        planePhysicalObject.applyForceLocal(new Vector3f(planePhysicalObject.velocity).mul(-drag));
    }

    private void applyLift() {
        if(planePhysicalObject.velocity.length() < 0.001) return;
        float dotProd = new Vector3f(getTransform().getFront()).dot(new Vector3f(planePhysicalObject.getVelocity()).normalize());
        planePhysicalObject.applyForceLocal(new Vector3f(0, 1, 0).mul(liftCoefficient * dotProd * planePhysicalObject.getVelocity().length()));
    }

    private void applyGravity() {
        if(!shouldApplyGravity) return;
        planePhysicalObject.applyAcceleration(new Vector3f(0, -PhysicalObject.GRAVITY, 0));
    }

    private void applyYaw(float deltaT) {
        if(yaw!=0) planePhysicalObject.applyTorqueAroundLocalAxis(new Vector3f(0, 1, 0), yaw * yawSensitivity * deltaT);
    }

    private void applyRollAndPitch(float deltaT) {
        if(pitch!=0) planePhysicalObject.applyTorqueAroundLocalAxis(new Vector3f(1, 0, 0), pitch * pitchSensitivity * deltaT);
        if(roll!=0) planePhysicalObject.applyTorqueAroundLocalAxis(new Vector3f(0, 0, 1), -roll * rollSensitivity * deltaT);
    }

    private void applyThrottle() {
        if(throttleLevel == 0 || throttlePower == 0) return;
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

    public Plane generateDefaultPlaneHboxes(Drawable drawable, Shader shader, UniformManager uniformManager, SimulationEndListener simulationEndListener) {
        List<PlaneHitbox> list = new ArrayList<>();

        PlaneHitbox hullHitbox = new PlaneHitbox(drawable, shader, uniformManager, this, simulationEndListener);
        hullHitbox.setOffset(0, -0.1f, -0.132f).setScale(0.087f, 0.072f, 0.468f);
        list.add(hullHitbox);

        PlaneHitbox wingHitbox = new PlaneHitbox(drawable, shader, uniformManager, this, simulationEndListener);
        wingHitbox.setOffset(0, -0.12f, 0.16f).setScale(0.93f, 0.075f, 0.17f);
        list.add(wingHitbox);

        PlaneHitbox tailHitbox = new PlaneHitbox(drawable, shader, uniformManager, this, simulationEndListener);
        tailHitbox.setOffset(0, 0.02f, 0.45f).setScale(0.2f, 0.117f, 0.212f);
        list.add(tailHitbox);

        hitboxVisitorList.addAll(list);
        hitboxRenderables.addAll(list);

        return this;
    }

    public Plane generateDefaultWheelHboxes(Drawable drawable, Shader shader, UniformManager uniformManager) {
        List<WheelHitbox> list = new ArrayList<>();

        WheelHitbox bigWheeler = new WheelHitbox(drawable, shader, uniformManager, this);
        bigWheeler.setOffset(0, -0.2f, -0.03f).setScale(0.087f, 0.152f, 0.468f);
        list.add(bigWheeler);

        WheelHitbox smallWheeler = new WheelHitbox(drawable, shader, uniformManager, this);
        smallWheeler.setOffset(0, -0.12f, 0.52f).setScale(0.08f, 0.044f, 0.101f);
        list.add(smallWheeler);

        hitboxVisitorList.addAll(list);
        hitboxRenderables.addAll(list);

        return this;
    }

    public Plane generateAllHitboxes(Drawable drawable, Shader shader, UniformManager uniformManager, SimulationEndListener simulationEndListener) {
        generateDefaultWheelHboxes(drawable, shader, uniformManager);
        generateDefaultPlaneHboxes(drawable, shader, uniformManager, simulationEndListener);
        return this;
    }

    public Plane(ObjectInstance planeRenderable, float planeMass) {
        this.planePhysicalObject = new PhysicalObject(planeMass, planeRenderable);
        this.hitboxVisitorList = new ArrayList<>();
        this.hitboxRenderables = new ArrayList<>();
        this.planeRenderable = planeRenderable;
    }

    void updateHitboxes() {
        for(HitboxVisitor visitor : hitboxVisitorList) {
            visitor.update(0);
        }
    }

    @Override
    public void update(float deltaT) {
        planePhysicalObject.update(deltaT);

        updateHitboxes();
        if(planeFollower!=null) planeFollower.update(deltaT);
        applyAllForces(deltaT);
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

    public void visitRunway(Runway runway) {
        for(HitboxVisitor hitboxVisitor : hitboxVisitorList) {
            hitboxVisitor.visitRunway(runway);
        }
    }

    public void visitSea(SeaObject seaObject) {
        for(HitboxVisitor hitboxVisitor : hitboxVisitorList) {
            hitboxVisitor.visitSea(seaObject);
        }
    }

    public void visitTerrain(TerrainObject terrainObject) {
        for(HitboxVisitor hitboxVisitor : hitboxVisitorList) {
            hitboxVisitor.visitTerrain(terrainObject);
        }
    }

    @Override
    public void notifyCollision() {
        planePhysicalObject.notifyCollision();
        isGrounded = true;
//        updateHitboxes();
    }

    @Override
    public void render(Camera camera, Light light) {
        for(Renderable renderable : hitboxRenderables) {
            renderable.render(camera, light);
        }
        planeRenderable.render(camera,light);
    }
}

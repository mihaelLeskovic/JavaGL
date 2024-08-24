package simulator.physics.hitboxes;

import org.joml.Vector3f;
import simulator.drawables.Drawable;
import simulator.transforms.TerrainObject;
import simulator.physics.Plane;
import simulator.shaders.Shader;
import simulator.shaders.UniformManager;
import simulator.transforms.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHitbox extends ObjectInstance implements HitboxVisitor {
    Plane owner;
    Vector3f offset;

    List<Vector3f> boundingPoints;
    boolean pointsAreUpToDate = false;
    boolean shouldRender = false;
    boolean isGrounded = false;
    float groundedTolerance = 0.2f;

    public AbstractHitbox setOffset(float x, float y, float z) {
        return setOffset(new Vector3f(x,y,z));
    }

    public void expand(Vector3f expandVector) {
        offset.add(expandVector);
        getScale().add(expandVector.absolute());
    }

    public void shrink(Vector3f shrinkVector) {
        offset.sub(shrinkVector);
        getScale().sub(shrinkVector.absolute());
    }

    public void moveOffset(Vector3f move) {
        offset.add(move);
    }

    public AbstractHitbox setOffset(Vector3f offset) {
        this.offset = offset;
        setPosition(new Vector3f(offset).add(this.owner.getTransform().getPosition()));
        return this;
    }

    public AbstractHitbox(Drawable drawable, Shader shader, UniformManager uniformManager, Plane owner) {
        super(drawable, shader, uniformManager);
        this.owner = owner;
        this.offset = new Vector3f(0);
        if(owner!=null) setPosition(owner.getTransform().getPosition());
        else setPosition(0,0,0);
    }

    @Override
    public void notifyCollisionListeners() {
        owner.notifyCollision();
    }

    @Override
    public boolean isGrounded() {
        return isGrounded;
    }

    @Override
    public AbstractHitbox setShouldRender(boolean shouldRender) {
        this.shouldRender = shouldRender;
        return this;
    }

    void updateBoundingPoints() {
        if(pointsAreUpToDate) return;

        Vector3f scale = getScale();
        List<Vector3f> boundingPoints = new ArrayList<>();

        Vector3f origin = new Vector3f(getPosition())
                .sub(new Vector3f(this.getFront().normalize()).mul(scale.z/2))
                .sub(new Vector3f(this.getUp().normalize()).mul(scale.y/2))
                .sub(new Vector3f(this.getRight().normalize()).mul(scale.x/2));

        for(int i=0; i<=1; i++) {
            for(int j=0; j<=1; j++) {
                for(int k=0; k<=1; k++) {
                    boundingPoints.add(new Vector3f(origin)
                            .add(new Vector3f(getFront()).mul(scale.z * i))
                            .add(new Vector3f(getUp()).mul(scale.y * j))
                            .add(new Vector3f(getRight()).mul(scale.x * k))
                    );
                }
            }
        }
        pointsAreUpToDate = true;
        this.boundingPoints = boundingPoints;
    }

    @Override
    public void visitRunway(Runway runway) {
        float maxDiff = Float.MIN_VALUE;
        boolean hasCollided = false;
        boolean groundedInThisRun = false;

        float heightOfRunway = runway.getHeight();

        for (Vector3f boundingPoint : boundingPoints) {
            if(!runway.pointIsCloseEnough(boundingPoint)) continue;

            if(heightOfRunway > boundingPoint.y - groundedTolerance) {
                groundedInThisRun = true;
                isGrounded = true;
            }

            if (heightOfRunway > boundingPoint.y) {
                hasCollided = true;
                float diff = heightOfRunway - boundingPoint.y;
                maxDiff = Math.max(diff, maxDiff);
            }
        }

        if(!groundedInThisRun) isGrounded = false;

        if(!hasCollided) return;
        onCollisionTerrain(new Vector3f(0, maxDiff, 0));

        notifyCollisionListeners();
//        updateBoundingPoints();
    }

    @Override
    public void visitTerrain(TerrainObject terrainObject) {
        float maxDiff = Float.MIN_VALUE;
        boolean hasCollided = false;
        boolean groundedInThisRun = false;

        for (Vector3f boundingPoint : boundingPoints) {
            if(!terrainObject.pointIsCloseEnough(boundingPoint)) continue;

            float heightAtPoint = terrainObject.getHeightAt(boundingPoint);

            if(heightAtPoint > boundingPoint.y - groundedTolerance) {
                groundedInThisRun = true;
                isGrounded = true;
            }

            if (heightAtPoint > boundingPoint.y) {
                hasCollided = true;
                float diff = heightAtPoint - boundingPoint.y;
                maxDiff = Math.max(diff, maxDiff);
            }
        }

        if(!groundedInThisRun) isGrounded = false;

        if(!hasCollided) return;
        onCollisionTerrain(new Vector3f(0, maxDiff, 0));
        notifyCollisionListeners();
//        updateBoundingPoints();
    }

    @Override
    public void visitSea(SeaObject seaObject) {
        float heightAtPoint = seaObject.getHeight()/2;

        float maxDiff = Float.MIN_VALUE;
        boolean hasCollided = false;

        for (Vector3f boundingPoint : boundingPoints) {
            if (heightAtPoint > boundingPoint.y) {
                hasCollided = true;
                float diff = heightAtPoint - boundingPoint.y;
                maxDiff = Math.max(diff, maxDiff);
            }
        }

        if(!hasCollided) return;
        onCollisionTerrain(new Vector3f(0, maxDiff, 0));
        notifyCollisionListeners();
//        updateBoundingPoints();
    }

    @Override
    public void update(float deltaT) {
        this.pointsAreUpToDate = false;
        Vector3f newPos = new Vector3f(owner.getTransform().getPosition());
        newPos.add(new Vector3f(owner.getTransform().getFront()).mul(-offset.z))
                .add(new Vector3f(owner.getTransform().getRight()).mul(offset.x))
                .add(new Vector3f(owner.getTransform().getUp()).mul(offset.y));


        setPosition(newPos);
        setLookDirection(owner.getTransform().getFront(), owner.getTransform().getUp());

        updateBoundingPoints();
    }

    @Override
    public void render(Camera camera, Light light) {
        if(!shouldRender) return;
        if(this.getDrawable()==null) return;
        super.render(camera, light);
    }
}

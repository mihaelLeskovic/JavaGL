package simulator.physics;

import org.joml.Vector3f;
import simulator.drawables.Drawable;
import simulator.drawables.TerrainObject;
import simulator.shaders.Shader;
import simulator.shaders.UniformManager;
import simulator.transforms.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHitbox extends ObjectInstance implements HitboxVisitor {
    Transform owner;
    Vector3f offset;

    List<Vector3f> boundingPoints;
    boolean pointsAreUpToDate = false;
    boolean shouldRender = false;

    public AbstractHitbox setOffset(float x, float y, float z) {
        return setOffset(new Vector3f(x,y,z));
    }

    public AbstractHitbox setOffset(Vector3f offset) {
        this.offset = offset;
        setPosition(offset.add(this.owner.getPosition()));
        return this;
    }

    public AbstractHitbox(Drawable drawable, Shader shader, UniformManager uniformManager, Transform owner) {
        super(drawable, shader, uniformManager);
        this.owner = owner;
        this.offset = new Vector3f(0);
        setPosition(owner.getPosition());
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
    public void visitTerrain(TerrainObject terrainObject) {
        update(0);

        float maxDiff = Float.MIN_VALUE;
        boolean hasCollided = false;

        for (Vector3f boundingPoint : boundingPoints) {
            if(!terrainObject.pointIsCloseEnough(boundingPoint)) continue;

            float heightAtPoint = terrainObject.getHeightAt(boundingPoint);
            if (heightAtPoint > boundingPoint.y) {
                hasCollided = true;
                float diff = heightAtPoint - boundingPoint.y;
                maxDiff = Math.max(diff, maxDiff);
            }
        }

        if(!hasCollided) return;
        onCollisionTerrain(new Vector3f(0, maxDiff, 0));
    }

    @Override
    public void visitSea(SeaObject seaObject) {
        update(0);

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
    }

    @Override
    public void update(float deltaT) {
        this.pointsAreUpToDate = false;
        setPosition(new Vector3f(this.offset).add(owner.getPosition()));
        setLookDirection(owner.getFront(), owner.getUp());

        updateBoundingPoints();
    }

    @Override
    public void render(Camera camera, Light light) {
        if(!shouldRender) return;
        super.render(camera, light);
    }
}

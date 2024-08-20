package simulator.transforms;

import org.joml.Vector3f;
import simulator.Main;
import simulator.drawables.Drawable;
import simulator.physics.hitboxes.HitboxVisitor;
import simulator.physics.hitboxes.VisitableHitbox;
import simulator.shaders.Shader;
import simulator.shaders.UniformManager;

import java.util.ArrayList;
import java.util.List;

public class Runway extends ObjectInstance implements VisitableHitbox, Renderable {
    //convention: position.y = 0 always; scale.y = height of the runway

    List<Vector3f> corners;
    Vector3f minVec, maxVec;

    public void updateCorners() {
        corners = new ArrayList<>();
        minVec = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        maxVec = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        Vector3f workingVec;
        for(int i=-1; i<2; i+=2) {
            for(int j=-1; j<2; j+=2) {
                workingVec = new Vector3f(this.getPosition()).add(0, getScale().y, 0)
                        .add(new Vector3f(this.getFront()).mul(i))
                        .add(new Vector3f(this.getRight()).mul(i));
                corners.add(new Vector3f(workingVec));
                minVec.min(workingVec);
                maxVec.max(workingVec);
            }
        }
    }

    public Runway(Drawable drawable, Shader shader, UniformManager uniformManager) {
        super(drawable, shader, uniformManager);
        this.color = new Vector3f(204/255f, 204/255f, 204/255f);

        updateCorners();
    }

    public void setUpFor(Transform transform, float height, float length, float width) {
        setLookDirection(transform.getFront(), transform.getUp());
        setScale(width/2, height, length/2);
        this.setPosition(
                new Vector3f(transform.getPosition())
                        .add(new Vector3f(transform.getFront()).mul(length/2 - transform.getScale().z))
//                        .add(new Vector3f())
        );
        this.setPosition(this.getPosition().x, 0, this.getPosition().z);

        updateCorners();
    }

    public float getHeight() {
        return maxVec.y;
    }

    @Override
    public void accept(HitboxVisitor visitor) {
        visitor.visitRunway(this);
    }

    public boolean pointIsCloseEnough(Vector3f boundingPoint) {
        if(boundingPoint.y > this.getScale().y) return false;

        Vector3f center = new Vector3f(getPosition());
        Vector3f halfWidth = new Vector3f(getRight()).mul(getScale().x);
        Vector3f halfLength = new Vector3f(getFront()).mul(getScale().z);

        Vector3f toPoint = new Vector3f(boundingPoint.x - center.x, 0, boundingPoint.z - center.z);

        float dotWidth = toPoint.dot(halfWidth.normalize());
        float dotLength = toPoint.dot(halfLength.normalize());

        return Math.abs(dotLength) < getScale().z && Math.abs(dotWidth) < getScale().x;
    }
}

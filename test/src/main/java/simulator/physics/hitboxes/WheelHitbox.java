package simulator.physics.hitboxes;

import org.joml.Vector3f;
import simulator.drawables.Drawable;
import simulator.physics.Plane;
import simulator.shaders.Shader;
import simulator.shaders.UniformManager;
import simulator.transforms.SeaObject;

public class WheelHitbox extends AbstractHitbox{
    public WheelHitbox(Drawable drawable, Shader shader, UniformManager uniformManager, Plane owner) {
        super(drawable, shader, uniformManager, owner);
    }


    @Override
    public void onCollisionSea(Vector3f diff) {/*do nothing*/}

    @Override
    public void onCollisionTerrain(Vector3f diff) {
        if(diff.length() < 0.0001f) return;

        this.owner.getTransform().translateGlobal(diff);
        setPosition(new Vector3f(this.offset).add(owner.getTransform().getPosition()));
        setLookDirection(owner.getTransform().getFront(), owner.getTransform().getUp());
        pointsAreUpToDate = false;
    }

    @Override
    public void visitSea(SeaObject seaObject) {
        return;
    }
}

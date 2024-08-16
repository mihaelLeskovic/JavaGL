package simulator.physics;

import org.joml.Vector3f;
import simulator.drawables.Drawable;
import simulator.shaders.Shader;
import simulator.shaders.UniformManager;
import simulator.transforms.SeaObject;
import simulator.transforms.Transform;

public class WheelHitbox extends AbstractHitbox{
    public WheelHitbox(Drawable drawable, Shader shader, UniformManager uniformManager, Transform owner) {
        super(drawable, shader, uniformManager, owner);
    }


    @Override
    public void onCollisionSea(Vector3f diff) {/*do nothing*/}

    @Override
    public void onCollisionTerrain(Vector3f diff) {
        if(diff.length() < 0.0001f) return;

        this.owner.translateGlobal(diff);
        setPosition(new Vector3f(this.offset).add(owner.getPosition()));
        setLookDirection(owner.getFront(), owner.getUp());
        pointsAreUpToDate = false;
    }

    @Override
    public void visitSea(SeaObject seaObject) {
        return;
    }
}

package simulator.physics;

import org.joml.Vector3f;
import simulator.transforms.Transform;

public class PlaneFollower implements Updateable{
    Transform transform;
    Plane plane;
    Vector3f offset = new Vector3f(0, 1, -1);

    public PlaneFollower(Transform transform) {
        this.transform = transform;
    }

    public PlaneFollower setOffset(Vector3f offset) {
        this.offset = offset;
        return this;
    }

    public void setPlane(Plane plane) {
        this.plane = plane;
        this.offset.mul(plane.getTransform().getScale());
    }

    public void updateTransform() {
        Transform planeTransform = plane.getTransform();

        transform.setPosition(new Vector3f(planeTransform.getPosition()));
        transform.setLookDirection(planeTransform.getFront(), planeTransform.getUp());
//        transform.rotate(transform.getUp(), (float) (-Math.PI/2));

//        transform.translateLocal(offset);

        transform.translateGlobal(new Vector3f(transform.getFront()).mul(offset.z));
        transform.translateGlobal(new Vector3f(transform.getUp()).mul(offset.y));
        transform.translateGlobal(new Vector3f(transform.getRight()).mul(offset.x));

    }

    @Override
    public void update(float deltaT) {
        updateTransform();
    }
}

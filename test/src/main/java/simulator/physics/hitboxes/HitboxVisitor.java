package simulator.physics.hitboxes;

import org.joml.Vector3f;
import simulator.drawables.TerrainObject;
import simulator.physics.Updateable;
import simulator.transforms.SeaObject;

public interface HitboxVisitor extends Updateable {
    void visitTerrain(TerrainObject terrainObject);
    void visitSea(SeaObject seaObject);
    void onCollisionSea(Vector3f diff);
    void onCollisionTerrain(Vector3f diff);
    HitboxVisitor setShouldRender(boolean shouldRender);
    boolean isGrounded();
    void notifyCollisionListeners();
}

package simulator.physics;

import org.joml.Vector3f;
import simulator.drawables.TerrainObject;

import java.util.List;

public class CollisionOverseer {
    List<TerrainObject> terrainObjectList;

    public CollisionOverseer(List<TerrainObject> terrainObjectList) {
        this.terrainObjectList = terrainObjectList;
    }

    public float getLegalPointHeight(Vector3f point) {
        float max = Float.MIN_VALUE;
        for(TerrainObject terrainObject : terrainObjectList) {
            max = Math.max(terrainObject.getHeightAt(point), max);
        }
        return max;
    }
}

package simulator.physics.hitboxes;

import org.joml.Vector3f;
import simulator.drawables.Drawable;
import simulator.physics.Plane;
import simulator.physics.SimulationEndListener;
import simulator.shaders.Shader;
import simulator.shaders.UniformManager;

public class PlaneHitbox extends AbstractHitbox{
    SimulationEndListener simulationEndListener;

    public PlaneHitbox(Drawable drawable, Shader shader, UniformManager uniformManager, Plane owner, SimulationEndListener simulationEndListener) {
        super(drawable, shader, uniformManager, owner);
        this.simulationEndListener = simulationEndListener;
    }

    void notifyListener() {
        if(simulationEndListener==null) return;
        simulationEndListener.endSimulation();
    }

    @Override
    public void onCollisionSea(Vector3f diff) {
        notifyListener();
    }

    @Override
    public void onCollisionTerrain(Vector3f diff) {
        notifyListener();
    }
}

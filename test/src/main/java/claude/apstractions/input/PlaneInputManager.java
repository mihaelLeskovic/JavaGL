package claude.apstractions.input;

import claude.apstractions.transforms.Transform;

public class PlaneInputManager implements InputManager {
    private Transform plane;

    public PlaneInputManager(Transform plane) {
        this.plane = plane;
    }

    @Override
    public void processInput(long window) {

    }

    @Override
    public void procesInputDeltaT(long window, float deltaT) {

    }
}

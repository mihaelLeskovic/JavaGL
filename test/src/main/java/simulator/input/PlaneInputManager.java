package simulator.input;

import simulator.physics.PhysicalObject;

public class PlaneInputManager implements InputManager {
    private PhysicalObject planePO;

    public PlaneInputManager(PhysicalObject planePO) {
        this.planePO = planePO;
    }

    @Override
    public void procesInputDeltaT(long window, float deltaT) {

    }
}

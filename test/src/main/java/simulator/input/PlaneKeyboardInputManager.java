package simulator.input;

import simulator.physics.PhysicalObject;

public class PlaneKeyboardInputManager implements InputManager {
    private PhysicalObject planePO;

    public PlaneKeyboardInputManager(PhysicalObject planePO) {
        this.planePO = planePO;
    }

    @Override
    public void procesInputDeltaT(long window, float deltaT) {

    }
}

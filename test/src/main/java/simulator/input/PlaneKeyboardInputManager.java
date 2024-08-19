package simulator.input;

import simulator.physics.PhysicalObject;
import simulator.physics.Plane;

import static org.lwjgl.glfw.GLFW.*;

public class PlaneKeyboardInputManager implements InputManager {
    private Plane plane;
    boolean shouldToggleThrottle = false;
    float pitchSensitivity = 0.5f;
    float yawSensitivity = 0.5f;
    float rollSensitivity = 0.5f;

    public PlaneKeyboardInputManager(Plane plane) {
        this.plane = plane;
    }

    @Override
    public void procesInputDeltaT(long window, float deltaT) {
        if(plane==null) return;

        if(glfwGetKey(window, GLFW_KEY_T) == GLFW_PRESS) this.shouldToggleThrottle = true;
        if(shouldToggleThrottle && glfwGetKey(window, GLFW_KEY_T) == GLFW_RELEASE) plane.setShouldApplyThrottle(!plane.getShouldApplyThrottle());

        int kb0 = GLFW_KEY_0;
        int np0 = GLFW_KEY_KP_0;
        for(int val=0; val <=9 ; val++) {
            if(glfwGetKey(window, kb0+val) == GLFW_PRESS) plane.setThrottleLevel(val / 9.f);
            if(glfwGetKey(window, np0+val) == GLFW_PRESS) plane.setThrottleLevel(val / 9.f);
        }

        if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) plane.setPitch(-1 * pitchSensitivity);
        else if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) plane.setPitch(1 * pitchSensitivity);
        else plane.setPitch(0);

        if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) plane.setRoll(1 * rollSensitivity);
        else if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) plane.setRoll(-1 * rollSensitivity);
        else plane.setRoll(0);

        if(glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) plane.setYaw(1 * yawSensitivity);
        else if(glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) plane.setYaw(-1 * yawSensitivity);
        else plane.setYaw(0);
    }
}

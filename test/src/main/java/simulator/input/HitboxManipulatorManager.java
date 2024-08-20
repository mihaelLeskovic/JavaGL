package simulator.input;

import org.joml.Vector3f;
import simulator.physics.hitboxes.AbstractHitbox;


import static org.lwjgl.glfw.GLFW.*;

public class HitboxManipulatorManager implements InputManager{
    AbstractHitbox abstractHitbox;
    boolean shouldExpand = true;
    boolean shouldToggleExpand = false;
    float changeFactor = 1;

    public HitboxManipulatorManager(AbstractHitbox abstractHitbox) {
        this.abstractHitbox = abstractHitbox;
    }

    void action(Vector3f vec) {
        if(shouldExpand) abstractHitbox.expand(vec);
        else abstractHitbox.shrink(vec);
    }

    @Override
    public void procesInputDeltaT(long window, float deltaT) {
        if(glfwGetKey(window, GLFW_KEY_KP_5) == GLFW_PRESS) shouldToggleExpand = true;
        if(shouldToggleExpand && glfwGetKey(window, GLFW_KEY_KP_5) == GLFW_RELEASE) {
            shouldExpand = !shouldExpand;
            shouldToggleExpand = false;
        }

        if(glfwGetKey(window, GLFW_KEY_KP_8) == GLFW_PRESS) {
            action(new Vector3f(0, 1, 0).mul(changeFactor * deltaT));
        }
        if(glfwGetKey(window, GLFW_KEY_KP_2) == GLFW_PRESS) {
            action(new Vector3f(0, -1, 0).mul(changeFactor * deltaT));
        }
        if(glfwGetKey(window, GLFW_KEY_KP_4) == GLFW_PRESS) {
            action(new Vector3f(1, 0, 0).mul(changeFactor * deltaT));
        }
        if(glfwGetKey(window, GLFW_KEY_KP_6) == GLFW_PRESS) {
            action(new Vector3f(-1, 0, 0).mul(changeFactor * deltaT));
        }
        if(glfwGetKey(window, GLFW_KEY_KP_7) == GLFW_PRESS) {
            action(new Vector3f(0, 0, 1).mul(changeFactor * deltaT));
        }
        if(glfwGetKey(window, GLFW_KEY_KP_9) == GLFW_PRESS) {
            action(new Vector3f(0, 0, -1).mul(changeFactor * deltaT));
        }

        if(glfwGetKey(window, GLFW_KEY_I) == GLFW_PRESS) {
            abstractHitbox.moveOffset(new Vector3f(0, 1, 0).mul(changeFactor * deltaT * 2));
        }
        if(glfwGetKey(window, GLFW_KEY_K) == GLFW_PRESS) {
            abstractHitbox.moveOffset(new Vector3f(0, -1, 0).mul(changeFactor * deltaT * 2));
        }
        if(glfwGetKey(window, GLFW_KEY_J) == GLFW_PRESS) {
            abstractHitbox.moveOffset(new Vector3f(1, 0, 0).mul(changeFactor * deltaT * 2));
        }
        if(glfwGetKey(window, GLFW_KEY_L) == GLFW_PRESS) {
            abstractHitbox.moveOffset(new Vector3f(-1, 0, 0).mul(changeFactor * deltaT * 2));
        }
        if(glfwGetKey(window, GLFW_KEY_U) == GLFW_PRESS) {
            abstractHitbox.moveOffset(new Vector3f(0, 0, 1).mul(changeFactor * deltaT * 2));
        }
        if(glfwGetKey(window, GLFW_KEY_O) == GLFW_PRESS) {
            abstractHitbox.moveOffset(new Vector3f(0, 0, -1).mul(changeFactor * deltaT * 2));
        }
    }
}

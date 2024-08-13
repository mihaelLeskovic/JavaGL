package simulator.input;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;

public class StateControlsInputManager implements InputManager{
    private boolean shouldClose = false;
    private boolean shouldChangeCull = false;

    @Override
    public void procesInputDeltaT(long window, float deltaT) {
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            shouldClose = true;
        }
        if (shouldClose && glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_RELEASE) {
            glfwSetWindowShouldClose(window, true);
            shouldClose = false;
        }

        if (glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS) {
            shouldChangeCull = true;
        }
        if (shouldChangeCull && glfwGetKey(window, GLFW_KEY_C) == GLFW_RELEASE) {
            if (glIsEnabled(GL_CULL_FACE)) {
                glDisable(GL_CULL_FACE);
            } else {
                glEnable(GL_CULL_FACE);
            }
            shouldChangeCull = false;
        }
    }
}

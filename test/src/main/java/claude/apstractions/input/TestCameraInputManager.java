package claude.apstractions.input;

import claude.apstractions.transforms.Camera;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;

public class TestCameraInputManager implements InputManager{
    private Camera camera;
    private boolean shouldClose = false;
    private boolean shouldChangeCull = false;

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public TestCameraInputManager(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void processInput(long window) {
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

    @Override
    public void procesInputDeltaT(long window, float deltaT) {
        if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            camera.translateLocal(new Vector3f(0, 0, -1).mul(deltaT));
        }
        if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            camera.translateLocal(new Vector3f(0, 0, 1).mul(deltaT));
        }
        if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            camera.translateLocal(new Vector3f(-1, 0, 0).mul(deltaT));
        }
        if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            camera.translateLocal(new Vector3f(1, 0, 0).mul(deltaT));
        }
        if(glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) {
            camera.translateLocal(new Vector3f(0, 1, 0).mul(deltaT));
        }
        if(glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
            camera.translateLocal(new Vector3f(0, -1, 0).mul(deltaT));
        }
    }
}

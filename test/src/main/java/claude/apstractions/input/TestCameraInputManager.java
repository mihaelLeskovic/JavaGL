package claude.apstractions.input;

import claude.apstractions.transforms.Camera;
import claude.apstractions.transforms.Transform;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;

public class TestCameraInputManager implements InputManager{
    private double xSens, ySens;
    private float moveSpeed;
    private Transform movable;
    private boolean shouldClose = false;
    private boolean shouldChangeCull = false;
    private boolean firstMouse = true;
    private boolean lockMouse;
    private int width, height;

    //initialized array values to avoid initialization every time we poll for cursor pos
    private double[] xPos = new double[1];
    private double[] yPos = new double[1];

    private float lastX = getCenterX();
    private float lastY = getCenterY();
    private float yaw = 0.0f;
    private float pitch = 0.0f;

    public Transform getMovable() {
        return movable;
    }

    public void setMovable(Camera movable) {
        this.movable = movable;
    }

    public TestCameraInputManager(long window, int width, int height, Transform movable, boolean lockMouse) {
        this.movable = movable;
        this.lockMouse = lockMouse;
        this.width = width;
        this.height = height;

        this.ySens = 0.1;
        this.xSens = 0.1;
        this.moveSpeed = 4f;

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
    }

    public TestCameraInputManager setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
        return this;
    }

    public TestCameraInputManager setxSens(double xSens) {
        this.xSens = xSens;
        return this;
    }

    public TestCameraInputManager setySens(double ySens) {
        this.ySens = ySens;
        return this;
    }

    private int getCenterX() {
        return width/2;
    }

    private int getCenterY() {
        return height/2;
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
            movable.translateLocal(new Vector3f(0, 0, -1).mul(deltaT * moveSpeed));
        }
        if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            movable.translateLocal(new Vector3f(0, 0, 1).mul(deltaT * moveSpeed));
        }
        if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            movable.translateLocal(new Vector3f(-1, 0, 0).mul(deltaT * moveSpeed));
        }
        if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            movable.translateLocal(new Vector3f(1, 0, 0).mul(deltaT * moveSpeed));
        }
        if(glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) {
            movable.translateLocal(new Vector3f(0, 1, 0).mul(deltaT * moveSpeed));
        }
        if(glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
            movable.translateLocal(new Vector3f(0, -1, 0).mul(deltaT * moveSpeed));
        }

        processMouse(window, deltaT);
    }

    private void processMouse(long window, float deltaT) {
        if (!lockMouse) return;

        glfwGetCursorPos(window, xPos, yPos);
        float xpos = (float) xPos[0];
        float ypos = (float) yPos[0];

        if (firstMouse) {
            lastX = xpos;
            lastY = ypos;
            firstMouse = false;
            return;
        }

        float xoffset = xpos - lastX;
        float yoffset = lastY - ypos; // Reversed since y-coordinates go from bottom to top

        xoffset *= xSens;
        yoffset *= ySens;

        // Calculate rotation around Y-axis (yaw)
        if (xoffset != 0) {
            movable.rotate(new Vector3f(0, 1, 0), (float) Math.toRadians(-xoffset));
        }

        // Calculate rotation around X-axis (pitch)
        if (yoffset != 0) {
            movable.rotate(movable.getRight(), (float) Math.toRadians(yoffset));
        }

        // Reset cursor to center if it's too far
        if (Math.abs(xpos - getCenterX()) > width / 4 || Math.abs(ypos - getCenterY()) > height / 4) {
            glfwSetCursorPos(window, getCenterX(), getCenterY());
            xpos = getCenterX();
            ypos = getCenterY();
        }

        lastX = xpos;
        lastY = ypos;
    }
}

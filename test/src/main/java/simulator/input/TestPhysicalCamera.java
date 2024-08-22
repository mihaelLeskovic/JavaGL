package simulator.input;

import simulator.physics.PhysicalObject;
import org.joml.Vector3f;
import simulator.utility.WindowResizeListener;

import static org.lwjgl.glfw.GLFW.*;

public class TestPhysicalCamera implements InputManager, WindowResizeListener {
    private double xSens, ySens;
    private float moveSpeed;
    private PhysicalObject physicalObject;
    private boolean firstMouse = true;
    private boolean lockMouse;
    private boolean shouldToggleLock = false;
    private int width, height;

    //initialized array values to avoid initialization every time we poll for cursor pos
    private double[] xPos = new double[1];
    private double[] yPos = new double[1];

    private float lastX;
    private float lastY;

    public PhysicalObject getPhysicalObject() {
        return physicalObject;
    }

    public void setPhysicalObject(PhysicalObject physicalObject) {
        this.physicalObject = physicalObject;
    }

    public TestPhysicalCamera(long window, int width, int height, PhysicalObject physicalObject, boolean lockMouse) {
        this.physicalObject = physicalObject;
        this.lockMouse = lockMouse;
        this.width = width;
        this.height = height;

        this.ySens = 0.1;
        this.xSens = 0.1;
        this.moveSpeed = 10f;

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
    }

    public TestPhysicalCamera setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
        return this;
    }

    public TestPhysicalCamera setxSens(double xSens) {
        this.xSens = xSens;
        return this;
    }

    public TestPhysicalCamera setySens(double ySens) {
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
    public void procesInputDeltaT(long window, float deltaT) {

        if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            physicalObject.applyForceLocal(new Vector3f(0, 0, -moveSpeed));
        }
        if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            physicalObject.applyForceLocal(new Vector3f(0, 0, moveSpeed));
        }
        if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            physicalObject.applyForceLocal(new Vector3f(-moveSpeed, 0, 0));
        }
        if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            physicalObject.applyForceLocal(new Vector3f(moveSpeed, 0, 0));
        }
        if(glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) {
            physicalObject.applyForceLocal(new Vector3f(0, moveSpeed, 0));
        }
        if(glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
            physicalObject.applyForceLocal(new Vector3f(0, -moveSpeed, 0));
        }
        if(glfwGetKey(window, GLFW_KEY_I) == GLFW_RELEASE) {
            if(shouldToggleLock) {
                if(!lockMouse) {
                    glfwSetCursorPos(window, getCenterX(), getCenterY());
                }
                lockMouse = !lockMouse;
            }
            shouldToggleLock = false;
        }
        if(glfwGetKey(window, GLFW_KEY_I) == GLFW_PRESS) {
            shouldToggleLock = true;
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
        float yoffset = ypos - lastY;

        xoffset *= xSens;
        yoffset *= ySens;

        if (xoffset*xoffset > 0.0001f) {
//            physicalObject.getTransform().rotate(new Vector3f(0, 1, 0), (float) Math.toRadians(-xoffset));
            physicalObject.applyTorqueAroundAxis(new Vector3f(0, 1, 0), (float) Math.toRadians(-xoffset));
        }

        if (yoffset*yoffset > 0.0001f) {
//            physicalObject.getTransform().rotate(physicalObject.getTransform().getRight(), (float) Math.toRadians(-yoffset));
//            physicalObject.applyTorqueAroundAxis(new Vector3f(physicalObject.getTransform().getRight()), (float) Math.toRadians(-xoffset));
            physicalObject.applyTorqueAroundLocalAxis(new Vector3f(1, 0, 0), (float) Math.toRadians(-yoffset));
        }

        if (Math.abs(xpos - getCenterX()) > width / 4 || Math.abs(ypos - getCenterY()) > height / 4) {
            glfwSetCursorPos(window, getCenterX(), getCenterY());
            xpos = getCenterX();
            ypos = getCenterY();
        }

        lastX = xpos;
        lastY = ypos;
    }

    @Override
    public void resizeWindow(long window, int width, int height) {
        this.width = width;
        this.height = height;
    }
}

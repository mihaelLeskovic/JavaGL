package simulator.input;

import simulator.drawables.TerrainObject;
import simulator.transforms.Camera;
import simulator.transforms.ObjectInstance;
import simulator.transforms.Transform;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class TestCameraInputManager implements InputManager{
    private double xSens, ySens;
    private float moveSpeed;
    private Transform movable;
    private ObjectInstance testedMovable;
    private TerrainObject terrainObject;
    private float testedMovableDistance;
    private boolean firstMouse = true;
    private boolean lockMouse;
    private boolean shouldToggleLock = false;
    private int width, height;

    //initialized array values to avoid initialization every time we poll for cursor pos
    private double[] xPos = new double[1];
    private double[] yPos = new double[1];

    private float lastX;
    private float lastY;

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

    public TestCameraInputManager addTestThings(ObjectInstance testedMovable, TerrainObject terrainObject) {
        this.terrainObject = terrainObject;
        this.testedMovable = testedMovable;
        this.testedMovableDistance = 1f;

        return this;
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

        testedMovableProcedure(window, deltaT);
    }

    private void testedMovableProcedure(long window, float deltaT) {
        if(testedMovable == null || terrainObject == null) return;

        if(glfwGetKey(window, GLFW_KEY_O) == GLFW_PRESS) {
            testedMovableDistance += 1*deltaT;
        }
        if(glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS) {
            testedMovableDistance -= 1*deltaT;
        }

        testedMovable.setPosition(new Vector3f(movable.getPosition()).add(new Vector3f(movable.getFront()).mul(testedMovableDistance)));
        if(terrainObject.isAbovePoint(testedMovable.getPosition())) {
            testedMovable.setColor(1, 0, 0);
        } else {
            testedMovable.setColor(0, 1, 0);
        }
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

        if (xoffset != 0) {
            movable.rotate(new Vector3f(0, 1, 0), (float) Math.toRadians(-xoffset));
        }

        if (yoffset != 0) {
            movable.rotate(movable.getRight(), (float) Math.toRadians(-yoffset));
        }

        if (Math.abs(xpos - getCenterX()) > width / 4 || Math.abs(ypos - getCenterY()) > height / 4) {
            glfwSetCursorPos(window, getCenterX(), getCenterY());
            xpos = getCenterX();
            ypos = getCenterY();
        }

        lastX = xpos;
        lastY = ypos;
    }
}

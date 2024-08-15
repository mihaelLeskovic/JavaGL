package simulator.input;

import simulator.drawables.TerrainObject;
import simulator.physics.PhysicalObject;
import simulator.transforms.ObjectInstance;
import org.joml.Vector3f;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class TestPhysicalCamera implements InputManager{
    private double xSens, ySens;
    private float moveSpeed;
    private PhysicalObject physicalObject;
    private ObjectInstance testedMovable;
    private List<TerrainObject> terrainObjects;
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

    public TestPhysicalCamera addTestThings(ObjectInstance testedMovable, List<TerrainObject> terrainObjects) {
        this.terrainObjects = terrainObjects;
        this.testedMovable = testedMovable;
        this.testedMovableDistance = 1f;

        return this;
    }

    @Override
    public void procesInputDeltaT(long window, float deltaT) {

        if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            physicalObject.applyForceLocal(new Vector3f(0, 0, -moveSpeed));
        }
        if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            physicalObject.applyForce(new Vector3f(physicalObject.getTransform().getFront()).mul(-moveSpeed));
        }
        if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            physicalObject.applyForce(new Vector3f(physicalObject.getTransform().getRight()).mul(-moveSpeed));
        }
        if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            physicalObject.applyForce(new Vector3f(physicalObject.getTransform().getRight()).mul(moveSpeed));
        }
        if(glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) {
            physicalObject.applyForce(new Vector3f(0, 1, 0).mul(moveSpeed));
        }
        if(glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
            physicalObject.applyForce(new Vector3f(0, -1, 0).mul(moveSpeed));
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
        if(testedMovable == null || terrainObjects == null) return;

        if(glfwGetKey(window, GLFW_KEY_O) == GLFW_PRESS) {
            testedMovableDistance += 1*deltaT;
        }
        if(glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS) {
            testedMovableDistance -= 1*deltaT;
        }

        testedMovable.setPosition(new Vector3f(physicalObject.getTransform().getPosition()).add(new Vector3f(physicalObject.getTransform().getFront()).mul(testedMovableDistance)));
        for(TerrainObject to : terrainObjects) {
            if(to.isAbovePoint(testedMovable.getPosition())) {
                testedMovable.setColor(1, 0, 0);
                break;
            } else {
                testedMovable.setColor(0, 1, 0);
            }
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
            physicalObject.getTransform().rotate(new Vector3f(0, 1, 0), (float) Math.toRadians(-xoffset));
        }

        if (yoffset != 0) {
            physicalObject.getTransform().rotate(physicalObject.getTransform().getRight(), (float) Math.toRadians(-yoffset));
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

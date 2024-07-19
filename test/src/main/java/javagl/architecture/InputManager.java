package javagl.architecture;

import javagl.architecture.transforms.Transform;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.DoubleBuffer;
import java.time.Duration;
import java.time.Instant;

import static org.lwjgl.glfw.GLFW.*;

public class InputManager {
    final static float WS_MULTIPLIER = 5;
    final static float AD_MULTIPLIER = -5;
    final static float QE_MULTIPLIER = 5;
    final static float X_SENS = 1;
    final static float Y_SENS = 1;

    boolean firstMouse = true;
    boolean shouldPlaceObject = false;
    Instant lastTime;

    Renderer renderer;
    Transform movable;
    int width, height;
    float centerX, centerY;
    boolean lockMouse = true;

    public InputManager(Renderer renderer, Transform movable, int width, int height) {
        this.renderer = renderer;
        this.movable = movable;
        resize(width, height);
        lastTime = Instant.now();
    }

    private void resize(int width, int height) {
        this.width = width;
        this.height = height;
        centerX = width/2.f;
        centerY = height/2.f;
    }

    void processInput(long window) {
        float deltaT = Duration.between(lastTime, Instant.now()).toMillis() * 0.001f;
        lastTime = Instant.now();

        if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            movable.position.add(movable.front.mul(WS_MULTIPLIER * deltaT, new Vector3f()));
        }
        if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            movable.position.sub(movable.front.mul(WS_MULTIPLIER * deltaT, new Vector3f()));
        }
        if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            movable.position.sub(movable.right.mul(AD_MULTIPLIER * deltaT, new Vector3f()));
        }
        if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            movable.position.add(movable.right.mul(AD_MULTIPLIER * deltaT, new Vector3f()));
        }
        if(glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
            movable.position.add(movable.up.mul(QE_MULTIPLIER * deltaT, new Vector3f()));
        }
        if(glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) {
            movable.position.sub(movable.up.mul(QE_MULTIPLIER * deltaT, new Vector3f()));
        }

        if(firstMouse) {
            if(lockMouse) glfwSetCursorPos(window, width/2., height/2.);
            firstMouse = false;
            return;
        }

        if(lockMouse) {
            DoubleBuffer posXbuffer = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer posYbuffer = BufferUtils.createDoubleBuffer(1);
            glfwGetCursorPos(window, posXbuffer, posYbuffer);
            float posX = (float) posXbuffer.get();
            float posY = (float) posYbuffer.get();
            movable.rotate(new Vector3f(0, 1, 0), (posX - centerX) * X_SENS * deltaT);
            movable.rotate(movable.right.mul(-1, new Vector3f()), (posY - centerY) * Y_SENS * deltaT);
            glfwSetCursorPos(window, centerX, centerY);
        }
    }
}

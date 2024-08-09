package claude.apstractions.input;

import org.lwjgl.glfw.GLFWKeyCallbackI;

public interface InputManager {
    void processInput(long window);
    void procesInputDeltaT(long window, float deltaT);
}

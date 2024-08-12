package claude.apstractions.input;

import org.lwjgl.glfw.GLFWKeyCallbackI;

public interface InputManager {
    void procesInputDeltaT(long window, float deltaT);
}

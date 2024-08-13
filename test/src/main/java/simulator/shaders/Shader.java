package simulator.shaders;

import simulator.Cleanable;

import static org.lwjgl.opengl.GL20.*;

public class Shader implements Cleanable {
    private int shader;
    private boolean isCleaned = false;

    public int getShader() {
        return shader;
    }

    public Shader(int shader) {
        this.shader = shader;
    }

    @Override
    public void cleanup() {
        if(isCleaned) return;
        isCleaned = true;

        glDeleteProgram(shader);
    }

    @Override
    public boolean isCleaned() {
        return isCleaned;
    }
}

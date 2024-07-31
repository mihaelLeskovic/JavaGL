package claude.apstractions.shaders;

import claude.apstractions.Cleanable;

import static org.lwjgl.opengl.GL20.glDeleteShader;

public class Shader implements Cleanable {
    private int shader;

    public int getShader() {
        return shader;
    }

    public Shader(int shader) {
        this.shader = shader;
    }

    @Override
    public void cleanup() {
        glDeleteShader(shader);
    }
}

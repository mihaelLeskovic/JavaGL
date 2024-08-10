package claude.apstractions.renderables;

import claude.apstractions.Cleanable;

import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

public abstract class Drawable implements Cleanable {
    int[] VAOs;
    public abstract void draw();

    @Override
    public void cleanup() {
        glDeleteVertexArrays(VAOs);
    }
}

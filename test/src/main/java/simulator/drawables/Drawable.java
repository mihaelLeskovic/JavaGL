package simulator.drawables;

import simulator.utility.Cleanable;

import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

public abstract class Drawable implements Cleanable {
    int[] VAOs;
    boolean isCleaned = false;
    public abstract void draw();

    @Override
    public void cleanup() {
        if(isCleaned()) return;
        isCleaned = true;

        glDeleteVertexArrays(VAOs);
    }

    @Override
    public boolean isCleaned() {
        return false;
    }
}

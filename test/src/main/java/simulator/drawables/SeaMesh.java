package simulator.drawables;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

public class SeaMesh extends Drawable {
    //multiple VBOs, all containing vertices of a unique triangle strip
    int[] VBOs;

    @Override
    public void draw() {
        for(int i=0; i<VAOs.length; i++) {
            glBindVertexArray(VAOs[i]);
            glDrawArrays(GL_TRIANGLE_STRIP, 0, VAOs.length * 2);
            glBindVertexArray(0);
        }
    }

    @Override
    public void cleanup() {
        glDeleteBuffers(VBOs);
        super.cleanup();
    }
}

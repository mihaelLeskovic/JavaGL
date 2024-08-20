package simulator.drawables;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

public class TerrainMesh extends Drawable{
    int[] VBOs;

    public int getVAO(int i) {
        return VAOs[i];
    }

    public int getVBO(int i) {
        return VBOs[i];
    }

    public void initializeBuffers(int countOfStrips) {
        VAOs = new int[countOfStrips];
        VBOs = new int[2 * countOfStrips];

        glGenVertexArrays(VAOs);
        glGenBuffers(VBOs);
    }

    @Override
    public void draw() {
//        glBindVertexArray(VAOs[VAOs.length/2]);
//        glDrawArrays(GL_TRIANGLE_STRIP, 0, VAOs.length * 2);
//        glBindVertexArray(0);

        for(int i=0; i<VAOs.length; i++) {
            glBindVertexArray(VAOs[i]);
            glDrawArrays(GL_TRIANGLE_STRIP, 0, VAOs.length * 2);
            glBindVertexArray(0);
        }
    }

    @Override
    public void cleanup() {
        if(isCleaned()) return;
        isCleaned = true;

        glDeleteBuffers(VBOs);
        super.cleanup();
    }
}

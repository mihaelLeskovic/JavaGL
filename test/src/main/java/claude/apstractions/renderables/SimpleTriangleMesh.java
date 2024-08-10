package claude.apstractions.renderables;

import claude.apstractions.Cleanable;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

public class SimpleTriangleMesh extends Drawable implements Cleanable {
    int[] VBO;
    int EBO;
    int numVertices;
    int numFaces;

    public int[] getVBO() {
        return VBO;
    }

    public int getVBOindexed(int index) {
        return VBO[index];
    }

    public int getEBO() {
        return EBO;
    }

    public int getNumVertices() {
        return numVertices;
    }

    public int getNumFaces() {
        return numFaces;
    }

    @Override
    public void draw() {
        glBindVertexArray(VAOs[0]);
        glDrawElements(GL_TRIANGLES, numFaces*3, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    @Override
    public void cleanup() {
        glDeleteBuffers(VBO);
        glDeleteBuffers(EBO);
        super.cleanup();
    }
}

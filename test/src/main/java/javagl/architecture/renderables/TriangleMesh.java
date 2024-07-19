package javagl.architecture.renderables;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class TriangleMesh extends Renderable{
    float[] vertices;
    float[] normals;
    float[] uvCoords;
    int[] indices;

//    VBO order of elements: vertices, normals, uvCoords
    float[][] getArrayReferences() {
        return new float[][]{vertices, normals, uvCoords};
    }
    int[] getNumOfElements() {
        return new int[]{3, 3, 2};
    }
    int[] VBO;
    int EBO;

    public TriangleMesh(AIScene scene, int n) {

        AIMesh mesh = AIMesh.create(scene.mMeshes().get(n));

        VAO = glGenVertexArrays();
        VBO = new int[3];
        for(int i=0; i<VBO.length; i++){
            VBO[i] = glGenBuffers();
        }
        EBO = glGenBuffers();

        vertices = AssimpUtil.processVertices(mesh);
        indices = AssimpUtil.processIndices(mesh);
        normals = AssimpUtil.processNormals(mesh);
        uvCoords = AssimpUtil.processTextCoords(mesh);

        for(int i=0; i<VBO.length; i++){
            bufferDataVBO(i, true);
        }

        bufferDataEBO();
    }

    @Override
    public void draw() {
        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    @Override
    public void normalize() {
        Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3f max = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

        Vector3f curr = new Vector3f();
        for(int i=0; i<vertices.length; i+=3){
            curr.set(vertices[i], vertices[i+1], vertices[i+2]);
            min.min(curr);
            max.max(curr);
        }

        Vector3f range = max.sub(min, new Vector3f());
        float M = range.get(range.maxComponent());
        Vector3f center = max.add(min, new Vector3f()).mul(0.5f);

        for(int i=0; i<vertices.length-3; i++){
            vertices[i] = (vertices[i] - center.x) * (2.0f / M);
            vertices[i+1] = (vertices[i+1] - center.y) * (2.0f / M);
            vertices[i+2] = (vertices[i+2] - center.z) * (2.0f / M);
        }

        bufferDataVBO(0);
    }

    void bufferDataEBO(){
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
            indicesBuffer.put(indices).flip();

            glBindVertexArray(VAO);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
            glBindVertexArray(VAO);
        }
    }

    void bufferDataVBO(int index){
        bufferDataVBO(index, false);
    }

    void bufferDataVBO(int index, boolean isInitializationBuffering){
        try (MemoryStack stack = MemoryStack.stackPush()){
            int size = index != 2 ? 3 : 2;      //uvCoords are 2 floats, others are 3
            float[][] arrayReferences = getArrayReferences();
            FloatBuffer buffer = BufferUtils.createFloatBuffer(arrayReferences[index].length);
            buffer.put(arrayReferences[index]);

            glBindVertexArray(VAO);
            glBindBuffer(GL_ARRAY_BUFFER, VBO[index]);
            glBufferData(GL_ARRAY_BUFFER, arrayReferences[index], GL_STATIC_DRAW);

            if (isInitializationBuffering) {
                glEnableVertexAttribArray(index);
                glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0);
            }

            glBindVertexArray(0);
        }
    }
}

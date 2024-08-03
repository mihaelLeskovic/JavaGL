package claude.apstractions.renderables;

import org.joml.Vector3f;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.assimp.Assimp.aiProcess_GenNormals;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class RenderableFactory {
    public static SimpleTriangleMesh makeSimpleTriangleMesh(String filePath) {
        return makeSimpleTriangleMesh(
                filePath,
                aiProcess_CalcTangentSpace |
                        aiProcess_Triangulate |
                        aiProcess_JoinIdenticalVertices |
                        aiProcess_SortByPType |
                        aiProcess_FlipUVs |
                        aiProcess_GenNormals
        );
    }

    public static SimpleTriangleMesh makeSimpleTriangleMesh(String filePath, int flags) {
        AIScene aiScene = aiImportFile(filePath, flags);

        if (aiScene == null) {
            throw new RuntimeException("Failed to load model: " + aiGetErrorString());
        }

        AIMesh aiMesh = AIMesh.create(aiScene.mMeshes().get(0));

        SimpleTriangleMesh triMesh = new SimpleTriangleMesh();
        int nVertices = aiMesh.mNumVertices();
        int nFaces = aiMesh.mNumFaces();

        //vertex positions (vbo[0])
        FloatBuffer vertices = MemoryUtil.memAllocFloat(nVertices * 3);
        AIVector3D.Buffer verticesBuffer = aiMesh.mVertices();

        Vector3f minVec = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3f maxVec = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        for (int i = 0; i < nVertices; i++) {
            AIVector3D vertex = verticesBuffer.get(i);
            Vector3f vertexVector = new Vector3f(vertex.x(), vertex.y(), vertex.z());

            maxVec.max(vertexVector);
            minVec.min(vertexVector);
        }
        Vector3f range = maxVec.sub(minVec, new Vector3f());
        float maxComponent = range.get(range.maxComponent());
        Vector3f center = (minVec.add(maxVec, new Vector3f())).div(2.0f);

        for (int i = 0; i < nVertices; i++) {
            AIVector3D vertex = verticesBuffer.get(i);
            Vector3f normalizedVertex = new Vector3f(vertex.x(), vertex.y(), vertex.z());

            normalizedVertex.sub(center);
            normalizedVertex.mul(2.0f / maxComponent);

            vertices.put(normalizedVertex.x())
                    .put(normalizedVertex.y())
                    .put(normalizedVertex.z());
        }
        vertices.flip();

        //normals (vbo[1])
        FloatBuffer normals = MemoryUtil.memAllocFloat(nVertices * 3);
        AIVector3D.Buffer normalsBuffer = aiMesh.mNormals();
        if (normalsBuffer != null) {
            for (int i = 0; i < nVertices; i++) {
                AIVector3D normal = normalsBuffer.get(i);
                normals.put(normal.x())
                        .put(normal.y())
                        .put(normal.z());
            }
            normals.flip();
        }

        //uvCoords (vbo[2])
        FloatBuffer uvCoords = MemoryUtil.memAllocFloat(nVertices * 2);
        AIVector3D.Buffer textureCoords = aiMesh.mTextureCoords(0);
        if (textureCoords != null) {
            for (int i = 0; i < nVertices; i++) {
                AIVector3D textureCoord = textureCoords.get(i);
                uvCoords.put(textureCoord.x())
                        .put(textureCoord.y());
            }
            uvCoords.flip();
        }

        //indices (ebo)
        IntBuffer indices = MemoryUtil.memAllocInt(aiMesh.mNumFaces() * 3);
        AIFace.Buffer faces = aiMesh.mFaces();
        for (int i = 0; i < aiMesh.mNumFaces(); i++) {
            AIFace face = faces.get(i);
            IntBuffer faceIndices = face.mIndices();
            // Reverse the order of indices for each triangle
            indices.put(faceIndices.get(2))
                    .put(faceIndices.get(1))
                    .put(faceIndices.get(0));
        }
        indices.flip();

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int[] vbo = new int[3];
        glGenBuffers(vbo);

        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3*Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        if (normalsBuffer != null) {
            glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
            glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);
        }

        if (textureCoords != null) {
            glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
            glBufferData(GL_ARRAY_BUFFER, uvCoords, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(2);
        }

        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        MemoryUtil.memFree(vertices);
        MemoryUtil.memFree(normals);
        MemoryUtil.memFree(uvCoords);
        MemoryUtil.memFree(indices);
        aiReleaseImport(aiScene);

        triMesh.numFaces = nFaces;
        triMesh.numVertices = nVertices;
        triMesh.VAO = vao;
        triMesh.VBO = vbo;
        triMesh.EBO = ebo;

        return triMesh;
    }
}

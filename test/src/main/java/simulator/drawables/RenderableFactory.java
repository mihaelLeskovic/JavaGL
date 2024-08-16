package simulator.drawables;

import simulator.shaders.UniformManager;
import simulator.shaders.Shader;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.Random;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public abstract class RenderableFactory {
    public static TerrainObject constructTerrainObject(Shader shader, UniformManager uniformManager, float span, float divisionSpan, float maxHeight) {
        return constructTerrainObject(shader, uniformManager, null, span, divisionSpan, maxHeight, -1, 2f);
    }

    //calculating the average of neighboring triangles' normals'
    //the neighboring vertices form a hexagon with the one exactly to the left being marked aVertex
    //the rest of the vertices are marked alphabetically, counter-clockwise, e.g. bVertex, cVertex
    //approximate sketch:
    // f - e
    // | \ | \
    // a - o - d
    //   \ | \ |
    //     b - c
    private static Vector3f calculateNormal(float[][] heightMap, float span, float divisionSpan, int i, int j) {

        Vector3f origin = new Vector3f(i, heightMap[i][j], j);
        Vector3f aVertex = new Vector3f(i-1, (i>0 ? heightMap[i-1][j] : 0), j);
        Vector3f bVertex = new Vector3f(i, (j>0 ? heightMap[i][j-1] : 0), j-1);
        Vector3f cVertex = new Vector3f(i+1, (i<heightMap.length-1 ? j>0 ? heightMap[i+1][j-1] : 0 : 0), j+1);
        Vector3f dVertex = new Vector3f(i+1, (i<heightMap.length-1 ? heightMap[i+1][j] : 0), j);
        Vector3f eVertex = new Vector3f(i, (j<heightMap.length-1 ? heightMap[i][j+1] : 0), j+1);
        Vector3f fVertex = new Vector3f(i-1, (i>0 ? j<heightMap.length-1 ? heightMap[i-1][j+1] : 0 : 0), j+1);

        Vector3f[] vecArr = new Vector3f[]{aVertex, bVertex, cVertex, dVertex, eVertex, fVertex};

        Vector3f sumVector = new Vector3f(0);
        for(int m=0; m<6; m++) {
            Vector3f vec1 = vecArr[m].sub(origin, new Vector3f());
            Vector3f vec2 = vecArr[m<6-1 ? m : 0].sub(origin, new Vector3f());
            sumVector.add(vec1.cross(vec2));
        }
        sumVector.div(6).normalize();
        return sumVector;
    }

    public static TerrainObject constructTerrainObject(Shader shader, UniformManager uniformManager,
                                                       Long seed, float span, float divisionSpan,
                                                       float maxHeight, float waterLevel, float maxWaterHeight) {

        TerrainObject terrainObject = new TerrainObject(shader, uniformManager);

        int n = (int) (span / divisionSpan);

        float[][] heights = new float[n+1][n+1];
        float[][] noise = new float[n+1][n+1];
        Random random = new Random();
        if (seed!=null) random.setSeed(seed);

        float center = span/2;
        float xCoord;
        float yCoord;

        float maxGeneratedHeight = 0f;

        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= n; j++) {
                xCoord = i * divisionSpan - center;
                yCoord = j * divisionSpan - center;
                float distance = (float) Math.sqrt(xCoord * xCoord + yCoord * yCoord);
                float normalizedDistance = distance / center;

                float gaussian = (float) Math.exp(-normalizedDistance * normalizedDistance * 4.0); // Adjust the width of the island

                float neighborEffect=0;
                if(i!=0 && j!=0) neighborEffect = noise[i][j-1] + noise[i-1][j];

                noise[i][j] = gaussian * random.nextFloat() * 0.2f + neighborEffect * 0.3f;

                float heightModifier = gaussian + noise[i][j];
                heights[i][j] = heightModifier * maxHeight;

                if(heights[i][j] > waterLevel && heights[i][j] < maxWaterHeight) {
                    heights[i][j] = waterLevel - 1;
                }

                maxGeneratedHeight = Math.max(maxGeneratedHeight, heights[i][j]);
            }
        }

        Vector3f[][] normals = new Vector3f[n+1][n+1];
        for(int i=0; i<=n; i++) {
            for(int j=0; j<=n; j++) {
                normals[i][j] = calculateNormal(heights, span, divisionSpan, i, j);
            }
        }

        terrainObject.heightMap = heights;
        terrainObject.divisionSpan = divisionSpan;
        terrainObject.span = span;
        terrainObject.maxHeight = maxGeneratedHeight;

        TerrainMesh terrainMesh = new TerrainMesh();
        terrainMesh.VAOs = new int[n];
        terrainMesh.VBOs = new int[n*2];

        glGenVertexArrays(terrainMesh.VAOs);
        glGenBuffers(terrainMesh.VBOs);

        for(int i = 0; i < n; i++) {
            FloatBuffer vertices = MemoryUtil.memAllocFloat((n + 1) * 6);
            FloatBuffer normalsBuffer = MemoryUtil.memAllocFloat((n + 1) * 6);

            for(int j = 0; j <= n; j++) {
                xCoord = i * divisionSpan - center;
                yCoord = j * divisionSpan - center;

                // First vertex of the pair
                vertices.put(xCoord)
                        .put(heights[i][j])
                        .put(yCoord);

                normalsBuffer.put(normals[i][j].x)
                        .put(normals[i][j].y)
                        .put(normals[i][j].z);

                // Second vertex of the pair
                vertices.put(xCoord + divisionSpan)
                        .put(heights[i+1][j])
                        .put(yCoord);

                normalsBuffer.put(normals[i+1][j].x)
                        .put(normals[i+1][j].y)
                        .put(normals[i+1][j].z);
            }

            vertices.flip();
            normalsBuffer.flip();

            glBindVertexArray(terrainMesh.VAOs[i]);

            glBindBuffer(GL_ARRAY_BUFFER, terrainMesh.VBOs[i]);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 3*Float.BYTES, 0);

            glBindBuffer(GL_ARRAY_BUFFER, terrainMesh.VBOs[i+n]);
            glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 3*Float.BYTES, 0);

            glEnableVertexAttribArray(0);

            MemoryUtil.memFree(vertices);
            MemoryUtil.memFree(normalsBuffer);
        }

        terrainObject.terrainMesh = terrainMesh;

        return terrainObject;
    }
}
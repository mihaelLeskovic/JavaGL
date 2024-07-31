package claude.apstractions;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Objects;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

public class UniformManager {
    // uniform locations per shader
    private HashMap<Integer, HashMap<String, Integer>> uniformLocations;

    public UniformManager() {
        uniformLocations = new HashMap<>();
    }

    public void setUniformMatrix4fv(int shader, String uniformName, Matrix4f matrix4f) {
        int location = fetchUniformLocation(shader, uniformName);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix4f.get(buffer);
        glUniformMatrix4fv(location, false, buffer);
    }

    public int fetchUniformLocation(int shader, String uniformName) {
        if (uniformLocations.get(shader) == null) {
            initializeShaderLevel(shader);
        }
        if(uniformLocations.get(shader).get(uniformName) == null) {
            initializeUniform(shader, uniformName);
        }
        return uniformLocations.get(shader).get(uniformName);
    }

    private void initializeUniform(int shader, String uniformName) {
        Objects.requireNonNull(uniformLocations.get(shader), "Uniform map for shader is null!");
        int uniformLocation = glGetUniformLocation(shader, uniformName);
        uniformLocations.get(shader).put(uniformName, uniformLocation);
    }

    private void initializeShaderLevel(int shader) {
        uniformLocations.put(shader, new HashMap<>());
    }
}

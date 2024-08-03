package claude.apstractions;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Objects;

import static org.lwjgl.opengl.GL20.*;

public class UniformManager {
    // uniform locations per shader
    private HashMap<Integer, HashMap<String, Integer>> uniformLocations;

    public UniformManager() {
        uniformLocations = new HashMap<>();
    }

    public void setUniformMatrix4f(int shader, String uniformName, Matrix4f matrix4f) {
        int location = fetchUniformLocation(shader, uniformName);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix4f.get(buffer);
        glUniformMatrix4fv(location, false, buffer);
    }

    public void setUniformVector3f(int shader, String uniformName, Vector3f vector3f) {
        int location = fetchUniformLocation(shader, uniformName);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
        vector3f.get(buffer);
        glUniform3fv(location, buffer);
    }

    public void setUniformVector4f(int shader, String uniformName, Vector4f vector4f) {
        int location = fetchUniformLocation(shader, uniformName);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
        vector4f.get(buffer);
        glUniform4fv(location, buffer);
    }

    public void setUniformFloat(int shader, String uniformName, float floatVar) {
        int location = fetchUniformLocation(shader, uniformName);
        glUniform1f(location, floatVar);
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

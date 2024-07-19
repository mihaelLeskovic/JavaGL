package javagl.architecture;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;

public class UniformManager {
    static HashMap<Integer, HashMap<String, Integer>> mapOfMapsOfUniforms = new HashMap<>();

    static int createUniformEntry(String name, int shaderID) {
        int uniform = glGetUniformLocation(shaderID, name);
        if (uniform < 0) throw new RuntimeException("Could not find uniform " + name + " in program " + shaderID);
        getForShaderID(shaderID).put(name, uniform);
        return getForShaderID(shaderID).get(name);
    }

    static HashMap<String, Integer> getForShaderID(int shaderID) {
        if(mapOfMapsOfUniforms.get(shaderID) != null) return mapOfMapsOfUniforms.get(shaderID);
        mapOfMapsOfUniforms.put(shaderID, new HashMap<>());
        return mapOfMapsOfUniforms.get(shaderID);
    }

    static int getUniformLocation(String name, int shaderID) {
        Integer location = getForShaderID(shaderID).get(name);
        if(location != null) return location;
        return createUniformEntry(name, shaderID);
    }

    public static void setUniform(String name, int shaderID, Matrix4f val) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int uniformLocation = getUniformLocation(name, shaderID);
            glUniformMatrix4fv(uniformLocation, false, val.get(BufferUtils.createFloatBuffer(16)));
        }
    }

    public static void setUniform(String name, int shaderID, Vector3f val) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int uniformLocation = getUniformLocation(name, shaderID);
            glUniform3fv(uniformLocation, val.get(BufferUtils.createFloatBuffer(3)));
        }
    }

    public static void setUniform(String name, int shaderID, Vector4f val) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int uniformLocation = getUniformLocation(name, shaderID);
            glUniform4fv(uniformLocation, val.get(BufferUtils.createFloatBuffer(4)));
        }
    }

    public static void setUniform(String name, int shaderID, float val) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int uniformLocation = getUniformLocation(name, shaderID);
            FloatBuffer buffer = BufferUtils.createFloatBuffer(1);
            buffer.put(val).flip();
            glUniform1fv(uniformLocation, buffer);
        }
    }
}

package claude.apstractions.shaders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class ShaderFactory {
    public static int compilePartShader(ShaderModule module){
        return compilePartShader(module.getType(), module.getCode());
    }

    public static int compilePartShader(int type, String code) {
        int shader = glCreateShader(type);
        glShaderSource(shader, code);
        glCompileShader(shader);

        return shader;
    }

    public static Shader makeWholeShader(Integer... shaderIDs) {
        int shader = glCreateProgram();

        for(Integer id : shaderIDs) {
            glAttachShader(shader, id);
        }

        glLinkProgram(shader);

        for(Integer id : shaderIDs){
            glDeleteShader(id);
        }

        return new Shader(shader);
    }

    public static Shader makeWholeShader(ShaderModule... modules) {
        int shader = glCreateProgram();
        int[] partShaders = new int[modules.length];
        for(int i=0; i<modules.length; i++) {
            partShaders[i] = compilePartShader(modules[i]);
            glAttachShader(shader, partShaders[i]);
        }

        glLinkProgram(shader);

        for(int i=0; i<modules.length; i++) {
            glDeleteShader(partShaders[i]);
        }

        return new Shader(shader);
    }

    public static ShaderModule makeShaderModule(int type, String path) {
        try {
            String shaderCode = Files.readString(Paths.get(path));
            return new ShaderModule(type, shaderCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

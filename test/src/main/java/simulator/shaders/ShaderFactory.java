package simulator.shaders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

public class ShaderFactory {
    static final private Map<Integer, String> intCodeToShaderType = Map.of(
            GL_VERTEX_SHADER, "VERTEX",
            GL_FRAGMENT_SHADER, "FRAGMENT",
            GL_GEOMETRY_SHADER, "GEOMETRY"
    );


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

    public static Shader constructShader(String pathToDir, String fileName, Integer... types) {
        StringBuilder pathBuilder = new StringBuilder(pathToDir);

        if(!pathToDir.endsWith("/") && !pathToDir.endsWith("/")) pathBuilder.append("/");

        pathBuilder.append(fileName).append(".");

        ShaderModule[] shaderModules = new ShaderModule[types.length];
        for(int i=0; i<types.length; i++) {
            StringBuilder modulePathBuilder = new StringBuilder(pathBuilder);
            if(intCodeToShaderType.get(types[i]) == null) throw new RuntimeException("Illegal shader type!");
            modulePathBuilder.append(intCodeToShaderType.get(types[i]).substring(0,4).toLowerCase());

            shaderModules[i] = makeShaderModule(
                    types[i],
                    modulePathBuilder.toString()
            );
        }

        return makeWholeShader(shaderModules);
    }
}

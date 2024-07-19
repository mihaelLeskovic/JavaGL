package javagl.architecture;

import javagl.Util;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

//code mostly taken from https://ahbejarano.gitbook.io/lwjglgamedev/chapter-03
public class ShaderProgram {
    public int shaderID;

    public ShaderProgram(List<ShaderModuleData> shaderModuleDataList) {
        shaderID = glCreateProgram();
        if(shaderID==0) throw new RuntimeException("Could not create shader.");
        List<Integer> shaderModules = new ArrayList<>();
        shaderModuleDataList.forEach(
                s -> shaderModules.add(createShader(Util.readFile(s.shaderFile), s.shaderType))
        );

        link(shaderModules);
    }

    void link(List<Integer> shaderModules) {
        glLinkProgram(shaderID);
        if(glGetProgrami(shaderID, GL_LINK_STATUS) == 0)
            throw new RuntimeException("Error linking shader code: " + glGetProgramInfoLog(shaderID, 1024));

        shaderModules.forEach(s -> glDetachShader(shaderID, s));
        shaderModules.forEach(GL30::glDeleteShader);
    }

    int createShader(String shaderCode, int shaderType) {
        int id = glCreateShader(shaderType);
        if(id==0) throw new RuntimeException("Error creating shader.");

        glShaderSource(id, shaderCode);
        glCompileShader(id);

        if(glGetShaderi(id, GL_COMPILE_STATUS) ==0)
            throw new RuntimeException("Error compiling shader coe: " + glGetShaderInfoLog(id, 1024));

        glAttachShader(shaderID, id);

        return id;
    }

    public void bind() {
        glUseProgram(shaderID);
    }

    public void unbind() {
        glUseProgram(0);
    }


}

package claude.apstractions.transforms;

import claude.apstractions.UniformManager;
import claude.apstractions.renderables.SeaMesh;
import claude.apstractions.shaders.Shader;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class SeaObject extends Transform implements Renderable{
    SeaMesh seaMesh;
    Shader shader;
    UniformManager uniformManager;

    public SeaObject(SeaMesh seaMesh, Shader shader, UniformManager uniformManager) {
        super();
        this.seaMesh = seaMesh;
        this.shader = shader;
        this.uniformManager = uniformManager;
    }


    @Override
    public void render(Camera camera, Light light) {
        glUseProgram(shader.getShader());

        uniformManager.setUniformMatrix4f(shader.getShader(), "projection", camera.getProjectionMatrix());
        uniformManager.setUniformMatrix4f(shader.getShader(), "model", this.getModelMatrix());
        uniformManager.setUniformMatrix4f(shader.getShader(), "view", camera.getViewMatrix());

        float time = (float) glfwGetTime();
        uniformManager.setUniformFloat(shader.getShader(), "time", time);

        uniformManager.setUniformVector3f(shader.getShader(), "viewPos", camera.getPosition());
        uniformManager.setUniformVector3f(shader.getShader(), "lightColor", light.getColor());
        uniformManager.setUniformVector3f(shader.getShader(), "lightPos", light.getPosition());
        uniformManager.setUniformFloat(shader.getShader(), "ambientIntensity", light.getAmbientIntensity());

        seaMesh.draw();
    }
}

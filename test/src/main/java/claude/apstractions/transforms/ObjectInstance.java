package claude.apstractions.transforms;

import claude.apstractions.UniformManager;
import claude.apstractions.renderables.Renderable;
import claude.apstractions.shaders.Shader;
import org.joml.Vector3f;

public class ObjectInstance extends Transform{
    Renderable renderable;
    Shader shader;
    UniformManager uniformManager;

    public ObjectInstance(Renderable renderable, Shader shader, UniformManager uniformManager) {
        super();
        this.renderable = renderable;
        this.shader = shader;
        this.uniformManager = uniformManager;
    }

    public void render(Camera camera, Light light) {
        uniformManager.setUniformMatrix4f(shader.getShader(), "projection", camera.getProjectionMatrix());
        uniformManager.setUniformMatrix4f(shader.getShader(), "model", this.getModelMatrix());
        uniformManager.setUniformMatrix4f(shader.getShader(), "view", camera.getViewMatrix());
        uniformManager.setUniformVector3f(shader.getShader(), "viewPos", camera.getPosition());
        uniformManager.setUniformVector3f(shader.getShader(), "lightColor", light.getColor());
        uniformManager.setUniformVector3f(shader.getShader(), "lightPos", light.getPosition());
        uniformManager.setUniformFloat(shader.getShader(), "ambientIntensity", light.getAmbientIntensity());
        uniformManager.setUniformVector3f(shader.getShader(), "objectColor", new Vector3f(1, 0, 1));

        renderable.draw();
    }
}

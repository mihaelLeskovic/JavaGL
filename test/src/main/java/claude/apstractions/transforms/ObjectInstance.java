package claude.apstractions.transforms;

import claude.apstractions.UniformManager;
import claude.apstractions.renderables.Drawable;
import claude.apstractions.shaders.Shader;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.glUseProgram;

public class ObjectInstance extends Transform implements Renderable{
    Drawable drawable;
    Shader shader;
    UniformManager uniformManager;

    public ObjectInstance(Drawable drawable, Shader shader, UniformManager uniformManager) {
        super();
        this.drawable = drawable;
        this.shader = shader;
        this.uniformManager = uniformManager;
    }

    @Override
    public void render(Camera camera, Light light) {
        glUseProgram(shader.getShader());

        uniformManager.setUniformMatrix4f(shader.getShader(), "projection", camera.getProjectionMatrix());
        uniformManager.setUniformMatrix4f(shader.getShader(), "model", this.getModelMatrix());
        uniformManager.setUniformMatrix4f(shader.getShader(), "view", camera.getViewMatrix());
        uniformManager.setUniformVector3f(shader.getShader(), "viewPos", camera.getPosition());
        uniformManager.setUniformVector3f(shader.getShader(), "lightColor", light.getColor());
        uniformManager.setUniformVector3f(shader.getShader(), "lightPos", light.getPosition());
        uniformManager.setUniformFloat(shader.getShader(), "ambientIntensity", light.getAmbientIntensity());
        uniformManager.setUniformVector3f(shader.getShader(), "objectColor", new Vector3f(1, 0, 1));

        drawable.draw();
    }
}

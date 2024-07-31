package claude.apstractions.transforms;

import claude.apstractions.Main;
import claude.apstractions.UniformManager;
import claude.apstractions.renderables.Renderable;
import claude.apstractions.shaders.Shader;
import org.joml.Matrix4f;

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

    public void render(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        uniformManager.setUniformMatrix4fv(shader.getShader(), "projection", projectionMatrix);
        uniformManager.setUniformMatrix4fv(shader.getShader(), "model", this.getModelMatrix());
        uniformManager.setUniformMatrix4fv(shader.getShader(), "view", viewMatrix);

        renderable.draw();
    }
}

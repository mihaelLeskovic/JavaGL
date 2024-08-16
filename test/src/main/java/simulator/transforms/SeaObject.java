package simulator.transforms;

import simulator.utility.Cleanable;
import simulator.physics.HitboxVisitor;
import simulator.physics.VisitableHitbox;
import simulator.shaders.UniformManager;
import simulator.drawables.SeaMesh;
import simulator.shaders.Shader;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class SeaObject extends Transform implements Renderable, Cleanable, VisitableHitbox {
    SeaMesh seaMesh;
    Shader shader;
    UniformManager uniformManager;
    float height = 0.4f;

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

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

        uniformManager.setUniformFloat(shader.getShader(), "height", height);

        seaMesh.draw();
    }

    @Override
    public void cleanup() {
        this.seaMesh.cleanup();
        this.shader.cleanup();
    }

    @Override
    public boolean isCleaned() {
        return false;
    }

    @Override
    public void accept(HitboxVisitor visitor) {
        visitor.visitSea(this);
    }
}

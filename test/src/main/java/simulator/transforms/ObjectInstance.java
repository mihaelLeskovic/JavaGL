package simulator.transforms;

import simulator.utility.Cleanable;
import simulator.shaders.UniformManager;
import simulator.drawables.Drawable;
import simulator.shaders.Shader;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.glUseProgram;

public class ObjectInstance extends Transform implements Renderable, Cleanable {
    Drawable drawable;
    Shader shader;
    UniformManager uniformManager;
    Vector3f color;

    public ObjectInstance(Drawable drawable, Shader shader, UniformManager uniformManager) {
        super();
        this.drawable = drawable;
        this.shader = shader;
        this.uniformManager = uniformManager;
        this.color = new Vector3f((float) Math.random(), (float) Math.random(), (float) Math.random());
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public Vector3f getColor() {
        return color;
    }

    public ObjectInstance setColor(float r, float g, float b) {
        return setColor(new Vector3f(r, g, b));
    }

    public ObjectInstance setColor(Vector3f color) {
        this.color = color;
        return this;
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
        uniformManager.setUniformVector3f(shader.getShader(), "objectColor", this.color);

        drawable.draw();
    }

    @Override
    public void cleanup() {
        this.shader.cleanup();
        this.drawable.cleanup();
    }

    @Override
    public boolean isCleaned() {
        return false;
    }
}

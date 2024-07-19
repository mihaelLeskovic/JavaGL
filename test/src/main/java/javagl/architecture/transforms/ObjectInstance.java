package javagl.architecture.transforms;

import javagl.architecture.UniformManager;
import javagl.architecture.renderables.Renderable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glGetIntegerv;

public class ObjectInstance extends Transform {
    int shaderID = 0;
    public List<Renderable> renderables;

    public ObjectInstance(int shaderID) {
        super();
        renderables = new ArrayList<>();
        this.shaderID = shaderID;
    }

    public void render(Camera camera, Light light) {
        Matrix4f perspMatrix = camera.getProjectionMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();

        UniformManager.setUniform("modelMatr", this.shaderID, this.getModelMatrix());
        UniformManager.setUniform("viewMatr", this.shaderID, this.getViewMatrix());
        UniformManager.setUniform("projMatr", this.shaderID, camera.getProjectionMatrix());
//        UniformManager.setUniform("cameraPos", this.shaderID, new Vector4f(camera.position,1));
//        UniformManager.setUniform("lightIntensity", this.shaderID, light.intensity);
//        UniformManager.setUniform("lightPos", this.shaderID, new Vector4f(light.position, 1));
//        UniformManager.setUniform("ambientIntensity", this.shaderID, light.ambientIntensity);
//        UniformManager.setUniform("objectColor", this.shaderID, new Vector3f(1, 0, 1));
//        UniformManager.setUniform("normalMatr", this.shaderID, this.getNormalMatrix());

        for(Renderable renderable : renderables) {
            renderable.draw();
        }
    }

    public boolean isEmpty() {
        return renderables.isEmpty();
    }
}

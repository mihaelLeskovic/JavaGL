package javagl.architecture;

import javagl.architecture.transforms.Camera;
import javagl.architecture.transforms.Light;
import javagl.architecture.transforms.ObjectInstance;

import java.util.ArrayList;
import java.util.List;

public class Renderer {
    public Camera camera;
    public Light light;
    public List<ObjectInstance> objects;
    public ResourceManager resourceManager;

    public Renderer(Camera camera, Light light, ResourceManager resourceManager) {
        this.camera = camera;
        this.light = light;
        this.resourceManager = resourceManager;
        objects = new ArrayList<>();
    }

    public void addObject(String path, int shaderID) {
        objects.add(resourceManager.constructObject(path, shaderID));
    }

    public void render() {
        for(ObjectInstance objectInstance : objects) {
            objectInstance.render(camera, light);
        }
    }
}

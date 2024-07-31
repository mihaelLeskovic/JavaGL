package javagl.architecture;

import javagl.architecture.renderables.Renderable;
import javagl.architecture.renderables.TriangleMesh;
import javagl.architecture.transforms.ObjectInstance;
import org.lwjgl.assimp.AIScene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.assimp.Assimp.*;

public class ResourceManager {
    //TODO
    //importanje materiala
    String resourcePath;
    HashMap<String, List<Renderable>> renderableLists;

    public ResourceManager(String resourcePath) {
        this.renderableLists = new HashMap<>();
        this.resourcePath = resourcePath;
    }

//    public static ObjectInstance loadModel(String modelPath) {
//        return loadModel(modelPath, aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices |
//                aiProcess_Triangulate | aiProcess_FixInfacingNormals | aiProcess_CalcTangentSpace | aiProcess_LimitBoneWeights |
//                aiProcess_PreTransformVertices);
//    }
//
//    public static ObjectInstance loadModel(String modelPath, int flags) {
//        File file = new File(modelPath);
//        if(!file.exists()) throw new RuntimeException("Model path: \"" + modelPath + "\" does not exist.");
//
//        String modelDir = file.getParent();
//        AIScene aiScene = aiImportFile(modelPath, flags);
//        if(aiScene == null) throw new RuntimeException("Error loading model from path: " + modelPath);
//
//    }

    List<Renderable> constructRenderableList(String path) {
        AIScene aiScene = aiImportFile(path,
                aiProcess_CalcTangentSpace |
                        aiProcess_Triangulate |
                        aiProcess_JoinIdenticalVertices |
                        aiProcess_SortByPType |
                        aiProcess_FlipUVs |
                        aiProcess_GenNormals
                );

        List<Renderable> list = new ArrayList<>();
        if(aiScene == null) throw new RuntimeException("Error loading model from path: " + path);

        for(int i=0; i<aiScene.mNumMeshes(); i++){
            list.add(new TriangleMesh(aiScene, i));
        }

        for(Renderable r : list) {
            r.normalize();
        }

//        aiScene.free();
        return list;
    }

    List<Renderable> getRenderableList(String path) {
        if(renderableLists.get(path)==null)
            renderableLists.put(path, constructRenderableList(path));
        return renderableLists.get(path);
    }

    ObjectInstance constructObject(String path, int shaderID) {
        ObjectInstance objectInstance = new ObjectInstance(shaderID);
        objectInstance.renderables = getRenderableList(path);

        return objectInstance;
    }
}

package claude.apstractions;

import claude.apstractions.renderables.RenderableFactory;
import claude.apstractions.shaders.Shader;
import claude.apstractions.shaders.ShaderFactory;
import claude.apstractions.shaders.ShaderModule;
import claude.apstractions.transforms.Camera;
import claude.apstractions.transforms.Light;
import claude.apstractions.transforms.ObjectInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;
    Instant lastTime;
    Shader shader;
    List<ObjectInstance> objectInstances;
    Camera camera;
    UniformManager uniformManager;
    Light light;
    boolean renderDocDebugTime = false;
    String[] args;
    HashMap<String, Consumer<Main>> argParserMap;


    public Main(String[] args) {
        this.args = args;
    }

    public static void main(String[] args) {
        new Main(args).run();
    }

    public void run() {
        System.out.println("LWJGL " + Version.getVersion());

        init();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void parseArguments() {
        for(String arg : args) {
            Consumer<Main> func = argParserMap.get(arg);
            if(func == null) throw new IllegalArgumentException("Cannot parse argument: ´" + args + "´");
            func.accept(this);
        }

        if(renderDocDebugTime) System.out.println("Using renderDoc settings.");
    }

    private void init() {
        argParserMap = new HashMap<>();
        argParserMap.put("-renderDoc", m -> m.renderDocDebugTime=true);
        parseArguments();

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(800, 600, "My main", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);

            if (key == GLFW_KEY_C && action == GLFW_PRESS) {
                if (glIsEnabled(GL_CULL_FACE)) {
                    glDisable(GL_CULL_FACE);
                } else {
                    glEnable(GL_CULL_FACE);
                }
            }

            if (key == GLFW_KEY_W && action == GLFW_PRESS) {
                camera.translateLocal(new Vector3f(0, 0, -1));
            }
            if (key == GLFW_KEY_S && action == GLFW_PRESS) {
                camera.translateLocal(new Vector3f(0, 0, 1));
            }
            if (key == GLFW_KEY_A && action == GLFW_PRESS) {
                camera.translateLocal(new Vector3f(-1, 0, 0));
            }
            if (key == GLFW_KEY_D && action == GLFW_PRESS) {
                camera.translateLocal(new Vector3f(1, 0, 0));
            }
            if (key == GLFW_KEY_Q && action == GLFW_PRESS) {
                camera.translateLocal(new Vector3f(0, -1, 0));
            }
            if (key == GLFW_KEY_E && action == GLFW_PRESS) {
                camera.translateLocal(new Vector3f(0, 1, 0));
            }
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        objectInstances = new ArrayList<>();

        uniformManager = new UniformManager();

        ShaderModule vertexModule = ShaderFactory.makeShaderModule(
                GL_VERTEX_SHADER,
                renderDocDebugTime ?
                        "C:\\Users\\dews\\Documents\\GitHub\\JavaGL\\test\\src\\main\\resources\\shaders\\shader.vert"
                        : "src/main/resources/shaders/shader.vert"
        );
        ShaderModule fragmentModule = ShaderFactory.makeShaderModule(
                GL_FRAGMENT_SHADER,
                renderDocDebugTime ?
                        "C:\\Users\\dews\\Documents\\GitHub\\JavaGL\\test\\src\\main\\resources\\shaders\\shader.frag"
                        : "src/main/resources/shaders/shader.frag"
        );
        ShaderModule geometryModule = ShaderFactory.makeShaderModule(
                GL_GEOMETRY_SHADER,
                renderDocDebugTime ?
                        "C:\\Users\\dews\\Documents\\GitHub\\JavaGL\\test\\src\\main\\resources\\shaders\\shader.geom"
                        : "src/main/resources/shaders/shader.geom"
        );

        shader = ShaderFactory.makeWholeShader(vertexModule, geometryModule, fragmentModule);

        ObjectInstance planeInstance = new ObjectInstance(
                RenderableFactory.makeSimpleTriangleMesh(renderDocDebugTime ?
                                "C:\\Users\\dews\\Documents\\GitHub\\JavaGL\\test\\src\\main\\resources\\models\\jet.obj"
                                : "src\\main\\resources\\models\\jet.obj"
                ),
                shader,
                uniformManager
        );
        planeInstance.setAdjustmentMatrix(new Matrix4f(
                0, 0, -1, 0,
                0, 1, 0, 0,
                1, 0, 0, 0,
                0, 0, 0, 1
        ));
        objectInstances.add(planeInstance);
//        planeInstance.setScale(0.05f, 0.05f, 0.05f);

        ObjectInstance centerCube = new ObjectInstance(
                RenderableFactory.makeSimpleTriangleMesh(renderDocDebugTime ?
                        "C:\\Users\\dews\\Documents\\GitHub\\JavaGL\\test\\src\\main\\resources\\models\\kocka.obj"
                        : "src\\main\\resources\\models\\kocka.obj"
                ),
                shader,
                uniformManager
        );
        centerCube.setScale(0.01f, 0.01f, 0.01f);

        ObjectInstance centerCube2 = new ObjectInstance(
                RenderableFactory.makeSimpleTriangleMesh(renderDocDebugTime ?
                        "C:\\Users\\dews\\Documents\\GitHub\\JavaGL\\test\\src\\main\\resources\\models\\kocka.obj"
                        : "src\\main\\resources\\models\\kocka.obj"
                ),
                shader,
                uniformManager
        );
//        objectInstances.add(centerCube);
//        objectInstances.add(centerCube2);

        lastTime = null;

        camera = new Camera();
        camera.setPosition(1.0f, 1.5f, 3.0f)
                .rotate(new Vector3f(-1, 0, 0), 0.4f);
//                .setScale(0.5f, 0.5f, 0.5f);

        light = new Light();
        light.setPosition(new Vector3f(300, 50f, -1.5f));
        light.setLookDirection(new Vector3f(0, 2, -1), new Vector3f(0, 1, 0));
    }

    private void loop() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glEnable(GL_DEPTH_TEST);

        lastTime = Instant.now();

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shader.getShader());

            float deltaT = Duration.between(lastTime, Instant.now()).toMillis() * 0.001f;
            lastTime = Instant.now();

//            model.rotate((float) 0.5f * deltaT, 0.0f, 1f, 0.0f);
//            model.get(modelBuffer);

            objectInstances.get(0).rotate(new Vector3f(0, 1, 0), 0.5f * deltaT);
            objectInstances.get(0).translateLocal(new Vector3f(0, 0, 1).mul(0.5f*deltaT));

            for(ObjectInstance objectInstance : objectInstances) {
                objectInstance.render(camera, light);
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
}
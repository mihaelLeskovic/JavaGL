package claude.apstractions;

import claude.apstractions.renderables.RenderableFactory;
import claude.apstractions.shaders.Shader;
import claude.apstractions.shaders.ShaderFactory;
import claude.apstractions.shaders.ShaderModule;
import claude.apstractions.transforms.Camera;
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

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;
    Instant lastTime;
    Shader shader;
    ObjectInstance objectInstance;
    Camera camera;
    UniformManager uniformManager;

    public void run() {
        System.out.println("LWJGL " + Version.getVersion());

        init();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
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

        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        uniformManager = new UniformManager();

        ShaderModule vertexModule = ShaderFactory.makeShaderModule(
                GL_VERTEX_SHADER,
                "src/main/resources/shaders/gptExample/vertex.glsl"
        );
        ShaderModule fragmentModule = ShaderFactory.makeShaderModule(
                GL_FRAGMENT_SHADER,
                "src/main/resources/iterative/shaders/shader.frag"
        );

        shader = ShaderFactory.makeWholeShader(vertexModule, fragmentModule);

        objectInstance = new ObjectInstance(
                RenderableFactory
                        .makeSimpleTriangleMesh("src\\main\\resources\\models\\jet.obj"),
                shader,
                uniformManager
        );

        lastTime = null;

        camera = new Camera();
        camera.setPosition(1.0f, 1.5f, 3.0f)
                .rotate(new Vector3f(-1, 0, 0), 0.4f);
    }

    private void loop() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glEnable(GL_DEPTH_TEST);

        lastTime = Instant.now();
        objectInstance.setAdjustmentMatrix(new Matrix4f(
                0, 0, -1, 0,
                0, 1, 0, 0,
                1, 0, 0, 0,
                0, 0, 0, 1
        ));

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shader.getShader());

            float deltaT = Duration.between(lastTime, Instant.now()).toMillis() * 0.001f;
            lastTime = Instant.now();

//            model.rotate((float) 0.5f * deltaT, 0.0f, 1f, 0.0f);
//            model.get(modelBuffer);
            objectInstance.rotate(new Vector3f(0, 1, 0), 0.5f * deltaT);
            objectInstance.translateLocal(new Vector3f(0, 0, 1).mul(0.5f*deltaT));

            objectInstance.render(camera.getProjectionMatrix(), camera.getViewMatrix());

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
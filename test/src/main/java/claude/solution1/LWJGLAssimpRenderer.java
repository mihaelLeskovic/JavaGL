package claude.solution1;

import org.lwjgl.Version;
import org.lwjgl.assimp.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class LWJGLAssimpRenderer {
    private long window;
    private int vao;
    private int vbo;
    private int vertexCount;
    private int shaderProgram;

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

        window = glfwCreateWindow(800, 600, "LWJGL Assimp Renderer", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
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

        loadModel("C:\\Users\\dews\\Documents\\GitHub\\JavaGL\\test\\src\\main\\resources\\models\\kocka.obj");
        createShaders();
    }

    private void loadModel(String filePath) {
        AIScene scene = aiImportFile(filePath, aiProcess_Triangulate | aiProcess_FlipUVs);

        if (scene == null) {
            throw new RuntimeException("Failed to load model: " + aiGetErrorString());
        }

        AIMesh mesh = AIMesh.create(scene.mMeshes().get(0));
        vertexCount = mesh.mNumVertices();

        FloatBuffer vertices = MemoryUtil.memAllocFloat(vertexCount * 3);
        AIVector3D.Buffer verticesBuffer = mesh.mVertices();
        for (int i = 0; i < vertexCount; i++) {
            AIVector3D vertex = verticesBuffer.get(i);
            vertices.put(vertex.x()).put(vertex.y()).put(vertex.z());
        }
        vertices.flip();

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        MemoryUtil.memFree(vertices);
        aiReleaseImport(scene);
    }

    private void createShaders() {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader,
                "#version 330 core\n" +
                        "layout (location = 0) in vec3 aPos;\n" +
                        "void main()\n" +
                        "{\n" +
                        "    gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);\n" +
                        "}"
        );
        glCompileShader(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader,
                "#version 330 core\n" +
                        "out vec4 FragColor;\n" +
                        "void main()\n" +
                        "{\n" +
                        "    FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);\n" +
                        "}"
        );
        glCompileShader(fragmentShader);

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private void loop() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shaderProgram);
            glBindVertexArray(vao);
            glDrawArrays(GL_TRIANGLES, 0, vertexCount);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new LWJGLAssimpRenderer().run();
    }
}
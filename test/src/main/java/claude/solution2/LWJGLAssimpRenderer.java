package claude.solution2;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.assimp.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.time.Duration;
import java.time.Instant;

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
    private int ebo;
    private int numFaces;
    private int vertexCount;
    private int shaderProgram;
    private int uniModel;
    private int uniView;
    private int uniProjection;
    private Matrix4f model;
    private Matrix4f view;
    private Matrix4f projection;
    Instant lastTime;

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

        loadModel("src\\main\\resources\\models\\glava\\glava.obj");
        createShaders();
        setupMatrices();
        lastTime = null;
    }

    private void loadModel(String filePath) {
        AIScene scene = aiImportFile(filePath,
                aiProcess_CalcTangentSpace |
                aiProcess_Triangulate |
                aiProcess_JoinIdenticalVertices |
                aiProcess_SortByPType |
                aiProcess_FlipUVs |
                aiProcess_GenNormals);

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

        IntBuffer indices = MemoryUtil.memAllocInt(mesh.mNumFaces() * 3);
        AIFace.Buffer faces = mesh.mFaces();
        for(int i = 0; i < mesh.mNumFaces(); i++) {
            AIFace face = faces.get(i);
            indices.put(face.mIndices());
        }
        indices.flip();

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        numFaces = mesh.mNumFaces();

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        MemoryUtil.memFree(vertices);
        MemoryUtil.memFree(indices);
        aiReleaseImport(scene);
    }


    private void createShaders() {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader,
                "#version 330 core\n" +
                        "layout (location = 0) in vec3 aPos;\n" +
                        "uniform mat4 model;\n" +
                        "uniform mat4 view;\n" +
                        "uniform mat4 projection;\n" +
                        "void main()\n" +
                        "{\n" +
                        "    gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
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

        uniModel = glGetUniformLocation(shaderProgram, "model");
        uniView = glGetUniformLocation(shaderProgram, "view");
        uniProjection = glGetUniformLocation(shaderProgram, "projection");
    }

    private void setupMatrices() {
        model = new Matrix4f().identity();

        view = new Matrix4f().lookAt(1.0f, 1.5f, 3.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f);

        projection = new Matrix4f().perspective((float) Math.toRadians(45.0f),
                800.0f / 600.0f, 0.01f, 100.0f);
    }

    private void loop() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glEnable(GL_DEPTH_TEST);

        FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
        FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
        FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);

        lastTime = Instant.now();

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shaderProgram);

            float deltaT = Duration.between(lastTime, Instant.now()).toMillis() * 0.001f;
            lastTime = Instant.now();

            model.rotate((float) 0.5f * deltaT, 0.0f, 1f, 0.0f);
            model.get(modelBuffer);
            view.get(viewBuffer);
            projection.get(projectionBuffer);

            glUniformMatrix4fv(uniModel, false, modelBuffer);
            glUniformMatrix4fv(uniView, false, viewBuffer);
            glUniformMatrix4fv(uniProjection, false, projectionBuffer);

            glBindVertexArray(vao);
            glDrawElements(GL_TRIANGLES, numFaces * 3, GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new LWJGLAssimpRenderer().run();
    }
}
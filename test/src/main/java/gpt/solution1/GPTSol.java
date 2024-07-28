package gpt.solution1;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.assimp.*;

import java.io.*;
import java.nio.*;
import java.nio.file.*;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.assimp.Assimp.aiProcess_GenNormals;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import org.joml.Matrix4f;

public class GPTSol {

    private long window;
    private int vertexArrayId;
    private int vertexBufferId;
    private int shaderProgram;

    private int modelMatrixLocation;
    private int viewMatrixLocation;
    private int projectionMatrixLocation;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loadModel("C:\\Users\\mih\\Documents\\GitHub\\javaGL\\test\\src\\main\\resources\\models\\glava\\glava.obj");  // Provide the correct path to your model file
        initShaders();
        setupUniforms();
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
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
//        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // For macOS

        window = glfwCreateWindow(800, 600, "Hello LWJGL", NULL, NULL);
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
    }

    private void loadModel(String modelPath) {
        AIScene scene = Assimp.aiImportFile(modelPath,
                aiProcess_CalcTangentSpace |
                        aiProcess_Triangulate |
                        aiProcess_JoinIdenticalVertices |
                        aiProcess_SortByPType |
                        aiProcess_FlipUVs |
                        aiProcess_GenNormals);
        if (scene == null) {
            throw new RuntimeException("Error loading model");
        }

        AIMesh mesh = AIMesh.create(scene.mMeshes().get(0));

        FloatBuffer vertices = MemoryUtil.memAllocFloat(mesh.mNumVertices() * 3);
        AIVector3D.Buffer aiVertices = mesh.mVertices();
        System.out.println("Number of vertices: " + aiVertices.remaining());
        while (aiVertices.remaining() > 0) {
            AIVector3D aiVertex = aiVertices.get();
            vertices.put(aiVertex.x());
            vertices.put(aiVertex.y());
            vertices.put(aiVertex.z());
        }
        vertices.flip();

        vertexArrayId = glGenVertexArrays();
        glBindVertexArray(vertexArrayId);

        vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        MemoryUtil.memFree(vertices);
        Assimp.aiReleaseImport(scene);
    }

    private void initShaders() {
        int vertexShader = createShader("C:\\Users\\mih\\Documents\\GitHub\\javaGL\\test\\src\\main\\resources\\shaders\\gptExample\\vertex.glsl", GL_VERTEX_SHADER);
        int fragmentShader = createShader("C:\\Users\\mih\\Documents\\GitHub\\javaGL\\test\\src\\main\\resources\\shaders\\gptExample\\fragment.glsl", GL_FRAGMENT_SHADER);
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Failed to link shader program");
        }
        glUseProgram(shaderProgram);
    }

    private int createShader(String filePath, int shaderType) {
        String shaderSource = readFile(filePath);
        int shaderId = glCreateShader(shaderType);
        glShaderSource(shaderId, shaderSource);
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error compiling shader: " + glGetShaderInfoLog(shaderId));
        }
        return shaderId;
    }

    private String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read shader file", e);
        }
    }

    private void setupUniforms() {
        modelMatrixLocation = glGetUniformLocation(shaderProgram, "model");
        viewMatrixLocation = glGetUniformLocation(shaderProgram, "view");
        projectionMatrixLocation = glGetUniformLocation(shaderProgram, "projection");

        Matrix4f model = new Matrix4f().identity();
        Matrix4f view = new Matrix4f().lookAt(
                0.0f, 0.0f, 3.0f,  // Camera position
                0.0f, 0.0f, 0.0f,  // Look at point
                0.0f, 1.0f, 0.0f   // Up vector
        );
        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(45.0f),
                800.0f / 600.0f,
                0.1f,
                100.0f
        );

        try (MemoryStack stack = stackPush()) {
            FloatBuffer modelBuffer = stack.mallocFloat(16);
            model.get(modelBuffer);
            glUniformMatrix4fv(modelMatrixLocation, false, modelBuffer);

            FloatBuffer viewBuffer = stack.mallocFloat(16);
            view.get(viewBuffer);
            glUniformMatrix4fv(viewMatrixLocation, false, viewBuffer);

            FloatBuffer projectionBuffer = stack.mallocFloat(16);
            projection.get(projectionBuffer);
            glUniformMatrix4fv(projectionMatrixLocation, false, projectionBuffer);
        }
    }

    private void loop() {
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shaderProgram);
            glBindVertexArray(vertexArrayId);

            glDrawArrays(GL_LINES, 0, 6371); // Adjust the count according to your model

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new GPTSol().run();
    }
}

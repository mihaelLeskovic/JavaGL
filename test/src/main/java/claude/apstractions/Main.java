package claude.apstractions;

import claude.apstractions.input.InputManager;
import claude.apstractions.input.TestCameraInputManager;
import claude.apstractions.physics.PhysicsWorker;
import claude.apstractions.renderables.DrawableFactory;
import claude.apstractions.renderables.RenderableFactory;
import claude.apstractions.renderables.TerrainObject;
import claude.apstractions.shaders.Shader;
import claude.apstractions.shaders.ShaderFactory;
import claude.apstractions.transforms.Camera;
import claude.apstractions.transforms.Light;
import claude.apstractions.transforms.ObjectInstance;
import claude.apstractions.transforms.SeaObject;
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
    int width = 800;
    int height = 600;
    final double xSens = 0.1;
    final double ySens = 0.1;

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
    List<InputManager> inputManagers;
    boolean lockMouse = true;
    SeaObject seaObject;
    TerrainObject terrainObject;
    ObjectInstance testCube;

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

        objectInstances = new ArrayList<>();
        uniformManager = new UniformManager();

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "My main", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

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

        camera = new Camera();
        inputManagers = new ArrayList<>();

        shader = ShaderFactory.constructShader(
                renderDocDebugTime ?
                        "C:\\Users\\dews\\Documents\\GitHub\\JavaGL\\test\\src\\main\\resources\\shaders"
                        : "src/main/resources/shaders",
                "shader",
                GL_VERTEX_SHADER, GL_FRAGMENT_SHADER, GL_GEOMETRY_SHADER
        );

        ObjectInstance centerCube = new ObjectInstance(
                DrawableFactory.makeSimpleTriangleMesh(renderDocDebugTime ?
                        "C:\\Users\\dews\\Documents\\GitHub\\JavaGL\\test\\src\\main\\resources\\models\\kocka.obj"
                        : "src\\main\\resources\\models\\kocka.obj"
                ),
                shader,
                uniformManager
        );
        centerCube.setScale(0.01f, 0.01f, 0.01f);
        centerCube.setColor(0, 1, 0);

        testCube = centerCube;

        Shader seaShader = ShaderFactory.constructShader(
                "src/main/resources/shaders/sea_shaders/",
                "sea",
                GL_VERTEX_SHADER, GL_FRAGMENT_SHADER
        );

        seaObject = new SeaObject(
                DrawableFactory.makeSeaMesh(1000, 4),
                seaShader,
                uniformManager
        );

        Shader terrainShader = ShaderFactory.constructShader(
                "src/main/resources/shaders/terrain_shaders/",
                "terrain",
                GL_VERTEX_SHADER, GL_FRAGMENT_SHADER
        );

        terrainObject = RenderableFactory.constructTerrainObject(
                terrainShader,
                uniformManager,
                30,
                1,
                5
        );

//        terrainObject.translateGlobal(new Vector3f(0, -2, 0));

        ObjectInstance planeInstance = new ObjectInstance(
                DrawableFactory.makeSimpleTriangleMesh(renderDocDebugTime ?
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
        planeInstance
//                .setColor(new Vector3f(1, 0, 1))
                .translateGlobal(new Vector3f(0, 1, 0));

        objectInstances.add(planeInstance);
//        planeInstance.setScale(0.05f, 0.05f, 0.05f);

        ObjectInstance centerCube2 = new ObjectInstance(
                DrawableFactory.makeSimpleTriangleMesh(renderDocDebugTime ?
                        "C:\\Users\\dews\\Documents\\GitHub\\JavaGL\\test\\src\\main\\resources\\models\\kocka.obj"
                        : "src\\main\\resources\\models\\kocka.obj"
                ),
                shader,
                uniformManager
        );
//        objectInstances.add(centerCube);
//        objectInstances.add(centerCube2);

        lastTime = null;

        camera.setNstaySame(0.01f);
        camera.setPosition(1.0f, 1.5f, 3.0f)
                .rotate(new Vector3f(-1, 0, 0), 0.4f);
//                .setScale(0.5f, 0.5f, 0.5f);

        light = new Light();
        light.setPosition(new Vector3f(300, 50f, -1.5f));
        light.setLookDirection(new Vector3f(0, 2, -1), new Vector3f(0, 1, 0));

        inputManagers.add(
                new TestCameraInputManager(window, width, height, camera, lockMouse)
                        .setySens(ySens)
                        .setxSens(xSens)
                        .setMoveSpeed(4f)
                        .addTestThings(testCube, terrainObject)
        );

        seaObject.setScale(0.5f, 0.5f, 0.5f);
    }

    private void loop() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glEnable(GL_DEPTH_TEST);

        lastTime = Instant.now();

//        testCube.setScale(1,1,1);

        // HEIGHTMAP TESTING
        /*
        int i=10; int j=10;
        Vector3f positionOfCube = terrainObject.getOrigin();
        float cubeMoveTimer = 0;
         /**/

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shader.getShader());

            float deltaT = Duration.between(lastTime, Instant.now()).toMillis() * 0.001f;
            lastTime = Instant.now();

//            model.rotate((float) 0.5f * deltaT, 0.0f, 1f, 0.0f);
//            model.get(modelBuffer);

            objectInstances.get(0).rotate(new Vector3f(0, 1, 0), 0.5f * deltaT);
            objectInstances.get(0).translateLocal(new Vector3f(0, 0, 1).mul(0.5f*deltaT))
                    .setPosition(terrainObject.getOrigin().add(0, 0, 1));
//            .add(terrainObject.getSpan(), 0, terrainObject.getSpan())
//            objectInstances.get(0).translateGlobal(new Vector3f(0, 3, 0));

            glfwPollEvents();
            for(InputManager inputManager : inputManagers) {
                inputManager.procesInputDeltaT(window, deltaT);
            }

            // HEIGHTMAP TESTING
            /*
            cubeMoveTimer += deltaT;
            if(cubeMoveTimer > 1) {
                cubeMoveTimer = 0;
                float x = (i) * terrainObject.getDivisionSpan();
                float z = (j) * terrainObject.getDivisionSpan();
                float addX = (float) (Math.random() * terrainObject.getDivisionSpan());
                float addZ = (float) (Math.random() * terrainObject.getDivisionSpan());
                positionOfCube = terrainObject.getOrigin();
                positionOfCube.add(
                        x + addX,
                        0,
                        z + addZ
                );
                positionOfCube.y = terrainObject.getHeightAt(positionOfCube);
                positionOfCube.add(new Vector3f(
                        0,
                        0,
                        0
                ));
                j++;
                System.out.println("i:"+i + " j:"+j);
                if(j>=30) {
                    j=0;
                    i++;
                }
            }

            camera.setPosition(positionOfCube.add(camera.getFront().mul(-2, new Vector3f()), new Vector3f()));
            testCube.setPosition(positionOfCube);
            /**/

            testCube.render(camera,light);

            for(ObjectInstance objectInstance : objectInstances) {
                objectInstance.render(camera, light);
            }

            seaObject.render(camera, light);
//            seaObject.setHeight(0.);

            terrainObject.render(camera, light);

            glfwSwapBuffers(window);
        }
    }
}
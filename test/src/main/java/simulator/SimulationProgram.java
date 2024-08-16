package simulator;

import simulator.input.InputManager;
import simulator.input.StateControlsInputManager;
import simulator.input.TestPhysicalCamera;
import simulator.physics.*;
import simulator.drawables.DrawableFactory;
import simulator.drawables.RenderableFactory;
import simulator.drawables.TerrainObject;
import simulator.shaders.Shader;
import simulator.shaders.ShaderFactory;
import simulator.shaders.UniformManager;
import simulator.swing.WindowSwitchListener;
import simulator.transforms.Camera;
import simulator.transforms.Light;
import simulator.transforms.ObjectInstance;
import simulator.transforms.SeaObject;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import simulator.utility.Cleanable;

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

public class SimulationProgram implements Runnable{
    int width = 800;
    int height = 600;
    final double xSens = 0.1;
    final double ySens = 0.1;

    WindowSwitchListener main;
    private long window;
    Instant lastTime;
    Shader shader;
    List<ObjectInstance> objectInstances;
    Camera camera;
    UniformManager uniformManager;
    Light light;
    boolean renderDocDebugTime = false;
    String[] args;
    HashMap<String, Consumer<SimulationProgram>> argParserMap;
    List<InputManager> inputManagers;
    boolean lockMouse = true;
    SeaObject seaObject;
    List<TerrainObject> terrainObjects;
    ObjectInstance testCube;
    PhysicalObject testPhysicalObject;
    List<Cleanable> cleanables;
    PhysicalObject cameraPhysicalObject;
    List<HitboxVisitor> hitboxVisitors;

    public SimulationProgram(String[] args, WindowSwitchListener main) {
        this.args = args;
        this.main = main;
    }

    @Override
    public void run() {
        System.out.println("LWJGL " + Version.getVersion());

        init();
        loop();
        cleanup();
    }

    private void cleanup() {
        for(Cleanable cleanable : cleanables) {
            cleanable.cleanup();
        }

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
//        glfwTerminate();
//        glfwSetErrorCallback(null).free();

        main.switchToSwing();
    }

    private void parseArguments() {
        for(String arg : args) {
            Consumer<SimulationProgram> func = argParserMap.get(arg);
            if(func == null) continue;
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
        cleanables = new ArrayList<>();
        terrainObjects = new ArrayList<>();
        hitboxVisitors = new ArrayList<>();

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
//        testCube.setScale(0.5f);
        testPhysicalObject = new PhysicalObject(10, testCube);

        Shader seaShader = ShaderFactory.constructShader(
                "src/main/resources/shaders/sea_shaders/",
                "sea",
                GL_VERTEX_SHADER, GL_FRAGMENT_SHADER
        );

        seaObject = new SeaObject(
                DrawableFactory.makeSeaMesh(2000, 10),
                seaShader,
                uniformManager
        );
        seaObject.setHeight(3);
        cleanables.add(seaObject);

        Shader terrainShader = ShaderFactory.constructShader(
                "src/main/resources/shaders/terrain_shaders/",
                "terrain",
                GL_VERTEX_SHADER, GL_FRAGMENT_SHADER
        );

        TerrainObject terrainObjectMain = RenderableFactory.constructTerrainObject(
                terrainShader,
                uniformManager,
                200,
                1,
                20
        );
        terrainObjects.add(terrainObjectMain);

        TerrainObject terrainObject2 = RenderableFactory.constructTerrainObject(
                terrainShader,
                uniformManager,
                50,
                1,
                40
        );
        terrainObject2.translateGlobal(new Vector3f(-20, 0, -20));
        terrainObjects.add(terrainObject2);

        cleanables.add(terrainObjectMain);

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
        cleanables.add(planeInstance);

        lastTime = null;

        camera.setNstaySame(0.01f);
        camera.setPosition(5.0f, 7f, 7.0f)
                .rotate(new Vector3f(-1, 0, 0), 0.4f);
        testCube.setPosition(new Vector3f(camera.getPosition())
                .add(new Vector3f(camera.getFront()).mul(15)).
                add(new Vector3f(camera.getUp()).mul(4))
        );

        light = new Light();
        light.setPosition(new Vector3f(300, 50f, -1.5f));
        light.setLookDirection(new Vector3f(0, 2, -1), new Vector3f(0, 1, 0));

        cameraPhysicalObject = new PhysicalObject(1, camera);
        inputManagers.add(
                new TestPhysicalCamera(window, width, height, cameraPhysicalObject, lockMouse)
                        .setySens(ySens)
                        .setxSens(xSens)
                        .setMoveSpeed(50f)
        );
        inputManagers.add(new StateControlsInputManager());

        seaObject.setScale(0.5f, 0.5f, 0.5f);


        double greatestHeight = terrainObjects.stream().mapToDouble(TerrainObject::getMaxHeight).max().getAsDouble();
        terrainObjects.stream().forEach(terrainObject -> terrainObject.setMaxHeight((float)greatestHeight));

        camera.translateGlobal(0, 30, 0);
        WheelHitbox hitbox = new WheelHitbox(
                DrawableFactory.makeSimpleTriangleMesh("C:\\Users\\dews\\Documents\\GitHub\\JavaGL\\test\\src\\main\\resources\\models\\kocka.obj"),
                shader,
                uniformManager,
                camera
//                ,new SimulationEndListener() {
//                    @Override
//                    public void endSimulation() {
//                        glfwSetWindowShouldClose(window, true);
//                    }
//                }
        );
        hitbox.setShouldRender(false);
        hitboxVisitors.add(hitbox);
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

            objectInstances.get(0).rotate(new Vector3f(0, 1, 0), 0.5f * deltaT);
//            objectInstances.get(0).translateLocal(new Vector3f(0, 0, 1).mul(0.5f*deltaT))
//                    .setPosition(te.getOrigin().add(0, 0, 1));
//            .add(terrainObject.getSpan(), 0, terrainObject.getSpan())
//            objectInstances.get(0).translateGlobal(new Vector3f(0, 3, 0));

            glfwPollEvents();
            for(InputManager inputManager : inputManagers) {
                inputManager.procesInputDeltaT(window, deltaT);
            }

            if(cameraPhysicalObject!=null) cameraPhysicalObject.update(deltaT);

            for(TerrainObject terrainObject : terrainObjects) {
                //TODO
            }

            testCube.render(camera, light);

            for(ObjectInstance objectInstance : objectInstances) {
                objectInstance.render(camera, light);
            }

            seaObject.render(camera, light);
//            seaObject.setHeight(0.);

            for(TerrainObject terrainObject : terrainObjects) {
                terrainObject.render(camera, light);
            }

            glfwSwapBuffers(window);
        }
    }
}
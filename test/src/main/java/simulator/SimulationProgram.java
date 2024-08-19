package simulator;

import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import simulator.drawables.DrawableFactory;
import simulator.drawables.RenderableFactory;
import simulator.drawables.TerrainObject;
import simulator.input.InputManager;
import simulator.input.StateControlsInputManager;
import simulator.input.TestCameraInputManager;
import simulator.input.TestPhysicalCamera;
import simulator.physics.PhysicalObject;
import simulator.physics.Plane;
import simulator.physics.Updateable;
import simulator.shaders.Shader;
import simulator.shaders.ShaderFactory;
import simulator.shaders.UniformManager;
import simulator.swing.WindowSwitchListener;
import simulator.transforms.*;
import simulator.utility.Cleanable;

import java.nio.IntBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_CCW;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SimulationProgram implements Runnable{
    int width = 800;
    int height = 600;
    String[] args;
    WindowSwitchListener main;
    long window;
    List<Cleanable> cleanables = new ArrayList<>();;
    HashMap<String, Consumer<SimulationProgram>> argParserMap;
    boolean developerMode = false;
    boolean enableCulling = true;

    public SimulationProgram(String[] args, WindowSwitchListener main) {
        this.args = args;
        this.main = main;
    }

    @Override
    public void run() {
        System.out.println("LWJGL " + Version.getVersion());

        parseArguments();
        init();
        loop();
        cleanup();
    }

    private void parseArguments() {
        argParserMap = new HashMap<>();
        argParserMap.put("-developerMode", m -> m.developerMode = true);
        argParserMap.put("-disableCulling", m -> m.enableCulling=false);

        for(int i=0; i<args.length; i++) {
            Consumer<SimulationProgram> func = argParserMap.get(args[i]);
            if(func == null) continue;
            func.accept(this);
        }

        if(developerMode) System.out.println("Running simulation in developer mode.");
    }

    private void init() {
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

        //TODO TODO TODO check if okay to put everything inside the conditional
        if(enableCulling) {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
            glFrontFace(GL_CCW);
        }

        glEnable(GL_DEPTH_TEST);
    }

    private void loop() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

        Instant lastTime = Instant.now();
        List<InputManager> inputManagers = new ArrayList<>();
        List<Renderable> renderables = new ArrayList<>();
        List<Updateable> updateables = new ArrayList<>();
        List<TerrainObject> terrainObjects = new ArrayList<>();
        UniformManager uniformManager = new UniformManager();

        Shader mainShader = ShaderFactory.constructShader(
                developerMode ? "src/main/resources/shaders" : "",
                "shader",
                GL_VERTEX_SHADER, GL_FRAGMENT_SHADER, GL_GEOMETRY_SHADER
        );
        cleanables.add(mainShader);
        Shader seaShader = ShaderFactory.constructShader(
                developerMode ? "src/main/resources/shaders/sea_shaders" : "",
                "sea",
                GL_VERTEX_SHADER, GL_FRAGMENT_SHADER
        );
        cleanables.add(seaShader);
        Shader terrainShader = ShaderFactory.constructShader(
                developerMode ? "src/main/resources/shaders/terrain_shaders" : "",
                "terrain",
                GL_VERTEX_SHADER, GL_FRAGMENT_SHADER
        );
        cleanables.add(terrainShader);

        //CAMERA
        Camera camera = new Camera();
        camera.setNstaySame(0.01f);
        camera.setPosition(5.0f, 7f, 7.0f)
                .rotate(new Vector3f(-1, 0, 0), 0.4f);


        //LIGHT
        Light light = new Light();
        light.setPosition(new Vector3f(300, 50f, -1.5f));
        light.setLookDirection(new Vector3f(0, 2, -1), new Vector3f(0, 1, 0));


        //PLANE
        ObjectInstance planeInstance = new ObjectInstance(
                DrawableFactory.makeSimpleTriangleMesh(developerMode ?
                        "src\\main\\resources\\models\\jet.obj"
                        : ""
                ),
                mainShader,
                uniformManager
        );
        renderables.add(planeInstance);
        cleanables.add(planeInstance);
        Plane plane = new Plane(planeInstance, 2000);
        updateables.add(plane);


        //SEA
        SeaObject seaObject = new SeaObject(
                DrawableFactory.makeSeaMesh(2000, 10),
                seaShader,
                uniformManager
        );
        seaObject.setHeight(3);
        cleanables.add(seaObject);
        seaObject.setScale(0.5f, 0.5f, 0.5f);
        renderables.add(seaObject);


        //TERRAIN
        TerrainObject mainIslandTerrain = RenderableFactory.constructTerrainObject(
                terrainShader,
                uniformManager,
                200,
                1,
                30
        );
        renderables.add(mainIslandTerrain);
        terrainObjects.add(mainIslandTerrain);


        double greatestHeight = terrainObjects.stream().mapToDouble(TerrainObject::getMaxHeight).max().getAsDouble();
        terrainObjects.stream().forEach(terrainObject -> terrainObject.setMaxHeight((float)greatestHeight));


        //INPUT MANAGERS
        inputManagers.add(new StateControlsInputManager());
        PhysicalObject physicalCamera = new PhysicalObject(1, camera);

        updateables.add(physicalCamera);
        inputManagers.add(new TestPhysicalCamera(
                window, width, height, physicalCamera, true
        ));


        while(!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // unnecessary?
            glUseProgram(mainShader.getShader());

            float deltaT = Duration.between(lastTime, Instant.now()).toMillis() * 0.001f;
            lastTime = Instant.now();

            glfwPollEvents();
            for(InputManager inputManager : inputManagers) {
                inputManager.procesInputDeltaT(window, deltaT);
            }

            for(Updateable updateable : updateables) {
                updateable.update(deltaT);
            }

            for(Renderable renderable : renderables) {
                renderable.render(camera, light);
            }

            glfwSwapBuffers(window);
        }
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
}

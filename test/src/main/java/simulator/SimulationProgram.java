package simulator;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import simulator.drawables.Drawable;
import simulator.drawables.DrawableFactory;
import simulator.physics.hitboxes.PlaneHitbox;
import simulator.transforms.RenderableFactory;
import simulator.transforms.TerrainObject;
import simulator.input.*;
import simulator.physics.*;
import simulator.shaders.Shader;
import simulator.shaders.ShaderFactory;
import simulator.shaders.UniformManager;
import simulator.swing.WindowSwitchListener;
import simulator.transforms.*;
import simulator.utility.Cleanable;
import simulator.utility.WindowResizeListener;

import java.nio.IntBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_CCW;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SimulationProgram implements Runnable {
    int width = 800;
    int height = 600;
    String[] args;
    WindowSwitchListener main;
    long window;
    List<Cleanable> cleanables = new ArrayList<>();
    List<WindowResizeListener> windowResizeListeners = new ArrayList<>();
    HashMap<String, Consumer<SimulationProgram>> argParserMap;
    boolean developerMode = false;
    boolean enableCulling = true;
    boolean isTimeToEnd = false;
    boolean enableFreecam = false;
    boolean enableHitboxRendering = false;

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
        argParserMap.put("-enableFreecam", m->m.enableFreecam=true);
        argParserMap.put("-enableHitboxes", m->m.enableHitboxRendering=true);

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

        window = glfwCreateWindow(width, height, "Simulation", NULL, NULL);
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

        glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                glViewport(0, 0, width, height);

                for(WindowResizeListener windowResizeListener : windowResizeListeners) {
                    windowResizeListener.resizeWindow(window, width, height);
                }
            }
        });

        GL.createCapabilities();

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
                developerMode ? "src/main/resources/shaders" : "resources/shaders",
                "shader",
                GL_VERTEX_SHADER, GL_FRAGMENT_SHADER, GL_GEOMETRY_SHADER
        );
        cleanables.add(mainShader);
        Shader seaShader = ShaderFactory.constructShader(
                developerMode ? "src/main/resources/shaders/sea_shaders" : "resources/shaders/sea_shaders",
                "sea",
                GL_VERTEX_SHADER, GL_FRAGMENT_SHADER
        );
        cleanables.add(seaShader);
        Shader terrainShader = ShaderFactory.constructShader(
                developerMode ? "src/main/resources/shaders/terrain_shaders" : "resources/shaders/terrain_shaders",
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


        Drawable cubeModel = DrawableFactory.makeSimpleTriangleMesh(developerMode ?
                "src/main/resources/models/kocka.obj"
                : "resources/models/kocka.obj"
        );


        //PLANE
        ObjectInstance planeInstance = new ObjectInstance(
                DrawableFactory.makeSimpleTriangleMesh(developerMode ?
                        "src/main/resources/models/jet.obj"
                        : "resources/models/jet.obj"
                ),
                mainShader,
                uniformManager
        );
        planeInstance.setColor(0.95f, 0.95f, 0.95f);
        cleanables.add(planeInstance);

        planeInstance.setPosition(5, 5, 5);
        Plane plane = new Plane(planeInstance, 2000);
        plane.generateAllHitboxes(cubeModel, mainShader, uniformManager, new SimulationEndListener() {
            @Override
            public void endSimulation() {
                isTimeToEnd = true;
            }
        });
        renderables.add(plane);
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

//        PhysicalObject physicalCamera = new PhysicalObject(1, camera);
        planeInstance.setLookDirection(new Vector3f(-1, 0, -5).normalize(), new Vector3f(0,1,0));

//        enableFreecam = true;
//        enableHitboxRendering = true;
        if(enableFreecam) {
            TestCameraInputManager testCameraInputManager = new TestCameraInputManager(
                    window, width, height, camera, true
            );
            inputManagers.add(testCameraInputManager);
            windowResizeListeners.add(testCameraInputManager);
        } else {
            plane.setPlaneFollower(
                    new PlaneFollower(
                            camera
                    )
                            .setOffset(new Vector3f(0,0.5f, -3f))
            );
            inputManagers.add(new PlaneKeyboardInputManager(plane));
        }

//        PlaneHitbox planeHitbox = new PlaneHitbox(cubeModel, mainShader, uniformManager, plane, null);
//        planeHitbox.setShouldRender(enableHitboxRendering);
//        renderables.add(planeHitbox);
//        inputManagers.add(new HitboxManipulatorManager(planeHitbox));

//        updateables.add(physicalCamera);
//        TestPhysicalCamera testPhysicalCamera = new TestPhysicalCamera(
//                window, width, height, physicalCamera, true
//        );
//        inputManagers.add(testPhysicalCamera);
//        windowResizeListeners.add(testPhysicalCamera);

        //put plane above ground on the edge of the island
        Vector3f planePos = new Vector3f(80, 0, 0);
        planeInstance.setPosition(planePos.x, mainIslandTerrain.getHeightAt(planePos)+2, 0);
        camera.setPosition(plane.getTransform().getPosition());
        plane.setShouldRender(enableHitboxRendering);


        //RUNWAY
        Runway runway = new Runway(cubeModel, mainShader, uniformManager);
        runway.setUpFor(planeInstance, 3.8f, 50, 3);
        renderables.add(runway);

        float deltaTsum = 0;
        int frameCount = 0;

        while(!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(mainShader.getShader());

            float deltaT = Duration.between(lastTime, Instant.now()).toMillis() * 0.001f;
            lastTime = Instant.now();

            deltaTsum += deltaT;
            frameCount++;
            if(deltaTsum > 1) {
                glfwSetWindowTitle(window, String.format("Simulation - %.2f FPS", frameCount/deltaTsum));
                deltaTsum = 0;
                frameCount = 0;
            }

            glfwPollEvents();

            for (InputManager inputManager : inputManagers) {
                inputManager.procesInputDeltaT(window, deltaT);
            }

            int threadCount = updateables.size();
            CountDownLatch latch = new CountDownLatch(threadCount);
            for(Updateable updateable : updateables) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            updateable.update(deltaT);
                        } finally {
                            latch.countDown();
                        }
                    }
                }).start();
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            for(Renderable renderable : renderables) {
                renderable.render(camera, light);
            }

            threadCount = terrainObjects.size()+1;
            CountDownLatch latch2 = new CountDownLatch(threadCount);
            for(TerrainObject terrainObject : terrainObjects) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            plane.visitTerrain(terrainObject);
                        } finally {
                            latch.countDown();
                        }
                    }
                }).start();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        plane.visitSea(seaObject);
                        plane.visitRunway(runway);
                    } finally {
                        latch.countDown();
                    }
                }
            }).start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            glfwSwapBuffers(window);

            if(isTimeToEnd) {
                try {
                    glfwSetWindowShouldClose(window, true);
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void cleanup() {
        for(Cleanable cleanable : cleanables) {
            cleanable.cleanup();
        }

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();

        main.switchToSwing();
    }
}

package simulator;

import simulator.swing.SwingApp;
import simulator.swing.WindowSwitchListener;

import javax.swing.*;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

public class Main implements WindowSwitchListener {
    String[] args;
    Runnable activeRunnable;
    SwingApp swingApp;
    SimulationProgram simulationProgram;
    boolean shouldOpenSim = false;

    public Main(String[] args) {
        this.args = args;
        this.swingApp = new SwingApp(args, this);
        this.simulationProgram = new SimulationProgram(args, this);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            glfwTerminate();
        }));
    }

    public static void main(String[] args) throws InterruptedException {
        Main main = new Main(args);
        main.run();
        while(true) {
            if(main.shouldOpenSim) {
                main.simulationProgram.run();
                main.shouldOpenSim = false;
            }
            Thread.sleep(200);
        }
    }

    public void run() {

        swingApp.run();
    }

    @Override
    public void switchToSwing() {

        swingApp.switchToSwing();

    }

    @Override
    public void switchToSimulation() {

        this.shouldOpenSim = true;
    }
}

package simulator;

import simulator.swing.AppCloseListener;
import simulator.swing.SwingApp;
import simulator.swing.WindowSwitchListener;

public class Main implements WindowSwitchListener, AppCloseListener {
    String[] args;
    SwingApp swingApp;
    Runnable simulationProgram;
    boolean shouldOpenSim = false;
    boolean shouldClose = false;

    public Main(String[] args) {
        this.args = args;
        this.swingApp = new SwingApp(args, this);
    }

    public static void main(String[] args) throws InterruptedException {
        Main main = new Main(args);
        main.run();
    }

    public void run() throws InterruptedException {
        swingApp.run();

        synchronized (this) {
            while(!shouldClose) {
                while(!shouldOpenSim) {
                    wait();
                    if(shouldClose) return;
                }

                shouldOpenSim = false;
                simulationProgram = new SimulationProgram(args, this);
                simulationProgram.run();
            }
        }
    }

    @Override
    public void switchToSwing() {
        swingApp.switchToSwing();
    }

    @Override
    public void switchToSimulation() {
        synchronized (this) {
            shouldOpenSim = true;
            notify();
        }
    }

    @Override
    public void onClose() {
        synchronized (this) {
            shouldClose = true;
            notify();
        }
    }
}

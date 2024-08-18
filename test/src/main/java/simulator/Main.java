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
            while(true) {
                if(!shouldOpenSim) {
                    wait();
                }

                if(shouldClose) break;

                simulationProgram = new FinalSimulationProgram(args, this);
                simulationProgram.run();
                shouldOpenSim = false;
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

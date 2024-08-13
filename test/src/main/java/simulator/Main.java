package simulator;

import simulator.swing.SwingApp;
import simulator.swing.WindowSwitchListener;

public class Main implements WindowSwitchListener {
    String[] args;
    Runnable activeRunnable;
    SwingApp swingApp;

    public Main(String[] args) {
        this.args = args;
        this.swingApp = new SwingApp(args, this);
    }

    public void run() {
        new SimulationProgram(args, this).run();

    }

    public static void main(String[] args) {
        new Main(args).run();
    }

    @Override
    public void switchToSwing() {
        this.activeRunnable = swingApp;
    }

    @Override
    public void switchToSimulation() {
        this.activeRunnable = new SimulationProgram(args, this);
    }
}

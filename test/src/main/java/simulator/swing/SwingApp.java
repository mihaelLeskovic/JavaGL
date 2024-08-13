package simulator.swing;

public class SwingApp implements Runnable, WindowSwitchListener{
    WindowSwitchListener main;

    public SwingApp(String[] args, WindowSwitchListener main) {
        this.main = main;
    }

    @Override
    public void switchToSwing() {

    }

    @Override
    public void switchToSimulation() {

    }

    @Override
    public void run() {

    }
}

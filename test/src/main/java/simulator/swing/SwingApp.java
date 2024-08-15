package simulator.swing;

import javax.swing.*;

public class SwingApp implements Runnable, WindowSwitchListener{
    WindowSwitchListener main;
    JFrame menuFrame;

    public SwingApp(String[] args, WindowSwitchListener main) {
        this.main = main;
    }

    @Override
    public void switchToSwing() {
        menuFrame.setVisible(true);
    }

    @Override
    public void switchToSimulation() {
        menuFrame.setVisible(false);

        main.switchToSimulation();
    }

    @Override
    public void run() {

        menuFrame = new MenuFrame(this);
        menuFrame.setVisible(true);

    }
}

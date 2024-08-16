package simulator.swing;

import simulator.Main;

import javax.swing.*;

public class SwingApp implements Runnable, WindowSwitchListener, AppCloseListener{
    WindowSwitchListener windowSwitchListener;
    AppCloseListener appCloseListener;
    JFrame menuFrame;

    public SwingApp(String[] args, Main main) {
        this.windowSwitchListener = main;
        this.appCloseListener = main;
    }

    @Override
    public void switchToSwing() {
        menuFrame.setVisible(true);
    }

    @Override
    public void switchToSimulation() {
        menuFrame.setVisible(false);

        windowSwitchListener.switchToSimulation();
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(()->{
            menuFrame = new MenuFrame(this, this);
            menuFrame.setVisible(true);
        });
    }

    @Override
    public void onClose() {
        menuFrame.dispose();
        appCloseListener.onClose();
    }
}

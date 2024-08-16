package simulator.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuFrame extends JFrame {
    WindowSwitchListener windowSwitchListener;
    AppCloseListener appCloseListener;

    public MenuFrame(WindowSwitchListener windowSwitchListener, AppCloseListener appCloseListener) {
        this.windowSwitchListener = windowSwitchListener;
        this.appCloseListener = appCloseListener;

//        setTitle("Simulation Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        ImageIcon logoIcon = new ImageIcon("path/to/your/logo.png");
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(logoLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton startButton = new JButton("Start Simulation");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setFont(new Font("Arial", Font.BOLD, 18));
        startButton.setMaximumSize(new Dimension(200, 100));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                windowSwitchListener.switchToSimulation();
            }
        });

        JButton optionsButton = new JButton("Close program");
        optionsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        optionsButton.setFont(new Font("Arial", Font.PLAIN, 14));
        optionsButton.setMaximumSize(new Dimension(150, 30));
        optionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                appCloseListener.onClose();
            }
        });

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(startButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(optionsButton);
        buttonPanel.add(Box.createVerticalGlue());

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        add(mainPanel);
    }
}
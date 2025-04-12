package com.esgdev.amaranthui.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        // Set the title of the frame
        setTitle("MainFrame with BorderLayout");

        // Set the layout to BorderLayout
        setLayout(new BorderLayout());

        // Add components to different regions
        add(new JButton("North"), BorderLayout.NORTH);
        add(new JButton("South"), BorderLayout.SOUTH);
        add(new JButton("East"), BorderLayout.EAST);
        add(new JButton("West"), BorderLayout.WEST);
        add(new JButton("Center"), BorderLayout.CENTER);

        // Set default close operation
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the size of the frame
        setSize(800, 600);

        // Center the frame on the screen
        setLocationRelativeTo(null);
    }
}
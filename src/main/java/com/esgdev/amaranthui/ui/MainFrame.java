package com.esgdev.amaranthui.ui;

import com.esgdev.amaranthui.engine.ModelClient;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Amaranth - Model Client");
        setLayout(new BorderLayout());

        // Create the ModelClient
        ModelClient modelClient = new ModelClient();

        // Add the ChatPanel with the ModelClient
        ChatPanel chatPanel = new ChatPanel(modelClient);
        add(chatPanel, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }
}
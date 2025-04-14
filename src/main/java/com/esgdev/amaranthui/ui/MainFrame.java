package com.esgdev.amaranthui.ui;

import com.esgdev.amaranthui.engine.ModelClient;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame for the Amaranth Model Client.
 * This frame serves as the main window for the application.
 */
public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Amaranth - Model Client");
        setLayout(new BorderLayout());

        // Create the ModelClient
        ModelClient modelClient = new ModelClient();

        // Create the JTabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));

        // Add the ChatPanel as the first tab
        ChatPanel chatPanel = new ChatPanel(modelClient);
        tabbedPane.addTab("Chat", chatPanel);

        // Add the RagSearchPanel as the second tab
        RagSearchPanel ragSearchPanel = new RagSearchPanel(modelClient);
        tabbedPane.addTab("RAG Search", ragSearchPanel);

        // Add the tabbed pane to the frame
        add(tabbedPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }
}

package com.esgdev.amaranthui.ui;

import com.esgdev.amaranthui.engine.ModelClient;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame(ModelClient client) {
        setTitle("Amaranth - Model Client");
        setLayout(new BorderLayout());

        // Create the ModelClient

        // Set up the File Menu
        setJMenuBar(new FileMenu(client, this));

        // Create the JTabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));

        // Add the ChatPanel as the first tab
        ChatPanel chatPanel = new ChatPanel(client);
        tabbedPane.addTab("Chat", chatPanel);

        // Add the RagSearchPanel as the second tab
        RagSearchPanel ragSearchPanel = new RagSearchPanel(client);
        tabbedPane.addTab("RAG Search", ragSearchPanel);

        // Add the tabbed pane to the frame
        add(tabbedPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }
}
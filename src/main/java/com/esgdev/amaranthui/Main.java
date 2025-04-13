package com.esgdev.amaranthui;

import com.esgdev.amaranthui.engine.ModelClient;
import com.esgdev.amaranthui.ui.MainFrame;

import javax.swing.*;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Main.class.getName());
        ModelClient client = new ModelClient();
        SwingUtilities.invokeLater(() -> {
            try {
                // Set the system Look and Feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                logger.log(java.util.logging.Level.SEVERE, "Failed to set Look and Feel", e);
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
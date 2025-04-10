package com.esgdev.amethystui;

import com.esgdev.amethystui.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set the system Look and Feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Create and show the main frame
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
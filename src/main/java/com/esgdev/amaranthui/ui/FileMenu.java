package com.esgdev.amaranthui.ui;

import com.esgdev.amaranthui.engine.ModelClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileMenu extends JMenuBar {
    public FileMenu(ModelClient modelClient, MainFrame mainFrame) {
        // Create the File menu
        JMenu fileMenu = new JMenu("File");

        // Add "New Chat" menu item
        JMenuItem newChatItem = new JMenuItem("New Chat");
        newChatItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Clear the chat history in the ModelClient
                modelClient.clearChatHistory();
                JOptionPane.showMessageDialog(mainFrame, "Chat history cleared.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        fileMenu.add(newChatItem);

        // Add "Quit" menu item
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Exit the application
                System.exit(0);
            }
        });
        fileMenu.add(quitItem);

        // Add the File menu to the menu bar
        add(fileMenu);
    }
}
package com.esgdev.amaranthui.ui;

import com.esgdev.amaranthui.engine.ChatEntry;

import javax.swing.*;
import java.awt.*;

public class ChatEntryRenderer extends JPanel implements ListCellRenderer<ChatEntry> {
    private final Color clientColor = UIManager.getColor("Panel.background");
    private final Color userColor = UIManager.getColor("TextField.background");

    private final JTextArea chunkTextArea;

    public ChatEntryRenderer() {
        setLayout(new BorderLayout());
        chunkTextArea = new JTextArea();
        chunkTextArea.setLineWrap(true);
        chunkTextArea.setWrapStyleWord(true);
        chunkTextArea.setOpaque(true);
        chunkTextArea.setEditable(false);
        chunkTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        add(chunkTextArea, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ChatEntry> list, ChatEntry value, int index, boolean isSelected, boolean cellHasFocus) {
        // Set the text and background color based on the role
        chunkTextArea.setText(value.getChunk());
        if ("user".equalsIgnoreCase(value.getRole())) {
            chunkTextArea.setBackground(userColor);
        } else {
            chunkTextArea.setBackground(clientColor);
        }

        // Highlight selection
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            chunkTextArea.setBackground(list.getSelectionBackground());
        } else {
            setBackground(list.getBackground());
        }

        return this;
    }
}
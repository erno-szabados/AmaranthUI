package com.esgdev.amaranthui.ui;

import com.esgdev.amaranthui.engine.ChatEntry;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * Custom ListCellRenderer for displaying chat entries in a JList.
 * It formats the chat entry with a talker label, timestamp, and message content.
 */
public class ChatEntryRenderer extends JPanel implements ListCellRenderer<ChatEntry> {
    private final Color clientColor = UIManager.getColor("Panel.background");
    private final Color userColor = UIManager.getColor("TextField.background");

    private final JTextArea chunkTextArea;
    private final JLabel talkerLabel;
    private final JLabel timestampLabel;

    public ChatEntryRenderer() {
        setLayout(new BorderLayout());

        // Panel for talker and timestamp
        JPanel headerPanel = new JPanel(new BorderLayout());
        talkerLabel = new JLabel();
        timestampLabel = new JLabel();
        timestampLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        headerPanel.add(talkerLabel, BorderLayout.WEST);
        headerPanel.add(timestampLabel, BorderLayout.EAST);

        // Add header panel to the top
        add(headerPanel, BorderLayout.NORTH);

        // Text area for the message content
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
            talkerLabel.setText("User");
        } else {
            chunkTextArea.setBackground(clientColor);
            talkerLabel.setText("Model");
        }

        // Format and set the timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        timestampLabel.setText(dateFormat.format(value.getCreationDate()));

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
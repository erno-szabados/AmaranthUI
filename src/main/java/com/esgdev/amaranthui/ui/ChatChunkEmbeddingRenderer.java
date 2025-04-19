package com.esgdev.amaranthui.ui;

import com.esgdev.amaranthui.engine.embedding.ChatChunkEmbedding;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Custom ListCellRenderer for displaying RAG search results (Chat chunk embeddings) with metadata and content styling.
 */
public class ChatChunkEmbeddingRenderer extends JPanel implements ListCellRenderer<ChatChunkEmbedding> {
    private final JLabel metadataLabel;
    private final JTextArea contentArea;

    public ChatChunkEmbeddingRenderer() {
        setLayout(new BorderLayout());
        setOpaque(true);

        // Metadata label with smaller font and color
        metadataLabel = new JLabel();
        metadataLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        metadataLabel.setForeground(new Color(100, 100, 100)); // Gray color
        metadataLabel.setBorder(new EmptyBorder(5, 5, 0, 5));

        // Content area with larger font and wrapping
        contentArea = new JTextArea();
        contentArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(5, 5, 5, 5));

        add(metadataLabel, BorderLayout.NORTH);
        add(contentArea, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends ChatChunkEmbedding> list,
            ChatChunkEmbedding value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        if (value != null) {
            // Set metadata text
            String metadata = String.format(
                    "Conversation ID: %d | User ID: %d | Role: %s | Topic: %s | Similarity: %.2f",
                    value.getConversationId(),
                    value.getUserId(),
                    value.getRole(),
                    value.getTopic(),
                    value.getSimilarity()
            );
            metadataLabel.setText(metadata);

            // Set content text
            contentArea.setText(value.getChunk());
        } else {
            metadataLabel.setText("No metadata available");
            contentArea.setText("No content available");
        }

        // Handle selection and focus
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            metadataLabel.setForeground(list.getSelectionForeground());
            contentArea.setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            metadataLabel.setForeground(new Color(100, 100, 100)); // Reset to gray
            contentArea.setForeground(list.getForeground());
        }

        return this;
    }
}
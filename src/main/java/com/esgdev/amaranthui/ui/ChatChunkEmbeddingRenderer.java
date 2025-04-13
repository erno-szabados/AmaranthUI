package com.esgdev.amaranthui.ui;

import com.esgdev.amaranthui.engine.embedding.ChatChunkEmbedding;

import javax.swing.*;
import java.awt.*;

/**
 * Custom ListCellRenderer for displaying RAG search results (Chat chunk embeddings) with metadata.
 */
public class ChatChunkEmbeddingRenderer extends JTextArea implements ListCellRenderer<ChatChunkEmbedding> {

    public ChatChunkEmbeddingRenderer() {
        setOpaque(true);
        setLineWrap(true);
        setWrapStyleWord(true);
        setEditable(false);
        setFont(new Font("SansSerif", Font.PLAIN, 12)); // Set font size to 12pt
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends ChatChunkEmbedding> list,
            ChatChunkEmbedding value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        if (value != null) {
            // Build the display text with metadata and chunk content
            String metadata = String.format(
                    "Conversation ID: %d, User ID: %d, Role: %s, Reply To: %d",
                    value.getConversationId(),
                    value.getUserId(),
                    value.getRole(),
                    value.getReplyToChunkId() != null ? value.getReplyToChunkId() : -1
            );
            String content = value.getChunk();

            setText(metadata + "\n" + content);
        } else {
            setText("No data available");
        }

        // Handle selection and focus
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}
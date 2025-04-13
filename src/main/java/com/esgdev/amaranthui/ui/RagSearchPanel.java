package com.esgdev.amaranthui.ui;

import com.esgdev.amaranthui.engine.ModelClient;
import com.esgdev.amaranthui.engine.embedding.ChatChunkEmbedding;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * RAGSearchPanel is a Swing component that allows users to perform similarity searches.
 * It includes a text input field, a search button, and a results display area.
 */
public class RagSearchPanel extends JPanel {
    private final JTextField queryField;
    private final JButton searchButton;
    private final JList<ChatChunkEmbedding> resultsList;
    private final DefaultListModel<ChatChunkEmbedding> listModel;
    private final ModelClient modelClient;
    private final Logger logger = Logger.getLogger(RagSearchPanel.class.getName());

    public RagSearchPanel(ModelClient modelClient) {
        this.modelClient = modelClient;
        setLayout(new BorderLayout());

        // Create the input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        queryField = new JTextField();
        searchButton = new JButton("Search");

        inputPanel.add(queryField, BorderLayout.CENTER);
        inputPanel.add(searchButton, BorderLayout.EAST);

        // Create the results list
        listModel = new DefaultListModel<>();
        resultsList = new JList<>(listModel);
        resultsList.setCellRenderer(new ChatChunkEmbeddingRenderer());
        JScrollPane scrollPane = new JScrollPane(resultsList);

        // Add components to the panel
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add action listener for the search button
        searchButton.addActionListener(e -> performSearch());
        queryField.addActionListener(e -> performSearch());
    }

    private void performSearch() {
        String query = queryField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a query.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Clear previous results
        listModel.clear();

        // Perform the search in a background thread
        SwingWorker<Void, ChatChunkEmbedding> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    List<ChatChunkEmbedding> results = modelClient.findSimilarChatEmbeddings(query, ModelClient.SIMILAR_CHAT_CHUNK_LIMIT);

                    for (ChatChunkEmbedding embedding : results) {
                        publish(embedding);
                    }
                } catch (Exception ex) {
                    logger.severe("Error performing similarity search: " + ex.getMessage());
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            RagSearchPanel.this,
                            "An error occurred while performing the search.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    ));
                }
                return null;
            }

            @Override
            protected void process(List<ChatChunkEmbedding> embeddings) {
                for (ChatChunkEmbedding embedding : embeddings) {
                    listModel.addElement(embedding);
                }
            }
        };
        worker.execute();
    }
}
package com.esgdev.amaranthui.ui;

import com.esgdev.amaranthui.engine.ChatEntry;
import com.esgdev.amaranthui.engine.EmbeddingGenerationException;
import com.esgdev.amaranthui.engine.ModelClient;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.logging.Logger;

public class ChatPanel extends JPanel {
    private final JList<ChatEntry> chatList;
    private final DefaultListModel<ChatEntry> listModel;
    private final JTextField inputField;
    private final JButton sendButton;
    private final ModelClient modelClient;
    private final Logger logger = Logger.getLogger(ChatPanel.class.getName());

    private final JProgressBar spinner;

    public ChatPanel(ModelClient modelClient) {
        this.modelClient = modelClient;
        setLayout(new BorderLayout());

        // Create the list model and JList
        listModel = new DefaultListModel<>();
        chatList = new JList<>(listModel);
        chatList.setCellRenderer(new ChatEntryRenderer());
        chatList.setFixedCellHeight(-1); // Enable variable row heights

        // Add the JList to a scroll pane
        JScrollPane scrollPane = new JScrollPane(chatList);
        add(scrollPane, BorderLayout.CENTER);

        // Create the input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");

        // Create the spinner
        spinner = new JProgressBar();
        spinner.setIndeterminate(true);
        spinner.setVisible(false); // Initially hidden

        // Add spinner, input field, and button to the input panel
        inputPanel.add(spinner, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Add the input panel to the bottom of the ChatPanel
        add(inputPanel, BorderLayout.SOUTH);

        // Add action listeners for sending messages
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void onMessageSend(ChatEntry userEntry) {
        // Show the spinner
        spinner.setVisible(true);

        // Use a SwingWorker to handle the interaction with the ModelClient
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Send the user message to the ModelClient and get the response
                    String response = modelClient.sendChatRequest(userEntry.getChunk());

                    // Create a new ChatEntry for the model's response
                    ChatEntry modelEntry = new ChatEntry(
                            response,
                            null, // conversationId (can be set later)
                            null, // userId (can be set later)
                            "model",
                            null, // replyToChunkId
                            new Date()
                    );

                    // Add the model's response to the chat list
                    SwingUtilities.invokeLater(() -> addChatEntry(modelEntry));
                } catch (Exception e) {
                    logger.log(java.util.logging.Level.SEVERE, "Error sending message to ModelClient", e);
                }
                return null;
            }

            @Override
            protected void done() {
                // Hide the spinner when processing is complete
                spinner.setVisible(false);
            }
        };
        worker.execute();
    }

    private void sendMessage() {
        String userMessage = inputField.getText().trim();
        if (!userMessage.isEmpty()) {
            // Create a ChatEntry for the user's message
            ChatEntry userEntry = new ChatEntry(
                    userMessage,
                    null, // conversationId (can be set later)
                    null, // userId (can be set later)
                    "user",
                    null, // replyToChunkId
                    new Date()
            );

            // Add the user's message to the chat list
            addChatEntry(userEntry);

            // Process the message
            onMessageSend(userEntry);

            // Clear the input field
            inputField.setText("");
        }
    }

    private void addChatEntry(ChatEntry chatEntry) {
        listModel.addElement(chatEntry);

        try {
            modelClient.addChatEntry(chatEntry);
        } catch (EmbeddingGenerationException e) {
            logger.log(java.util.logging.Level.SEVERE, "Error saving chat entry", e);
        }
    }
}
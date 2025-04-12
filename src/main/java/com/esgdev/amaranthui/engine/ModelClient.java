package com.esgdev.amaranthui.engine;

import com.esgdev.amaranthui.DependencyFactory;
import com.esgdev.amaranthui.db.ChatChunkEmbedding;
import com.esgdev.amaranthui.db.TextEmbedding;

import java.util.List;
import java.util.logging.Logger;

public class ModelClient {
    private static final Logger logger = Logger.getLogger(ModelClient.class.getName());

    private final EmbeddingManagerInterface<TextEmbedding, String> textEmbeddingManager;
    private final EmbeddingManagerInterface<ChatChunkEmbedding, ChatEntry> chatChunkEmbeddingManager;

    public ModelClient() {
        this.textEmbeddingManager = DependencyFactory.createEmbeddingManager();
        this.chatChunkEmbeddingManager = DependencyFactory.createChatChunkEmbeddingManager();
    }

    public void processChatEntry(ChatEntry chatEntry) {
        try {
            logger.info("Processing chat entry for embedding generation...");
            List<ChatChunkEmbedding> embeddings = chatChunkEmbeddingManager.generateEmbeddings(chatEntry);
            chatChunkEmbeddingManager.saveEmbeddings(embeddings);
            logger.info("Chat entry embeddings processed and saved successfully.");
        } catch (Exception e) {
            logger.severe("Failed to process chat entry: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void processText(String text) {
        try {
            logger.info("Processing text for embedding generation...");
            List<TextEmbedding> embeddings = textEmbeddingManager.generateEmbeddings(text);
            textEmbeddingManager.saveEmbeddings(embeddings);
            logger.info("Text embeddings processed and saved successfully.");
        } catch (Exception e) {
            logger.severe("Failed to process text: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
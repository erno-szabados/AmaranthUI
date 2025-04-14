package com.esgdev.amaranthui.engine.embedding;

import com.esgdev.amaranthui.engine.ChatEntry;
import com.esgdev.amaranthui.db.EmbeddingDao;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * ChatChunkEmbeddingManager is responsible for generating, saving, and retrieving chat chunk embeddings.
 */
public class ChatChunkEmbeddingManager implements EmbeddingManagerInterface<ChatChunkEmbedding, ChatEntry> {
    private static final Logger logger = Logger.getLogger(ChatChunkEmbeddingManager.class.getName());

    private final EmbeddingDao<ChatChunkEmbedding> chatChunkEmbeddingDao;
    private final OllamaAPI ollamaAPI;
    private final EmbeddingConfiguration configuration;

    public ChatChunkEmbeddingManager(EmbeddingDao<ChatChunkEmbedding> embeddingDao, OllamaAPI ollamaAPI, EmbeddingConfiguration configuration) {
        this.chatChunkEmbeddingDao = embeddingDao;
        this.ollamaAPI = ollamaAPI;
        this.configuration = configuration;
    }

    @Override
    public List<ChatChunkEmbedding> generateEmbeddings(ChatEntry chatEntry) throws EmbeddingGenerationException {
        String text = chatEntry.getChunk();
        logger.info("Generating embeddings for chat entry: " + text);

        try {
            List<String> chunks = new ArrayList<>();
            int encoded = 0;
            int total = text.length();

            // Split the text into chunks based on configuration
            while (encoded < total) {
                int end = Math.min(encoded + configuration.getChunkSize(), total);
                chunks.add(text.substring(encoded, end));
                encoded += configuration.getChunkSize() - configuration.getOverlap();
            }

            // Generate embeddings using OllamaAPI
            OllamaEmbedResponseModel embeddingResponse = ollamaAPI.embed(configuration.getEmbeddingModel(), chunks);
            List<List<Double>> embeddings = embeddingResponse.getEmbeddings();

            // Create ChatChunkEmbedding objects
            List<ChatChunkEmbedding> chatChunkEmbeddings = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                chatChunkEmbeddings.add(new ChatChunkEmbedding(
                        chunks.get(i),
                        embeddings.get(i),
                        new Date(),
                        new Date(),
                        chatEntry.getConversationId(),
                        chatEntry.getUserId(),
                        chatEntry.getRole(),
                        chatEntry.getReplyToChunkId(),
                        configuration.getEmbeddingModel(), // Set embedding model
                        0.0 // Default similarity value
                ));
            }

            logger.info("Embeddings generated successfully for chat entry.");
            return chatChunkEmbeddings;

        } catch (Exception e) {
            throw new EmbeddingGenerationException("Failed to generate embeddings for chat entry", e);
        }
    }

    @Override
    public void saveEmbeddings(List<ChatChunkEmbedding> embeddings) {
        // Save chat embeddings
        chatChunkEmbeddingDao.addEmbedding(embeddings);
    }

    @Override
    public List<ChatChunkEmbedding> findSimilarEmbeddings(ChatChunkEmbedding sourceEmbedding, int limit) {
        // Implement similarity search for chat embeddings
        return chatChunkEmbeddingDao.findEmbeddingsNear(sourceEmbedding, limit);
    }

    public EmbeddingDao<ChatChunkEmbedding> getChatChunkEmbeddingDao() {
        return chatChunkEmbeddingDao;
    }
}
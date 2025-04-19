package com.esgdev.amaranthui.engine.embedding;

import com.esgdev.amaranthui.db.EmbeddingDao;
import com.esgdev.amaranthui.engine.ChatEntry;
import io.github.ollama4j.OllamaAPI;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ChatChunkEmbeddingManager handles chat chunk embeddings.
 */
public class ChatChunkEmbeddingManager extends BaseEmbeddingManager<ChatChunkEmbedding, ChatEntry> {

    public ChatChunkEmbeddingManager(EmbeddingDao<ChatChunkEmbedding> embeddingDao, OllamaAPI ollamaAPI, EmbeddingConfiguration configuration) {
        super(embeddingDao, ollamaAPI, configuration);
    }

    @Override
    public List<ChatChunkEmbedding> generateEmbeddings(ChatEntry chatEntry) throws EmbeddingGenerationException {
        String text = chatEntry.getChunk();
        logger.info("Generating embeddings for chat entry: " + text);

        try {
            List<String> chunks = splitTextIntoChunks(text);
            List<List<Double>> embeddings = generateEmbeddingsFromChunks(chunks);

            List<ChatChunkEmbedding> chatChunkEmbeddings = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                chatChunkEmbeddings.add(createEmbedding(chunks.get(i), embeddings.get(i), new Date(), new Date(), chatEntry));
            }

            logger.info("Embeddings generated successfully for chat entry.");
            return chatChunkEmbeddings;

        } catch (Exception e) {
            throw new EmbeddingGenerationException("Failed to generate embeddings for chat entry", e);
        }
    }

    @Override
    protected ChatChunkEmbedding createEmbedding(String chunk, List<Double> embedding, Date creationDate, Date lastAccessed, ChatEntry chatEntry) {
        return new ChatChunkEmbedding(
                chunk,
                embedding,
                creationDate,
                lastAccessed,
                chatEntry.getConversationId(),
                chatEntry.getUserId(),
                chatEntry.getRole(),
                chatEntry.getReplyToChunkId(),
                chatEntry.getTopic(),
                configuration.getEmbeddingModel(),
                0.0
        );
    }
}
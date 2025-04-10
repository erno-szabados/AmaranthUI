package com.esgdev.amethystui.engine;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class EmbeddingManager {

    private static final Logger logger = Logger.getLogger(EmbeddingManager.class.getName());

    private final Encoding encoding;
    private final Connection dbConnection;
    private final OllamaAPI ollamaAPI;
    private final String embeddingModel;
    private int contextLimit = 8192;
    private static final int CHUNK_SIZE = 512;
    private static final int OVERLAP = 50;

    public EmbeddingManager(Connection dbConnection, OllamaAPI ollamaAPI, String embeddingModel) {
        this.dbConnection = dbConnection;
        this.ollamaAPI = ollamaAPI;
        this.embeddingModel = embeddingModel;

        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        EncodingType encodingType = EncodingType.CL100K_BASE;
        this.encoding = registry.getEncoding(encodingType);
    }

    private String truncateToLimit(String text) {
        return encoding.encode(text, contextLimit).getTokens().toString();
    }

    public int countTokens(String text) {
        return encoding.encode(text).size();
    }

    public void setContextLimit(int contextLimit) {
        this.contextLimit = contextLimit;
    }

    public int getContextLimit() {
        return contextLimit;
    }

    private void saveEmbedding(String text, String embedding) throws SQLException {
        String sql = "INSERT INTO embeddings (text, embedding) VALUES (?, ?)";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, text);
            stmt.setString(2, embedding);
            stmt.executeUpdate();
            logger.info("Embedding saved successfully for text: " + text);
        }
    }

    public List<String> generateEmbedding(String text) throws Exception {
        logger.info("Generating embeddings for text: " + text);

        // Tokenize and truncate the input text into chunks
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < encoding.encode(text).size()) {
            // Truncate tokens directly using the second parameter
            String chunk = encoding.decode(encoding.encode(text, start + CHUNK_SIZE).getTokens());
            chunks.add(chunk);

            // Move the start index forward by CHUNK_SIZE - OVERLAP
            start += CHUNK_SIZE - OVERLAP;
        }

        logger.info("Text split into " + chunks.size() + " chunks.");

        // Generate embeddings for each chunk
        List<String> embeddings = new ArrayList<>();
        for (String chunk : chunks) {
            OllamaEmbedResponseModel embedding = ollamaAPI.embed(embeddingModel, Collections.singletonList(chunk));
            embeddings.add(embedding.toString());
        }

        logger.info("Embeddings generated successfully for all chunks.");
        return embeddings;
    }

    private String getEmbedding(String text) throws SQLException {
        String sql = "SELECT embedding FROM embeddings WHERE text = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, text);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("Embedding retrieved successfully for text: " + text);
                    return rs.getString("embedding");
                }
            }
        }

        logger.warning("No embedding found for text: " + text);
        return null;
    }
}
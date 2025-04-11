package com.esgdev.amethystui.engine;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmbeddingManager {

    private static final Logger logger = Logger.getLogger(EmbeddingManager.class.getName());

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
    }

    public void setContextLimit(int contextLimit) {
        this.contextLimit = contextLimit;
    }

    public int getContextLimit() {
        return contextLimit;
    }

    public void saveEmbeddings(String text, List<List<Double>> embeddings) throws SQLException {
        if (embeddings == null || embeddings.isEmpty()) {
            throw new IllegalArgumentException("Embeddings cannot be null or empty.");
        }

        // Insert the text into the `texts` table and retrieve the generated ID
        String insertTextSQL = "INSERT INTO texts (text) VALUES (?)";
        int textId;
        try (PreparedStatement insertTextStmt = dbConnection.prepareStatement(insertTextSQL, Statement.RETURN_GENERATED_KEYS)) {
            insertTextStmt.setString(1, text);
            insertTextStmt.executeUpdate();

            try (ResultSet generatedKeys = insertTextStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    textId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve generated text ID.");
                }
            }
        }

        // Insert each embedding into the `embeddings` table
        String insertEmbeddingSQL = "INSERT INTO embeddings (text_id, embedding) VALUES (?, ?)";
        try (PreparedStatement insertEmbeddingStmt = dbConnection.prepareStatement(insertEmbeddingSQL)) {
            for (List<Double> embedding : embeddings) {
                Array embeddingArray = dbConnection.createArrayOf("DOUBLE PRECISION", embedding.toArray());
                insertEmbeddingStmt.setInt(1, textId);
                insertEmbeddingStmt.setArray(2, embeddingArray);
                insertEmbeddingStmt.addBatch();
            }
            insertEmbeddingStmt.executeBatch();
        }
    }

    public List<List<Double>> generateEmbeddings(String text) throws Exception {
        logger.info("Generating embeddings for text: " + text);

        // Sanity check for chunk size and overlap
        if (CHUNK_SIZE <= OVERLAP) {
            throw new RuntimeException("CHUNK_SIZE must be greater than OVERLAP");
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;

        // Split text into chunks with overlap
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end));
            start += CHUNK_SIZE - OVERLAP;
        }

        // Generate embeddings for all chunks in one API call
        OllamaEmbedResponseModel embeddingResponse = ollamaAPI.embed(embeddingModel, chunks);
        List<List<Double>> embeddings = embeddingResponse.getEmbeddings();

        logger.info("Embeddings generated successfully for all chunks.");
        return embeddings;
    }

    public List<String> retrieveSimilarEmbeddings(String sourceText, double similarityThreshold, int maxResults) throws Exception {
        List<List<Double>> sourceEmbeddings = generateEmbeddings(sourceText);
        if (sourceEmbeddings.isEmpty()) {
            throw new RuntimeException("No embeddings generated for the provided text.");
        }
        if (sourceEmbeddings.size() > 1) {
            logger.warning("Multiple embeddings generated. Text may be too long.");
        }
        // We can deal with a single source embedding for now
        List<Double> sourceEmbedding = sourceEmbeddings.get(0);

        Array h2Array = dbConnection.createArrayOf("DOUBLE PRECISION", sourceEmbedding.toArray());
        String sql = """
                WITH Similarities AS (
                    SELECT e.embedding, t.text, COSINE_SIMILARITY(e.embedding, ?) AS similarity
                    FROM embeddings e
                    JOIN texts t ON e.text_id = t.id
                )
                SELECT text, similarity
                FROM Similarities
                WHERE similarity >= ?
                ORDER BY similarity DESC
                LIMIT ?;""";
        try (PreparedStatement preparedStatement = dbConnection.prepareStatement(sql)) {
            preparedStatement.setArray(1, h2Array);
            preparedStatement.setDouble(2, similarityThreshold);
            preparedStatement.setInt(3, maxResults);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<String> similarTexts = new ArrayList<>();

                while (resultSet.next()) {
                    String text = resultSet.getString("text");
                    similarTexts.add(text);
                }

                return similarTexts;
            }
        }
    }
}
package com.esgdev.amaranthui;

import com.esgdev.amaranthui.db.*;
import com.esgdev.amaranthui.engine.*;
import io.github.ollama4j.OllamaAPI;

import java.util.Properties;

public class DependencyFactory {

    private static final OllamaAPI ollamaAPI;
    private static final TextEmbeddingDao textEmbeddingDao;
    private static final EmbeddingConfiguration embeddingConfiguration;

    static {
        try {
            // Load configuration from config.properties
            Properties properties = new Properties();
            properties.load(DependencyFactory.class.getClassLoader().getResourceAsStream("config.properties"));

            // Initialize OllamaAPI
            String host = properties.getProperty("host", "http://localhost:11434/");
            ollamaAPI = new OllamaAPI(host);

            // Load chunk size, overlap, and embedding model for EmbeddingConfiguration
            int chunkSize = Integer.parseInt(properties.getProperty("chunk_size", "512"));
            int overlap = Integer.parseInt(properties.getProperty("overlap", "128"));
            String embeddingModel = properties.getProperty("embedding_model", "nomic-embed-text:latest");
            String jdbcUrl = properties.getProperty("jdbc_url", "jdbc:h2:~/text_embeddings");
            String jdbcUser = properties.getProperty("jdbc_user", "sa");
            String jdbcPassword = properties.getProperty("jdbc_password", "");

            // Create EmbeddingConfiguration
            embeddingConfiguration = new EmbeddingConfiguration(chunkSize, overlap, embeddingModel, jdbcUrl, jdbcUser, jdbcPassword);

            // Initialize TextEmbeddingDao
            textEmbeddingDao = new TextEmbeddingDaoH2(embeddingConfiguration);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize dependencies", e);
        }
    }

    public static EmbeddingManager createEmbeddingManager() {
        return new EmbeddingManager(textEmbeddingDao, ollamaAPI, embeddingConfiguration);
    }

    public static TextEmbeddingDao getTextEmbeddingDao() {
        return textEmbeddingDao;
    }

    public static ChatChunkEmbeddingManager createChatChunkEmbeddingManager() {
        // Create an instance of ChatChunkEmbeddingDao (you may need to implement this if not already done)
        ChatChunkEmbeddingDao chatChunkEmbeddingDao = new ChatChunkEmbeddingDaoH2(embeddingConfiguration);

        // Return a new instance of ChatChunkEmbeddingManager
        return new ChatChunkEmbeddingManager(chatChunkEmbeddingDao);
    }
}
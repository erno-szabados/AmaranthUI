package com.esgdev.amaranthui;

import com.esgdev.amaranthui.db.TextEmbeddingDao;
import com.esgdev.amaranthui.db.TextEmbeddingDaoH2;
import com.esgdev.amaranthui.engine.EmbeddingConfiguration;
import com.esgdev.amaranthui.engine.EmbeddingManager;
import io.github.ollama4j.OllamaAPI;

import java.util.Properties;

public class DependencyFactory {

    private static final OllamaAPI ollamaAPI;
    private static final String embeddingModel;
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

            // Load embedding model
            embeddingModel = properties.getProperty("embedding_model", "nomic-embed-text:latest");

            // Initialize TextEmbeddingDao
            textEmbeddingDao = new TextEmbeddingDaoH2();

            // Load chunk size and overlap for EmbeddingConfiguration
            int chunkSize = Integer.parseInt(properties.getProperty("chunk_size", "512"));
            int overlap = Integer.parseInt(properties.getProperty("overlap", "128"));

            // Create EmbeddingConfiguration
            embeddingConfiguration = new EmbeddingConfiguration(chunkSize, overlap);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize dependencies", e);
        }
    }

    public static EmbeddingManager createEmbeddingManager() {
        return new EmbeddingManager(textEmbeddingDao, ollamaAPI, embeddingModel, embeddingConfiguration);
    }

    public static TextEmbeddingDao getTextEmbeddingDao() {
        return textEmbeddingDao;
    }
}
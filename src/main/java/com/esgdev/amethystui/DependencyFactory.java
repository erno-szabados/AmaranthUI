package com.esgdev.amethystui;

import com.esgdev.amethystui.db.TextEmbeddingDao;
import com.esgdev.amethystui.db.TextEmbeddingDaoH2;
import com.esgdev.amethystui.engine.EmbeddingManager;
import io.github.ollama4j.OllamaAPI;

import java.util.Properties;

public class DependencyFactory {

    private static final OllamaAPI ollamaAPI;
    private static final String embeddingModel;
    private static final TextEmbeddingDao textEmbeddingDao;

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

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize dependencies", e);
        }
    }

    public static EmbeddingManager createEmbeddingManager() {
        return new EmbeddingManager(textEmbeddingDao, ollamaAPI, embeddingModel);
    }

    public static TextEmbeddingDao getTextEmbeddingDao() {
        return textEmbeddingDao;
    }
}
package com.esgdev.amethystui;

import com.esgdev.amethystui.engine.EmbeddingManager;
import io.github.ollama4j.OllamaAPI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DependencyFactory {

    private static final Connection dbConnection;
    private static final OllamaAPI ollamaAPI;
    private static final String embeddingModel;

    static {
        try {
            // Load configuration from config.properties
            Properties properties = new Properties();
            properties.load(DependencyFactory.class.getClassLoader().getResourceAsStream("config.properties"));

            // Initialize database connection
            String dbUrl = properties.getProperty("db.url", "jdbc:sqlite:embeddings.db");
            dbConnection = DriverManager.getConnection(dbUrl);

            // Initialize OllamaAPI
            String host = properties.getProperty("host", "http://localhost:11434/");
            boolean proxyEnabled = Boolean.parseBoolean(properties.getProperty("proxy.enabled", "false"));

            if (proxyEnabled) {
                String proxyHost = properties.getProperty("proxy.host", "");
                String proxyPort = properties.getProperty("proxy.port", "");
                if (!proxyHost.isEmpty() && !proxyPort.isEmpty()) {
                    System.setProperty("http.proxyHost", proxyHost);
                    System.setProperty("http.proxyPort", proxyPort);
                }
            }

            ollamaAPI = new OllamaAPI(host);

            // Load embedding model
            embeddingModel = properties.getProperty("embedding_model", "nomic-embed-text:latest");

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize dependencies", e);
        }
    }

    public static EmbeddingManager createEmbeddingManager() {
        return new EmbeddingManager(dbConnection, ollamaAPI, embeddingModel);
    }

    public static Connection getDbConnection() {
        return dbConnection;
    }

    public static OllamaAPI getOllamaAPI() {
        return ollamaAPI;
    }

    public static String getEmbeddingModel() {
        return embeddingModel;
    }
}
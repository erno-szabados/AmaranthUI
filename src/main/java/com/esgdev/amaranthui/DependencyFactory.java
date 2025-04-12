package com.esgdev.amaranthui;

import com.esgdev.amaranthui.db.*;
import com.esgdev.amaranthui.engine.*;
import io.github.ollama4j.OllamaAPI;

import java.util.Properties;

public class DependencyFactory {

    private static final OllamaAPI ollamaAPI;
    private static final EmbeddingDao<TextEmbedding> textEmbeddingDao;
    private static final EmbeddingConfiguration embeddingConfiguration;
    private static final ChatConfiguration chatConfiguration;

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

            int chatHistorySize = Integer.parseInt(properties.getProperty("chat_history_size", "10"));
            String chatModel = properties.getProperty("chat_model", "gemma3:1b");

            // Create EmbeddingConfiguration
            embeddingConfiguration = new EmbeddingConfiguration(chunkSize, overlap, embeddingModel, jdbcUrl, jdbcUser, jdbcPassword);
            chatConfiguration = new ChatConfiguration(chatHistorySize, chatModel);
            // Initialize TextEmbeddingDao
            textEmbeddingDao = new TextEmbeddingDaoH2(embeddingConfiguration);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize dependencies", e);
        }
    }

    public static OllamaAPI getOllamaAPI() {
        return ollamaAPI;
    }

    public static TextEmbeddingManager createEmbeddingManager() {
        return new TextEmbeddingManager(textEmbeddingDao, ollamaAPI, embeddingConfiguration);
    }

    public static EmbeddingDao<TextEmbedding> getTextEmbeddingDao() {
        return textEmbeddingDao;
    }

    public static ChatChunkEmbeddingManager createChatChunkEmbeddingManager() {
        // Create an instance of ChatChunkEmbeddingDao (you may need to implement this if not already done)
        EmbeddingDao<ChatChunkEmbedding> chatChunkEmbeddingDao = new ChatChunkEmbeddingDaoH2(embeddingConfiguration);

        // Return a new instance of ChatChunkEmbeddingManager
        return new ChatChunkEmbeddingManager(chatChunkEmbeddingDao, ollamaAPI, embeddingConfiguration);
    }

    public static int getChatHistorySize() {
        // Load chat history size from properties or return a default value
        return chatConfiguration.getChatHistorySize();
    }
}
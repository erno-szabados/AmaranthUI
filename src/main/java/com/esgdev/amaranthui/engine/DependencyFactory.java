package com.esgdev.amaranthui.engine;

import com.esgdev.amaranthui.db.*;
import com.esgdev.amaranthui.db.h2.ChatChunkEmbeddingDaoH2;
import com.esgdev.amaranthui.db.h2.KeyValueStoreDaoH2;
import com.esgdev.amaranthui.db.h2.TextEmbeddingDaoH2;
import com.esgdev.amaranthui.engine.embedding.*;
import com.esgdev.amaranthui.engine.tagging.*;
import io.github.ollama4j.OllamaAPI;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * DependencyFactory is responsible for creating and managing dependencies used in the application.
 * It initializes the OllamaAPI, EmbeddingDao instances, and other configurations.
 * This is a lightweight dependency injection mechanism.
 */
public class DependencyFactory {

    private static final OllamaAPI ollamaAPI;
    private static final EmbeddingDao<TextEmbedding> textEmbeddingDao;
    private static final EmbeddingConfiguration embeddingConfiguration;
    private static final ChatConfiguration chatConfiguration;
    private static final TopicConfiguration topicConfiguration;
    private static final Logger logger = Logger.getLogger(DependencyFactory.class.getName());
    private static EmbeddingDao<ChatChunkEmbedding> chatChunkEmbeddingDao;

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
            logger.info("Chat model: " + chatModel);
            logger.info("Chat history size: " + chatHistorySize);
            logger.info("Chunk size: " + chunkSize);
            logger.info("Overlap: " + overlap);
            logger.info("Embedding model: " + embeddingModel);


            // Create EmbeddingConfiguration
            embeddingConfiguration = new EmbeddingConfiguration(chunkSize, overlap, embeddingModel, jdbcUrl, jdbcUser, jdbcPassword);
            chatConfiguration = new ChatConfiguration(chatHistorySize, chatModel);
            // Initialize TextEmbeddingDao
            textEmbeddingDao = new TextEmbeddingDaoH2(embeddingConfiguration);

            // Initialize TopicConfiguration
            String taggingModel = properties.getProperty("tagging_model", "default-tagging-model");
            float temperature = Float.parseFloat(properties.getProperty("temperature", "0.1"));
            float topP = Float.parseFloat(properties.getProperty("top_p", "0.9"));
            int topK = Integer.parseInt(properties.getProperty("top_k", "5"));

            topicConfiguration = new TopicConfiguration(taggingModel, temperature, topP, topK);
            logger.info("Tagging model: " + taggingModel);
            logger.info("Temperature: " + temperature);
            logger.info("Top P: " + topP);
            logger.info("Top K: " + topK);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize dependencies", e);
        }
    }

    public static OllamaAPI getOllamaAPI() {
        return ollamaAPI;
    }

    public static TextEmbeddingManager createTextEmbeddingManager() {
        return new TextEmbeddingManager(textEmbeddingDao, ollamaAPI, embeddingConfiguration);
    }

    public static ChatChunkEmbeddingManager createChatChunkEmbeddingManager() {
        // Create an instance of ChatChunkEmbeddingDao (you may need to implement this if not already done)
        chatChunkEmbeddingDao = new ChatChunkEmbeddingDaoH2(embeddingConfiguration);

        // Return a new instance of ChatChunkEmbeddingManager
        return new ChatChunkEmbeddingManager(chatChunkEmbeddingDao, ollamaAPI, embeddingConfiguration);
    }

    public static ChatConfiguration getChatConfiguration() {
        return chatConfiguration;
    }

    public static EmbeddingConfiguration getEmbeddingConfiguration() {
        return embeddingConfiguration;
    }

    public static TopicConfiguration getTopicConfiguration() {
        return topicConfiguration;
    }

    public static EmbeddingDao<TextEmbedding> getTextEmbeddingDao() {
        return textEmbeddingDao;
    }

    public static EmbeddingDao<ChatChunkEmbedding> getChatChunkEmbeddingDao() {
        return chatChunkEmbeddingDao;
    }

    public static KeyValueStoreDaoH2 getKeyValueStoreDao() {
        return new KeyValueStoreDaoH2(
                embeddingConfiguration.getJdbcUrl(),
                embeddingConfiguration.getJdbcUser(),
                embeddingConfiguration.getJdbcPassword()
        );
    }

}
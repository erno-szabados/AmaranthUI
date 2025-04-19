package com.esgdev.amaranthui.engine;

import com.esgdev.amaranthui.db.h2.KeyValueStoreDaoH2;
import com.esgdev.amaranthui.engine.embedding.*;
import com.esgdev.amaranthui.engine.tagging.TopicAnalyst;
import com.esgdev.amaranthui.engine.tagging.TopicConfiguration;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.*;
import io.github.ollama4j.models.response.Model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ModelClient is responsible for managing the interaction with the ollama models:
 * - Sending chat requests to the Ollama API.
 * - Generation and storage of embeddings for both text and chat entries.
 */
public class ModelClient {
    private static final int MAX_RETRIES = 3; // Maximum number of retries
    private static final int BASE_RETRY_INTERVAL_MS = 2000; // Base retry interval in milliseconds
    private static final int REQUEST_TIMEOUT_S = 120; // Request timeout in milliseconds
    private static final double MIN_SIMILARITY_SCORE = 0.2; // Minimum similarity score threshold

    private static final Logger logger = Logger.getLogger(ModelClient.class.getName());
    public static final int SIMILAR_CHAT_CHUNK_LIMIT = 10;

    private final EmbeddingManagerInterface<TextEmbedding, String> textEmbeddingManager;
    private final EmbeddingManagerInterface<ChatChunkEmbedding, ChatEntry> chatChunkEmbeddingManager;
    private final ChatHistory chatHistory;
    private final OllamaAPI ollamaAPI;
    private final ChatConfiguration chatConfiguration;
    private final EmbeddingConfiguration embeddingConfiguration;
    private static final String SYSTEM_PROMPT_KEY = "system_prompt";
    private final KeyValueStoreDaoH2 keyValueStoreDao;
    private final TopicAnalyst topicAnalyst;
    private final TopicConfiguration topicConfiguration;

    public ModelClient() {
        this.ollamaAPI = DependencyFactory.getOllamaAPI();
        this.ollamaAPI.setRequestTimeoutSeconds(REQUEST_TIMEOUT_S);
        this.textEmbeddingManager = DependencyFactory.createTextEmbeddingManager();
        this.chatChunkEmbeddingManager = DependencyFactory.createChatChunkEmbeddingManager();
        this.chatHistory = new ChatHistory(DependencyFactory.getChatConfiguration().getChatHistorySize());
        this.chatConfiguration = DependencyFactory.getChatConfiguration();
        this.embeddingConfiguration = DependencyFactory.getEmbeddingConfiguration();
        this.topicConfiguration = DependencyFactory.getTopicConfiguration();
        this.keyValueStoreDao = DependencyFactory.getKeyValueStoreDao();
        this.topicAnalyst = new TopicAnalyst(ollamaAPI, topicConfiguration, DependencyFactory.getTextEmbeddingDao(), DependencyFactory.getChatChunkEmbeddingDao(), new ArrayList<>());
        this.topicAnalyst.setTopics(List.of(
                "technology", "health", "sports", "politics", "entertainment",
                "history", "business", "travel", "food", "education",
                "environment", "science"
        ));
    }

    public void addChatEntry(ChatEntry chatEntry) throws EmbeddingGenerationException {
        chatHistory.addChatEntry(chatEntry);
        processChatEntry(chatEntry);

        logger.info("Chat entry added to history: " + chatEntry);
    }

    /**
     * Get the chat history.
     *
     * @return
     */
    public List<ChatEntry> getChatHistory() {
        return chatHistory.getChatHistory();
    }

    public void addChatHistoryObserver(ChatHistory.ChatHistoryObserver observer) {
        chatHistory.addObserver(observer);
    }

    public List<ChatChunkEmbedding> findSimilarChatEmbeddings(String query, int limit) throws Exception {
        // Generate embeddings for the query
        // TODO if query too short, we have a hard time classifying it
        String topic = topicAnalyst.classify(query);
        ChatEntry tempEntry = new ChatEntry(query, null, null, "user", topic,null, new Date());
        List<ChatChunkEmbedding> queryEmbeddings = chatChunkEmbeddingManager.generateEmbeddings(tempEntry);

        if (queryEmbeddings.isEmpty()) {
            throw new IllegalArgumentException("No embeddings could be generated for the query.");
        }

        // Find similar embeddings
        return chatChunkEmbeddingManager.findSimilarEmbeddings(queryEmbeddings.get(0), limit);
    }

    public String sendChatRequest(String systemPrompt, String userMessage,String topic, boolean useChatEmbeddings, boolean useTextEmbeddings) throws Exception {
        int retryCount = 0;
        int retryInterval = BASE_RETRY_INTERVAL_MS;

        while (retryCount < MAX_RETRIES) {
            try {
                // Create a request builder
                String modelName = chatConfiguration.getChatModel();
                if (modelName == null || modelName.isEmpty()) {
                    throw new IllegalArgumentException("Chat model name cannot be null or empty.");
                }
                OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(modelName);

                // Transform chat history into OllamaChatMessage objects
                List<OllamaChatMessage> historyMessages = chatHistory.getChatHistory().stream()
                        .map(entry -> new OllamaChatMessage(
                                entry.getRole().equalsIgnoreCase("user") ? OllamaChatMessageRole.USER : OllamaChatMessageRole.ASSISTANT,
                                entry.getChunk()
                        ))
                        .collect(Collectors.toList());

                if (!systemPrompt.isEmpty()) {
                    // The system prompt will not be shown to the user, but the first message will be
                    OllamaChatRequest requestModel = builder.withMessage(OllamaChatMessageRole.SYSTEM, systemPrompt)
                            .build();

                    // Start conversation with model
                    OllamaChatResult chatResult = ollamaAPI.chat(requestModel);
                    OllamaChatMessage firstMessage = chatResult.getChatHistory().getFirst();

                    // Prepend firstMessage to historyMessages
                    List<OllamaChatMessage> updatedHistoryMessages = new ArrayList<>();
                    updatedHistoryMessages.add(firstMessage);
                    updatedHistoryMessages.addAll(historyMessages);
                    historyMessages = updatedHistoryMessages;
                }

                List<String> ragContext = getRagContext(userMessage, useChatEmbeddings);

                getKnowledgeContext(userMessage, useTextEmbeddings, ragContext);

                if (!ragContext.isEmpty()) {
                    String contextMessage = "Topic:" + topic + "\nRelevant information:\n" + String.join("\n\n", ragContext);
                    builder.withMessage(OllamaChatMessageRole.SYSTEM, contextMessage);
                }

                logger.info("Sending chat request with the following details:");
                logger.info("Topic: " + topic);
                logger.info("User message: " + userMessage);
                logger.info("Chat history messages: " + historyMessages);
                logger.info("RAG context: " + String.join("\n", ragContext));

                OllamaChatRequest requestModel = builder
                        .withMessages(historyMessages)
                        .withMessage(OllamaChatMessageRole.USER, userMessage)
                        .build();

                OllamaChatResult chatResult = ollamaAPI.chat(requestModel);

                // Check if the response message is null
                if (chatResult.getResponseModel() == null || chatResult.getResponseModel().getMessage() == null) {
                    logger.severe("Received null message in response from Ollama API.");
                    throw new IllegalStateException("The response message from Ollama API is null.");
                }

                String response = chatResult.getResponseModel().getMessage().getContent();
                logger.info("Ollama response: " + response);
                return response;

            } catch (HttpTimeoutException e) {
                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    logger.severe("Max retries reached. Unable to send chat request.");
                    throw e; // Rethrow the exception if max retries are reached
                }
                logger.warning("Request timed out. Retrying in " + retryInterval + "ms... (Attempt " + retryCount + ")");
                Thread.sleep(retryInterval);
                retryInterval *= 2; // Exponential backoff
            } catch (Exception e) {
                logger.severe("An unexpected error occurred: " + e.getMessage());
                throw e; // Rethrow other exceptions
            }
        }
        return null; // This line should never be reached
    }

    private void getKnowledgeContext(String userMessage, boolean useTextEmbeddings, List<String> ragContext) throws EmbeddingGenerationException {
        if (useTextEmbeddings) {
            List<TextEmbedding> userTextEmbeddings = textEmbeddingManager.generateEmbeddings(userMessage);
            if (!userTextEmbeddings.isEmpty()) {
                List<TextEmbedding> similarTexts = textEmbeddingManager.findSimilarEmbeddings(
                        userTextEmbeddings.get(0), 3);
                for (TextEmbedding similar : similarTexts) {
                    ragContext.add("Knowledge: " + similar.getChunk());
                }
            }
        }
    }

    private List<String> getRagContext(String userMessage, boolean useChatEmbeddings) throws EmbeddingGenerationException {
        // Add RAG context from embeddings if enabled
        List<String> ragContext = new ArrayList<>();

        String userTopic = "";
        if (useChatEmbeddings) {
            try {
                userTopic = topicAnalyst.classify(userMessage);
            } catch (Exception e) {
                logger.fine("Error classifying user message: " + e.getMessage());
            }
            ChatEntry tempEntry = new ChatEntry(userMessage, null, null, "user", userTopic,null, new Date());
            List<ChatChunkEmbedding> userEmbeddings = chatChunkEmbeddingManager.generateEmbeddings(tempEntry);
            if (!userEmbeddings.isEmpty()) {
                List<ChatChunkEmbedding> similarChats = chatChunkEmbeddingManager.findSimilarEmbeddings(
                        userEmbeddings.get(0), SIMILAR_CHAT_CHUNK_LIMIT); // Get top 5 similar chat chunks
                for (ChatChunkEmbedding similar : similarChats) {
                    if (similar.getSimilarity() >= MIN_SIMILARITY_SCORE) {
                        ragContext.add("Chat history: " + similar.getChunk());
                    }
                }
            }
        }
        return ragContext;
    }

    /**
     * Processes a chat entry to generate and save embeddings.
     *
     * @param chatEntry The chat entry to process.
     */
    private void processChatEntry(ChatEntry chatEntry) throws EmbeddingGenerationException {
        logger.info("Processing chat entry for embedding generation...");
        List<ChatChunkEmbedding> embeddings = chatChunkEmbeddingManager.generateEmbeddings(chatEntry);
        chatChunkEmbeddingManager.saveEmbeddings(embeddings);
        logger.info("Chat entry embeddings processed and saved successfully.");
    }

    /**
     * Processes a text to generate and save embeddings.
     * Text and chat entry embeddings are stored in different tables.
     *
     * @param text The text to process.
     */
    void processText(String text) throws EmbeddingGenerationException {
        logger.info("Processing text for embedding generation...");
        List<TextEmbedding> embeddings = textEmbeddingManager.generateEmbeddings(text);
        textEmbeddingManager.saveEmbeddings(embeddings);
        logger.info("Text embeddings processed and saved successfully.");
    }

    public boolean saveSystemPrompt(String prompt) {
        return keyValueStoreDao.saveValue(SYSTEM_PROMPT_KEY, prompt);
    }

    public String loadSystemPrompt() {
        return keyValueStoreDao.getValue(SYSTEM_PROMPT_KEY);
    }

    public void clearChatHistory() {
        chatHistory.clear();
        logger.info("Chat history cleared.");
    }

    public List<Model> getModels() throws OllamaBaseException, IOException, URISyntaxException, InterruptedException {
        List<Model> models = ollamaAPI.listModels();
        if (models.isEmpty()) {
            logger.warning("No models found.");
        } else {
            logger.info("Available models: " + models);
        }
        return models;
    }

    public String getChatModel() {
        String modelName = keyValueStoreDao.getValue("chat_model");
        if (modelName == null || modelName.isEmpty()) {
            modelName = chatConfiguration.getChatModel();
        }
        if (modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("Chat model name cannot be null or empty.");
        }

        return modelName;
    }

    public String getEmbeddingModel() {
        String modelName = keyValueStoreDao.getValue("embedding_model");
        if (modelName == null || modelName.isEmpty()) {
            modelName = embeddingConfiguration.getEmbeddingModel();
        }
        if (modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("Embedding model name cannot be null or empty.");
        }

        return modelName;
    }

    public String getTaggingModel() {
        String modelName = keyValueStoreDao.getValue("tagging_model");
        if (modelName == null || modelName.isEmpty()) {
            modelName = topicConfiguration.getTaggingModel();
        }
        if (modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("Tagging model name cannot be null or empty.");
        }

        return modelName;
    }

    public void setChatModel(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("Model name cannot be null or empty.");
        }
        keyValueStoreDao.saveValue("chat_model", modelName);
        chatConfiguration.setChatModel(modelName);
        logger.info("Chat model set to: " + modelName);
    }

    public void setEmbeddingModel(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("Model name cannot be null or empty.");
        }
        keyValueStoreDao.saveValue("embedding_model", modelName);
        embeddingConfiguration.setEmbeddingModel(modelName);
        logger.info("Embedding model set to: " + modelName);
    }

    public void setTaggingModel(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("Model name cannot be null or empty.");
        }
        keyValueStoreDao.saveValue("tagging_model", modelName);
        topicConfiguration.setTaggingModel(modelName);
        logger.info("Tagging model set to: " + modelName);
    }

    public String classify(String text) throws IOException, OllamaBaseException, InterruptedException {
        return topicAnalyst.classify(text);
    }
}

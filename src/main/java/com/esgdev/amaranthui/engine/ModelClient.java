package com.esgdev.amaranthui.engine;

import com.esgdev.amaranthui.engine.embedding.ChatChunkEmbedding;
import com.esgdev.amaranthui.engine.embedding.EmbeddingGenerationException;
import com.esgdev.amaranthui.engine.embedding.EmbeddingManagerInterface;
import com.esgdev.amaranthui.engine.embedding.TextEmbedding;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.*;

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

    private static final Logger logger = Logger.getLogger(ModelClient.class.getName());

    private final EmbeddingManagerInterface<TextEmbedding, String> textEmbeddingManager;
    private final EmbeddingManagerInterface<ChatChunkEmbedding, ChatEntry> chatChunkEmbeddingManager;
    private final ChatHistory chatHistory;
    private final OllamaAPI ollamaAPI;

    public ModelClient() {
        this.ollamaAPI = DependencyFactory.getOllamaAPI();
        this.textEmbeddingManager = DependencyFactory.createEmbeddingManager();
        this.chatChunkEmbeddingManager = DependencyFactory.createChatChunkEmbeddingManager();
        this.chatHistory = new ChatHistory(DependencyFactory.getChatHistorySize());
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

    public String sendChatRequest(String userMessage, boolean useChatEmbeddings, boolean useTextEmbeddings) throws Exception {
        int retryCount = 0;
        int retryInterval = BASE_RETRY_INTERVAL_MS;

        while (retryCount < MAX_RETRIES) {
            try {
                // Transform chat history into OllamaChatMessage objects
                List<OllamaChatMessage> historyMessages = chatHistory.stream()
                        .map(entry -> new OllamaChatMessage(
                                entry.getRole().equalsIgnoreCase("user") ? OllamaChatMessageRole.USER : OllamaChatMessageRole.ASSISTANT,
                                entry.getChunk()
                        ))
                        .collect(Collectors.toList());

                // Create a request builder
                OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance("gemma3:1b");

                // Add RAG context from embeddings if enabled
                List<String> ragContext = new ArrayList<>();

                if (useChatEmbeddings) {
                    ChatEntry tempEntry = new ChatEntry(userMessage, null, null, "user", null, new Date());
                    List<ChatChunkEmbedding> userEmbeddings = chatChunkEmbeddingManager.generateEmbeddings(tempEntry);
                    if (!userEmbeddings.isEmpty()) {
                        List<ChatChunkEmbedding> similarChats = chatChunkEmbeddingManager.findSimilarEmbeddings(
                                userEmbeddings.get(0), 5); // Get top 5 similar chat chunks
                        for (ChatChunkEmbedding similar : similarChats) {
                            ragContext.add("Chat history: " + similar.getChunk());
                        }
                    }
                }

                if (useTextEmbeddings) {
                    List<TextEmbedding> userTextEmbeddings = textEmbeddingManager.generateEmbeddings(userMessage);
                    if (!userTextEmbeddings.isEmpty()) {
                        List<TextEmbedding> similarTexts = textEmbeddingManager.findSimilarEmbeddings(
                                userTextEmbeddings.get(0), 5); // Get top 5 similar text chunks
                        for (TextEmbedding similar : similarTexts) {
                            ragContext.add("Knowledge: " + similar.getChunk());
                        }
                    }
                }

                if (!ragContext.isEmpty()) {
                    String contextMessage = "Relevant information:\n" + String.join("\n\n", ragContext);
                    builder.withMessage(OllamaChatMessageRole.SYSTEM, contextMessage);
                }

                OllamaChatRequest requestModel = builder
                        .withMessages(historyMessages)
                        .withMessage(OllamaChatMessageRole.USER, userMessage)
                        .build();

                OllamaChatResult chatResult = ollamaAPI.chat(requestModel);

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

    /**
     * Processes a chat entry to generate and save embeddings.
     *
     * @param chatEntry
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
     * @param text
     */
    void processText(String text) throws EmbeddingGenerationException {
        logger.info("Processing text for embedding generation...");
        List<TextEmbedding> embeddings = textEmbeddingManager.generateEmbeddings(text);
        textEmbeddingManager.saveEmbeddings(embeddings);
        logger.info("Text embeddings processed and saved successfully.");
    }
}
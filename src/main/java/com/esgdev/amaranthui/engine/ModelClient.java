package com.esgdev.amaranthui.engine;

import com.esgdev.amaranthui.engine.embedding.ChatChunkEmbedding;
import com.esgdev.amaranthui.engine.embedding.EmbeddingGenerationException;
import com.esgdev.amaranthui.engine.embedding.EmbeddingManagerInterface;
import com.esgdev.amaranthui.engine.embedding.TextEmbedding;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.*;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ModelClient is responsible for managing the interaction with the ollama models:
 * - Sending chat requests to the Ollama API.
 * - Generation and storage of embeddings for both text and chat entries.
 */
public class ModelClient {
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
     * Get the chat history as a list.
     *
     * @return
     */
    public List<ChatEntry> getChatHistory() {
        return chatHistory.getChatHistory();
    }

    /**
     * Sends a chat request to the Ollama API using the chat history to augment the request.
     *
     * @param userMessage The new user message to send.
     * @return The response from the Ollama model.
     * @throws Exception If an error occurs during the request.
     */
    public String sendChatRequest(String userMessage) throws Exception {
        // Transform chat history into OllamaChatMessage objects using the stream
        List<OllamaChatMessage> historyMessages = chatHistory.stream()
                .map(entry -> new OllamaChatMessage(
                        entry.getRole().equalsIgnoreCase("user") ? OllamaChatMessageRole.USER : OllamaChatMessageRole.ASSISTANT,
                        entry.getChunk()
                ))
                .collect(Collectors.toList());

        // Create a request builder (note gemma3:1b is not among the constants yet, so we use it directly)
        OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance("gemma3:1b");

        // Build the request with history and the new user message
        OllamaChatRequest requestModel = builder
                .withMessages(historyMessages)
                .withMessage(OllamaChatMessageRole.USER, userMessage)
                .build();

        // Send the request to the Ollama API
        OllamaChatResult chatResult = ollamaAPI.chat(requestModel);

        // Log and return the response
        String response = chatResult.getResponseModel().getMessage().getContent();
        logger.info("Ollama response: " + response);
        return response;
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
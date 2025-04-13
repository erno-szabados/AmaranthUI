package com.esgdev.amaranthui.engine;

import com.esgdev.amaranthui.engine.embedding.ChatChunkEmbedding;
import com.esgdev.amaranthui.engine.embedding.TextEmbedding;
import com.esgdev.amaranthui.db.EmbeddingDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for the ModelClient class without using mocks.
 */
public class ModelClientIntegrationTest {

    private ModelClient modelClient;
    private EmbeddingDao<ChatChunkEmbedding> chatChunkEmbeddingDao;
    private EmbeddingDao<TextEmbedding> textEmbeddingDao;

    @Before
    public void setUp() {
        // Initialize ModelClient and real dependencies
        modelClient = new ModelClient();
        chatChunkEmbeddingDao = DependencyFactory.createChatChunkEmbeddingManager().getChatChunkEmbeddingDao();
        textEmbeddingDao = DependencyFactory.getTextEmbeddingDao();

        // Clean up database before each test
        chatChunkEmbeddingDao.getAllEmbeddings().forEach(embedding -> chatChunkEmbeddingDao.deleteEmbedding(embedding.getId()));
        textEmbeddingDao.getAllEmbeddings().forEach(embedding -> textEmbeddingDao.deleteEmbedding(embedding.getId()));
    }

    @After
    public void tearDown() {
        // Clean up database after each test
        //chatChunkEmbeddingDao.getAllEmbeddings().forEach(embedding -> chatChunkEmbeddingDao.deleteEmbedding(embedding.getId()));
        //textEmbeddingDao.getAllEmbeddings().forEach(embedding -> textEmbeddingDao.deleteEmbedding(embedding.getId()));
    }

    @Test
    public void testAddChatEntry() throws Exception {
        // Create a ChatEntry
        ChatEntry chatEntry = new ChatEntry(
                "Hello, how are you?",
                1L,
                1L,
                "user",
                null,
                new Date()
        );

        // Add the chat entry
        modelClient.addChatEntry(chatEntry);

        // Verify the chat entry is added to the history
        List<ChatEntry> chatHistory = modelClient.getChatHistory();
        assertNotNull(chatHistory);
        assertEquals(1, chatHistory.size());
        assertEquals("Hello, how are you?", chatHistory.get(0).getChunk());

        // Verify embeddings are generated and saved
        List<ChatChunkEmbedding> embeddings = chatChunkEmbeddingDao.getAllEmbeddings();
        assertNotNull(embeddings);
        assertFalse(embeddings.isEmpty());
        assertEquals(chatEntry.getChunk(), embeddings.get(0).getChunk());
    }

    @Ignore("This test requires a running Ollama API service.")
    @Test
    public void testSendChatRequest() throws Exception {
        // Send a chat request
        String userMessage = "What is the weather like today?";
        String response = modelClient.sendChatRequest(userMessage, false, false);

        // Verify the response
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    public void testProcessTextEmbeddings() throws Exception {
        // Process a text
        String text = "This is a sample text for embedding generation.";
        modelClient.processText(text);

        // Verify embeddings are generated and saved
        List<TextEmbedding> embeddings = textEmbeddingDao.getAllEmbeddings();
        assertNotNull(embeddings);
        assertFalse(embeddings.isEmpty());
        assertEquals(text, embeddings.get(0).getChunk());
    }
}
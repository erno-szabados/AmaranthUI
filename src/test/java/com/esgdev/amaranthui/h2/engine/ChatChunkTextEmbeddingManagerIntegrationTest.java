package com.esgdev.amaranthui.h2.engine;

import com.esgdev.amaranthui.DependencyFactory;
import com.esgdev.amaranthui.db.ChatChunkEmbedding;
import com.esgdev.amaranthui.db.EmbeddingDao;
import com.esgdev.amaranthui.engine.ChatChunkEmbeddingManager;
import com.esgdev.amaranthui.engine.ChatEntry;
import com.esgdev.amaranthui.engine.EmbeddingGenerationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration test for the ChatChunkEmbeddingManager class.
 * This test interacts with the actual database and dependencies.
 */
public class ChatChunkTextEmbeddingManagerIntegrationTest {

    private ChatChunkEmbeddingManager chatChunkEmbeddingManager;
    private EmbeddingDao<ChatChunkEmbedding> chatChunkEmbeddingDao;

    @Before
    public void setUp() {
        // Initialize dependencies
        chatChunkEmbeddingDao = DependencyFactory.createChatChunkEmbeddingManager().getChatChunkEmbeddingDao();
        chatChunkEmbeddingManager = DependencyFactory.createChatChunkEmbeddingManager();

        // Clean database before each test
        chatChunkEmbeddingDao.getAllEmbeddings().forEach(embedding -> chatChunkEmbeddingDao.deleteEmbedding(embedding.getId()));
    }

    @After
    public void tearDown() {
        // Clean database after each test
        //chatChunkEmbeddingDao.getAllEmbeddings().forEach(embedding -> chatChunkEmbeddingDao.deleteEmbedding(embedding.getId()));
    }

    @Ignore("This test requires a running embedding generation service.")
    @Test
    public void testGenerateEmbeddings() throws EmbeddingGenerationException {
        // Create a ChatEntry
        ChatEntry chatEntry = new ChatEntry(
                "Hello, how are you?",
                1L,
                1L,
                "user",
                null,
                new Date()
        );

        // Generate embeddings
        List<ChatChunkEmbedding> embeddings = chatChunkEmbeddingManager.generateEmbeddings(chatEntry);

        // Assertions
        assertNotNull(embeddings);
        assertFalse(embeddings.isEmpty());
        assertEquals(chatEntry.getChunk(), embeddings.get(0).getChunk());
    }

    @Test
    public void testSaveAndRetrieveEmbeddings() {
        // Create a ChatChunkEmbedding
        ChatChunkEmbedding embedding = new ChatChunkEmbedding(
                "Hello, how are you?",
                new ArrayList<>(), // Dummy embedding
                new Date(),
                new Date(),
                1L,
                1L,
                "user",
                null
        );

        // Save embedding
        List<ChatChunkEmbedding> embeddings = new ArrayList<>();
        embeddings.add(embedding);
        chatChunkEmbeddingManager.saveEmbeddings(embeddings);

        // Retrieve embeddings
        List<ChatChunkEmbedding> retrievedEmbeddings = chatChunkEmbeddingDao.getAllEmbeddings();

        // Assertions
        assertNotNull(retrievedEmbeddings);
        assertEquals(1, retrievedEmbeddings.size());
        assertEquals(embedding.getChunk(), retrievedEmbeddings.get(0).getChunk());
    }

    @Test
    public void testFindSimilarEmbeddings() {
        // Create and save embeddings
        ChatChunkEmbedding embedding = new ChatChunkEmbedding(
                "Hello, how are you?",
                new ArrayList<>(), // Dummy embedding
                new Date(),
                new Date(),
                1L,
                1L,
                "user",
                null
        );
        List<ChatChunkEmbedding> embeddings = new ArrayList<>();
        embeddings.add(embedding);
        chatChunkEmbeddingManager.saveEmbeddings(embeddings);

        // Find similar embeddings
        List<ChatChunkEmbedding> similarEmbeddings = chatChunkEmbeddingManager.findSimilarEmbeddings(embedding, 5);

        // Assertions
        assertNotNull(similarEmbeddings);
        assertFalse(similarEmbeddings.isEmpty());
        assertEquals(embedding.getChunk(), similarEmbeddings.get(0).getChunk());
    }
}
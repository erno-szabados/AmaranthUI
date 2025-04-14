package com.esgdev.amaranthui.h2.engine;

import com.esgdev.amaranthui.engine.DependencyFactory;
import com.esgdev.amaranthui.engine.embedding.ChatChunkEmbedding;
import com.esgdev.amaranthui.db.EmbeddingDao;
import com.esgdev.amaranthui.engine.embedding.ChatChunkEmbeddingManager;
import com.esgdev.amaranthui.engine.ChatEntry;
import com.esgdev.amaranthui.engine.embedding.EmbeddingGenerationException;
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

    @Ignore("This test requires a running embedding generation service.")
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
                null,
                "curren t-model", // Embedding model
                0.0 // Similarity
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

    @Ignore("This test requires a running embedding generation service.")
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
                null,
                "current-model", // Embedding model
                0.0 // Similarity
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

    @Ignore("This test requires a running embedding generation service.")
    @Test
    public void testFindSimilarEmbeddingsWithModelFilter() {
        // Create and save embeddings with the current model
        ChatChunkEmbedding embedding = new ChatChunkEmbedding(
                "Hello, how are you?",
                new ArrayList<>(), // Dummy embedding
                new Date(),
                new Date(),
                1L,
                1L,
                "user",
                null,
                "current-model",
                0.0
        );
        chatChunkEmbeddingManager.saveEmbeddings(List.of(embedding));

        // Create and save embeddings with a different model
        ChatChunkEmbedding differentModelEmbedding = new ChatChunkEmbedding(
                "Different text",
                new ArrayList<>(), // Dummy embedding
                new Date(),
                new Date(),
                1L,
                1L,
                "user",
                null,
                "different-model",
                0.0
        );
        chatChunkEmbeddingManager.saveEmbeddings(List.of(differentModelEmbedding));

        // Find similar embeddings using the current model
        List<ChatChunkEmbedding> similarEmbeddings = chatChunkEmbeddingManager.findSimilarEmbeddings(embedding, 5);

        // Assertions
        assertNotNull(similarEmbeddings);
        assertFalse(similarEmbeddings.isEmpty());
        assertTrue(similarEmbeddings.stream().allMatch(e -> e.getEmbeddingModel().equals(embedding.getEmbeddingModel())));
    }
}
package com.esgdev.amaranthui.h2.engine;

import com.esgdev.amaranthui.DependencyFactory;
import com.esgdev.amaranthui.db.TextEmbedding;
import com.esgdev.amaranthui.db.TextEmbeddingDao;
import com.esgdev.amaranthui.engine.EmbeddingManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration test for the EmbeddingManager class.
 * This test interacts with the actual database and Ollama API.
 */
public class EmbeddingManagerIntegrationTest {

    private EmbeddingManager embeddingManager;
    private TextEmbeddingDao textEmbeddingDao;

    @Before
    public void setUp() {
        // Initialize dependencies
        textEmbeddingDao = DependencyFactory.getTextEmbeddingDao();
        embeddingManager = DependencyFactory.createEmbeddingManager();

        // Clean database before each test
        textEmbeddingDao.getAllEmbeddings().forEach(embedding -> textEmbeddingDao.deleteEmbedding(embedding.getId()));
    }

    @After
    public void tearDown() {
        // Clean database after each test
        //textEmbeddingDao.getAllEmbeddings().forEach(embedding -> textEmbeddingDao.deleteEmbedding(embedding.getId()));
    }

    @Ignore("This test requires a running Ollama API instance.")
    @Test
    public void testSaveAndRetrieveEmbeddings() throws Exception {
        // Generate embeddings
        String text = "Hello, world!";
        List<TextEmbedding> embeddings = embeddingManager.generateEmbeddings(text);

        // Save embeddings
        embeddingManager.saveEmbeddings(embeddings);

        // Retrieve embeddings
        List<TextEmbedding> retrievedEmbeddings = textEmbeddingDao.getAllEmbeddings();

        // Assertions
        assertNotNull(retrievedEmbeddings);
        assertEquals(embeddings.size(), retrievedEmbeddings.size());
        assertEquals(embeddings.get(0).getChunk(), retrievedEmbeddings.get(0).getChunk());
    }


    @Ignore("This test requires a running Ollama API instance.")
    @Test
    public void testFindSimilarEmbeddings() throws Exception {
        // Generate and save embeddings
        String text = "Hello, world!";
        List<TextEmbedding> embeddings = embeddingManager.generateEmbeddings(text);
        embeddingManager.saveEmbeddings(embeddings);

        // Find similar embeddings
        TextEmbedding sourceEmbedding = embeddings.get(0);
        List<TextEmbedding> similarEmbeddings = embeddingManager.findSimilarEmbeddings(sourceEmbedding, 5);

        // Assertions
        assertNotNull(similarEmbeddings);
        assertFalse(similarEmbeddings.isEmpty());
        assertEquals(sourceEmbedding.getChunk(), similarEmbeddings.get(0).getChunk());
    }
}
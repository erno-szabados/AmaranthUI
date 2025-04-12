package com.esgdev.amaranthui.h2.engine;

import com.esgdev.amaranthui.DependencyFactory;
import com.esgdev.amaranthui.db.EmbeddingDao;
import com.esgdev.amaranthui.db.TextEmbedding;
import com.esgdev.amaranthui.engine.TextEmbeddingManager;
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
public class TextEmbeddingManagerIntegrationTest {

    private TextEmbeddingManager textEmbeddingManager;
    private EmbeddingDao<TextEmbedding> textEmbeddingDao;

    @Before
    public void setUp() {
        // Initialize dependencies
        textEmbeddingDao = DependencyFactory.getTextEmbeddingDao();
        textEmbeddingManager = DependencyFactory.createEmbeddingManager();

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
        List<TextEmbedding> embeddings = textEmbeddingManager.generateEmbeddings(text);

        // Save embeddings
        textEmbeddingManager.saveEmbeddings(embeddings);

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
        List<TextEmbedding> embeddings = textEmbeddingManager.generateEmbeddings(text);
        textEmbeddingManager.saveEmbeddings(embeddings);

        // Find similar embeddings
        TextEmbedding sourceEmbedding = embeddings.get(0);
        List<TextEmbedding> similarEmbeddings = textEmbeddingManager.findSimilarEmbeddings(sourceEmbedding, 5);

        // Assertions
        assertNotNull(similarEmbeddings);
        assertFalse(similarEmbeddings.isEmpty());
        assertEquals(sourceEmbedding.getChunk(), similarEmbeddings.get(0).getChunk());
    }
}
package com.esgdev.amaranthui.h2.engine;

import com.esgdev.amaranthui.DependencyFactory;
import com.esgdev.amaranthui.db.TextEmbedding;
import com.esgdev.amaranthui.db.TextEmbeddingDao;
import com.esgdev.amaranthui.engine.EmbeddingConfiguration;
import com.esgdev.amaranthui.engine.EmbeddingManager;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for the EmbeddingManager class using a mocked OllamaAPI.
 * This test does not interact with the actual database or API.
 */
public class EmbeddingManagerIntegrationWithMockTest {

    private EmbeddingManager embeddingManager;
    private OllamaAPI mockOllamaAPI;
    private TextEmbeddingDao textEmbeddingDao;

    @Before
    public void setUp() {
        // Mock OllamaAPI
        mockOllamaAPI = Mockito.mock(OllamaAPI.class);

        // Stub the embed method
        try {
            when(mockOllamaAPI.embed(anyString(), anyList())).thenAnswer(invocation -> {
                List<String> chunks = invocation.getArgument(1);
                List<List<Double>> embeddings = new ArrayList<>();
                for (String chunk : chunks) {
                    // Generate dummy embeddings for each chunk
                    List<Double> embedding = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        embedding.add((double) chunk.hashCode() % 100 + i);
                    }
                    embeddings.add(embedding);
                }
                OllamaEmbedResponseModel response = new OllamaEmbedResponseModel();
                response.setEmbeddings(embeddings);
                return response;
            });
        } catch (IOException | InterruptedException | OllamaBaseException e) {
            throw new RuntimeException(e);
        }

        // Initialize dependencies
        textEmbeddingDao = DependencyFactory.getTextEmbeddingDao();

        // Create EmbeddingConfiguration
        EmbeddingConfiguration configuration = new EmbeddingConfiguration(512, 128);

        // Initialize EmbeddingManager with configuration
        embeddingManager = new EmbeddingManager(textEmbeddingDao, mockOllamaAPI, "mock-model", configuration);

        // Clean database before each test
        textEmbeddingDao.getAllEmbeddings().forEach(embedding -> textEmbeddingDao.deleteEmbedding(embedding.getId()));
    }

    @Ignore
    @Test
    public void testGenerateEmbeddings() throws Exception {
        String text = "Hello, world!";
        List<TextEmbedding> embeddings = embeddingManager.generateEmbeddings(text);

        // Assertions
        assertNotNull(embeddings);
        assertFalse(embeddings.isEmpty());
        assertEquals(2, embeddings.size()); // Based on CHUNK_SIZE and OVERLAP
        assertEquals("Hello, world!", embeddings.get(0).getChunk());
        assertNotNull(embeddings.get(0).getEmbedding());
    }

    @Ignore
    @Test
    public void testSaveAndRetrieveEmbeddings() throws Exception {
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
}
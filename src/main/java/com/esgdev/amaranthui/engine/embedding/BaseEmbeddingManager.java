package com.esgdev.amaranthui.engine.embedding;

import com.esgdev.amaranthui.db.EmbeddingDao;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * BaseEmbeddingManager provides shared functionality for embedding managers.
 * @param <E> the type of embedding
 * @param <S> the type of source (e.g., text or chat entry)
 */
public abstract class BaseEmbeddingManager<E, S> implements EmbeddingManagerInterface<E, S> {

    protected final Logger logger = Logger.getLogger(getClass().getName());
    protected final EmbeddingDao<E> embeddingDao;
    protected final OllamaAPI ollamaAPI;
    protected final EmbeddingConfiguration configuration;

    /**
     * Constructor for BaseEmbeddingManager.
     *
     * @param embeddingDao the embedding DAO instance
     * @param ollamaAPI the Ollama API instance
     * @param configuration the embedding configuration
     */
    public BaseEmbeddingManager(EmbeddingDao<E> embeddingDao, OllamaAPI ollamaAPI, EmbeddingConfiguration configuration) {
        this.embeddingDao = embeddingDao;
        this.ollamaAPI = ollamaAPI;
        this.configuration = configuration;
    }

    /**
     * @return the embeddingDao instance used by this manager. For tests.
     */
    public EmbeddingDao<E> getEmbeddingDao() {
        return embeddingDao;
    }

    /**
     * Splits the input text into chunks based on the configured chunk size and overlap.
     * @param text the input text to be split
     * @return
     */
    protected List<String> splitTextIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        int encoded = 0;
        int total = text.length();

        while (encoded < total) {
            int end = Math.min(encoded + configuration.getChunkSize(), total);
            chunks.add(text.substring(encoded, end));
            encoded += configuration.getChunkSize() - configuration.getOverlap();
        }

        return chunks;
    }

    /**
     * Generates embeddings for the given chunks using the Ollama API.
     * @param chunks the list of text chunks to generate embeddings for
     * @return a list of embeddings
     * @throws EmbeddingGenerationException if an error occurs during embedding generation
     */
    protected List<List<Double>> generateEmbeddingsFromChunks(List<String> chunks) throws EmbeddingGenerationException {
        OllamaEmbedResponseModel embeddingResponse = null;
        try {
            embeddingResponse = ollamaAPI.embed(configuration.getEmbeddingModel(), chunks);
        } catch (IOException | InterruptedException | OllamaBaseException e) {
            throw new EmbeddingGenerationException("Failed to generate embeddings", e);
        }
        return embeddingResponse.getEmbeddings();
    }

    public void saveEmbeddings(List<E> embeddings) {
        embeddingDao.addEmbedding(embeddings);
    }

    public List<E> findSimilarEmbeddings(E sourceEmbedding, int limit) {
        return embeddingDao.findEmbeddingsNear(sourceEmbedding, limit);
    }

    protected abstract E createEmbedding(String chunk, List<Double> embedding, Date creationDate, Date lastAccessed, S source);
}
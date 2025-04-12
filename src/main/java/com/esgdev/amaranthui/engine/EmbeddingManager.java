package com.esgdev.amaranthui.engine;

import com.esgdev.amaranthui.db.TextEmbedding;
import com.esgdev.amaranthui.db.TextEmbeddingDao;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * EmbeddingManager is responsible for generating, saving, and retrieving text embeddings.
 */
public class EmbeddingManager implements EmbeddingManagerInterface<TextEmbedding, String> {

    private static final Logger logger = Logger.getLogger(EmbeddingManager.class.getName());

    private final TextEmbeddingDao textEmbeddingDao;
    private final OllamaAPI ollamaAPI;
    private final EmbeddingConfiguration configuration;

    public EmbeddingManager(TextEmbeddingDao textEmbeddingDao, OllamaAPI ollamaAPI, EmbeddingConfiguration configuration) {
        this.textEmbeddingDao = textEmbeddingDao;
        this.ollamaAPI = ollamaAPI;
        this.configuration = configuration;
    }

    public List<TextEmbedding> generateEmbeddings(String text) throws EmbeddingGenerationException {
        logger.info("Generating embeddings for text: " + text);

        try {
            List<String> chunks = new ArrayList<>();
            int encoded = 0;
            int total = text.length();

            while (encoded < total) {
                int end = Math.min(encoded + configuration.getChunkSize(), total);
                chunks.add(text.substring(encoded, end));
                encoded += configuration.getChunkSize() - configuration.getOverlap();
            }

            OllamaEmbedResponseModel embeddingResponse = ollamaAPI.embed(configuration.getEmbeddingModel(), chunks);
            List<List<Double>> embeddings = embeddingResponse.getEmbeddings();

            List<TextEmbedding> textEmbeddings = new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {
                textEmbeddings.add(new TextEmbedding(chunks.get(i), embeddings.get(i), new Date(), new Date()));
            }

            logger.info("Embeddings generated successfully for all chunks.");
            return textEmbeddings;

        } catch (Exception e) {
            throw new EmbeddingGenerationException("Failed to generate embeddings", e);
        }
    }

    public void saveEmbeddings(List<TextEmbedding> embeddings) {
        textEmbeddingDao.addEmbedding(embeddings);
    }

    public List<TextEmbedding> findSimilarEmbeddings(TextEmbedding sourceEmbedding, int limit) {
        return textEmbeddingDao.findEmbeddingsNear(sourceEmbedding, limit);
    }
}
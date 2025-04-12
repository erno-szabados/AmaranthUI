package com.esgdev.amaranthui.engine;

import com.esgdev.amaranthui.db.TextEmbedding;
import com.esgdev.amaranthui.db.TextEmbeddingDao;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class EmbeddingManager {

    private static final Logger logger = Logger.getLogger(EmbeddingManager.class.getName());

    private final TextEmbeddingDao textEmbeddingDao;
    private final OllamaAPI ollamaAPI;
    private final String embeddingModel;
    private int contextLimit = 8192;
    private static final int CHUNK_SIZE = 512;
    private static final int OVERLAP = 50;

    public EmbeddingManager(TextEmbeddingDao textEmbeddingDao, OllamaAPI ollamaAPI, String embeddingModel) {
        this.textEmbeddingDao = textEmbeddingDao;
        this.ollamaAPI = ollamaAPI;
        this.embeddingModel = embeddingModel;
    }

    public void setContextLimit(int contextLimit) {
        this.contextLimit = contextLimit;
    }

    public int getContextLimit() {
        return contextLimit;
    }

    public void saveEmbeddings(String text, List<TextEmbedding> embeddings) {
        if (embeddings == null || embeddings.isEmpty()) {
            throw new IllegalArgumentException("Embeddings cannot be null or empty.");
        }

        textEmbeddingDao.addEmbedding(embeddings);
    }

    public List<TextEmbedding> generateEmbeddings(String text) throws Exception {
        logger.info("Generating embeddings for text: " + text);

        if (CHUNK_SIZE <= OVERLAP) {
            throw new RuntimeException("CHUNK_SIZE must be greater than OVERLAP");
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end));
            start += CHUNK_SIZE - OVERLAP;
        }

        OllamaEmbedResponseModel embeddingResponse = ollamaAPI.embed(embeddingModel, chunks);
        List<List<Double>> embeddings = embeddingResponse.getEmbeddings();

        List<TextEmbedding> textEmbeddings = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            textEmbeddings.add(new TextEmbedding(chunks.get(i), embeddings.get(i), new Date(), new Date()));
        }

        logger.info("Embeddings generated successfully for all chunks.");
        return textEmbeddings;
    }

    public List<TextEmbedding> findSimilarEmbeddings(TextEmbedding sourceEmbedding, int limit) {
        return textEmbeddingDao.findEmbeddingsNear(sourceEmbedding, limit);
    }
}
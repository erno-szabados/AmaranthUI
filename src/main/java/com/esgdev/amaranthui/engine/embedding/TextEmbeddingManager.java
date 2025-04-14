package com.esgdev.amaranthui.engine.embedding;

import com.esgdev.amaranthui.db.EmbeddingDao;
import io.github.ollama4j.OllamaAPI;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TextEmbeddingManager handles text embeddings.
 */
public class TextEmbeddingManager extends BaseEmbeddingManager<TextEmbedding, String> {

    public TextEmbeddingManager(EmbeddingDao<TextEmbedding> textEmbeddingDao, OllamaAPI ollamaAPI, EmbeddingConfiguration configuration) {
        super(textEmbeddingDao, ollamaAPI, configuration);
    }

    @Override
    public List<TextEmbedding> generateEmbeddings(String text) throws EmbeddingGenerationException {
        logger.info("Generating embeddings for text: " + text);

        try {
            List<String> chunks = splitTextIntoChunks(text);
            List<List<Double>> embeddings = generateEmbeddingsFromChunks(chunks);

            List<TextEmbedding> textEmbeddings = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                textEmbeddings.add(createEmbedding(chunks.get(i), embeddings.get(i), new Date(), new Date(), text));
            }

            logger.info("Embeddings generated successfully for all chunks.");
            return textEmbeddings;

        } catch (Exception e) {
            throw new EmbeddingGenerationException("Failed to generate embeddings", e);
        }
    }

    @Override
    protected TextEmbedding createEmbedding(String chunk, List<Double> embedding, Date creationDate, Date lastAccessed, String source) {
        return new TextEmbedding(chunk, embedding, creationDate, lastAccessed, configuration.getEmbeddingModel(), 0.0);
    }
}
package com.esgdev.amaranthui.engine.embedding;

import java.util.List;

/**
 * Interface for managing embeddings.
 * @param <T> The type of the embedding object.
 * @param <V> The type of the source object used to generate embeddings.
 */
public interface EmbeddingManagerInterface<T, V> {
    List<T> generateEmbeddings(V source) throws EmbeddingGenerationException;

    void saveEmbeddings(List<T> embeddings);

    List<T> findSimilarEmbeddings(T sourceEmbedding, int limit);
}
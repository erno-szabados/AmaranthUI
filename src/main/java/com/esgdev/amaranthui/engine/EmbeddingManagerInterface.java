package com.esgdev.amaranthui.engine;

import java.util.List;

public interface EmbeddingManagerInterface<T, V> {
    List<T> generateEmbeddings(V source) throws EmbeddingGenerationException;

    void saveEmbeddings(List<T> embeddings);

    List<T> findSimilarEmbeddings(T sourceEmbedding, int limit);
}
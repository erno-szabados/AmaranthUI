package com.esgdev.amaranthui.db;

import java.util.List;

public interface EmbeddingDao<T> {
    T getEmbeddingById(Long id);
    List<T> getAllEmbeddings();
    void addEmbedding(T embedding);
    void addEmbedding(List<T> embeddings); // New overload
    void updateEmbedding(T embedding);
    void deleteEmbedding(Long id);
    public List<T> findEmbeddingsNear(T sourceEmbedding, int limit);
}
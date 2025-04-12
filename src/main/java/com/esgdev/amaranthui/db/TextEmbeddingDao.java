package com.esgdev.amaranthui.db;

import java.util.List;

public interface TextEmbeddingDao {
    TextEmbedding getEmbeddingById(long id);
    List<TextEmbedding> getAllEmbeddings();
    void addEmbedding(TextEmbedding embedding);
    void addEmbedding(List<TextEmbedding> embeddings); // New overload
    void updateEmbedding(TextEmbedding embedding);
    void deleteEmbedding(long id);
    public List<TextEmbedding> findEmbeddingsNear(TextEmbedding sourceEmbedding, int limit);
}
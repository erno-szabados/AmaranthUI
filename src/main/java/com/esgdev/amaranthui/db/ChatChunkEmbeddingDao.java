package com.esgdev.amaranthui.db;

import java.util.List;

public interface ChatChunkEmbeddingDao {
    Long insertEmbedding(ChatChunkEmbedding embedding);
    ChatChunkEmbedding getEmbeddingById(Long id);
    List<ChatChunkEmbedding> getEmbeddingsByConversationId(Long conversationId);
    List<ChatChunkEmbedding> getEmbeddingsByUserId(Long userId);
    void updateEmbedding(ChatChunkEmbedding embedding);
    void deleteEmbedding(Long id);
    List<ChatChunkEmbedding> findEmbeddingsNear(ChatChunkEmbedding sourceEmbedding, int limit);

    List<ChatChunkEmbedding> getAllEmbeddings();
    // Add other methods as needed (e.g., retrieval by role, etc.)
}
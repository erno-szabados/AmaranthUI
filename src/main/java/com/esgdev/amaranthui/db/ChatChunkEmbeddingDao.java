package com.esgdev.amaranthui.db;

import java.util.List;

public interface ChatChunkEmbeddingDao {
    Long insertChatChunkEmbedding(ChatChunkEmbedding embedding);
    ChatChunkEmbedding getChatChunkEmbeddingById(Long id);
    List<ChatChunkEmbedding> getChatChunkEmbeddingsByConversationId(Long conversationId);
    List<ChatChunkEmbedding> getChatChunkEmbeddingsByUserId(Long userId);
    void updateChatChunkEmbedding(ChatChunkEmbedding embedding);
    void deleteChatChunkEmbedding(Long id);
    List<ChatChunkEmbedding> findEmbeddingsNear(ChatChunkEmbedding sourceEmbedding, int limit);

    List<ChatChunkEmbedding> getAllEmbeddings();
    // Add other methods as needed (e.g., retrieval by role, etc.)
}
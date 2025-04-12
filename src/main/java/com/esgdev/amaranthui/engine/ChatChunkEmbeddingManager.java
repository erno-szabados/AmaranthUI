package com.esgdev.amaranthui.engine;

import com.esgdev.amaranthui.db.ChatChunkEmbedding;
import com.esgdev.amaranthui.db.ChatChunkEmbeddingDao;

import java.util.List;

public class ChatChunkEmbeddingManager implements EmbeddingManagerInterface<ChatChunkEmbedding, ChatEntry> {
    private final ChatChunkEmbeddingDao chatChunkEmbeddingDao;

    public ChatChunkEmbeddingManager(ChatChunkEmbeddingDao chatChunkEmbeddingDao) {
        this.chatChunkEmbeddingDao = chatChunkEmbeddingDao;
    }

  @Override
  public List<ChatChunkEmbedding> generateEmbeddings(ChatEntry chatEntry) throws EmbeddingGenerationException {
      // Use the data from ChatEntry to generate embeddings
      String text = chatEntry.getChunk();
      // Implement embedding generation logic here
      return null;
  }

    @Override
    public void saveEmbeddings(List<ChatChunkEmbedding> embeddings) {
        // Save chat embeddings
        for (ChatChunkEmbedding embedding : embeddings) {
            chatChunkEmbeddingDao.insertChatChunkEmbedding(embedding);
        }
    }

    @Override
    public List<ChatChunkEmbedding> findSimilarEmbeddings(ChatChunkEmbedding sourceEmbedding, int limit) {
        // Implement similarity search for chat embeddings
        return chatChunkEmbeddingDao.findEmbeddingsNear(sourceEmbedding, limit);
    }

    public ChatChunkEmbeddingDao getChatChunkEmbeddingDao() {
        return chatChunkEmbeddingDao;
    }
}
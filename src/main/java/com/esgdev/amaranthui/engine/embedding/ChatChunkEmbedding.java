package com.esgdev.amaranthui.engine.embedding;

import java.util.Date;
import java.util.List;

/**
 * Represents a chat chunk embedding, which includes metadata about the conversation and user.
 * This class extends TextEmbedding to include additional fields specific to chat interactions.
 */
public class ChatChunkEmbedding extends TextEmbedding {
    private Long conversationId;
    private Long userId;
    private String role; // 'user' or 'model'
    private Long replyToChunkId; // ID of the chunk this chunk is a response to
    private String topic;

    // Constructors, getters, setters
    public ChatChunkEmbedding() {
        super(); // Call the superclass constructor
    }

    /**
     * Constructor for ChatChunkEmbedding with additional fields.
     *
     * @param chunk          The text chunk.
     * @param embedding      The embedding vector.
     * @param creationDate   The date of creation.
     * @param lastAccessed   The date of last access.
     * @param conversationId The ID of the conversation this chunk belongs to.
     * @param userId         The ID of the user who created this chunk.
     * @param role           The role of the user ('user' or 'model').
     * @param replyToChunkId The ID of the chunk this chunk is a response to.
     * @param topic          The topic of the conversation.
     * @param embeddingModel The model used for embedding.
     * @param similarity     The similarity score.
     */
    public ChatChunkEmbedding(String chunk, List<Double> embedding, Date creationDate, Date lastAccessed,
                              Long conversationId, Long userId, String role, Long replyToChunkId, String topic,
                              String embeddingModel, double similarity) {
        super(chunk, embedding, creationDate, lastAccessed, embeddingModel, similarity); // Pass new fields to superclass
        this.conversationId = conversationId;
        this.userId = userId;
        this.role = role;
        this.topic = topic;
        this.replyToChunkId = replyToChunkId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getReplyToChunkId() {
        return replyToChunkId;
    }

    public void setReplyToChunkId(Long replyToChunkId) {
        this.replyToChunkId = replyToChunkId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
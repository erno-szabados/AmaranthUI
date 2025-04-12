package com.esgdev.amaranthui.db;

import java.util.Date;
import java.util.List;

public class ChatChunkEmbedding extends TextEmbedding {
    private Long conversationId;
    private Long userId;
    private String role; // 'user' or 'model'
    private Long replyToChunkId; // ID of the chunk this chunk is a response to

    // Constructors, getters, setters
    public ChatChunkEmbedding() {
        super(); // Call the superclass constructor
    }

    /**
     * Constructor for ChatChunkEmbedding.
     *
     * @param chunk          The text chunk.
     * @param embedding      The embedding vector.
     * @param creationDate   The date of creation.
     * @param lastAccessed   The date of last access.
     * @param conversationId The ID of the conversation this chunk belongs to.
     * @param userId        The ID of the user who created this chunk.
     * @param role          The role of the user ('user' or 'model').
     * @param replyToChunkId The ID of the chunk this chunk is a response to.
     */
    public ChatChunkEmbedding(String chunk, List<Double> embedding, Date creationDate, Date lastAccessed,
                              Long conversationId, Long userId, String role, Long replyToChunkId) {
        super(chunk, embedding, creationDate, lastAccessed); // Call the superclass constructor
        this.conversationId = conversationId;
        this.userId = userId;
        this.role = role;
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
}
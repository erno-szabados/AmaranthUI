package com.esgdev.amaranthui.engine;

import java.util.Date;

/**
 * Represents a chat entry in a conversation.
 * Each entry can be a user message or a model response.
 */
public class ChatEntry {
    private String chunk;
    private Long conversationId;
    private Long userId;
    private String role; // 'user' or 'model'
    private String topic;
    private Long replyToChunkId;
    private Date creationDate;

    // Constructors, getters, setters
    public ChatEntry(String chunk, Long conversationId, Long userId, String role, String topic, Long replyToChunkId, Date creationDate) {
        this.chunk = chunk;
        this.conversationId = conversationId;
        this.userId = userId;
        this.role = role;
        this.topic = topic;
        this.replyToChunkId = replyToChunkId;
        this.creationDate = creationDate;
    }

    public String getChunk() {
        return chunk;
    }

    public void setChunk(String chunk) {
        this.chunk = chunk;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
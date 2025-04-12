package com.esgdev.amaranthui.db;

import java.util.Date;
import java.util.List;

public class TextEmbedding {
    private Long id; // Primary key
    private String chunk;
    private List<Double> embedding;
    private Date creationDate;
    private Date lastAccessed;

    // Constructors, getters, setters
    public TextEmbedding() {
    }

    public TextEmbedding(String chunk, List<Double> embedding, Date creationDate, Date lastAccessed) {
        this.chunk = chunk;
        this.embedding = embedding;
        this.creationDate = creationDate;
        this.lastAccessed = lastAccessed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChunk() {
        return chunk;
    }

    public void setChunk(String chunk) {
        this.chunk = chunk;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Date lastAccessed) {
        this.lastAccessed = lastAccessed;
    }
}

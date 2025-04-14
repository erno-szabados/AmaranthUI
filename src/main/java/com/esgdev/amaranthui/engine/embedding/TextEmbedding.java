package com.esgdev.amaranthui.engine.embedding;

import java.util.Date;
import java.util.List;

/**
 * Represents a text embedding.
 * An embedding is a numerical representation of a text chunk, useful for various NLP tasks, like semantic search, and similarity.
 */
public class TextEmbedding {
    private Long id; // Primary key
    private String chunk;
    private List<Double> embedding;
    private Date creationDate;
    private Date lastAccessed;
    private String embeddingModel; // Metadata: embedding model name
    private double similarity; // Metadata: similarity score

    // Constructors, getters, setters
    public TextEmbedding() {
    }

    public TextEmbedding(String chunk, List<Double> embedding, Date creationDate, Date lastAccessed, String embeddingModel, double similarity) {
        this.chunk = chunk;
        this.embedding = embedding;
        this.creationDate = creationDate;
        this.lastAccessed = lastAccessed;
        this.embeddingModel = embeddingModel;
        this.similarity = similarity;
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

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }
}
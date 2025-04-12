package com.esgdev.amaranthui.engine;

/**
 * Configuration class for embedding-related settings.
 */
public class EmbeddingConfiguration {
    private final int chunkSize;
    private final int overlap;
    private final String embeddingModel;
    private final String jdbcUrl;
    private final String jdbcUser;
    private final String jdbcPassword;

    public EmbeddingConfiguration(int chunkSize, int overlap, String embeddingModel, String jdbcUrl, String jdbcUser, String jdbcPassword) {
        if (chunkSize <= overlap) {
            throw new IllegalArgumentException("CHUNK_SIZE must be greater than OVERLAP.");
        }
        this.chunkSize = chunkSize;
        this.overlap = overlap;
        this.embeddingModel = embeddingModel;
        this.jdbcUrl = jdbcUrl;
        this.jdbcUser = jdbcUser;
        this.jdbcPassword = jdbcPassword;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getOverlap() {
        return overlap;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getJdbcUser() {
        return jdbcUser;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }
}
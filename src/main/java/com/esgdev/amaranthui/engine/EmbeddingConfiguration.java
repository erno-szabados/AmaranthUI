package com.esgdev.amaranthui.engine;

/**
 * Configuration class for embedding-related settings.
 */
public class EmbeddingConfiguration {
    private final int chunkSize;
    private final int overlap;

    public EmbeddingConfiguration(int chunkSize, int overlap) {
        if (chunkSize <= overlap) {
            throw new IllegalArgumentException("CHUNK_SIZE must be greater than OVERLAP.");
        }
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getOverlap() {
        return overlap;
    }
}
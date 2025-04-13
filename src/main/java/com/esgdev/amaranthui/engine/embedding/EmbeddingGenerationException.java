package com.esgdev.amaranthui.engine.embedding;

/**
 * Custom exception for errors during embedding generation.
 */
public class EmbeddingGenerationException extends Exception {
    public EmbeddingGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
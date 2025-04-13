package com.esgdev.amaranthui.db.h2;

/**
 * Utility class for calculating vector similarity metrics.
 */
public class VectorSimilarity {

    /**
     * Calculates the cosine similarity between two vectors.
     *
     * @param vectorA First vector
     * @param vectorB Second vector
     * @return Cosine similarity value
     */
    public static Double cosineSimilarity(Double[] vectorA, Double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Calculates the Euclidean distance between two vectors.
     *
     * @param vectorA First vector
     * @param vectorB Second vector
     * @return Euclidean distance value
     */
    public static Double euclideanDistance(Double[] vectorA, Double[] vectorB) {
        double sum = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            sum += Math.pow(vectorA[i] - vectorB[i], 2);
        }
        return Math.sqrt(sum);
    }
}
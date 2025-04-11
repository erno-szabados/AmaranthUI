package com.esgdev.amethystui.h2;

import org.junit.Assert;
import org.junit.Test;

public class VectorSimilarityTest {

    @Test
    public void testCosineSimilarity() {
        double[] vectorA = {1.0f, 2.0f, 3.0f};
        double[] vectorB = {4.0f, 5.0f, 6.0f};

        double result = VectorSimilarity.cosineSimilarity(vectorA, vectorB);

        // Expected value calculated manually
        double expected = 0.9746318461970762;
        Assert.assertEquals(expected, result, 1e-6);
    }

    @Test
    public void testCosineSimilarityWithZeroVector() {
        double[] vectorA = {0.0f, 0.0f, 0.0f};
        double[] vectorB = {1.0f, 2.0f, 3.0f};

        double result = VectorSimilarity.cosineSimilarity(vectorA, vectorB);

        // Cosine similarity with a zero vector should result in NaN
        Assert.assertTrue(Double.isNaN(result));
    }

    @Test
    public void testEuclideanDistance() {
        double[] vectorA = {1.0f, 2.0f, 3.0f};
        double[] vectorB = {4.0f, 5.0f, 6.0f};

        double result = VectorSimilarity.euclideanDistance(vectorA, vectorB);

        // Expected value calculated manually
        double expected = 5.196152422706632;
        Assert.assertEquals(expected, result, 1e-6);
    }

    @Test
    public void testEuclideanDistanceWithSameVectors() {
        double[] vectorA = {1.0f, 2.0f, 3.0f};
        double[] vectorB = {1.0f, 2.0f, 3.0f};

        double result = VectorSimilarity.euclideanDistance(vectorA, vectorB);

        // Distance between identical vectors should be 0
        Assert.assertEquals(0.0, result, 1e-6);
    }
}
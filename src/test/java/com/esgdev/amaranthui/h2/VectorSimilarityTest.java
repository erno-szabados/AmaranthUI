package com.esgdev.amaranthui.h2;

import org.junit.Assert;
import org.junit.Test;

public class VectorSimilarityTest {

    @Test
    public void testCosineSimilarity() {
        Double[] vectorA = {1.0, 2.0, 3.0};
        Double[] vectorB = {4.0, 5.0, 6.0};

        double result = VectorSimilarity.cosineSimilarity(vectorA, vectorB);

        // Expected value calculated manually
        double expected = 0.9746318461970762;
        Assert.assertEquals(expected, result, 1e-6);
    }

    @Test
    public void testCosineSimilarityWithZeroVector() {
        Double[] vectorA = {0.0, 0.0, 0.0};
        Double[] vectorB = {1.0, 2.0, 3.0};

        double result = VectorSimilarity.cosineSimilarity(vectorA, vectorB);

        // Cosine similarity with a zero vector should result in NaN
        Assert.assertTrue(Double.isNaN(result));
    }

    @Test
    public void testEuclideanDistance() {
        Double[] vectorA = {1.0, 2.0, 3.0};
        Double[] vectorB = {4.0, 5.0, 6.0};

        double result = VectorSimilarity.euclideanDistance(vectorA, vectorB);

        // Expected value calculated manually
        double expected = 5.196152422706632;
        Assert.assertEquals(expected, result, 1e-6);
    }

    @Test
    public void testEuclideanDistanceWithSameVectors() {
        Double[] vectorA = {1.0, 2.0, 3.0};
        Double[] vectorB = {1.0, 2.0, 3.0};

        double result = VectorSimilarity.euclideanDistance(vectorA, vectorB);

        // Distance between identical vectors should be 0
        Assert.assertEquals(0.0, result, 1e-6);
    }
}
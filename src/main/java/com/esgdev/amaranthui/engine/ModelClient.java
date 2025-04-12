package com.esgdev.amaranthui.engine;

import com.esgdev.amaranthui.DependencyFactory;
import com.esgdev.amaranthui.db.TextEmbedding;

import java.util.List;
import java.util.logging.Logger;

public class ModelClient {
    Logger logger = Logger.getLogger(ModelClient.class.getName());
    private final EmbeddingManager embeddingManager;
    public ModelClient() {
        embeddingManager = DependencyFactory.createEmbeddingManager();
        try {
            List<TextEmbedding> embeddings = embeddingManager.generateEmbeddings("Hello, world!");

            embeddingManager.saveEmbeddings(embeddings);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Constructor logic here

    }

}

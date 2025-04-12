package com.esgdev.amethystui.engine;

import com.esgdev.amethystui.DependencyFactory;
import com.esgdev.amethystui.db.TextEmbedding;

import java.util.List;
import java.util.logging.Logger;

public class ModelClient {
    Logger logger = Logger.getLogger(ModelClient.class.getName());
    private final EmbeddingManager embeddingManager;
    public ModelClient() {
        embeddingManager = DependencyFactory.createEmbeddingManager();
        try {
            List<TextEmbedding> embeddings = embeddingManager.generateEmbeddings("Hello, world!");

            embeddingManager.saveEmbeddings("Hello, world!", embeddings);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Constructor logic here

    }

}

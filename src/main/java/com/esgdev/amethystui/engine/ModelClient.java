package com.esgdev.amethystui.engine;

import com.esgdev.amethystui.DependencyFactory;

public class ModelClient {
    private final EmbeddingManager embeddingManager;
    public ModelClient() {
        embeddingManager = DependencyFactory.createEmbeddingManager();
        // Constructor logic here

    }

}

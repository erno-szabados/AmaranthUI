package com.esgdev.amaranthui.engine.tagging;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.esgdev.amaranthui.db.EmbeddingDao;
import com.esgdev.amaranthui.engine.embedding.ChatChunkEmbedding;
import com.esgdev.amaranthui.engine.embedding.TextEmbedding;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.utils.OptionsBuilder;

public class TopicAnalyst {
    private final OllamaAPI ollamaAPI;
    private final List<String> topics;
    private final EmbeddingDao<TextEmbedding> textEmbeddingDao;
    private final EmbeddingDao<ChatChunkEmbedding> chatChunkEmbeddingDao;
    private final TopicConfiguration topicConfiguration;
    private float temperature = 0.1f;
    private int topK = 5;
    private float topP = 0.9f;

    private static Logger logger = Logger.getLogger(TopicAnalyst.class.getName());

    private String prompt = """
            You are a topic analyst. Classify the text into one of the following topics: %s. Respond with a single word.
            Text: %s
            Topic:
            """;

    public TopicAnalyst(OllamaAPI ollamaAPI,
                        TopicConfiguration configuration,
                        EmbeddingDao<TextEmbedding> textEmbeddingDao,
                        EmbeddingDao<ChatChunkEmbedding> chatChunkEmbeddingDao,
                        List<String> topics) {
        this.ollamaAPI = ollamaAPI;
        this.topicConfiguration = configuration;
        this.textEmbeddingDao = textEmbeddingDao;
        this.chatChunkEmbeddingDao = chatChunkEmbeddingDao;
        this.topics = topics;
        this.temperature = configuration.getTemperature();
        this.topK = configuration.getTopK();
        this.topP = configuration.getTopP();
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public void setTopics(List<String> topics) {
        this.topics.clear();
        this.topics.addAll(topics);
    }

    public void setSamplingParams(float temperature, int topK, float topP) {
        // Set sampling parameters for the Ollama API
        this.temperature = temperature;
        this.topK = topK;
        this.topP = topP;
    }

    public String classify(String text) throws OllamaBaseException, IOException, InterruptedException {
        OptionsBuilder builder = new OptionsBuilder();
        builder.setTemperature(temperature);
        builder.setTopK(topK);
        builder.setTopP(topP);
        // Longest entry from topic list and a safety margin
        builder.setNumPredict(topics.stream().mapToInt(String::length).max().orElse(0) + 10);

        String request = String.format(prompt, String.join(", ", topics), text);
        OllamaResult result = ollamaAPI.generate(topicConfiguration.getTaggingModel(), request, false, builder.build());

        logger.info("Topic Request: " + request);
        logger.info("Topic Response: " + result.getResponse());

        // Sanitize the response: remove leading and trailing whitespace, and ensure it's a single word from the topic list
        // remove all non-alphanumeric characters and convert to lowercase
        if (result.getHttpStatusCode() == 200) {
            String response = result.getResponse().trim();
            // Remove all non-alphanumeric characters, convert to lowercase, and trim whitespace
            String sanitizedResponse = response.replaceAll("[^a-zA-Z0-9]", "").toLowerCase().trim();
            if (topics.contains(sanitizedResponse)) {
                return sanitizedResponse;
            } else {
                return "error";
            }
        } else {
            throw new IOException("Error: " + result.getHttpStatusCode() + " - " + result.getResponse());
        }
    }

    public void classifyAndSave(String text) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void classifyAndSave(ChatChunkEmbedding chatChunkEmbedding) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

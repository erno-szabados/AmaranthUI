package com.esgdev.amaranthui.engine.tagging;

/**
 * Configuration class for topic-related settings.
 */
public class TopicConfiguration {
    private String taggingModel;
    private float temperature;
    private float topP;
    private int topK;

    public TopicConfiguration(String taggingModel, float temperature, float topP, int topK) {
        this.taggingModel = taggingModel;
        this.temperature = temperature;
        this.topP = topP;
        this.topK = topK;
    }

    public String getTaggingModel() {
        return taggingModel;
    }

    public void setTaggingModel(String taggingModel) {
        this.taggingModel = taggingModel;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getTopP() {
        return topP;
    }

    public void setTopP(float topP) {
        this.topP = topP;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }
}
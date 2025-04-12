package com.esgdev.amaranthui.engine;

public class ChatConfiguration {
    private int chatHistorySize;
    private String chatModel;

    public ChatConfiguration(int chatHistorySize, String chatModel) {
        this.chatHistorySize = chatHistorySize;
        this.chatModel =  chatModel;
    }

    public int getChatHistorySize() {
        return chatHistorySize;
    }

    public void setChatHistorySize(int chatHistorySize) {
        this.chatHistorySize = chatHistorySize;
    }

    public String getChatModel() {
        return chatModel;
    }

    public void setChatModel(String chatModel) {
        this.chatModel = chatModel;
    }
}

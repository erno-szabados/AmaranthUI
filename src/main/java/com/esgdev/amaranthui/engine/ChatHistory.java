package com.esgdev.amaranthui.engine;

import java.util.*;

public class ChatHistory {
    private final Deque<ChatEntry> chatEntries;
    private final int maxSize;
    private final List<ChatHistoryObserver> observers = new ArrayList<>();

    public ChatHistory(int maxSize) {
        this.maxSize = maxSize;
        this.chatEntries = new ArrayDeque<>(maxSize);
    }

    public synchronized void addChatEntry(ChatEntry chatEntry) {
        if (chatEntries.size() >= maxSize) {
            chatEntries.pollFirst();
        }
        chatEntries.addLast(chatEntry);
        notifyObservers();
    }

    public synchronized List<ChatEntry> getChatHistory() {
        return new ArrayList<>(chatEntries);
    }

    public synchronized void clear() {
        chatEntries.clear();
        notifyObservers();
    }

    public void addObserver(ChatHistoryObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ChatHistoryObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (ChatHistoryObserver observer : observers) {
            observer.onChatHistoryUpdated(new ArrayList<>(chatEntries));
        }
    }

    public interface ChatHistoryObserver {
        void onChatHistoryUpdated(List<ChatEntry> updatedChatEntries);
    }
}
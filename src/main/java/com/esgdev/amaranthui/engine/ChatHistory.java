package com.esgdev.amaranthui.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

/**
 * ChatHistory maintains a history of chat entries with a maximum size.
 * When the maximum size is reached, the oldest entry is removed.
 */
public class ChatHistory {
    private final Deque<ChatEntry> chatEntries;
    private final int maxSize;

    public ChatHistory(int maxSize) {
        this.maxSize = maxSize;
        this.chatEntries = new ArrayDeque<>(maxSize);
    }

    /**
     * Adds a new chat entry to the history.
     * If the history is full, the oldest entry is removed.
     * @param chatEntry The chat entry to add.
     */
    public synchronized void addChatEntry(ChatEntry chatEntry) {
        if (chatEntries.size() >= maxSize) {
            chatEntries.pollFirst(); // Remove the oldest entry
        }
        chatEntries.addLast(chatEntry); // Add the new entry
    }

    /**
     * Retrieves the chat history as a list.
     * @return A list of chat entries.
     */
    public synchronized List<ChatEntry> getChatHistory() {
        return new ArrayList<>(chatEntries);
    }

    /**
     * Retrieves the chat history as a stream.
     * @return A stream of chat entries.
     */
    public synchronized Stream<ChatEntry> stream() {
        return chatEntries.stream();
    }

    public synchronized int size() {
        return chatEntries.size();
    }
}

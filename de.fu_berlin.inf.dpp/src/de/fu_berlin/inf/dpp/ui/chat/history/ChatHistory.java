package de.fu_berlin.inf.dpp.ui.chat.history;

import java.util.LinkedList;
import java.util.List;

/**
 * This class describes the history of incoming messages of a chat. It saves
 * entries in the form of {@link ChatHistoryEntry} instances.
 */
public class ChatHistory {
    protected List<ChatHistoryEntry> history = new LinkedList<ChatHistoryEntry>();

    /**
     * Constructs a new instance
     */
    public ChatHistory() {
        // nothing to do
    }

    /**
     * Adds a new {@link ChatHistoryEntry}
     * 
     * @param entry
     *            that describes the chat message
     */
    public void addEntry(ChatHistoryEntry entry) {
        this.history.add(entry);
    }

    /**
     * Returns all added {@link ChatHistoryEntry}s
     * 
     * @return
     */
    public ChatHistoryEntry[] getEntries() {
        return this.history.toArray(new ChatHistoryEntry[0]);
    }

    /**
     * Removes all added {@link ChatHistoryEntry}s from the {@link ChatHistory}
     */
    public void clear() {
        this.history.clear();
    }
}

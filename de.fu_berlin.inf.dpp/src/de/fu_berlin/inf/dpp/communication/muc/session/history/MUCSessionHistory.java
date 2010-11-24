package de.fu_berlin.inf.dpp.communication.muc.session.history;

import java.util.LinkedList;
import java.util.List;

import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryElement;

/**
 * This class describes the history of incoming messages of a chat. It saves
 * entries in the form of {@link MUCSessionHistoryElement} instances.
 */
public class MUCSessionHistory {
    protected List<MUCSessionHistoryElement> history = new LinkedList<MUCSessionHistoryElement>();

    /**
     * Constructs a new instance
     */
    public MUCSessionHistory() {
        // nothing to do
    }

    /**
     * Adds a new {@link MUCSessionHistoryElement}
     * 
     * @param entry
     *            that describes the chat message
     */
    public void addEntry(MUCSessionHistoryElement entry) {
        this.history.add(entry);
    }

    /**
     * Returns all added {@link MUCSessionHistoryElement}s
     * 
     * @return
     */
    public MUCSessionHistoryElement[] getEntries() {
        return this.history.toArray(new MUCSessionHistoryElement[0]);
    }

    /**
     * Removes all added {@link MUCSessionHistoryElement}s from the
     * {@link MUCSessionHistory}
     */
    public void clear() {
        this.history.clear();
    }
}

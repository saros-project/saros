package de.fu_berlin.inf.dpp.ui.chat.history;

import java.util.Date;

import de.fu_berlin.inf.dpp.User;

/**
 * This class describes an entry a {@link ChatHistory}
 */
public class ChatHistoryEntry {
    protected User sender;
    protected String message;
    protected Date receivedOn;

    public ChatHistoryEntry(User sender, String message, Date receivedOn) {
        super();
        this.sender = sender;
        this.message = message;
        this.receivedOn = receivedOn;
    }

    public User getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Date getReceivedOn() {
        return receivedOn;
    }
}

package de.fu_berlin.inf.dpp.communication.muc.session.history.elements;

import java.util.Date;

import org.jivesoftware.smackx.ChatState;

import de.fu_berlin.inf.dpp.net.JID;

public class MUCSessionHistoryStateChangeElement extends MUCSessionHistoryElement {
    protected ChatState state;

    public MUCSessionHistoryStateChangeElement(JID jid, Date date, ChatState state) {
        super(jid, date);
        this.state = state;
    }

    public ChatState getState() {
        return state;
    }
}

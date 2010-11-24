package de.fu_berlin.inf.dpp.communication.muc.session.history.elements;

import java.util.Date;

import de.fu_berlin.inf.dpp.net.JID;

public class MUCSessionHistoryMessageReceptionElement extends MUCSessionHistoryElement {
    protected String message;

    public MUCSessionHistoryMessageReceptionElement(JID jid, Date date, String message) {
        super(jid, date);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}

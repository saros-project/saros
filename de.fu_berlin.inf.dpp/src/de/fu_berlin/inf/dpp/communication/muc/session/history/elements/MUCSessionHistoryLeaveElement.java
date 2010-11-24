package de.fu_berlin.inf.dpp.communication.muc.session.history.elements;

import java.util.Date;

import de.fu_berlin.inf.dpp.net.JID;

public class MUCSessionHistoryLeaveElement extends MUCSessionHistoryElement {

    public MUCSessionHistoryLeaveElement(JID jid, Date date) {
        super(jid, date);
    }

}

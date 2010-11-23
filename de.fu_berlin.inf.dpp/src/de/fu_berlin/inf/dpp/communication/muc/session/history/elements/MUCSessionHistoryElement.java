package de.fu_berlin.inf.dpp.communication.muc.session.history.elements;

import java.util.Date;

import de.fu_berlin.inf.dpp.communication.muc.session.history.MUCSessionHistory;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * This class describes an entry a {@link MUCSessionHistory}
 */
public abstract class MUCSessionHistoryElement {
    protected JID jid;
    protected Date date;

    public MUCSessionHistoryElement(JID jid, Date date) {
        super();
        this.jid = jid;
        this.date = date;
    }

    public JID getSender() {
        return jid;
    }

    public Date getDate() {
        return date;
    }
}

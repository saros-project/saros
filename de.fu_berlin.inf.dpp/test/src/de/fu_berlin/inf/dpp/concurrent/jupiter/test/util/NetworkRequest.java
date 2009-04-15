package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.net.JID;

public class NetworkRequest {

    private JID from;

    private JID to;

    private Request request;

    public NetworkRequest(JID from, JID to, Request req) {
        this.from = from;
        this.to = to;
        /* adaption to new request format. */
        if (req.getJID() == null) {
            this.request = new Request(req.getSiteId(), req.getTimestamp(), req
                .getOperation(), from, req.getEditorPath());
        } else {
            this.request = req;
        }
    }

    public JID getFrom() {
        return from;
    }

    public JID getTo() {
        return to;
    }

    public Request getRequest() {
        return request;
    }
}

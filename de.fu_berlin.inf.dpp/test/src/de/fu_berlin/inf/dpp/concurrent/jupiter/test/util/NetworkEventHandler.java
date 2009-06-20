package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * handler for simulated network events.
 * 
 * @author troll
 * 
 */
public interface NetworkEventHandler {

    /**
     * receive a remote document request.
     * 
     * @param req
     */
    public void receiveNetworkEvent(NetworkRequest req);

    /**
     * network Jabber id of appropriate client.
     */
    public JID getJID();
}

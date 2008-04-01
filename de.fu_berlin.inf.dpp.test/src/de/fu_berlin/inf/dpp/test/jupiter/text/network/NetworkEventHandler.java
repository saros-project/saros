package de.fu_berlin.inf.dpp.test.jupiter.text.network;

import de.fu_berlin.inf.dpp.jupiter.Request;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * handler for simulated network events.
 * @author troll
 *
 */
public interface NetworkEventHandler {

	/**
	 * receive a remote document request.
	 * @param req
	 */
	public void receiveNetworkEvent(Request req);
	
	/**
	 * network Jabber id of appropriate client. 
	 */
	public JID getJID();
}

package de.fu_berlin.inf.dpp.test.jupiter.text.network;

import de.fu_berlin.inf.dpp.jupiter.Request;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.NetworkRequest;

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
	 * receive a remote document request.
	 * @param req
	 */
	public void receiveNetworkEvent(NetworkRequest req);
	
	
	
	/**
	 * network Jabber id of appropriate client. 
	 */
	public JID getJID();
}

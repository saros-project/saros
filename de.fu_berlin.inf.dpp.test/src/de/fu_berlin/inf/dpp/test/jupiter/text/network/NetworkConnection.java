package de.fu_berlin.inf.dpp.test.jupiter.text.network;

import de.fu_berlin.inf.dpp.jupiter.Request;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * interface for simulated network.
 * @author troll
 *
 */

public interface NetworkConnection {
	
	/**
	 * send operation over the network with delay.
	 * @param jid
	 * @param req
	 * @param delay in millis
	 */
	public void sendOperation(JID jid, Request req, int delay);
	
	public void addClient(NetworkEventHandler remote);
	
	public void removeClient(NetworkEventHandler remote);
}

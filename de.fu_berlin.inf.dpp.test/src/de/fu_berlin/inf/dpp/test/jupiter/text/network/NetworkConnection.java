package de.fu_berlin.inf.dpp.test.jupiter.text.network;

import de.fu_berlin.inf.dpp.jupiter.Request;
import de.fu_berlin.inf.dpp.net.JID;

public interface NetworkConnection {

	/**
	 * send operation over network.
	 * @param jid
	 * @param req
	 */
	public void sendOperation(JID jid, Request req);
	
	public void addClient(NetworkEventHandler remote);
	
	public void removeClient(NetworkEventHandler remote);
}

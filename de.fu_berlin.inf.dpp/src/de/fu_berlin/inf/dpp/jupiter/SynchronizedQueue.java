package de.fu_berlin.inf.dpp.jupiter;

import de.fu_berlin.inf.dpp.net.JID;

public interface SynchronizedQueue {

	public JID getJID();
	
	public void sendOperation(Operation op);

	public Operation receiveOperation(Request req);
	
	/**
	 * send a transformed operation to client side.
	 * @param op operation has transformed and only send to
	 * client side.
	 */
	public void sendTransformedOperation(Operation op, JID toJID);
	
	public Algorithm getAlgorithm();
}

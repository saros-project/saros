package de.fu_berlin.inf.dpp.jupiter;

import de.fu_berlin.inf.dpp.net.JID;

public interface SynchronizedQueue {

	public JID getJID();
	
	public void sendOperation(Operation op);

	public Operation receiveOperation();
}

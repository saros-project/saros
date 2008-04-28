package de.fu_berlin.inf.dpp.concurrent.jupiter;

import de.fu_berlin.inf.dpp.net.JID;

public interface JupiterClient extends JupiterEditor{

	public Request generateRequest(Operation op);
	
	public Operation receiveRequest(Request req) throws TransformationException;
	
	/**
	 * get the jid of the appropriate client.
	 * @return
	 */
	public JID getJID();
}

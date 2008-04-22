package de.fu_berlin.inf.dpp.concurrent.jupiter;

import de.fu_berlin.inf.dpp.net.JID;

public interface JupiterClient {

	public Request generateRequest(Operation op);
	
	public Operation receiveRequest(Request req) throws TransformationException;
	
	public JID getJID();
}

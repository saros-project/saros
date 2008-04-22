package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.net.JID;

public class JupiterDocumentClient implements JupiterClient {

	/**
	 * 1. Outgoing queue
	 * 2. request forwarder
	 * 
	 */
	
	/** jid of remote client*/
	private JID jid;
	/** jupiter sync algorithm. */
	private Algorithm jupiter;
	
	public JupiterDocumentClient(JID jid){
		this.jid = jid;
		this.jupiter = new Jupiter(true);
	}
	
	public Request generateRequest(Operation op) {
		// TODO Auto-generated method stub
		return null;
	}

	public Operation receiveRequest(Request req) {
		// TODO Auto-generated method stub
		return null;
	}

	public JID getJID() {
		// TODO Auto-generated method stub
		return null;
	}

	
}

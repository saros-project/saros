package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.RequestForwarder;
import de.fu_berlin.inf.dpp.concurrent.jupiter.SynchronizedQueue;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.JID;

public class ProxyJupiterClient implements JupiterClient{

	/**
	 * 1. Outgoing queue
	 * 2. request forwarder
	 * 
	 */
	
	private static Logger logger = Logger.getLogger(ProxyJupiterClient.class);
	
	/** jid of remote client*/
	private JID jid;
	/** jupiter sync algorithm. */
	private Algorithm jupiter;
	/** forwarder send request to client. */
	private RequestForwarder forwarder;
	
	public ProxyJupiterClient(JID jid, RequestForwarder forwarder){
		this.jid = jid;
		jupiter = new Jupiter(false);
		this.forwarder = forwarder;
	}
	
	
	public Request generateRequest(Operation op) {
		Request req = null;
		logger.debug(jid.toString()+" proxy client generate request for "+op);
		req = jupiter.generateRequest(op);
		/* send request*/
		forwarder.forwardOutgoingRequest(req);
		
		return req;
	}

	public Operation receiveRequest(Request req) throws TransformationException {
		Operation op = null;
		logger.debug(jid.toString()+" proxy client receive request "+req.getOperation());
		/* receive request action */
		op =  jupiter.receiveRequest(req);
		logger.debug(jid.toString()+" proxy client operation of IT: "+op);
		return op;
	}

	/**
	 * @see de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient#getJID()
	 */
	public JID getJID() {
		return jid;
	}


}

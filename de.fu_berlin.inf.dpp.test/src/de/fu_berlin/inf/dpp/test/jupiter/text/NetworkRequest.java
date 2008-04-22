package de.fu_berlin.inf.dpp.test.jupiter.text;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.RequestImpl;
import de.fu_berlin.inf.dpp.net.JID;

public class NetworkRequest {

	private JID from;
	
	private JID to;
	
	private Request request;
	
	public NetworkRequest(JID from, JID to, Request req){
		this.from = from;
		this.to = to;
		/* adaption to new request format. */
		if(req.getJID() == null){
			this.request = new RequestImpl(req.getSiteId(),req.getTimestamp(),req.getOperation(),from);
		}else{
			this.request = req;
		}
	}

	public JID getFrom() {
		return from;
	}

	public JID getTo() {
		return to;
	}

	public Request getRequest() {
		return request;
	}
}

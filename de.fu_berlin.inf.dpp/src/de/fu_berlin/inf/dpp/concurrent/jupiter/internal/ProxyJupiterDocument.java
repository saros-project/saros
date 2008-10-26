package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.RequestForwarder;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.JID;

public class ProxyJupiterDocument implements JupiterClient {

    /**
     * 1. Outgoing queue 2. request forwarder
     * 
     */

    private static Logger logger = Logger.getLogger(ProxyJupiterDocument.class);

    private IPath editor;
    /** forwarder send request to client. */
    private final RequestForwarder forwarder;
    /** jid of remote client */
    private final JID jid;

    /** jupiter sync algorithm. */
    private final Algorithm jupiter;

    public ProxyJupiterDocument(JID jid, RequestForwarder forwarder) {
	this.jid = jid;
	this.jupiter = new Jupiter(false);
	this.forwarder = forwarder;
    }

    public Request generateRequest(Operation op) {
	Request req = null;
	ProxyJupiterDocument.logger.debug(this.jid.toString()
		+ " proxy client generate request for " + op);
	req = this.jupiter.generateRequest(op);
	req.setJID(this.jid);
	req.setEditorPath(this.editor);
	/* send request */
	ProxyJupiterDocument.logger.debug(this.jid.toString()
		+ " proxy client forward request:  " + req);
	this.forwarder.forwardOutgoingRequest(req);

	return req;
    }

    public IPath getEditor() {
	return this.editor;
    }

    /**
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient#getJID()
     */
    public JID getJID() {
	return this.jid;
    }

    public Timestamp getTimestamp() {
	return this.jupiter.getTimestamp();
    }

    public Operation receiveRequest(Request req) throws TransformationException {
	Operation op = null;
	ProxyJupiterDocument.logger.debug(this.jid.toString()
		+ " proxy client receive request " + req.getOperation());
	/* receive request action */
	op = this.jupiter.receiveRequest(req);
	ProxyJupiterDocument.logger.debug(this.jid.toString()
		+ " proxy client operation of IT: " + op);
	return op;
    }

    public void setEditor(IPath path) {
	this.editor = path;
    }

    public void updateVectorTime(Timestamp timestamp)
	    throws TransformationException {
	this.jupiter.updateVectorTime(timestamp);
    }

}

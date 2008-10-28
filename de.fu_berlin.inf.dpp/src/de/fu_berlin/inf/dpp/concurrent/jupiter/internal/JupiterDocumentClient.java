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

public class JupiterDocumentClient implements JupiterClient {

    /**
     * 1. Outgoing queue 2. request forwarder
     * 
     */

    private static Logger logger = Logger.getLogger(JupiterDocumentClient.class
	    .toString());

    /** jid of remote client */
    private final JID jid;
    /** jupiter sync algorithm. */
    private final Algorithm jupiter;

    private IPath editor;

    /** forwarder send request to server. */
    private final RequestForwarder forwarder;

    @Deprecated
    public JupiterDocumentClient(JID jid, RequestForwarder forwarder) {
	this.jid = jid;
	this.jupiter = new Jupiter(true);
	this.forwarder = forwarder;
    }

    public JupiterDocumentClient(JID jid, RequestForwarder forwarder,
	    IPath editor) {
	this.jid = jid;
	this.jupiter = new Jupiter(true);
	this.forwarder = forwarder;
	this.editor = editor;
    }

    public Request generateRequest(Operation op) {
	Request req = null;
	JupiterDocumentClient.logger.debug(this.jid.toString()
		+ " client generate request for " + op);
	req = this.jupiter.generateRequest(op);
	req.setJID(this.jid);
	req.setEditorPath(this.editor);
	/* send request */
	this.forwarder.forwardOutgoingRequest(req);

	return req;
    }

    public Operation receiveRequest(Request req) throws TransformationException {
	Operation op = null;
	JupiterDocumentClient.logger.debug(this.jid.toString()
		+ " client receive request " + req.getOperation());
	/* receive request action */
	op = this.jupiter.receiveRequest(req);
	JupiterDocumentClient.logger.debug(this.jid.toString()
		+ " client operation of IT: " + op);
	return op;
    }

    public JID getJID() {
	return this.jid;
    }

    public IPath getEditor() {
	return this.editor;
    }

    public void setEditor(IPath path) {
	this.editor = path;

    }

    public Timestamp getTimestamp() {
	return this.jupiter.getTimestamp();
    }

    public void updateVectorTime(Timestamp timestamp)
	    throws TransformationException {
	this.jupiter.updateVectorTime(timestamp);
    }

}

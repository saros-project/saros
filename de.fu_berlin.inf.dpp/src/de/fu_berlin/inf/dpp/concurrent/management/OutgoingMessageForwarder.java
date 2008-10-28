package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.RequestForwarder;

public class OutgoingMessageForwarder implements RequestForwarder {

    private static Logger logger = Logger
	    .getLogger(OutgoingMessageForwarder.class);

    /** outgoing queue to transfer request to appropriate clients. */
    public List<Request> outgoingQueue;

    public OutgoingMessageForwarder() {
	this.outgoingQueue = new Vector<Request>();
    }

    public synchronized void forwardOutgoingRequest(Request req) {
	/* add request to outgoing queue. */
	this.outgoingQueue.add(req);

	OutgoingMessageForwarder.logger
		.debug("add request to outgoing queue : " + req.getJID() + " "
			+ req.getOperation());
	notifyAll();
    }

    public synchronized Request getNextOutgoingRequest()
	    throws InterruptedException {
	Request req = null;
	/* get next message and transfer to client. */
	while (!(this.outgoingQueue.size() > 0)) {
	    wait();
	}
	/* remove first queue element. */
	req = this.outgoingQueue.remove(0);

	OutgoingMessageForwarder.logger
		.debug("read next request from outgoing queue: " + req.getJID()
			+ " " + req.getOperation());
	return req;
    }

}

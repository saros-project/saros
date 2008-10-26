package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.RequestForwarder;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.JID;

public class JupiterDocumentServer implements JupiterServer {

    /**
     * this forwarder reads request form the local outgoing queue and transmit
     * the requests to the global outgoing queue.
     */
    class RequestTransmitter extends Thread {

	private static final int MILLIS = 100;
	public Timer flushTimer = new Timer(true);

	private final RequestForwarder rf;

	public RequestTransmitter(RequestForwarder forw) {
	    this.rf = forw;
	}

	@Override
	public void run() {
	    this.flushTimer.schedule(new TimerTask() {

		@Override
		public void run() {
		    /* forwarding */
		    try {
			JupiterDocumentServer.logger
				.debug("Forwarding requests to activity sequencer. ");
			RequestTransmitter.this.rf
				.forwardOutgoingRequest(getNextOutgoingRequest());
			JupiterDocumentServer.logger
				.debug("Forwarding is sended to activity sequencer. ");
		    } catch (InterruptedException e) {
			JupiterDocumentServer.logger.warn(
				"Exception forwarding request.", e);
		    }

		}

	    }, 0, RequestTransmitter.MILLIS);
	}

    }

    private static Logger logger = Logger
	    .getLogger(JupiterDocumentServer.class);

    private IPath editor;

    // /** outgoing queue to transfer request to appropriate clients. */
    private final List<Request> outgoingQueue;

    /**
     * List of proxy clients.
     */
    private final HashMap<JID, JupiterClient> proxies;
    private final List<Request> requestList;

    // /** for add and remove client synchronization. */
    // public boolean waitForSerializer = false;
    // /** counter for remove client synchronization.*/
    // public int requestSyncCounter = 0;

    /**
     * forward outgoing request to activity sequencer;
     */
    private RequestTransmitter transmitter;

    /**
     * default constructor. The server contains his own outgoing forwarding
     * queue.
     */
    public JupiterDocumentServer() {
	this.proxies = new HashMap<JID, JupiterClient>();
	this.requestList = new Vector<Request>();
	this.outgoingQueue = new Vector<Request>();

	new Serializer(this);
    }

    /**
     * this constructor init an external request forwarder. The generate answer
     * request of the proxy clients forwarding to this forwarder.
     */
    public JupiterDocumentServer(RequestForwarder forwarder) {
	this.proxies = new HashMap<JID, JupiterClient>();
	this.requestList = new Vector<Request>();
	this.outgoingQueue = new Vector<Request>();

	new Serializer(this);
	this.transmitter = new RequestTransmitter(forwarder);
	this.transmitter.start();
    }

    public synchronized void addProxyClient(JID jid) {
	JupiterClient proxy = new ProxyJupiterDocument(jid, this);
	proxy.setEditor(this.editor);
	// /* add to serializer. */
	// waitForSerializer = true;
	// TODO: Sync with serializer before add action.
	JupiterDocumentServer.logger.debug("add new proxy client : " + jid);
	this.proxies.put(jid, proxy);
    }

    /**
     * add request from transmitter to request queue.
     */
    public synchronized void addRequest(Request request) {

	/* TODO: Sync with serializer. */

	/**
	 * add request to serialized queue.
	 */
	JupiterDocumentServer.logger.debug("add new Request: "
		+ request.getJID() + " " + request.getOperation());
	this.requestList.add(request);
	notify();
    }

    /**
     * proxies add generated request to outgoing queue.
     */
    public synchronized void forwardOutgoingRequest(Request req) {
	/* add request to outgoing queue. */
	// if(outgoing == null){
	this.outgoingQueue.add(req);
	// }else{
	// /* forward request.*/
	// outgoing.forwardOutgoingRequest(req);
	// }
	JupiterDocumentServer.logger.debug("add request to outgoing queue : "
		+ req.getJID() + " " + req);
	notify();
    }

    public IPath getEditor() {
	return this.editor;
    }

    /**
     * transmitter interface get next request for transfer.
     */
    public synchronized Request getNextOutgoingRequest()
	    throws InterruptedException {
	Request req = null;
	// if(outgoing == null){
	/* get next message and transfer to client. */
	while (!(this.outgoingQueue.size() > 0)) {
	    wait(200);
	}
	/* remove first queue element. */
	req = this.outgoingQueue.remove(0);
	// }
	// else{
	// req = outgoing.getNextOutgoingRequest();
	//			
	// }
	JupiterDocumentServer.logger
		.debug("read next request from outgoing queue: " + req.getJID()
			+ " " + req);
	return req;

	// return outgoing.getNextOutgoingRequest();
    }

    /* start transfer section. */

    /**
     * next message in request queue.
     */
    public synchronized Request getNextRequestInSynchronizedQueue()
	    throws InterruptedException {
	/* if queue is empty or proxy managing action is running. */
	while (!(this.requestList.size() > 0)) {
	    wait();
	}
	JupiterDocumentServer.logger.debug("read out next request in queue! "
		+ this.requestList.get(0).getJID() + this.requestList.get(0));
	/* get next request. */
	return this.requestList.remove(0);
    }

    public synchronized HashMap<JID, JupiterClient> getProxies()
	    throws InterruptedException {
	/*
	 * Was Passiert, wenn während der Bearbeitung ein neuer proxy
	 * eingefügt wird
	 */

	// /* Synchronistation für das Client Management.*/
	// while(waitForSerializer && requestSyncCounter == 0){
	// wait();
	// }
	JupiterDocumentServer.logger.debug("Get jupiter proxies.");
	return this.proxies;
    }

    public boolean isExist(JID jid) {
	if (this.proxies.containsKey(jid)) {
	    return true;
	}
	return false;
    }

    public synchronized void removeProxyClient(JID jid) {

	/**
	 * TODO: sync with serializer.
	 * 
	 * 1. save current action count 2. stop serializer after this cound and
	 * remove client.
	 */
	this.proxies.remove(jid);
	notifyAll();
    }

    public void setEditor(IPath path) {
	this.editor = path;

    }

    public void transformationErrorOccured() {
	forwardOutgoingRequest(new RequestError(this.editor));
    }

    public void updateVectorTime(JID source, JID dest) {
	JupiterClient proxy = this.proxies.get(source);
	if (proxy != null) {
	    try {
		Timestamp ts = proxy.getTimestamp();
		getProxies().get(dest).updateVectorTime(
			new JupiterVectorTime(ts.getComponents()[1], ts
				.getComponents()[0]));
	    } catch (TransformationException e) {
		JupiterDocumentServer.logger.error(
			"Error during update vector time for " + dest, e);
	    } catch (InterruptedException e) {
		JupiterDocumentServer.logger.error(
			"Error during update vector time for " + dest, e);
	    }
	} else {
	    JupiterDocumentServer.logger
		    .error("No proxy found for given source jid: " + source);
	}

    }

    /* end transfer section */

}

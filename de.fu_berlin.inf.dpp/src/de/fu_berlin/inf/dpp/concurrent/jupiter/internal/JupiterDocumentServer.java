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
import de.fu_berlin.inf.dpp.concurrent.jupiter.OperationSerializer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.RequestForwarder;
import de.fu_berlin.inf.dpp.concurrent.management.OutgoingMessageForwarder;
import de.fu_berlin.inf.dpp.net.JID;

public class JupiterDocumentServer implements JupiterServer{
	
	private static Logger logger = Logger.getLogger(JupiterDocumentServer.class);
	
	/**
	 * List of proxy clients.
	 */
	private HashMap<JID, JupiterClient> proxies;
	
	private List<Request> requestList;
	
//	/** outgoing queue to transfer request to appropriate clients. */
	private List<Request> outgoingQueue;
	
	private RequestForwarder outgoing;
	
	private OperationSerializer serializer;
	
	private IPath editor;
	/**
	 * forward outgoing request to activity sequencer;
	 */
	private RequestTransmitter transmitter;
	
//	/** for add and remove client synchronization. */
//	public boolean waitForSerializer = false;
//	/** counter for remove client synchronization.*/
//	public int requestSyncCounter = 0;
	
	class RequestTransmitter extends Thread{
		
		private final RequestForwarder rf;
		private static final int MILLIS = 100;
		
		public RequestTransmitter(RequestForwarder forw){
			this.rf = forw;
		}
		
		public Timer flushTimer = new Timer(true);
		
		public void run(){
			flushTimer.schedule(new TimerTask(){

				@Override
				public void run() {
					/*forwarding */
					try {
						logger.debug("Forwarding requests to activity sequencer. ");
						rf.forwardOutgoingRequest(getNextOutgoingRequest());
						logger.debug("Forwarding is sended to activity sequencer. ");
					} catch (InterruptedException e) {
						logger.warn("Exception forwarding request.",e);
					}
					
				}
				
			},0, MILLIS);
		}
		
	}
	
	/**
	 * this constructor init an external request forwarder.
	 * The generate answer request of the proxy clients forwarding
	 * to this forwarder.
	 */
	public JupiterDocumentServer(RequestForwarder forwarder){
		proxies = new HashMap<JID, JupiterClient>();
		requestList = new Vector<Request>();
		this.outgoingQueue = new Vector<Request>();
		
//		this.outgoing = forwarder;
		serializer = new Serializer(this);
		this.transmitter = new RequestTransmitter(forwarder);
		transmitter.start();
	}
	
	/**
	 * default constructor. The server contains his own outgoing 
	 * forwarding queue. 
	 */
	public JupiterDocumentServer(){
		proxies = new HashMap<JID, JupiterClient>();
		requestList = new Vector<Request>();
		this.outgoingQueue = new Vector<Request>();

//		this.outgoing = forwarder;
		serializer = new Serializer(this);
	}
	
	
	
	public synchronized void addProxyClient(JID jid) {
		JupiterClient proxy = new ProxyJupiterDocument(jid,this);
		proxy.setEditor(editor);
//		/* add to serializer. */
//		waitForSerializer = true;
		//TODO: Sync with serializer before add action.
		logger.debug("add new proxy client : "+jid);
		proxies.put(jid, proxy);
	}

	public synchronized void removeProxyClient(JID jid) {
		
		/** 
		 * TODO: sync with serializer. 
		 * 
		 * 1. save current action count
		 * 2. stop serializer after this cound and remove client.
		 */
		proxies.remove(jid);
		notifyAll();
	}
	
	/**
	 * add request from transmitter to request queue.
	 */
	public synchronized void addRequest(Request request) {
		
		/*TODO: Sync with serializer. */
		
		/**
		 * add request to serialized queue. 
		 */
		logger.debug("add new Request: "+request.getJID()+" "+request.getOperation());
		requestList.add(request);
		notify();
	}

	/**
	 * next message in request queue.
	 */
	public synchronized Request getNextRequestInSynchronizedQueue() throws InterruptedException {
		/* if queue is empty or proxy managing action is running. */
		if(!(requestList.size() > 0)){
			wait();
		}
		logger.debug("read out next request in queue! "+requestList.get(0).getJID()+requestList.get(0));
		/*get next request. */
		return requestList.remove(0);
	}

	public synchronized HashMap<JID, JupiterClient> getProxies() throws InterruptedException {
		/* Was Passiert, wenn während der Bearbeitung ein neuer proxy eingefügt wird */
		
//		/* Synchronistation für das Client Management.*/
//		while(waitForSerializer && requestSyncCounter == 0){
//			wait();
//		}
		logger.debug("Get jupiter proxies.");
		return proxies;
	}
	
	
	/* start transfer section. */
	
	/**
	 * proxies add generated request to outgoing queue.
	 */
	public synchronized void forwardOutgoingRequest(Request req) {
		/* add request to outgoing queue. */
//		if(outgoing == null){
			outgoingQueue.add(req);
//		}else{
//			/* forward request.*/
//			outgoing.forwardOutgoingRequest(req);
//		}
		logger.debug("add request to outgoing queue : "+req.getJID()+" "+req);
		notify();
	}

	/**
	 * transmitter interface get next request for transfer.
	 */
	public synchronized Request getNextOutgoingRequest() throws InterruptedException {
		Request req = null;
//		if(outgoing == null){
			/* get next message and transfer to client.*/
			while(!(outgoingQueue.size() >0)){
				wait(200);
			}
			/* remove first queue element. */
			req = outgoingQueue.remove(0);
//		}
//		else{
//			req = outgoing.getNextOutgoingRequest();
//			
//		}
		logger.debug("read next request from outgoing queue: "+req.getJID()+" "+req);
		return req;
		
//		return outgoing.getNextOutgoingRequest();
	}

	public IPath getEditor() {
		return editor;
	}

	public void setEditor(IPath path) {
		this.editor = path;
		
	}

	public boolean isExist(JID jid) {
		if(proxies.containsKey(jid)){
			return true;
		}
		return false;
	}

	/* end transfer section  */
	


}

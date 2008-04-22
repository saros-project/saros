package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.OperationSerializer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.net.JID;

public class JupiterDocumentServer implements JupiterServer{
	
	/**
	 * List of proxy clients.
	 */
	private HashMap<JID, JupiterClient> proxies;
	
	public List<Request> requestList;
	
	/** outgoing queue to transfer request to appropriate clients. */
	public List<Request> outgoingQueue;
	
	public OperationSerializer serializer;
	
//	/** for add and remove client synchronization. */
//	public boolean waitForSerializer = false;
//	/** counter for remove client synchronization.*/
//	public int requestSyncCounter = 0;
	
	public JupiterDocumentServer(){
		proxies = new HashMap<JID, JupiterClient>();
		requestList = new Vector<Request>();
		serializer = new Serializer(this);
		this.outgoingQueue = new Vector<Request>();
	}
	
	public synchronized void addProxyClient(JID jid) {
		JupiterClient proxy = new ProxyJupiterClient(jid,this);
//		/* add to serializer. */
//		waitForSerializer = true;
		//TODO: Sync with serializer before add action.
		
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
	
	public synchronized void addRequest(Request request) {
		
		/*TODO: Sync with serializer. */
		
		/**
		 * add request to serialized queue. 
		 */
		requestList.add(request);
		
		notifyAll();
	}

	public synchronized Request getNextRequestInSynchronizedQueue() throws InterruptedException {
		/* if queue is empty or proxy managing action is running. */
		if(!(requestList.size() > 0)){
			wait();
		}
		/*get next request. */
		return requestList.remove(0);
	}

	public synchronized HashMap<JID, JupiterClient> getProxies() throws InterruptedException {
		/* Was Passiert, wenn während der Bearbeitung ein neuer proxy eingefügt wird */
		
//		/* Synchronistation für das Client Management.*/
//		while(waitForSerializer && requestSyncCounter == 0){
//			wait();
//		}
		
		return proxies;
	}
	
	
	/* start transfer section. */
	/**
	 * proxies add generated request to outgoing queue.
	 */
	public synchronized void forwardOutgoingRequest(Request req) {
		/* add request to outgoing queue. */
		outgoingQueue.add(req);
		notifyAll();
	}

	/**
	 * transmitter interface get next request for transfer.
	 */
	public synchronized Request getNextOutgoingRequest() throws InterruptedException {
		Request req = null;
		/* get next message and transfer to client.*/
		while(!(outgoingQueue.size() >0)){
			wait();
		}
		//TODO: transfer action have to be implement.
		
		return req;
	}

	/* end transfer section  */
	


}

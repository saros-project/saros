package de.fu_berlin.inf.dpp.test.jupiter.server.impl;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.RequestForwarder;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.management.OutgoingMessageForwarder;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.NetworkRequest;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.NetworkConnection;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.NetworkEventHandler;

public class ConcurrentManager implements NetworkEventHandler{

	private static Logger logger = Logger.getLogger(ConcurrentManager.class);
	
	private JupiterDocumentServer server;
	private JID jid = new JID("ori78@jabber.cc");
	private NetworkConnection connection;
//	private RequestForwarder outgoing;
	
	public ConcurrentManager(NetworkConnection con, JID jid){
		this.connection = con;
		this.jid = jid;
		init();
	}
	
	private void init(){
//		this.outgoing = new OutgoingMessageForwarder();
//		server = new JupiterDocumentServer(outgoing);
		server = new JupiterDocumentServer();
		
		new Thread(new Runnable(){

			public void run() {
				while(true){
					sendRequest();
				}
				
			}
			
		}).start();
	}

	/**
	 * Update vector time.
	 * @param source
	 * @param dest
	 */
	public void updateVectorTime(JID source, JID dest){
		server.updateVectorTime(source, dest);
	}
	
	public void addProxyClient(JID jid){
		/* if client not in proxy list*/
		try {
			if(!server.getProxies().containsKey(jid)){
				logger.info("add new proxy client for "+jid);
				server.addProxyClient(jid);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* test network methods. */
	public JID getJID() {
		return jid;
	}

	@Deprecated
	public void receiveNetworkEvent(Request req) {	
//		server.addRequest(req.g)
	}

	public void receiveNetworkEvent(NetworkRequest req) {	
		/* if client not in proxy list*/
		try {
			if(!server.getProxies().containsKey(req.getRequest().getJID())){
				logger.info("add new proxy client for "+req.getFrom());
				server.addProxyClient(req.getRequest().getJID());
			}
			server.addRequest(req.getRequest());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void sendRequest(){
		try {
//			Request request = outgoing.getNextOutgoingRequest();
			Request request = server.getNextOutgoingRequest();
			logger.debug("send transformed operation to client side. ");
			/* send operation to client. */
			connection.sendOperation(new NetworkRequest(this.jid,request.getJID(),request), 0);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
	}
}

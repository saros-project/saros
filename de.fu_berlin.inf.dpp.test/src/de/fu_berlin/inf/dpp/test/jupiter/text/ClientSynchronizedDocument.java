package de.fu_berlin.inf.dpp.test.jupiter.text;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.jupiter.Operation;
import de.fu_berlin.inf.dpp.jupiter.Request;
import de.fu_berlin.inf.dpp.jupiter.SynchronizedQueue;
import de.fu_berlin.inf.dpp.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.NetworkConnection;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.NetworkEventHandler;


/**
 * test document to simulate the client site.
 * @author orieger
 *
 */

public class ClientSynchronizedDocument implements SynchronizedQueue, NetworkEventHandler, DocumentTestChecker{

	private static Logger logger = Logger.getLogger(ClientSynchronizedDocument.class);
	
	private Document doc;
	private Algorithm algorithm;
	
	private JID jid;
	private JID server_jid = new JID("ori78@jabber.cc");
	private NetworkConnection connection;
	
	public ClientSynchronizedDocument(String content, NetworkConnection con){
		this.doc = new Document(content);
		this.algorithm = new Jupiter(true);
		this.connection = con;
	}
	
	public ClientSynchronizedDocument(String content, NetworkConnection con, JID jid){
		this.doc = new Document(content);
		this.algorithm = new Jupiter(false);
		this.connection = con;
		this.jid = jid;
	}
	
	public JID getJID() {
		return this.jid;
	}
	
	public void setJID(JID jid){
		this.jid = jid;
	}

	public Operation receiveOperation(Request req) {
		Operation op = null;
		try {
			logger.debug("Client: "+jid+ " receive "+req.getOperation().toString());
			/* 1. transform operation. */
			op = algorithm.receiveRequest(req);
//			op = algorithm.receiveTransformedRequest(req);
			/* 2. execution on server document*/
			doc.execOperation(op);
		} catch (TransformationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return op;
	}

	public void sendOperation(Operation op) {
		sendOperation(server_jid, op, 0);
	}
	
	
	public void sendOperation(Operation op, int delay) {
		logger.info("send "+op.toString());
		sendOperation(server_jid, op, delay);
	}

	public void sendOperation(JID remoteJid, Operation op, int delay) {
		/* 1. execute locally*/
		doc.execOperation(op);
		/* 2. transform operation. */
		Request req = algorithm.generateRequest(op);
		/* 3. send operation. */
//		connection.sendOperation(remoteJid, req, delay);
		connection.sendOperation(new NetworkRequest(this.jid, remoteJid,req), delay);
	}
	
	public void receiveNetworkEvent(Request req) {
		logger.info(this.jid+ " receive operation : "+req.getOperation().toString());
		receiveOperation(req);
	}

	public String getDocument() {
		return doc.getDocument();
	}


	@Deprecated
	public void sendTransformedOperation(Operation op, JID toJID) {
		// TODO Auto-generated method stub
		
	}

	public void receiveNetworkEvent(NetworkRequest req) {
		logger.info(this.jid+ " receive operation : "+req.getRequest().getOperation().toString()+" timestamp : "+req.getRequest().getTimestamp());
		receiveOperation(req.getRequest());
		
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}


	public void addJupiterDocumentListener(JupiterDocumentListener jdl) {
		// TODO Auto-generated method stub
		
	}

	public void removeJupiterDocumentListener(String id) {
		// TODO Auto-generated method stub
		
	}



	
}

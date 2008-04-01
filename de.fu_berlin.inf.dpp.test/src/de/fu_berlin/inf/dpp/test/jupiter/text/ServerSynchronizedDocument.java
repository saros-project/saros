package de.fu_berlin.inf.dpp.test.jupiter.text;

import de.fu_berlin.inf.dpp.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.jupiter.Operation;
import de.fu_berlin.inf.dpp.jupiter.Request;
import de.fu_berlin.inf.dpp.jupiter.SynchronizedQueue;
import de.fu_berlin.inf.dpp.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.NetworkConnection;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.NetworkEventHandler;

public class ServerSynchronizedDocument implements SynchronizedQueue, NetworkEventHandler, DocumentTestChecker{
	
	private Document doc;
	private Algorithm algorithm;
	
	
	private JID jid;
	private NetworkConnection connection;
	
	
	public ServerSynchronizedDocument(String content, NetworkConnection con){
		this.doc = new Document(content);
		this.algorithm = new Jupiter(true);
		this.connection = con;
	}

	public void setJID(JID jid){
		this.jid = jid;
	}
	
	public JID getJID() {
		return jid;
	}

	public Operation receiveOperation() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void sendOperation(Operation op) {
		
	}
	
	/**
	 * send operation to special jid.
	 * @param jid
	 * @param op
	 */
	public void sendOperation(JID jid, Operation op){
		/* 1. execute locally*/
		doc.execOperation(op);
		/* 2. transform operation. */
		Request req = algorithm.generateRequest(op);
		/*sent to client*/
		connection.sendOperation(jid, req);
	}

	public void receiveNetworkEvent(Request req) {
		System.out.println("network request received.");
		Operation op = null;
		try {
			/* 1. transform operation. */
			op = algorithm.receiveRequest(req);
			/* 2. execution on server document*/
			doc.execOperation(op);
		} catch (TransformationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	public String getDocument() {
		return doc.getDocument();
	}

}

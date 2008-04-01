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

/**
 * test document to simulate the client site.
 * @author orieger
 *
 */

public class ClientSynchronizedDocument implements SynchronizedQueue, NetworkEventHandler, DocumentTestChecker{

	private Document doc;
	private Algorithm algorithm;
	
	private JID jid;
	private JID server_jid = new JID("ori78@jabber.cc");
	private NetworkConnection connection;
	
	public ClientSynchronizedDocument(String content, NetworkConnection con){
		this.doc = new Document(content);
		this.algorithm = new Jupiter(false);
		this.connection = con;
	}
	
	public JID getJID() {
		return this.jid;
	}
	
	public void setJID(JID jid){
		this.jid = jid;
	}

	public Operation receiveOperation() {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendOperation(Operation op) {
		/* 1. execute locally*/
		doc.execOperation(op);
		/* 2. transform operation. */
		Request req = algorithm.generateRequest(op);
		/* 3. send operation. */
		connection.sendOperation(server_jid, req);
	}

	public void receiveNetworkEvent(Request req) {
		System.out.println("request");
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

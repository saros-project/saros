package de.fu_berlin.inf.dpp.test.jupiter.text;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.SynchronizedQueue;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.NetworkConnection;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.NetworkEventHandler;


public class TwoWayJupiterServerDocument implements SynchronizedQueue, NetworkEventHandler, DocumentTestChecker{
	
	private static Logger logger = Logger.getLogger(TwoWayJupiterServerDocument.class);
	
	private Document doc;
	/* sync algorithm with ack-operation list. */
	private Algorithm algorithm;
	
	private JID jid = new JID("ori78@jabber.cc");
	private JID jid_client = new JID("ori79@jabber.cc");
	private NetworkConnection connection;
	
	private List<SynchronizedQueue> proxyQueues;
	
	
	public TwoWayJupiterServerDocument(String content, NetworkConnection con){
		init(content,con);
	}


	public TwoWayJupiterServerDocument(String content, NetworkConnection con, JID jid){		
		this.jid = jid;
		init(content,con);
	}
	
	/* init proxy queue and all necessary objects. */
	private void init(String content, NetworkConnection con){
		this.doc = new Document(content);
		this.algorithm = new Jupiter(false);
		this.connection = con;
		this.proxyQueues = new Vector<SynchronizedQueue>();
	}
	
	public void setJID(JID jid){
		this.jid = jid;
	}
	
	public JID getJID() {
		return jid;
	}

	/**
	 * {@inheritDoc}
	 */
	public Operation receiveOperation(Request req) {
		Operation op = null;
		try {
			logger.debug("Operation before OT:"+req.getOperation().toString());
			/* 1. transform operation. */
			op = algorithm.receiveRequest(req);
			
			logger.debug("Operation after OT: "+op.toString());
			/* 2. execution on server document*/
			doc.execOperation(op);
		} catch (TransformationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return op;
	}

	@Deprecated
	public void sendOperation(Operation op) {
		
	}
	
	/**
	 * send operation to special jid.
	 * @param jid
	 * @param op
	 */
	public void sendOperation(JID jid, Operation op){
		sendOperation(jid, op, 0);
	}
	
	public void sendOperation(JID jid, Operation op, int delay) {
		/* 1. execute locally*/
		doc.execOperation(op);
		/* 2. transform operation. */
		Request req = algorithm.generateRequest(op);
		/*sent to client*/
		connection.sendOperation(new NetworkRequest(this.jid,jid,req), delay);
//		connection.sendOperation(jid, req,delay);
		
	}
	
	/**
	 * send operation to twowayjupiterclient
	 * @param jid
	 * @param op
	 * @param delay
	 */
	public void sendOperation(Operation op, int delay) {
		sendOperation(jid_client, op, delay);
	}

	public void receiveNetworkEvent(Request req) {
		logger.info("receive operation : "+req.getOperation().toString());
		receiveOperation(req);	

	}

	public String getDocument() {
		return doc.getDocument();
	}


	public Algorithm getAlgorithm() {
		return this.algorithm;
	}


	
	public void sendTransformedOperation(Operation op, JID toJID) {
		// TODO Auto-generated method stub
		
	}


	/**
	 * receive network request.
	 */
	public void receiveNetworkEvent(NetworkRequest req) {
		receiveOperation(req.getRequest());
	}



	public void addJupiterDocumentListener(JupiterDocumentListener jdl) {
		// TODO Auto-generated method stub
		
	}

	public void removeJupiterDocumentListener(String id) {
		// TODO Auto-generated method stub
		
	}


	public void updateVectorTime(Timestamp timestamp) {
		// TODO Auto-generated method stub
		
	}



}

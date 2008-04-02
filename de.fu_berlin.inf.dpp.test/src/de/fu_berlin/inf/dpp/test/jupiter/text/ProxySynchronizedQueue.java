package de.fu_berlin.inf.dpp.test.jupiter.text;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.jupiter.Operation;
import de.fu_berlin.inf.dpp.jupiter.Request;
import de.fu_berlin.inf.dpp.jupiter.RequestImpl;
import de.fu_berlin.inf.dpp.jupiter.SynchronizedQueue;
import de.fu_berlin.inf.dpp.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.NetworkConnection;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.NetworkEventHandler;

/**
 * This proxy class on server represent the server side of the two-way jupiter protocol.
 * @author troll
 *
 */
public class ProxySynchronizedQueue implements SynchronizedQueue{

	private static Logger logger = Logger.getLogger(ProxySynchronizedQueue.class);
	
	private Algorithm algorithm;
	private NetworkConnection connection;
	private JID jid;
	
	public ProxySynchronizedQueue(JID jid, NetworkConnection con){
		this.jid = jid;
		this.algorithm = new Jupiter(false);
		this.connection = con;
	}
	
	public JID getJID() {
		return jid;
	}

	public Operation receiveOperation(Request req) {
		Operation op = null;
		try {
			logger.debug(jid+ " : Operation before OT:"+req.getOperation().toString());
			/* 1. transform operation. */
			op = algorithm.receiveRequest(req);
			
//			//TODO: Only for testing: create new request.
//			if(!jid.toString().equals("ori79@jabber.cc")){
//				Request send_req = new RequestImpl(
//						algorithm.getSiteId(), 
//						algorithm.getTimestamp(), 
//						op);
//				
//				connection.sendOperation(jid, send_req, 0);
//			}
			
			logger.debug(jid+ " : Operation after OT: "+op.toString());
		} catch (TransformationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return op;
	}

	/**
	 * send a transformed operation to client side.
	 * @param op operation has transformed and only send to
	 * client side.
	 */
	public void sendTransformedOperation(Operation op, JID jid){
		Request send_req = new RequestImpl(
				algorithm.getSiteId(), 
				algorithm.getTimestamp(), 
				op);
		
		connection.sendOperation(jid, send_req, 0);
	}
	
	public void sendOperation(Operation op) {
		/* 1. transform operation. */
		Request req = algorithm.generateRequest(op);
		/* 2. opertion to client. */
		connection.sendOperation(jid, req, 0);
//		connection
	}

}

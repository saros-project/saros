package de.fu_berlin.inf.dpp.test.jupiter.client;

import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.RequestForwarder;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.server.impl.ConcurrentManager;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.JupiterTestCase;
import de.fu_berlin.inf.dpp.test.jupiter.text.ServerSynchronizedDocument;
import junit.framework.TestCase;

public class ClientAddedToServerTest extends JupiterTestCase implements RequestForwarder{

	public ClientAddedToServerTest(String name) {
		super(name);
	}



	/**
	 * dOPT puzzle scenario with three sides and three concurrent insert
	 * operations of a character at the same position.
	 * @throws Exception
	 */
	public void testThreeConcurrentInsertOperations() throws Exception{
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_c3 = new JID("ori81@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		/* init simulated client and server components. */
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abcd",
				network,jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument("abcd",
				network,jid_c2);
		ClientSynchronizedDocument c3 = new ClientSynchronizedDocument("abcd",
				network,jid_c3);
//		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(
//				network,jid_server);
		ConcurrentManager s1 = new ConcurrentManager(
				network,jid_server);

		/* connect all with simulated network. */
		network.addClient(c1);
		network.addClient(c2);
		network.addClient(c3);
		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
//		s1.addProxyClient(jid_c3);
		
		Thread.sleep(100);
		
		c1.sendOperation(new InsertOperation(0,"z"),100);
		c2.sendOperation(new InsertOperation(0,"x"),200);
		c2.sendOperation(new InsertOperation(0,"x"),200);
		

		Thread.sleep(1000);
		
		/* update vector time on client site.*/
		s1.addProxyClient(jid_c3);
		c3.updateVectorTime(c1.getAlgorithm().getTimestamp());
		
		/* update vector time on jupiter site. */
		s1.updateVectorTime(jid_c1, jid_c3);
		
		Thread.sleep(300);
		/* O1 -> O3  */
		c3.sendOperation(new InsertOperation(0,"y"),100);
		
		
		Thread.sleep(300);
		c1.sendOperation(new InsertOperation(0,"1"),100);

		
		Thread.sleep(300);
		System.out.println(c1.getDocument());
	}



	public void forwardOutgoingRequest(Request req) {
		// TODO Auto-generated method stub
		
	}



	public Request getNextOutgoingRequest() throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}
}

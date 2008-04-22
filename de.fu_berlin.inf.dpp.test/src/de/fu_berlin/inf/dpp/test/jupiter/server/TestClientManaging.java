package de.fu_berlin.inf.dpp.test.jupiter.server;

import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.server.impl.ConcurrentManager;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.JupiterTestCase;
import de.fu_berlin.inf.dpp.test.jupiter.text.ServerSynchronizedDocument;
import junit.framework.TestCase;
/**
 * this test class contains test cases for managing jupiter proxy clients.
 * @author orieger
 *
 */
public class TestClientManaging extends JupiterTestCase {

	public TestClientManaging(String name) {
		super(name);
	}

	public void testConcurrentJupiterDocumentServer() throws Exception{
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_c3 = new JID("ori81@jabber.cc");;
		JID jid_server = new JID("ori78@jabber.cc");
		
		String initDocumentState = "core";
		
		/* init simulated client and server components. */
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument(initDocumentState,
				network,jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument(initDocumentState,
				network,jid_c2);
		ClientSynchronizedDocument c3 = new ClientSynchronizedDocument(initDocumentState,
				network,jid_c3);
		ConcurrentManager s1 = new ConcurrentManager(
				network,jid_server);

		/* connect all with simulated network. */
		network.addClient(c1);
		network.addClient(c2);
		network.addClient(c3);
		network.addClient(s1);
		
//		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
		s1.addProxyClient(jid_c3);
		
		
		Thread.sleep(100);
		
		/* O3 || O2*/
		c1.sendOperation(new InsertOperation(3,"f"), 100);
		c2.sendOperation(new DeleteOperation(2,"r"), 100);
		c3.sendOperation(new InsertOperation(2,"f"), 100);
		
		Thread.sleep(1500);
		
		assertEquals(c1.getDocument(),c2.getDocument());
		assertEquals(c2.getDocument(),c3.getDocument());
		assertEquals("coffe",c1.getDocument());
	}
}

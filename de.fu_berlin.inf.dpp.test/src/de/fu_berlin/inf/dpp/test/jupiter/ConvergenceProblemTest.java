package de.fu_berlin.inf.dpp.test.jupiter;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.fu_berlin.inf.dpp.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.JupiterTestCase;
import de.fu_berlin.inf.dpp.test.jupiter.text.ServerSynchronizedDocument;

/**
 * This test class contains convergence problem scenarios described  in
 * "Achieving Convergence with Operational Transformation in Distributed Groupware
 * Systems" by Abdessamad Imine, Pascal Molli, Gerald Oster, Michael Rusinowitch.
 * 
 * @author orieger
 *
 */
public class ConvergenceProblemTest extends JupiterTestCase{

	public ConvergenceProblemTest(String method){
		super(method);
	}
	
	/**
	 * Scenario in fig. 3 described in 3.1 Scenarios violating convergence.
	 */
	public void testC2PuzzleP1() throws Exception {
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
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(
				network,jid_server);

		/* connect all with simulated network. */
		network.addClient(c1);
		network.addClient(c2);
		network.addClient(c3);
		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
		s1.addProxyClient(jid_c3);
		
		Thread.sleep(100);
		
		/* O3 || O2*/
		c1.sendOperation(new InsertOperation(3,"f"), 100);
		c2.sendOperation(new DeleteOperation(2,"r"), 200);
		c3.sendOperation(new InsertOperation(2,"f"), 1000);
		
		Thread.sleep(1500);
		
		assertEquals(c1.getDocument(),c2.getDocument());
		assertEquals(c2.getDocument(),c3.getDocument());
		assertEquals("coffe",c1.getDocument());
	}
	
	/**
	 * Scenario in fig. 5 described in 3.1 Scenarios violating convergence.
	 */
	public void testC2PuzzleP2() throws Exception {
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_c3 = new JID("ori81@jabber.cc");
		JID jid_c4 = new JID("ori82@jabber.cc");
		JID jid_c5 = new JID("ori83@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		String initDocumentState = "abcd";
		
		/* init simulated client and server components. */
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument(initDocumentState,
				network,jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument(initDocumentState,
				network,jid_c2);
		ClientSynchronizedDocument c3 = new ClientSynchronizedDocument(initDocumentState,
				network,jid_c3);
		ClientSynchronizedDocument c4 = new ClientSynchronizedDocument(initDocumentState,
				network,jid_c4);
		ClientSynchronizedDocument c5 = new ClientSynchronizedDocument(initDocumentState,
				network,jid_c5);
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(
				network,jid_server);

		/* connect all with simulated network. */
		network.addClient(c1);
		network.addClient(c2);
		network.addClient(c3);
		network.addClient(c4);
		network.addClient(c5);
		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
		s1.addProxyClient(jid_c3);
		s1.addProxyClient(jid_c4);
		s1.addProxyClient(jid_c5);
		
		Thread.sleep(100);
		
		/* O3 || O2*/
		c1.sendOperation(new DeleteOperation(1,"a"), 100);
		c4.sendOperation(new InsertOperation(3,"x"), 1000);
		c5.sendOperation(new DeleteOperation(3,"c"), 1100);
		
		c1.sendOperation(new InsertOperation(3,"x"), 1500);
		Thread.sleep(2000);
		
		assertEquals(c1.getDocument(),c2.getDocument());
		assertEquals(c2.getDocument(),c3.getDocument());
		assertEquals(c3.getDocument(),c4.getDocument());
		assertEquals(c4.getDocument(),c5.getDocument());
		assertEquals("acxx",c1.getDocument());

	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite("Convergence violating scenarios.");
		//$JUnit-BEGIN$
		suite.addTest(new ConvergenceProblemTest("testC2PuzzleP1"));
		suite.addTest(new ConvergenceProblemTest("testC2PuzzleP2"));
		//$JUnit-END$
		return suite;
	}
}

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
 * This class contains three test scenarios to verify transformation functions 
 * out of ""Providing Correctness of Transformation Functions in Real-Time 
 * Groupware"
 * @author orieger
 *
 */
public class CounterExampleTest extends JupiterTestCase{

	public CounterExampleTest(String method){
		super(method);
	}
	
	/**
	 * Scenario described in fig. 3 of discussed paper. 
	 * @throws Exception
	 */
	public void testCounterExampleViolatingConditionC1() throws Exception{
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		
		JID jid_server = new JID("ori78@jabber.cc");
		
		String initDocumentState = "abc";
		
		/* init simulated client and server components. */
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument(initDocumentState,
				network,jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument(initDocumentState,
				network,jid_c2);

		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(
				network,jid_server);

		/* connect all with simulated network. */
		network.addClient(c1);
		network.addClient(c2);

		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);

		
		Thread.sleep(100);
		
		/* O3 || O2*/

		c1.sendOperation(new InsertOperation(1,"x"),100);
		c2.sendOperation(new DeleteOperation(1,"b"),200);
		
		Thread.sleep(400);
	
		assertEquals(c1.getDocument(),c2.getDocument());
		assertEquals("axc",c1.getDocument());
	}
	
	/**
	 * Scenario described in fig. 4 of discussed paper. 
	 * @throws Exception
	 */
	public void testCounterExampleViolatingConditionC2() throws Exception{
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_c3 = new JID("ori81@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		String initDocumentState = "abc";
		
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
		c1.sendOperation(new InsertOperation(1,"x"),100);
		c2.sendOperation(new DeleteOperation(1,"b"),100);
		c3.sendOperation(new InsertOperation(2,"y"),1000);
	
		Thread.sleep(2000);
		
		assertEquals(c1.getDocument(),c2.getDocument());
		assertEquals(c2.getDocument(),c3.getDocument());
		System.out.println(c1.getDocument());
	}
	
	/**
	 * Scenario described in fig. 5 of discussed paper. 
	 * @throws Exception
	 */
	public void testCounterExample2ViolatingConditionC2() throws Exception{
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_c3 = new JID("ori81@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		String initDocumentState = "abc";
		
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
		c1.sendOperation(new InsertOperation(1,"y"),100);
		c2.sendOperation(new DeleteOperation(1,"b"),200);
		c3.sendOperation(new InsertOperation(2,"y"),1000);
	
		Thread.sleep(2000);
		
		assertEquals(c1.getDocument(),c2.getDocument());
		assertEquals(c2.getDocument(),c3.getDocument());
		System.out.println(c1.getDocument());
	}
	
	public static Test suite() {
	TestSuite suite = new TestSuite("Test for counter scenarios.");
	//$JUnit-BEGIN$
	suite.addTest(new CounterExampleTest("testCounterExampleViolatingConditionC1"));
	suite.addTest(new CounterExampleTest("testCounterExampleViolatingConditionC2"));
	suite.addTest(new CounterExampleTest("testCounterExample2ViolatingConditionC2"));
	//$JUnit-END$
	return suite;
}
}

package de.fu_berlin.inf.dpp.test.jupiter;

import org.apache.log4j.PropertyConfigurator;



import de.fu_berlin.inf.dpp.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.JupiterTestCase;
import de.fu_berlin.inf.dpp.test.jupiter.text.ServerSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.SimulateNetzwork;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * this class contains test cases for testing init server side and
 * communication with client sides.
 * @author orieger
 *
 */
public class SimpleServerProxyTest extends JupiterTestCase {

	public SimpleServerProxyTest(String method){
		super(method);
	}
	
//	/**
//	 * one client connect with server and create an operation.
//	 * @throws Exception
//	 */
//	public void xtestOneSimpleConnectionWithJupiterProxy() throws Exception{
//		JID jid_c1 = new JID("ori79@jabber.cc");
//		JID jid_server = new JID("ori78@jabber.cc");
//		
//		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abcdefg",
//				network,jid_c1);
//		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument("abcdefg",
//				network,jid_server);
//
//		network.addClient(c1);
//		network.addClient(s1);
//		
//		/* create proxyqueues. */
//		s1.addProxyClient(jid_c1);
//		
//		c1.sendOperation(new InsertOperation(1,"c"),0);
//		Thread.sleep(100);
////		assertEquals(c1.getDocument(), s1.getDocument());
//		assertEquals("acbcdefg",c1.getDocument());
//		
//		s1.sendOperation(new DeleteOperation(1,"cb"));
//		Thread.sleep(300);
//		assertEquals(c1.getDocument(), s1.getDocument());
//		assertEquals("acdefg",c1.getDocument());
//	}
	
	/**
	 * two clients connect with jupiter server.
	 * @throws Exception
	 */
	public void testTwoConcurrentInsertOperations() throws Exception{
		System.out.println("START: testTwoConcurrentInsertOperations");
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("X",
				network,jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument("X",
				network,jid_c2);
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument("X",
				network,jid_server);

		network.addClient(c1);
		network.addClient(c2);
		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
		
		c1.sendOperation(new InsertOperation(0,"a"),2000);
		c2.sendOperation(new InsertOperation(1,"b"),4000);
		Thread.sleep(5000);

		assertEquals("aXb",c1.getDocument());
		assertEquals("aXb",c2.getDocument());
		assertEquals(c1.getDocument(),c2.getDocument());
		System.out.println("END OF METHOD: testTwoConcurrentInsertOperations");
	}

	/**
	 * two clients connect with jupiter server.
	 * @throws Exception
	 */
	public void testThreeConcurrentInsertOperations() throws Exception{
		
		System.out.println("START: testThreeConcurrentInsertOperations");
		
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("X",
				network,jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument("X",
				network,jid_c2);
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument("X",
				network,jid_server);

		network.addClient(c1);
		network.addClient(c2);
		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
		
		/*TODO: Die Operation wird schon beim Server umgewandelt
		 * und muss auf Client-Seite einfach nur entsprechnend
		 * ausgeführt werden.
		 * */
		
		c1.sendOperation(new InsertOperation(0,"a"),1000);
		c1.sendOperation(new InsertOperation(1,"b"),1500);
		c2.sendOperation(new InsertOperation(1,"c"),3000);
		
		Thread.sleep(5500);

//		assertEquals("aXb",c1.getDocument());
//		assertEquals("aXb",c2.getDocument());
		assertEquals(c1.getDocument(),c2.getDocument());
		System.out.println("END OF METHOD: testThreeConcurrentInsertOperations");
	}
	
	/**
	 * two clients connect with jupiter server.
	 * @throws Exception
	 */
	public void testTwoClientWithJupiterProxy() throws Exception{
		
		System.out.println("START: testTwoClientWithJupiterProxy");
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abcdefg",
				network,jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument("abcdefg",
				network,jid_c2);
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument("abcdefg",
				network,jid_server);

		network.addClient(c1);
		network.addClient(c2);
		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
		
		c1.sendOperation(new InsertOperation(1,"c"),0);
		Thread.sleep(1000);

		assertEquals("acbcdefg",c1.getDocument());
		assertEquals("acbcdefg",c2.getDocument());
		
		/* send two concurrent operations. */
		c1.sendOperation(new InsertOperation(1,"x"), 100);
		c2.sendOperation(new InsertOperation(2,"t"), 1000);
		Thread.sleep(500);
		/* assert local execution. */
		assertEquals("axcbcdefg",c1.getDocument());
		assertEquals("axctbcdefg",c2.getDocument());
		Thread.sleep(2500);
		
		/* assert remote operations. */
		/**FAILURE*/
		assertEquals(c1.getDocument(),c2.getDocument());
		
		/* send two concurrent operations. */
		c1.sendOperation(new InsertOperation(1,"t"), 2000);
		c2.sendOperation(new InsertOperation(3,"x"), 6000);
//		c1.sendOperation(new InsertOperation(4,"q"), 2000);
		
		Thread.sleep(6500);
		/* assert remote operations. */
		assertEquals(c1.getDocument(),c2.getDocument());
		System.out.println("END OF METHOD: testTwoClientWithJupiterProxy");
	}
	
	/**
	 * two clients connect with jupiter server.
	 * Concurrent delete and insert operations.
	 * @throws Exception
	 */
	public void testTwoClientWithJupiterProxyDeleteInsertOperations() throws Exception{
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abcdefg",
				network,jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument("abcdefg",
				network,jid_c2);
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument("abcdefg",
				network,jid_server);

		network.addClient(c1);
		network.addClient(c2);
		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
		
//		c1.sendOperation(new InsertOperation(1,"c"),0);
//		Thread.sleep(300);
////		assertEquals(c1.getDocument(), s1.getDocument());
//		assertEquals("acbcdefg",c1.getDocument());
//		assertEquals("acbcdefg",c2.getDocument());
		
		/* send two concurrent operations. */
		c1.sendOperation(new DeleteOperation(0,"abc"), 1000);
		c2.sendOperation(new InsertOperation(1,"t"), 2000);
		
		Thread.sleep(100);
		/* assert local execution. */
		assertEquals("defg",c1.getDocument());
		assertEquals("atbcdefg",c2.getDocument());
		Thread.sleep(2500);
		/* assert remote operations. */
//		assertEquals(s1.getDocument(),c1.getDocument());
		
		
		assertEquals(c1.getDocument(),c2.getDocument());
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for Proxy Test.");
		//$JUnit-BEGIN$
		suite.addTest(new SimpleServerProxyTest("testTwoConcurrentInsertOperations"));
		suite.addTest(new SimpleServerProxyTest("testTwoClientWithJupiterProxy"));
		suite.addTest(new SimpleServerProxyTest("testThreeConcurrentInsertOperations"));
		suite.addTest(new SimpleServerProxyTest("testTwoClientWithJupiterProxyDeleteInsertOperations"));
		//$JUnit-END$
		return suite;
	}
	
}

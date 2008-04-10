package de.fu_berlin.inf.dpp.test.jupiter;

import de.fu_berlin.inf.dpp.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument2;
import de.fu_berlin.inf.dpp.test.jupiter.text.ServerSynchronizedDocument2;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * this test case simulate the dOPT Puzzle scenario.
 * @author troll
 *
 */
public class DOptPuzzleTest extends JupiterTestCase{
	
	public DOptPuzzleTest(String method){
		super(method);
	}

	/**
	 * dOPT puzzle scenario with three sides.
	 * @throws Exception
	 */
	public void testThreeConcurrentInsertOperations() throws Exception{
		System.out.println("START: testTwoConcurrentInsertOperations");
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_c3 = new JID("ori81@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		ClientSynchronizedDocument2 c1 = new ClientSynchronizedDocument2("abcd",
				network,jid_c1);
		ClientSynchronizedDocument2 c2 = new ClientSynchronizedDocument2("abcd",
				network,jid_c2);
		ClientSynchronizedDocument2 c3 = new ClientSynchronizedDocument2("abcd",
				network,jid_c3);
		ServerSynchronizedDocument2 s1 = new ServerSynchronizedDocument2("abcd",
				network,jid_server);

		network.addClient(c1);
		network.addClient(c2);
		network.addClient(c3);
		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
		s1.addProxyClient(jid_c3);
		
		Thread.sleep(1500);
		c3.sendOperation(new InsertOperation(0,"z"),100);
		c2.sendOperation(new InsertOperation(0,"x"),2000);
		Thread.sleep(300);
		c1.sendOperation(new InsertOperation(0,"y"),100);
		
		
		Thread.sleep(3000);
		
		/* cefg befg .*/
		assertEquals(c1.getDocument(),c2.getDocument());
		
		assertEquals(c2.getDocument(),c3.getDocument());
		System.out.println(c1.getDocument());
	}
	
	/**
	 * dOPT puzzle scenario with three sides.
	 * @throws Exception
	 */
	public void testTwoConcurrentDeleteOperations() throws Exception{
		System.out.println("START: testTwoConcurrentInsertOperations");
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_c3 = new JID("ori81@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		ClientSynchronizedDocument2 c1 = new ClientSynchronizedDocument2("abcdefg",
				network,jid_c1);
		ClientSynchronizedDocument2 c2 = new ClientSynchronizedDocument2("abcdefg",
				network,jid_c2);
		ClientSynchronizedDocument2 c3 = new ClientSynchronizedDocument2("abcdefg",
				network,jid_c3);
		ServerSynchronizedDocument2 s1 = new ServerSynchronizedDocument2("abcdefg",
				network,jid_server);

		network.addClient(c1);
		network.addClient(c2);
		network.addClient(c3);
		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
		s1.addProxyClient(jid_c3);
		
		Thread.sleep(1500);
		c1.sendOperation(new DeleteOperation(0,"a"),0);
		Thread.sleep(1000);
		c3.sendOperation(new DeleteOperation(0,"b"),0);
		c2.sendOperation(new DeleteOperation(3,"e"),0);
		
		Thread.sleep(5000);
		
		/* cefg befg .*/
		assertEquals(c1.getDocument(),c2.getDocument());
		
		assertEquals(c2.getDocument(),c3.getDocument());
		System.out.println(c1.getDocument());
		System.out.println("END OF METHOD: testTwoConcurrentInsertOperations");
	}
	
	/**
	 * dOPT puzzle scenario with three sides.
	 * @throws Exception
	 */
	public void testTwoConcurrentInsertOperations() throws Exception{
		System.out.println("START: testTwoConcurrentInsertOperations");
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_c3 = new JID("ori81@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		ClientSynchronizedDocument2 c1 = new ClientSynchronizedDocument2("abc",
				network,jid_c1);
		ClientSynchronizedDocument2 c2 = new ClientSynchronizedDocument2("abc",
				network,jid_c2);
		ClientSynchronizedDocument2 c3 = new ClientSynchronizedDocument2("abc",
				network,jid_c3);
		ServerSynchronizedDocument2 s1 = new ServerSynchronizedDocument2("abc",
				network,jid_server);

		network.addClient(c1);
		network.addClient(c2);
		network.addClient(c3);
		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
		s1.addProxyClient(jid_c3);
		
		
		c1.sendOperation(new InsertOperation(0,"a"),0);
		c2.sendOperation(new InsertOperation(1,"b"),100);
		
		Thread.sleep(1500);
		c3.sendOperation(new InsertOperation(2,"x"),1000);
		c2.sendOperation(new InsertOperation(2,"by"),100);
		c1.sendOperation(new InsertOperation(1,"xxx"),600);
		
		Thread.sleep(4000);
		assertEquals(c1.getDocument(),c2.getDocument());
		assertEquals(c2.getDocument(),c3.getDocument());
		System.out.println(c1.getDocument());
		/* xyz != yxz */
//		assertEquals(c2.getDocument(),c3.getDocument());
		System.out.println("END OF METHOD: testTwoConcurrentInsertOperations");
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for dOPT puzzle.");
		//$JUnit-BEGIN$
//		suite.addTest(new DOptPuzzleTest("testTwoConcurrentInsertOperations"));
//		suite.addTest(new DOptPuzzleTest("testTwoConcurrentDeleteOperations"));
		suite.addTest(new DOptPuzzleTest("testThreeConcurrentInsertOperations"));
		//$JUnit-END$
		return suite;
	}

}

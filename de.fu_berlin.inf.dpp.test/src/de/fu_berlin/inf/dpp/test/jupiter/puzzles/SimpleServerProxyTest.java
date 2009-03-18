package de.fu_berlin.inf.dpp.test.jupiter.puzzles;

import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.JupiterTestCase;
import de.fu_berlin.inf.dpp.test.jupiter.text.ServerSynchronizedDocument;

/**
 * this class contains test cases for testing init server side and communication
 * with client sides.
 * 
 * @author orieger
 * 
 */
public class SimpleServerProxyTest extends JupiterTestCase {

	public SimpleServerProxyTest() {
		super();
		setName("Test for Proxy Test.");
	}

	JID jid_c1 = new JID("Alice");
	JID jid_c2 = new JID("Bob");
	JID jid_server = new JID("Carl");

	ClientSynchronizedDocument alice;
	ClientSynchronizedDocument bob;
	ServerSynchronizedDocument s1;
	
	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		super.setUp();
		
		// 01234567890123456789012345
		// abcdefghijklmnopqrstuvwxyz

		alice = new ClientSynchronizedDocument(jid_server,
				"abcdefghijklmnopqrstuvwxyz", network, jid_c1);
		bob = new ClientSynchronizedDocument(jid_server,
				"abcdefghijklmnopqrstuvwxyz", network, jid_c2);
		s1 = new ServerSynchronizedDocument(network,
				jid_server);

		network.addClient(alice);
		network.addClient(bob);
		network.addClient(s1);

		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
	}
	
	// /**
	// * one client connect with server and create an operation.
	// * @throws Exception
	// */
	// public void xtestOneSimpleConnectionWithJupiterProxy() throws Exception{
	// JID jid_c1 = new JID("ori79@jabber.cc");
	// JID jid_server = new JID("ori78@jabber.cc");
	//		
	// ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abcdefg",
	// network,jid_c1);
	// ServerSynchronizedDocument s1 = new ServerSynchronizedDocument("abcdefg",
	// network,jid_server);
	//
	// network.addClient(c1);
	// network.addClient(s1);
	//		
	// /* create proxyqueues. */
	// s1.addProxyClient(jid_c1);
	//		
	// c1.sendOperation(new InsertOperation(1,"c"),0);
	// Thread.sleep(100);
	// // assertEquals(c1.getDocument(), s1.getDocument());
	// assertEquals("acbcdefg",c1.getDocument());
	//		
	// s1.sendOperation(new DeleteOperation(1,"cb"));
	// Thread.sleep(300);
	// assertEquals(c1.getDocument(), s1.getDocument());
	// assertEquals("acdefg",c1.getDocument());
	// }

	/**
	 * two clients connect with jupiter server.
	 * 
	 * @throws Exception
	 */
	public void testTwoConcurrentInsertOperations() throws Exception {
		System.out.println("START: testTwoConcurrentInsertOperations");
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");

		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("X",
				network, jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument("X",
				network, jid_c2);
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(network,
				jid_server);

		network.addClient(c1);
		network.addClient(c2);
		network.addClient(s1);

		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);

		c1.sendOperation(new InsertOperation(0, "a"), 100);
		c2.sendOperation(new InsertOperation(1, "b"), 200);
		Thread.sleep(300);

		assertEquals("aXb", c1.getDocument());
		assertEquals("aXb", c2.getDocument());
		assertEquals(c1.getDocument(), c2.getDocument());
		System.out.println("END OF METHOD: testTwoConcurrentInsertOperations");
	}

	/**
	 * two clients connect with jupiter server.
	 * 
	 */
	public void testThreeConcurrentInsertOperations() throws Exception {

		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");

		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("X",
				network, jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument("X",
				network, jid_c2);
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(network,
				jid_server);

		network.addClient(c1);
		network.addClient(c2);
		network.addClient(s1);

		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);

		c1.sendOperation(new InsertOperation(0, "a"), 100);
		c1.sendOperation(new InsertOperation(1, "b"), 200);
		c2.sendOperation(new InsertOperation(1, "c"), 300);

		Thread.sleep(500);

		assertEquals(c1.getDocument(), c2.getDocument());
		assertEquals("abXc", c1.getDocument());
	}

	/**
	 * two clients connect with jupiter server.
	 * 
	 * @throws Exception
	 */
	public void testTwoClientWithJupiterProxy() throws Exception {

		System.out.println("START: testTwoClientWithJupiterProxy");
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");

		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument(
				"abcdefg", network, jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument(
				"abcdefg", network, jid_c2);
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(network,
				jid_server);

		network.addClient(c1);
		network.addClient(c2);
		network.addClient(s1);

		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);

		c1.sendOperation(new InsertOperation(1, "c"), 0);
		Thread.sleep(200);

		assertEquals("acbcdefg", c1.getDocument());
		assertEquals("acbcdefg", c2.getDocument());

		/* send two concurrent operations. */
		c1.sendOperation(new InsertOperation(1, "x"), 100);
		c2.sendOperation(new InsertOperation(2, "t"), 1000);
		Thread.sleep(500);
		/* assert local execution. */
		assertEquals("axcbcdefg", c1.getDocument());
		assertEquals("axctbcdefg", c2.getDocument());
		Thread.sleep(2500);

		/* assert remote operations. */
		/** FAILURE */
		assertEquals(c1.getDocument(), c2.getDocument());

		/* send two concurrent operations. */
		c1.sendOperation(new InsertOperation(1, "t"), 2000);
		c2.sendOperation(new InsertOperation(3, "x"), 6000);
		// c1.sendOperation(new InsertOperation(4,"q"), 2000);

		Thread.sleep(6500);
		/* assert remote operations. */
		assertEquals(c1.getDocument(), c2.getDocument());
		System.out.println("END OF METHOD: testTwoClientWithJupiterProxy");
	}

	/**
	 * two clients connect with jupiter server. Concurrent delete and insert
	 * operations.
	 * 
	 * @throws Exception
	 */
	public void testTwoClientWithJupiterProxyDeleteInsertOperations()
			throws Exception {
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");

		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument(
				"abcdefg", network, jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument(
				"abcdefg", network, jid_c2);
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(network,
				jid_server);

		network.addClient(c1);
		network.addClient(c2);
		network.addClient(s1);

		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);

		/* send two concurrent operations. */
		c1.sendOperation(new DeleteOperation(0, "abc"), 300);
		c2.sendOperation(new InsertOperation(1, "t"), 400);

		Thread.sleep(100);
		/* assert local execution. */
		assertEquals("defg", c1.getDocument());
		assertEquals("atbcdefg", c2.getDocument());
		Thread.sleep(500);
		/* assert remote operations. */
		assertEquals(c1.getDocument(), c2.getDocument());
	}

	public static void assertEqualDocs(String s,
			ClientSynchronizedDocument... docs) {

		for (int i = 0; i < docs.length; i++) {
			assertEquals("Client " + docs[i].getJID() + " is wrong", s, docs[i].getDocument());
		}
	}

	public void testSplitOperations() throws Exception {

		alice.sendOperation(new SplitOperation(new DeleteOperation(4, "efg"),
				new InsertOperation(4, "123")), 100);
		alice.sendOperation(new SplitOperation(new DeleteOperation(8, "ijk"),
				new InsertOperation(8, "789")), 200);
		bob.sendOperation(new SplitOperation(new DeleteOperation(6, "ghi"),
				new InsertOperation(6, "456")), 200);

		Thread.sleep(500);

		assertTrue("" + network.getLastError(), network.getLastError() == null);
		                
		assertEqualDocs("abcd123456789lmnopqrstuvwxyz", alice, bob);
	}

	public void testDeleteOperations() throws Exception {

		alice.sendOperation(new DeleteOperation(4, "efg"), 100);
		alice.sendOperation(new DeleteOperation(5, "ijk"), 200);
		bob.sendOperation(new DeleteOperation(6, "ghi"), 200);

		Thread.sleep(500);

		assertTrue("" + network.getLastError(), network.getLastError() == null);
		                
		assertEqualDocs("abcdlmnopqrstuvwxyz", alice, bob);
	}
	
	public void testDeleteSplitOperations() throws Exception {

		// 01234567890123456789012345
		// abcdefghijklmnopqrstuvwxyz
		
		alice.sendOperation(new SplitOperation(new DeleteOperation(4, "efg"), new InsertOperation(4, "123")), 100);
		assertEqualDocs("abcd123hijklmnopqrstuvwxyz", alice);
		alice.sendOperation(new DeleteOperation(8, "ijk"), 200);
		assertEqualDocs("abcd123hlmnopqrstuvwxyz", alice);
		bob.sendOperation(new DeleteOperation(6, "ghi"), 200);
		assertEqualDocs("abcdefjklmnopqrstuvwxyz", bob);

		Thread.sleep(300);

		assertTrue("" + network.getLastError(), network.getLastError() == null);
		                
		assertEqualDocs("abcd123lmnopqrstuvwxyz", alice, bob);
	}

	public void testDeleteSplitOperations2() throws Exception {
		
		// 01234567890123456789012345
		// abcdefghijklmnopqrstuvwxyz
		
		alice.sendOperation(new SplitOperation(new DeleteOperation(8, "ijk"), new InsertOperation(8, "789")), 100);
		assertEqualDocs("abcdefgh789lmnopqrstuvwxyz", alice);
		alice.sendOperation(new DeleteOperation(4, "efg"), 200);
		assertEqualDocs("abcdh789lmnopqrstuvwxyz", alice);
		bob.sendOperation(new DeleteOperation(6, "ghi"), 200);
		assertEqualDocs("abcdefjklmnopqrstuvwxyz", bob);

		Thread.sleep(300);

		assertTrue("" + network.getLastError(), network.getLastError() == null);
		                
		assertEqualDocs("abcd789lmnopqrstuvwxyz", alice, bob);
	}

	
	public void testDeleteInsertDelete() throws Exception {

		// 01234567890123456789012345
		// abcdefghijklmnopqrstuvwxyz
		
		alice.sendOperation(new DeleteOperation(4, "efg"), 100);
		alice.sendOperation(new InsertOperation(4, "123"), 200);
		alice.sendOperation(new DeleteOperation(8, "ijk"), 300);
		bob.sendOperation(new DeleteOperation(6, "ghi"), 300);

		Thread.sleep(400);

		assertTrue("" + network.getLastError(), network.getLastError() == null);
		                
		assertEqualDocs("abcd123lmnopqrstuvwxyz", alice, bob);
	}

	
	public void testInsertInsideDelete() throws Exception {

		alice.sendOperation(new DeleteOperation(4, "efgh"), 100);
		bob.sendOperation(new InsertOperation(6, "12"), 200);

		Thread.sleep(2000);

		assertTrue("" + network.getLastError(), network.getLastError() == null);
		                
		assertEqualDocs("abcd12ijklmnopqrstuvwxyz", alice, bob);
	}
		
}

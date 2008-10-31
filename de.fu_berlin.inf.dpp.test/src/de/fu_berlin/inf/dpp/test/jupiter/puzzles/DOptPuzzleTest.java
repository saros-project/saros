package de.fu_berlin.inf.dpp.test.jupiter.puzzles;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.server.impl.ConcurrentManager;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.JupiterTestCase;
import de.fu_berlin.inf.dpp.test.jupiter.text.ServerSynchronizedDocument;

/**
 * this test case simulate the unsolved dOPT Puzzle scenario which described in
 * Fig. 2 in "Operational Transformation in Real-Time Group Editors: Issues,
 * Algorithm, Achievements", Sun et.al.
 * 
 * @author orieger
 * 
 */
public class DOptPuzzleTest extends JupiterTestCase {

    public DOptPuzzleTest(String method) {
	super(method);
    }

    /**
     * dOPT puzzle scenario with three sides and three concurrent insert
     * operations of a character at the same position.
     * 
     * @throws Exception
     */
    public void testThreeConcurrentInsertOperations() throws Exception {
	JID jid_c1 = new JID("ori79@jabber.cc");
	JID jid_c2 = new JID("ori80@jabber.cc");
	JID jid_c3 = new JID("ori81@jabber.cc");
	JID jid_server = new JID("ori78@jabber.cc");

	/* init simulated client and server components. */
	ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abcd",
		network, jid_c1);
	ClientSynchronizedDocument c2 = new ClientSynchronizedDocument("abcd",
		network, jid_c2);
	ClientSynchronizedDocument c3 = new ClientSynchronizedDocument("abcd",
		network, jid_c3);
	// ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(
	// network,jid_server);
	ConcurrentManager s1 = new ConcurrentManager(network, jid_server);

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

	/* O3 || O2 */
	c3.sendOperation(new InsertOperation(0, "z"), 100);
	c2.sendOperation(new InsertOperation(0, "x"), 2000);

	Thread.sleep(300);
	/* O1 -> O3 */
	c1.sendOperation(new InsertOperation(0, "y"), 100);

	Thread.sleep(3000);

	assertEquals(c1.getDocument(), c2.getDocument());
	assertEquals(c2.getDocument(), c3.getDocument());
	System.out.println(c1.getDocument());
    }

    /**
     * dOPT puzzle scenario with three sides and three concurrent insert
     * operations of a character at the same position.
     * 
     * @throws Exception
     */
    public void testThreeConcurrentInsertStringOperations() throws Exception {
	JID jid_c1 = new JID("ori79@jabber.cc");
	JID jid_c2 = new JID("ori80@jabber.cc");
	JID jid_c3 = new JID("ori81@jabber.cc");
	JID jid_server = new JID("ori78@jabber.cc");

	/* init simulated client and server components. */
	ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abcd",
		network, jid_c1);
	ClientSynchronizedDocument c2 = new ClientSynchronizedDocument("abcd",
		network, jid_c2);
	ClientSynchronizedDocument c3 = new ClientSynchronizedDocument("abcd",
		network, jid_c3);
	ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(network,
		jid_server);

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

	/* O3 || O2 */
	c3.sendOperation(new InsertOperation(0, "zzz"), 100);
	c2.sendOperation(new InsertOperation(0, "x"), 2000);

	Thread.sleep(300);
	/* O1 -> O3 */
	c1.sendOperation(new InsertOperation(0, "yy"), 100);

	Thread.sleep(3000);

	assertEquals(c1.getDocument(), c2.getDocument());
	assertEquals(c2.getDocument(), c3.getDocument());
	System.out.println(c1.getDocument());
    }

    /**
     * dOPT puzzle scenario with three sides and three concurrent delete
     * operations.
     * 
     * @throws Exception
     */
    public void testThreeConcurrentDeleteOperations() throws Exception {
	JID jid_c1 = new JID("ori79@jabber.cc");
	JID jid_c2 = new JID("ori80@jabber.cc");
	JID jid_c3 = new JID("ori81@jabber.cc");
	JID jid_server = new JID("ori78@jabber.cc");

	ClientSynchronizedDocument c1 = new ClientSynchronizedDocument(
		"abcdefg", network, jid_c1);
	ClientSynchronizedDocument c2 = new ClientSynchronizedDocument(
		"abcdefg", network, jid_c2);
	ClientSynchronizedDocument c3 = new ClientSynchronizedDocument(
		"abcdefg", network, jid_c3);
	ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(network,
		jid_server);

	network.addClient(c1);
	network.addClient(c2);
	network.addClient(c3);
	network.addClient(s1);

	/* create proxyqueues. */
	s1.addProxyClient(jid_c1);
	s1.addProxyClient(jid_c2);
	s1.addProxyClient(jid_c3);

	Thread.sleep(1500);
	c1.sendOperation(new DeleteOperation(0, "a"), 100);
	Thread.sleep(1000);
	c3.sendOperation(new DeleteOperation(1, "abc"), 500);
	c2.sendOperation(new DeleteOperation(3, "e"), 300);

	Thread.sleep(5000);

	/* cefg befg . */
	assertEquals(c1.getDocument(), c2.getDocument());

	assertEquals(c2.getDocument(), c3.getDocument());
	System.out.println(c1.getDocument());
    }

    /**
     * dOPT puzzle scenario with three sides and insert / delete operations.
     * 
     * @throws Exception
     */
    public void testConcurrentInsertDeleteOperations() throws Exception {
	JID jid_c1 = new JID("ori79@jabber.cc");
	JID jid_c2 = new JID("ori80@jabber.cc");
	JID jid_c3 = new JID("ori81@jabber.cc");
	JID jid_server = new JID("ori78@jabber.cc");

	ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abc",
		network, jid_c1);
	ClientSynchronizedDocument c2 = new ClientSynchronizedDocument("abc",
		network, jid_c2);
	ClientSynchronizedDocument c3 = new ClientSynchronizedDocument("abc",
		network, jid_c3);
	ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(network,
		jid_server);

	network.addClient(c1);
	network.addClient(c2);
	network.addClient(c3);
	network.addClient(s1);

	/* create proxyqueues. */
	s1.addProxyClient(jid_c1);
	s1.addProxyClient(jid_c2);
	s1.addProxyClient(jid_c3);

	c1.sendOperation(new InsertOperation(0, "a"), 0);
	c2.sendOperation(new InsertOperation(1, "b"), 100);

	Thread.sleep(500);
	c3.sendOperation(new DeleteOperation(1, "ab"), 1000);
	c2.sendOperation(new InsertOperation(2, "by"), 100);
	c1.sendOperation(new InsertOperation(1, "x"), 600);

	Thread.sleep(4000);
	assertEquals(c1.getDocument(), c2.getDocument());
	assertEquals(c2.getDocument(), c3.getDocument());
	System.out.println(c1.getDocument());

    }

    public static Test suite() {
	TestSuite suite = new TestSuite("Test for dOPT puzzle.");
	// $JUnit-BEGIN$
	suite
		.addTest(new DOptPuzzleTest(
			"testThreeConcurrentInsertOperations"));
	suite.addTest(new DOptPuzzleTest(
		"testThreeConcurrentInsertStringOperations"));
	suite
		.addTest(new DOptPuzzleTest(
			"testThreeConcurrentDeleteOperations"));
	suite
		.addTest(new DOptPuzzleTest(
			"testConcurrentInsertDeleteOperations"));
	// $JUnit-END$
	return suite;
    }

}

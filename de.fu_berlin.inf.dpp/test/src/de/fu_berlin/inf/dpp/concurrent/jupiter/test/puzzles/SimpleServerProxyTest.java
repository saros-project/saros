package de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles;

import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterTestCase;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.ServerSynchronizedDocument;

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

    ClientSynchronizedDocument alice, c1;
    ClientSynchronizedDocument bob, c2;
    ServerSynchronizedDocument server, s1;

    public void setUp(String text) {

        // 01234567890123456789012345
        // abcdefghijklmnopqrstuvwxyz

        c1 = alice = new ClientSynchronizedDocument(jidServer, text, network,
            jidAlice);
        c2 = bob = new ClientSynchronizedDocument(jidServer, text, network,
            jidBob);
        s1 = server = new ServerSynchronizedDocument(network, jidServer);

        network.addClient(alice);
        network.addClient(bob);
        network.addClient(server);

        /* create proxyqueues. */
        server.addProxyClient(jidAlice);
        server.addProxyClient(jidBob);
    }

    /**
     * two clients connect with jupiter server.
     * 
     * @throws Exception
     */
    public void testTwoConcurrentInsertOperations() throws Exception {
        System.out.println("START: testTwoConcurrentInsertOperations");
        setUp("X");

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

        setUp("X");

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

        setUp("abcdefg");

        c1.sendOperation(new InsertOperation(1, "c"), 0);
        Thread.sleep(200);

        assertEquals("acbcdefg", c1.getDocument());
        assertEquals("acbcdefg", c2.getDocument());

        /* send two concurrent operations. */
        c1.sendOperation(new InsertOperation(1, "x"), 100);
        c2.sendOperation(new InsertOperation(2, "t"), 500);
        Thread.sleep(300);
        /* assert local execution. */
        assertEquals("axcbcdefg", c1.getDocument());
        assertEquals("axctbcdefg", c2.getDocument());
        Thread.sleep(500);

        /* assert remote operations. */
        assertEquals(c1.getDocument(), c2.getDocument());

        /* send two concurrent operations. */
        c1.sendOperation(new InsertOperation(1, "t"), 100);
        c2.sendOperation(new InsertOperation(3, "x"), 300);

        Thread.sleep(500);
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

        setUp("abcdefg");

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
}

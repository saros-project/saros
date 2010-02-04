package de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
    }

    protected ClientSynchronizedDocument client_1;
    protected ClientSynchronizedDocument client_2;
    protected ServerSynchronizedDocument server;

    // FIXME: This should not have any parameters
    public void setUp(String text) {
        super.setup();

        // 01234567890123456789012345
        // abcdefghijklmnopqrstuvwxyz

        client_1 = new ClientSynchronizedDocument(host.getJID(), text, network,
            alice);
        client_2 = new ClientSynchronizedDocument(host.getJID(), text, network,
            bob);
        server = new ServerSynchronizedDocument(network, host);

        network.addClient(client_1);
        network.addClient(client_2);
        network.addClient(server);

        /* create proxyqueues. */
        server.addProxyClient(alice);
        server.addProxyClient(bob);
    }

    /**
     * two clients connect with jupiter server.
     * 
     * @throws Exception
     */
    @Test
    public void testTwoConcurrentInsertOperations() throws Exception {
        System.out.println("START: testTwoConcurrentInsertOperations");
        setUp("X");

        client_1.sendOperation(new InsertOperation(0, "a"), 100);
        client_2.sendOperation(new InsertOperation(1, "b"), 200);
        Thread.sleep(400);

        assertEquals("aXb", client_1.getDocument());
        assertEquals("aXb", client_2.getDocument());
        assertEquals(client_1.getDocument(), client_2.getDocument());
        System.out.println("END OF METHOD: testTwoConcurrentInsertOperations");
    }

    /**
     * two clients connect with jupiter server.
     * 
     */
    @Test
    public void testThreeConcurrentInsertOperations() throws Exception {

        setUp("X");

        client_1.sendOperation(new InsertOperation(0, "a"), 100);
        client_1.sendOperation(new InsertOperation(1, "b"), 200);
        client_2.sendOperation(new InsertOperation(1, "c"), 300);

        Thread.sleep(500);

        assertEquals(client_1.getDocument(), client_2.getDocument());
        assertEquals("abXc", client_1.getDocument());
    }

    /**
     * two clients connect with jupiter server.
     * 
     * @throws Exception
     */
    @Test
    public void testTwoClientWithJupiterProxy() throws Exception {

        setUp("abcdefg");

        client_1.sendOperation(new InsertOperation(1, "c"), 0);
        Thread.sleep(200);

        assertEquals("acbcdefg", client_1.getDocument());
        assertEquals("acbcdefg", client_2.getDocument());

        /* send two concurrent operations. */
        client_1.sendOperation(new InsertOperation(1, "x"), 100);
        client_2.sendOperation(new InsertOperation(2, "t"), 500);
        Thread.sleep(300);
        /* assert local execution. */
        assertEquals("axcbcdefg", client_1.getDocument());
        assertEquals("axctbcdefg", client_2.getDocument());
        Thread.sleep(500);

        /* assert remote operations. */
        assertEquals(client_1.getDocument(), client_2.getDocument());

        /* send two concurrent operations. */
        client_1.sendOperation(new InsertOperation(1, "t"), 100);
        client_2.sendOperation(new InsertOperation(3, "x"), 300);

        Thread.sleep(500);
        /* assert remote operations. */
        assertEquals(client_1.getDocument(), client_2.getDocument());
        System.out.println("END OF METHOD: testTwoClientWithJupiterProxy");
    }

    /**
     * two clients connect with jupiter server. Concurrent delete and insert
     * operations.
     * 
     * @throws Exception
     */
    @Test
    public void testTwoClientWithJupiterProxyDeleteInsertOperations()
        throws Exception {

        setUp("abcdefg");

        /* send two concurrent operations. */
        client_1.sendOperation(new DeleteOperation(0, "abc"), 300);
        client_2.sendOperation(new InsertOperation(1, "t"), 500);

        Thread.sleep(100);
        /* assert local execution. */
        assertEquals("defg", client_1.getDocument());
        assertEquals("atbcdefg", client_2.getDocument());
        Thread.sleep(500);
        /* assert remote operations. */
        assertEquals(client_1.getDocument(), client_2.getDocument());
    }
}

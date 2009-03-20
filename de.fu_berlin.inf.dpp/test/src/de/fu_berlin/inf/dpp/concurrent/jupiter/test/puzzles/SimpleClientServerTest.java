package de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles;

import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterTestCase;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.TwoWayJupiterClientDocument;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.TwoWayJupiterServerDocument;

public class SimpleClientServerTest extends JupiterTestCase {

    ClientSynchronizedDocument client;
    TwoWayJupiterServerDocument server;

    public void setUp(String initialText) {
        super.setUp();

        client = new TwoWayJupiterClientDocument(initialText, network);
        server = new TwoWayJupiterServerDocument(initialText, network);

        network.addClient(client);
        network.addClient(server);
    }

    /**
     * simple test scenario between server and client. The client and server
     * send operation from same state. Server message has delay, so that client
     * create a new Operation. So if server message arrive client and server
     * have different document state and jupiter algorithm has to converge the
     * document states.
     */
    public void test2WayProtocol() throws Exception {

        setUp("abc");

        client.sendOperation(new InsertOperation(0, "e"), 500);

        Thread.sleep(100);
        assertEquals("eabc", client.getDocument());
        assertEquals("abc", server.getDocument());

        client.sendOperation(new InsertOperation(0, "x"), 700);

        Thread.sleep(100);
        assertEquals("xeabc", client.getDocument());
        assertEquals("abc", server.getDocument());

        server.sendOperation(client.getJID(), new DeleteOperation(0, "a"), 0);

        Thread.sleep(100);
        assertEquals("xebc", client.getDocument());
        assertEquals("bc", server.getDocument());

        Thread.sleep(300); // 1st Client operation arrives
        assertEquals("ebc", server.getDocument());

        Thread.sleep(300); // Server operation arrives
        assertEqualDocs("xebc", client, server);
    }

    /**
     * Site A insert a char into the delete area of site b. The delete operation
     * has delay of two seconds.
     */
    public void testDeleteStringWithConcurentInsert() throws Exception {

        setUp("abcdefg");

        client.sendOperation(new InsertOperation(3, "x"), 100);
        server.sendOperation(client.getJID(), new DeleteOperation(1, "bcde"),
            400);
        Thread.sleep(300);
        assertEquals("abcxdefg", client.getDocument());

        Thread.sleep(200);

        assertEqualDocs("axfg", client, server);
    }

    /**
     * Client insert a char into the delete area of Server.
     */
    public void testInsertStringWithConcurentDelete() throws Exception {

        setUp("abcdefg");

        client.sendOperation(new InsertOperation(3, "x"), 300);
        server
            .sendOperation(client.getJID(), new DeleteOperation(1, "bcde"), 0);
        Thread.sleep(100);
        assertEquals("afg", server.getDocument());

        Thread.sleep(500);

        assertEqualDocs("axfg", client, server);
    }
}

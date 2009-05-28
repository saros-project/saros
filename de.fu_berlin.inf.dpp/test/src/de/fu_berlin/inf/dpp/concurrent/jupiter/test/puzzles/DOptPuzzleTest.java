package de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles;

import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterTestCase;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.ServerSynchronizedDocument;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * this test case simulate the unsolved dOPT Puzzle scenario which described in
 * Fig. 2 in "Operational Transformation in Real-Time Group Editors: Issues,
 * Algorithm, Achievements", Sun et.al.
 * 
 * @author orieger
 * 
 */
public class DOptPuzzleTest extends JupiterTestCase {

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
        ClientSynchronizedDocument c1 = new ClientSynchronizedDocument(
            jid_server, "abcd", network, jid_c1);
        ClientSynchronizedDocument c2 = new ClientSynchronizedDocument(
            jid_server, "abcd", network, jid_c2);
        ClientSynchronizedDocument c3 = new ClientSynchronizedDocument(
            jid_server, "abcd", network, jid_c3);
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
        c3.sendOperation(new InsertOperation(0, "z"), 100);
        c2.sendOperation(new InsertOperation(0, "x"), 700);

        Thread.sleep(300);
        /* O1 -> O3 */
        c1.sendOperation(new InsertOperation(0, "y"), 100);

        Thread.sleep(700);

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

        ClientSynchronizedDocument[] c = setUp(3, "abcd");

        /* O2 || O1 */
        c[2].sendOperation(new InsertOperation(0, "zzz"), 100);
        c[1].sendOperation(new InsertOperation(0, "x"), 700);

        Thread.sleep(300);
        /* O0 -> O2 */
        c[0].sendOperation(new InsertOperation(0, "yy"), 100);

        Thread.sleep(700);

        assertEqualDocs("yyzzzxabcd", c);
    }

    /**
     * dOPT puzzle scenario with three sides and three concurrent delete
     * operations.
     * 
     * @throws Exception
     */
    public void testThreeConcurrentDeleteOperations() throws Exception {

        ClientSynchronizedDocument[] c = setUp(3, "abcdefg");

        c[0].sendOperation(new DeleteOperation(0, "a"), 100);
        Thread.sleep(300);
        c[2].sendOperation(new DeleteOperation(1, "cde"), 500);
        c[1].sendOperation(new DeleteOperation(3, "e"), 300);

        Thread.sleep(1000);

        assertEqualDocs("bfg", c);
    }

    /**
     * dOPT puzzle scenario with three sides and insert / delete operations.
     * 
     * @throws Exception
     */
    public void testConcurrentInsertDeleteOperations() throws Exception {

        ClientSynchronizedDocument[] c = setUp(3, "abc");

        c[0].sendOperation(new InsertOperation(0, "a"), 0);
        c[1].sendOperation(new InsertOperation(1, "b"), 100);

        Thread.sleep(200);
        c[2].sendOperation(new DeleteOperation(1, "ab"), 700);
        c[1].sendOperation(new InsertOperation(2, "by"), 100);
        c[0].sendOperation(new InsertOperation(1, "x"), 400);

        Thread.sleep(1000);

        assertEqualDocs("axbybc", c);
    }
}

package de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles;

import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterTestCase;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.SimulateNetzwork;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.TwoWayJupiterClientDocument;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.TwoWayJupiterServerDocument;

/**
 * Test class contains all possible transformation of operations.
 * 
 * @author orieger
 * 
 */
public class InclusionTransformationTest extends JupiterTestCase {

    TwoWayJupiterClientDocument client;
    TwoWayJupiterServerDocument server;

    @Override
    public void setUp() {
        setUp("abcdefg");
    }

    public void setUp(String initialText) {
        network = new SimulateNetzwork();

        client = new TwoWayJupiterClientDocument(initialText, network);
        server = new TwoWayJupiterServerDocument(initialText, network);

        network.addClient(client);
        network.addClient(server);
    }

    /**
     * insert before insert
     * 
     * @throws Exception
     */
    public void testCase1() throws Exception {
        client.sendOperation(new InsertOperation(0, "x"), 100);
        server.sendOperation(new InsertOperation(0, "y"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());

        client.sendOperation(new InsertOperation(0, "x"), 100);
        server.sendOperation(new InsertOperation(1, "y"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals(client.getDocument(), "xyyxabcdefg");
    }

    /**
     * insert after insert
     * 
     * @throws Exception
     */
    public void testCase2() throws Exception {
        client.sendOperation(new InsertOperation(1, "xx"), 100);
        server.sendOperation(new DeleteOperation(0, "abc"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());

        client.sendOperation(new InsertOperation(2, "x"), 100);
        server.sendOperation(new InsertOperation(1, "y"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals(client.getDocument(), "xyxxdefg");
    }

    /**
     * insert before delete operation
     * 
     * @throws Exception
     */
    public void testCase3() throws Exception {
        client.sendOperation(new InsertOperation(1, "x"), 100);
        server.sendOperation(new DeleteOperation(2, "c"), 200);

        Thread.sleep(400);

        assertEquals(client.getDocument(), server.getDocument());

        client.sendOperation(new InsertOperation(0, "y"), 100);
        server.sendOperation(new DeleteOperation(0, "a"), 200);

        Thread.sleep(400);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals(client.getDocument(), "yxbdefg");
    }

    /**
     * insert after delete operation
     * 
     * @throws Exception
     */
    public void testCase4() throws Exception {
        client.sendOperation(new InsertOperation(1, "x"), 100);
        server.sendOperation(new DeleteOperation(0, "a"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals(client.getDocument(), "xbcdefg");
    }

    /**
     * insert operation inside delete operation area
     * 
     * @throws Exception
     */
    public void testCase5() throws Exception {
        client.sendOperation(new InsertOperation(1, "x"), 100);
        server.sendOperation(new DeleteOperation(0, "abc"), 200);

        Thread.sleep(400);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals(client.getDocument(), "xdefg");
    }

    /**
     * insert operation after delete operation
     * 
     * @throws Exception
     */
    public void testCase6() throws Exception {
        client.sendOperation(new DeleteOperation(0, "a"), 100);
        server.sendOperation(new InsertOperation(1, "x"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals(client.getDocument(), "xbcdefg");
    }

    /**
     * insert operation at same position of delete operation
     * 
     * @throws Exception
     */
    public void testCase7() throws Exception {
        client.sendOperation(new DeleteOperation(0, "a"), 200);
        server.sendOperation(new InsertOperation(0, "x"), 100);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals(client.getDocument(), "xbcdefg");
    }

    /**
     * insert operation is in area of delete operation.
     * 
     * @throws Exception
     */
    public void testCase8() throws Exception {
        client.sendOperation(new DeleteOperation(0, "abc"), 100);
        server.sendOperation(new InsertOperation(1, "x"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals(client.getDocument(), "xdefg");
    }

    /**
     * first delete operation is completely before second operation.
     * 
     * @throws Exception
     */
    public void testCase9() throws Exception {
        client.sendOperation(new DeleteOperation(0, "a"), 100);
        server.sendOperation(new DeleteOperation(1, "bc"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals(client.getDocument(), "defg");
    }

    /**
     * delete operation inside delete operation area
     * 
     * @throws Exception
     */
    public void testCase10() throws Exception {
        client.sendOperation(new DeleteOperation(0, "abcd"), 100);
        server.sendOperation(new DeleteOperation(1, "bc"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals(client.getDocument(), "efg");
    }

    /**
     * delete operation starts before second delete operation and ends inside.
     * 
     * @throws Exception
     */
    public void testCase11() throws Exception {
        client.sendOperation(new DeleteOperation(0, "ab"), 100);
        server.sendOperation(new DeleteOperation(1, "bcd"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals(client.getDocument(), "efg");
    }

    /**
     * delete operation starts inside of delete operation area and ends after
     * this.
     * 
     * @throws Exception
     */
    public void testCase12() throws Exception {
        client.sendOperation(new DeleteOperation(1, "bcd"), 100);
        server.sendOperation(new DeleteOperation(0, "abc"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals(client.getDocument(), "efg");
    }

    /**
     * delete operation inside second delete operation area
     * 
     * @throws Exception
     */
    public void testCase13() throws Exception {
        client.sendOperation(new DeleteOperation(1, "b"), 100);
        server.sendOperation(new DeleteOperation(0, "abc"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals(client.getDocument(), "defg");

    }

    public void testCase14OverlappingSplitOperations() throws Exception {
        client.sendOperation(new SplitOperation(new DeleteOperation(1, "bcd"),
            new InsertOperation(0, "xyz")), 100);
        server.sendOperation(new SplitOperation(new DeleteOperation(0, "abc"),
            new InsertOperation(0, "uvw")), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());
        assertEquals("uvwxyzefg", client.getDocument());

    }

    public void testCase15SplitOperations() throws Exception {

        // ----01234567890123456789012345
        setUp("abcdefghijklmnopqrstuvwxyz");

        client.sendOperation(new SplitOperation(new DeleteOperation(4, "efg"),
            new InsertOperation(4, "123")), 100);
        client.sendOperation(new SplitOperation(new DeleteOperation(8, "ijk"),
            new InsertOperation(8, "789")), 200);
        server.sendOperation(new SplitOperation(new DeleteOperation(6, "ghi"),
            new InsertOperation(6, "456")), 200);

        Thread.sleep(500);

        assertEqualDocs("abcd123456789lmnopqrstuvwxyz", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    public void testCase16DeleteOperations() throws Exception {

        // ----01234567890123456789012345
        setUp("abcdefghijklmnopqrstuvwxyz");

        client.sendOperation(new DeleteOperation(4, "efg"), 100);
        client.sendOperation(new DeleteOperation(5, "ijk"), 200);
        server.sendOperation(new DeleteOperation(6, "ghi"), 200);

        Thread.sleep(500);

        assertEqualDocs("abcdlmnopqrstuvwxyz", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    public void testCase17DeleteSplitOperations() throws Exception {

        // ----01234567890123456789012345
        setUp("abcdefghijklmnopqrstuvwxyz");

        client.sendOperation(new SplitOperation(new DeleteOperation(4, "efg"),
            new InsertOperation(4, "123")), 100);
        assertEqualDocs("abcd123hijklmnopqrstuvwxyz", client);
        client.sendOperation(new DeleteOperation(8, "ijk"), 400);
        assertEqualDocs("abcd123hlmnopqrstuvwxyz", client);
        server.sendOperation(new DeleteOperation(6, "ghi"), 700);
        assertEqualDocs("abcdefjklmnopqrstuvwxyz", server);

        Thread.sleep(300);

        assertEqualDocs("abcd123jklmnopqrstuvwxyz", server);

        Thread.sleep(300);

        assertEqualDocs("abcd123lmnopqrstuvwxyz", server);

        Thread.sleep(300);

        assertEqualDocs("abcd123lmnopqrstuvwxyz", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    public void testCase18DeleteSplitOperations2() throws Exception {

        // ----01234567890123456789012345
        setUp("abcdefghijklmnopqrstuvwxyz");

        client.sendOperation(new SplitOperation(new DeleteOperation(8, "ijk"),
            new InsertOperation(8, "789")), 100);
        assertEqualDocs("abcdefgh789lmnopqrstuvwxyz", client);
        client.sendOperation(new DeleteOperation(4, "efg"), 200);
        assertEqualDocs("abcdh789lmnopqrstuvwxyz", client);
        server.sendOperation(new DeleteOperation(6, "ghi"), 200);
        assertEqualDocs("abcdefjklmnopqrstuvwxyz", server);

        Thread.sleep(300);

        assertEqualDocs("abcd789lmnopqrstuvwxyz", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    public void testCase19DeleteInsertDelete() throws Exception {

        // ----01234567890123456789012345
        setUp("abcdefghijklmnopqrstuvwxyz");

        client.sendOperation(new DeleteOperation(4, "efg"), 100);
        client.sendOperation(new InsertOperation(4, "123"), 200);
        client.sendOperation(new DeleteOperation(8, "ijk"), 300);
        server.sendOperation(new DeleteOperation(6, "ghi"), 300);

        Thread.sleep(400);

        assertEqualDocs("abcd123lmnopqrstuvwxyz", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    public void testCase20InsertInsideDelete() throws Exception {

        // ----01234567890123456789012345
        setUp("abcdefghijklmnopqrstuvwxyz");

        client.sendOperation(new DeleteOperation(4, "efgh"), 100);
        server.sendOperation(new InsertOperation(6, "12"), 200);

        Thread.sleep(400);

        assertEqualDocs("abcd12ijklmnopqrstuvwxyz", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

}

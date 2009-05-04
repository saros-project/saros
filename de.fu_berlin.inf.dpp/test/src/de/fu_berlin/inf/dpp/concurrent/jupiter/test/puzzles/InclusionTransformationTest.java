package de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterSimulator;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterTestCase;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.TwoWayJupiterClientDocument;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.TwoWayJupiterServerDocument;

/**
 * Test class contains all possible transformation of operations.
 * 
 * @author orieger
 * 
 */
public class InclusionTransformationTest extends JupiterTestCase {

    public static Operation S(Operation one, Operation two) {
        return new SplitOperation(one, two);
    }

    public static Operation I(int i, String s) {
        return new InsertOperation(i, s);
    }

    public static Operation D(int i, String s) {
        return new DeleteOperation(i, s);
    }

    TwoWayJupiterClientDocument client;
    TwoWayJupiterServerDocument server;

    @Override
    public void setUp() {
        super.setUp();
        setUp("abcdefg");
    }

    public void setUp(String initialText) {
        super.setUp();

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
        client.sendOperation(I(0, "x"), 100);
        server.sendOperation(I(0, "y"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());

        client.sendOperation(I(0, "x"), 100);
        server.sendOperation(I(1, "y"), 200);

        Thread.sleep(300);

        assertEqualDocs("xyyxabcdefg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    /**
     * insert after insert
     * 
     * @throws Exception
     */
    public void testCase2() throws Exception {
        client.sendOperation(I(1, "xx"), 100);
        server.sendOperation(D(0, "abc"), 200);

        Thread.sleep(300);

        assertEquals(client.getDocument(), server.getDocument());

        client.sendOperation(I(2, "x"), 100);
        server.sendOperation(I(1, "y"), 200);

        Thread.sleep(300);

        assertEqualDocs("xyxxdefg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    /**
     * insert before delete operation
     * 
     * @throws Exception
     */
    public void testCase3() throws Exception {
        client.sendOperation(I(1, "x"), 100);
        server.sendOperation(D(2, "c"), 200);

        Thread.sleep(400);

        assertEquals(client.getDocument(), server.getDocument());

        client.sendOperation(I(0, "y"), 100);
        server.sendOperation(D(0, "a"), 200);

        Thread.sleep(400);

        assertEqualDocs("yxbdefg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    /**
     * insert after delete operation
     * 
     * @throws Exception
     */
    public void testCase4() throws Exception {
        client.sendOperation(I(1, "x"), 100);
        server.sendOperation(D(0, "a"), 200);

        Thread.sleep(300);

        assertEqualDocs("xbcdefg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    /**
     * insert operation inside delete operation area
     * 
     * @throws Exception
     */
    public void testCase5() throws Exception {
        client.sendOperation(I(1, "x"), 100);
        server.sendOperation(D(0, "abc"), 200);

        Thread.sleep(400);

        assertEqualDocs("xdefg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    /**
     * insert operation after delete operation
     * 
     * @throws Exception
     */
    public void testCase6() throws Exception {
        client.sendOperation(D(0, "a"), 100);
        server.sendOperation(I(1, "x"), 200);

        Thread.sleep(300);

        assertEqualDocs("xbcdefg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    /**
     * insert operation at same position of delete operation
     * 
     * @throws Exception
     */
    public void testCase7() throws Exception {
        client.sendOperation(D(0, "a"), 200);
        server.sendOperation(I(0, "x"), 100);

        Thread.sleep(300);

        assertEqualDocs("xbcdefg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    /**
     * insert operation is in area of delete operation.
     * 
     * @throws Exception
     */
    public void testCase8() throws Exception {
        client.sendOperation(D(0, "abc"), 100);
        server.sendOperation(I(1, "x"), 200);

        Thread.sleep(300);

        assertEqualDocs("xdefg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    /**
     * first delete operation is completely before second operation.
     * 
     * @throws Exception
     */
    public void testCase9() throws Exception {
        client.sendOperation(D(0, "a"), 100);
        server.sendOperation(D(1, "bc"), 200);

        Thread.sleep(300);

        assertEqualDocs("defg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    /**
     * delete operation inside delete operation area
     * 
     * @throws Exception
     */
    public void testCase10() throws Exception {
        client.sendOperation(D(0, "abcd"), 100);
        server.sendOperation(D(1, "bc"), 200);

        Thread.sleep(300);

        assertEqualDocs("efg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    /**
     * delete operation starts before second delete operation and ends inside.
     * 
     * @throws Exception
     */
    public void testCase11() throws Exception {
        client.sendOperation(D(0, "ab"), 100);
        server.sendOperation(D(1, "bcd"), 200);

        Thread.sleep(300);

        assertEqualDocs("efg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    /**
     * delete operation starts inside of delete operation area and ends after
     * this.
     * 
     * @throws Exception
     */
    public void testCase12() throws Exception {
        client.sendOperation(D(1, "bcd"), 100);
        server.sendOperation(D(0, "abc"), 200);

        Thread.sleep(300);

        assertEqualDocs("efg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    /**
     * delete operation inside second delete operation area
     * 
     * @throws Exception
     */
    public void testCase13() throws Exception {
        client.sendOperation(D(1, "b"), 100);
        server.sendOperation(D(0, "abc"), 200);

        Thread.sleep(300);

        assertEqualDocs("defg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    public void testCase14OverlappingSplitOperations() throws Exception {
        client.sendOperation(S(D(1, "bcd"), I(0, "xyz")), 100);
        server.sendOperation(S(D(0, "abc"), I(0, "uvw")), 200);

        Thread.sleep(300);

        assertEqualDocs("uvwxyzefg", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    public void testCase15SplitOperations() throws Exception {

        // ----01234567890123456789012345
        setUp("abcdefghijklmnopqrstuvwxyz");

        client.sendOperation(S(D(4, "efg"), I(4, "123")), 100);
        client.sendOperation(S(D(8, "ijk"), I(8, "789")), 200);
        server.sendOperation(S(D(6, "ghi"), I(6, "456")), 200);

        Thread.sleep(500);

        assertEqualDocs("abcd123456789lmnopqrstuvwxyz", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    public void testCase16DeleteOperations() throws Exception {

        // ----01234567890123456789012345
        setUp("abcdefghijklmnopqrstuvwxyz");

        client.sendOperation(D(4, "efg"), 100);
        client.sendOperation(D(5, "ijk"), 200);
        server.sendOperation(D(6, "ghi"), 200);

        Thread.sleep(500);

        assertEqualDocs("abcdlmnopqrstuvwxyz", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    public void testCase17DeleteSplitOperations() throws Exception {

        // ----01234567890123456789012345
        setUp("abcdefghijklmnopqrstuvwxyz");

        client.sendOperation(S(D(4, "efg"), I(4, "123")), 100);
        assertEqualDocs("abcd123hijklmnopqrstuvwxyz", client);
        client.sendOperation(D(8, "ijk"), 400);
        assertEqualDocs("abcd123hlmnopqrstuvwxyz", client);
        server.sendOperation(D(6, "ghi"), 700);
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

        client.sendOperation(S(D(8, "ijk"), I(8, "789")), 100);
        assertEqualDocs("abcdefgh789lmnopqrstuvwxyz", client);
        client.sendOperation(D(4, "efg"), 200);
        assertEqualDocs("abcdh789lmnopqrstuvwxyz", client);
        server.sendOperation(D(6, "ghi"), 200);
        assertEqualDocs("abcdefjklmnopqrstuvwxyz", server);

        Thread.sleep(300);

        assertEqualDocs("abcd789lmnopqrstuvwxyz", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    public void testCase19DeleteInsertDelete() throws Exception {

        // ----01234567890123456789012345
        setUp("abcdefghijklmnopqrstuvwxyz");

        client.sendOperation(D(4, "efg"), 100);
        client.sendOperation(I(4, "123"), 200);
        client.sendOperation(D(8, "ijk"), 300);
        server.sendOperation(D(6, "ghi"), 300);

        Thread.sleep(400);

        assertEqualDocs("abcd123lmnopqrstuvwxyz", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    public void testCase20InsertInsideDelete() throws Exception {

        // ----01234567890123456789012345
        setUp("abcdefghijklmnopqrstuvwxyz");

        client.sendOperation(D(4, "efgh"), 100);
        server.sendOperation(I(6, "12"), 200);

        Thread.sleep(400);

        assertEqualDocs("abcd12ijklmnopqrstuvwxyz", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    public void testCase21NestedSplitOperation() throws Exception {

        // ----01234567890123456789012345
        setUp("abcdefghijklmnopqrstuvwxyz");

        client.sendOperation(
            (S(D(8, "ijk"), S(D(3, "def"), I(3, "replaced")))), 100);
        server.sendOperation(D(4, "efgh"), 200);
        Thread.sleep(350);

        assertEqualDocs("abcreplacedlmnopqrstuvwxyz", client, server);
        assertTrue("" + network.getLastError(), network.getLastError() == null);
    }

    /**
     * The following test case failed during a real world stress Test and
     * revealed an incorrect handling of NoOperations.
     */
    public void testCaseRealworldFailure() throws Exception {

        // ----------------------------------------01234567890
        JupiterSimulator j = new JupiterSimulator("XXXX-pqrstu");

        j.client.generate(S(D(4, "-pqrst"), I(4, "F")));
        assertEquals("XXXXFu", j.client.getDocument());

        j.server.generate(I(11, "v"));
        assertEquals("XXXX-pqrstuv", j.server.getDocument());

        j.server.generate(S(D(4, "-pqrstuv"), I(4, "-")));
        assertEquals("XXXX-", j.server.getDocument());

        j.server.generate(I(5, "w"));
        assertEquals("XXXX-w", j.server.getDocument());

        j.server.receive();
        assertEquals("XXXX-Fw", j.server.getDocument());

        j.client.receive();
        assertEquals("XXXXFuv", j.client.getDocument());

        j.client.receive();
        assertEquals("XXXX-F", j.client.getDocument());

        j.client.receive();
        assertEquals("XXXX-Fw", j.client.getDocument());

        j.assertDocs("XXXX-Fw");
    }
}

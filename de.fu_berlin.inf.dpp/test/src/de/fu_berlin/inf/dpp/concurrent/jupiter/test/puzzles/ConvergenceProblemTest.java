package de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles;

import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterTestCase;

/**
 * This test class contains convergence problem scenarios described in
 * "Achieving Convergence with Operational Transformation in Distributed
 * Groupware Systems" by Abdessamad Imine, Pascal Molli, Gerald Oster, Michael
 * Rusinowitch.
 * 
 * @author orieger
 * @author oezbek
 * 
 */
public class ConvergenceProblemTest extends JupiterTestCase {

    /**
     * Scenario in fig. 3 described in 3.1 Scenarios violating convergence.
     */
    public void testC2PuzzleP1() throws Exception {

        ClientSynchronizedDocument[] c = setUp(3, "core");

        /* O2 || O1 */
        c[0].sendOperation(new InsertOperation(3, "f"), 100);
        c[1].sendOperation(new DeleteOperation(2, "r"), 200);
        c[2].sendOperation(new InsertOperation(2, "f"), 1000);

        Thread.sleep(1500);

        assertEqualDocs("coffe", c);
    }

    /**
     * Scenario in fig. 5 described in 3.1 Scenarios violating convergence.
     */
    public void testC2PuzzleP2() throws Exception {

        ClientSynchronizedDocument[] c = setUp(5, "abcd");

        c[0].sendOperation(new DeleteOperation(1, "b"), 100);
        c[3].sendOperation(new InsertOperation(3, "x"), 1000);
        c[4].sendOperation(new DeleteOperation(3, "d"), 1100);

        c[0].sendOperation(new InsertOperation(3, "x"), 1500);
        Thread.sleep(2000);

        assertEqualDocs("acxx", c);
    }
}

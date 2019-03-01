package de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles;

import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterTestCase;
import org.junit.Test;

/**
 * This test class contains convergence problem scenarios described in "Achieving Convergence with
 * Operational Transformation in Distributed Groupware Systems" by Abdessamad Imine, Pascal Molli,
 * Gerald Oster, Michael Rusinowitch.
 *
 * @author orieger
 * @author oezbek
 */
public class ConvergenceProblemTest extends JupiterTestCase {

  /** Scenario in fig. 3 described in 3.1 Scenarios violating convergence. */
  @Test
  public void testC2PuzzleP1() throws Exception {

    ClientSynchronizedDocument[] c = setUp(3, "core");

    /* O2 || O1 */
    c[0].sendOperation(new InsertOperation(3, "f"), 100);
    c[1].sendOperation(new DeleteOperation(2, "r"), 300);
    c[2].sendOperation(new InsertOperation(2, "f"), 500);

    network.execute(500);

    assertEqualDocs("coffe", c);
  }

  /** Scenario in fig. 5 described in 3.1 Scenarios violating convergence. */
  @Test
  public void testC2PuzzleP2() throws Exception {

    ClientSynchronizedDocument[] c = setUp(5, "abcd");

    c[0].sendOperation(new DeleteOperation(1, "b"), 100);
    c[3].sendOperation(new InsertOperation(3, "x"), 300);
    c[4].sendOperation(new DeleteOperation(3, "d"), 500);
    c[0].sendOperation(new InsertOperation(3, "x"), 700);
    network.execute(700);

    assertEqualDocs("acxx", c);
  }
}

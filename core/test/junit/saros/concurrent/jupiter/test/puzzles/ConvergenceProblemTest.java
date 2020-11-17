package saros.concurrent.jupiter.test.puzzles;

import static saros.test.util.OperationHelper.D;
import static saros.test.util.OperationHelper.I;

import org.junit.Test;
import saros.concurrent.jupiter.test.util.ClientSynchronizedDocument;
import saros.concurrent.jupiter.test.util.JupiterTestCase;

/**
 * This test class contains convergence problem scenarios described in "Achieving Convergence with
 * Operational Transformation in Distributed Groupware Systems" by Abdessamad Imine, Pascal Molli,
 * Gerald Oster, Michael Rusinowitch.
 */
public class ConvergenceProblemTest extends JupiterTestCase {

  /** Scenario in fig. 3 described in 3.1 Scenarios violating convergence. */
  @Test
  public void testC2PuzzleP1() {

    ClientSynchronizedDocument[] c = setUp(3, "core");

    /* O2 || O1 */
    c[0].sendOperation(I(3, "f"), 100);
    c[1].sendOperation(D(2, "r"), 300);
    c[2].sendOperation(I(2, "f"), 500);

    network.execute(500);

    assertEqualDocs("coffe", c);
  }

  /** Scenario in fig. 5 described in 3.1 Scenarios violating convergence. */
  @Test
  public void testC2PuzzleP2() {

    ClientSynchronizedDocument[] c = setUp(5, "abcd");

    c[0].sendOperation(D(1, "b"), 100);
    c[3].sendOperation(I(3, "x"), 300);
    c[4].sendOperation(D(3, "d"), 500);
    c[0].sendOperation(I(3, "x"), 700);
    network.execute(700);

    assertEqualDocs("acxx", c);
  }
}

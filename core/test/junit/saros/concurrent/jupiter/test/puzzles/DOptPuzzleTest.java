package saros.concurrent.jupiter.test.puzzles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import saros.concurrent.jupiter.internal.text.DeleteOperation;
import saros.concurrent.jupiter.internal.text.InsertOperation;
import saros.concurrent.jupiter.test.util.ClientSynchronizedDocument;
import saros.concurrent.jupiter.test.util.JupiterTestCase;
import saros.concurrent.jupiter.test.util.ServerSynchronizedDocument;

/**
 * this test case simulate the unsolved dOPT Puzzle scenario which described in Fig. 2 in
 * "Operational Transformation in Real-Time Group Editors: Issues, Algorithm, Achievements", Sun
 * et.al.
 *
 * @author orieger
 */
public class DOptPuzzleTest extends JupiterTestCase {

  /**
   * dOPT puzzle scenario with three sides and three concurrent insert operations of a character at
   * the same position.
   *
   * @throws Exception
   */
  @Test
  public void testThreeConcurrentInsertOperations() throws Exception {
    /* init simulated client and server components. */
    ClientSynchronizedDocument client_1 =
        new ClientSynchronizedDocument(host, "abcd", network, alice);
    ClientSynchronizedDocument client_2 =
        new ClientSynchronizedDocument(host, "abcd", network, bob);
    ClientSynchronizedDocument client_3 =
        new ClientSynchronizedDocument(host, "abcd", network, carl);
    ServerSynchronizedDocument server = new ServerSynchronizedDocument(network, host);

    /* connect all with simulated network. */
    network.addClient(client_1);
    network.addClient(client_2);
    network.addClient(client_3);
    network.addClient(server);

    /* create proxyqueues. */
    server.addProxyClient(alice);
    server.addProxyClient(bob);
    server.addProxyClient(carl);

    /* O3 || O2 */
    client_3.sendOperation(new InsertOperation(0, "z"), 100);
    client_2.sendOperation(new InsertOperation(0, "x"), 700);

    network.execute(300);

    /* O1 -> O3 */
    client_1.sendOperation(new InsertOperation(0, "y"), 400);

    network.execute(700);

    assertEquals(client_1.getDocument(), client_2.getDocument());
    assertEquals(client_2.getDocument(), client_3.getDocument());
    System.out.println(client_1.getDocument());
  }

  /**
   * dOPT puzzle scenario with three sides and three concurrent insert operations of a character at
   * the same position.
   *
   * @throws Exception
   */
  @Test
  public void testThreeConcurrentInsertStringOperations() throws Exception {

    ClientSynchronizedDocument[] clients = setUp(3, "abcd");

    /* O2 || O1 */
    clients[2].sendOperation(new InsertOperation(0, "zzz"), 100);
    clients[1].sendOperation(new InsertOperation(0, "x"), 700);

    network.execute(300);
    /* O0 -> O2 */
    clients[0].sendOperation(new InsertOperation(0, "yy"), 400);

    network.execute(700);

    assertEqualDocs("yyzzzxabcd", clients);
  }

  /**
   * dOPT puzzle scenario with three sides and three concurrent delete operations.
   *
   * @throws Exception
   */
  @Test
  public void testThreeConcurrentDeleteOperations() throws Exception {

    ClientSynchronizedDocument[] clients = setUp(3, "abcdefg");

    clients[0].sendOperation(new DeleteOperation(0, "a"), 100);

    network.execute(300);

    clients[2].sendOperation(new DeleteOperation(1, "cde"), 800);
    clients[1].sendOperation(new DeleteOperation(3, "e"), 500);

    network.execute(800);

    assertEqualDocs("bfg", clients);
  }

  /**
   * dOPT puzzle scenario with three sides and insert / delete operations.
   *
   * @throws Exception
   */
  @Test
  public void testConcurrentInsertDeleteOperations() throws Exception {

    ClientSynchronizedDocument[] clients = setUp(3, "abc");

    try {
      clients[0].sendOperation(new InsertOperation(0, "a"), 200);
      clients[1].sendOperation(new InsertOperation(1, "b"), 400);

      network.execute(600);
      clients[1].sendOperation(new InsertOperation(2, "by"), 700);
      clients[0].sendOperation(new InsertOperation(1, "x"), 1000);
      clients[2].sendOperation(new DeleteOperation(1, "ab"), 1300);
    } catch (RuntimeException e) {
      // Document.execOperation throws RuntimeException on inconsistency
      // Convert this to a test failure.
      fail(e.getMessage());
    }

    network.execute();

    assertEqualDocs("axbybc", clients);
  }
}

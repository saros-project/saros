package saros.concurrent.jupiter.test.puzzles;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import saros.concurrent.jupiter.internal.text.DeleteOperation;
import saros.concurrent.jupiter.internal.text.InsertOperation;
import saros.concurrent.jupiter.test.util.ClientSynchronizedDocument;
import saros.concurrent.jupiter.test.util.JupiterTestCase;
import saros.concurrent.jupiter.test.util.ServerSynchronizedDocument;

/**
 * This class contains three test scenarios to verify transformation functions out of "Providing
 * Correctness of Transformation Functions in Real-Time Groupware"
 *
 * <p>TODO Setup of all three methods looks very similar -> refactor.
 *
 * @author orieger
 */
public class CounterExampleTest extends JupiterTestCase {

  /**
   * Scenario described in fig. 3 of discussed paper.
   *
   * @throws Exception
   */
  @Test
  public void testCounterExampleViolatingConditionC1() throws Exception {
    String initDocumentState = "abc";

    /* init simulated client and server components. */
    ClientSynchronizedDocument client_1 =
        new ClientSynchronizedDocument(host, initDocumentState, network, alice);
    ClientSynchronizedDocument client_2 =
        new ClientSynchronizedDocument(host, initDocumentState, network, bob);

    ServerSynchronizedDocument server = new ServerSynchronizedDocument(network, host);

    /* connect all with simulated network. */
    network.addClient(client_1);
    network.addClient(client_2);

    network.addClient(server);

    /* create proxyqueues. */
    server.addProxyClient(alice);
    server.addProxyClient(bob);

    /* O3 || O2 */

    client_1.sendOperation(new InsertOperation(1, "x"), 100);
    client_2.sendOperation(new DeleteOperation(1, "b"), 200);

    network.execute(200);

    assertEquals(client_1.getDocument(), client_2.getDocument());
    assertEquals("axc", client_1.getDocument());
  }

  /**
   * Scenario described in fig. 4 of discussed paper.
   *
   * @throws Exception
   */
  @Test
  public void testCounterExampleViolatingConditionC2() throws Exception {
    String initDocumentState = "abc";

    /* init simulated client and server components. */
    ClientSynchronizedDocument client_1 =
        new ClientSynchronizedDocument(host, initDocumentState, network, alice);
    ClientSynchronizedDocument client_2 =
        new ClientSynchronizedDocument(host, initDocumentState, network, bob);
    ClientSynchronizedDocument client_3 =
        new ClientSynchronizedDocument(host, initDocumentState, network, carl);
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
    client_1.sendOperation(new InsertOperation(1, "x"), 100);
    client_2.sendOperation(new DeleteOperation(1, "b"), 200);
    client_3.sendOperation(new InsertOperation(2, "y"), 1000);

    network.execute(1000);

    assertEquals(client_1.getDocument(), client_2.getDocument());
    assertEquals(client_2.getDocument(), client_3.getDocument());
    System.out.println(client_1.getDocument());
  }

  /**
   * Scenario described in fig. 5 of discussed paper.
   *
   * @throws Exception
   */
  @Test
  public void testCounterExample2ViolatingConditionC2() throws Exception {
    String initDocumentState = "abc";

    /* init simulated client and server components. */
    ClientSynchronizedDocument client_1 =
        new ClientSynchronizedDocument(host, initDocumentState, network, alice);
    ClientSynchronizedDocument client_2 =
        new ClientSynchronizedDocument(host, initDocumentState, network, bob);
    ClientSynchronizedDocument client_3 =
        new ClientSynchronizedDocument(host, initDocumentState, network, carl);
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
    client_1.sendOperation(new InsertOperation(1, "y"), 100);
    client_2.sendOperation(new DeleteOperation(1, "b"), 200);
    client_3.sendOperation(new InsertOperation(2, "y"), 1000);

    network.execute(1000);

    assertEquals(client_1.getDocument(), client_2.getDocument());
    assertEquals(client_2.getDocument(), client_3.getDocument());
  }
}

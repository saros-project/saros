package de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles;

import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterTestCase;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.ServerSynchronizedDocument;
import org.junit.Test;

/**
 * this class contains test cases for testing init server side and communication with client sides.
 *
 * @author orieger
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

    client_1 = new ClientSynchronizedDocument(host, text, network, alice);
    client_2 = new ClientSynchronizedDocument(host, text, network, bob);
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
    setUp("X");

    client_1.sendOperation(new InsertOperation(0, "a"), 300);
    client_2.sendOperation(new InsertOperation(1, "b"), 400);
    network.execute();

    assertEquals("aXb", client_1.getDocument());
    assertEquals("aXb", client_2.getDocument());
    assertEquals(client_1.getDocument(), client_2.getDocument());
  }

  /** two clients connect with jupiter server. */
  @Test
  public void testThreeConcurrentInsertOperations() throws Exception {

    setUp("X");

    client_1.sendOperation(new InsertOperation(0, "a"), 200);
    client_1.sendOperation(new InsertOperation(1, "b"), 400);
    client_2.sendOperation(new InsertOperation(1, "c"), 600);

    network.execute();

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

    network.execute();

    assertEquals("acbcdefg", client_1.getDocument());
    assertEquals("acbcdefg", client_2.getDocument());

    /* send two concurrent operations. */
    client_1.sendOperation(new InsertOperation(1, "x"), 100);
    client_2.sendOperation(new InsertOperation(2, "t"), 200);

    /* assert local execution. */
    assertEquals("axcbcdefg", client_1.getDocument());
    assertEquals("actbcdefg", client_2.getDocument());

    /* assert remote operation client 1 */
    network.execute(100);
    assertEquals("axcbcdefg", client_1.getDocument());
    assertEquals("axctbcdefg", client_2.getDocument());

    /* assert remote operation client 2. */
    network.execute(200);
    assertEquals("axctbcdefg", client_1.getDocument());
    assertEquals("axctbcdefg", client_2.getDocument());

    /* send two concurrent operations. */
    client_1.sendOperation(new InsertOperation(1, "t"), 300);
    client_2.sendOperation(new InsertOperation(3, "x"), 400);

    /* assert remote operations. */
    network.execute();
    assertEquals(client_1.getDocument(), client_2.getDocument());
  }

  /**
   * two clients connect with jupiter server. Concurrent delete and insert operations.
   *
   * @throws Exception
   */
  @Test
  public void testTwoClientWithJupiterProxyDeleteInsertOperations() throws Exception {

    setUp("abcdefg");

    /* send two concurrent operations. */
    client_1.sendOperation(new DeleteOperation(0, "abc"), 100);
    client_2.sendOperation(new InsertOperation(1, "t"), 200);

    /* assert local execution. */
    assertEquals("defg", client_1.getDocument());
    assertEquals("atbcdefg", client_2.getDocument());

    /* assert remote operations. */
    network.execute();
    assertEquals(client_1.getDocument(), client_2.getDocument());
  }
}

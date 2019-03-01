package saros.concurrent.jupiter.test.puzzles;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import saros.concurrent.jupiter.internal.text.DeleteOperation;
import saros.concurrent.jupiter.internal.text.InsertOperation;
import saros.concurrent.jupiter.test.util.ClientSynchronizedDocument;
import saros.concurrent.jupiter.test.util.JupiterTestCase;
import saros.concurrent.jupiter.test.util.TwoWayJupiterClientDocument;
import saros.concurrent.jupiter.test.util.TwoWayJupiterServerDocument;

public class SimpleClientServerTest extends JupiterTestCase {

  ClientSynchronizedDocument client;
  TwoWayJupiterServerDocument server;

  // NOTE: Manually called fixture because it has a parameter
  public void setupClientServer(String initialText) {
    super.setup();

    client = new TwoWayJupiterClientDocument(initialText, network);
    server = new TwoWayJupiterServerDocument(initialText, network);

    network.addClient(client);
    network.addClient(server);
  }

  /**
   * simple test scenario between server and client. The client and server send operation from same
   * state. Server message has delay, so that client create a new Operation. So if server message
   * arrive client and server have different document state and jupiter algorithm has to converge
   * the document states.
   */
  @Test
  public void test2WayProtocol() throws Exception {

    setupClientServer("abc");

    client.sendOperation(new InsertOperation(0, "e"), 100);

    assertEquals("eabc", client.getDocument());
    assertEquals("abc", server.getDocument());

    client.sendOperation(new InsertOperation(0, "x"), 200);

    assertEquals("xeabc", client.getDocument());
    assertEquals("abc", server.getDocument());

    server.sendOperation(client.getUser(), new DeleteOperation(0, "a"), 50);

    assertEquals("xeabc", client.getDocument());
    assertEquals("bc", server.getDocument());

    network.execute(50);
    assertEquals("xebc", client.getDocument());
    assertEquals("bc", server.getDocument());

    network.execute(100); // 1st Client operation arrives
    assertEquals("xebc", client.getDocument());
    assertEquals("ebc", server.getDocument());

    network.execute(200); // 2nd client operation arrives
    assertEquals("xebc", client.getDocument());
    assertEquals("xebc", server.getDocument());
  }

  /**
   * Site A insert a char into the delete area of site b. The delete operation has delay of two
   * seconds.
   */
  @Test
  public void testDeleteStringWithConcurentInsert() throws Exception {

    setupClientServer("abcdefg");

    client.sendOperation(new InsertOperation(3, "x"), 100);
    server.sendOperation(client.getUser(), new DeleteOperation(1, "bcde"), 400);

    network.execute(200);
    assertEquals("abcxdefg", client.getDocument());

    network.execute(400);
    assertEqualDocs("axfg", client, server);
  }

  /** Client insert a char into the delete area of Server. */
  @Test
  public void testInsertStringWithConcurentDelete() throws Exception {

    setupClientServer("abcdefg");

    client.sendOperation(new InsertOperation(3, "x"), 300);
    server.sendOperation(client.getUser(), new DeleteOperation(1, "bcde"), 100);

    network.execute(100);
    assertEquals("afg", server.getDocument());

    network.execute();
    assertEqualDocs("axfg", client, server);
  }
}

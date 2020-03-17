package saros.concurrent.jupiter.test.puzzles;

import static org.junit.Assert.assertEquals;
import static saros.test.util.OperationHelper.D;
import static saros.test.util.OperationHelper.I;
import static saros.test.util.OperationHelper.S;

import org.junit.Before;
import org.junit.Test;
import saros.concurrent.jupiter.test.util.JupiterSimulator;
import saros.concurrent.jupiter.test.util.JupiterTestCase;
import saros.concurrent.jupiter.test.util.TwoWayJupiterClientDocument;
import saros.concurrent.jupiter.test.util.TwoWayJupiterServerDocument;

/**
 * Test class contains all possible transformation of operations.
 *
 * @author orieger
 */
public class InclusionTransformationTest extends JupiterTestCase {

  TwoWayJupiterClientDocument client;
  TwoWayJupiterServerDocument server;

  @Override
  @Before
  public void setup() {
    super.setup();
    setUp("abcdefg");
  }

  public void setUp(String initialText) {
    super.setup();

    client = new TwoWayJupiterClientDocument(initialText, network);
    server = new TwoWayJupiterServerDocument(initialText, network);

    network.addClient(client);
    network.addClient(server);
  }

  /** insert before insert */
  @Test
  public void insertBeforeInsert() {
    client.sendOperation(I(0, "x"), 100);
    server.sendOperation(I(0, "y"), 200);

    network.execute(300);

    assertEquals(client.getDocument(), server.getDocument());

    client.sendOperation(I(0, "x"), 400);
    server.sendOperation(I(1, "y"), 500);

    network.execute(700);

    assertEqualDocs("xyyxabcdefg", client, server);
  }

  /** insert after insert */
  @Test
  public void insertAfterInsert() {
    client.sendOperation(I(1, "xx"), 100);
    server.sendOperation(D(0, "abc"), 200);

    network.execute(300);

    assertEquals(client.getDocument(), server.getDocument());

    client.sendOperation(I(2, "x"), 400);
    server.sendOperation(I(1, "y"), 500);

    network.execute(700);

    assertEqualDocs("xyxxdefg", client, server);
  }

  /** insert before delete operation */
  @Test
  public void insertBeforeDelete() {
    client.sendOperation(I(1, "x"), 100);
    server.sendOperation(D(2, "c"), 200);

    network.execute(300);

    assertEquals(client.getDocument(), server.getDocument());

    client.sendOperation(I(0, "y"), 400);
    server.sendOperation(D(0, "a"), 500);

    network.execute();

    assertEqualDocs("yxbdefg", client, server);
  }

  /** insert after delete operation */
  @Test
  public void insertAfterDelete() {
    client.sendOperation(I(1, "x"), 100);
    server.sendOperation(D(0, "a"), 200);

    network.execute(200);

    assertEqualDocs("xbcdefg", client, server);
  }

  /** insert operation inside delete operation area */
  @Test
  public void insertInsideDelete() {
    client.sendOperation(I(1, "x"), 100);
    server.sendOperation(D(0, "abc"), 200);

    network.execute(200);

    assertEqualDocs("xdefg", client, server);
  }

  /** insert operation after delete operation */
  @Test
  public void insertAfterDeleteClient() {
    client.sendOperation(D(0, "a"), 100);
    server.sendOperation(I(1, "x"), 200);

    network.execute(200);

    assertEqualDocs("xbcdefg", client, server);
  }

  /** insert operation at same position of delete operation */
  @Test
  public void insertDeleteSamePosition() {
    client.sendOperation(D(0, "a"), 200);
    server.sendOperation(I(0, "x"), 100);

    network.execute(200);

    assertEqualDocs("xbcdefg", client, server);
  }

  /** insert operation is in area of delete operation. */
  @Test
  public void insertAreaOfDelete() {
    client.sendOperation(D(0, "abc"), 100);
    server.sendOperation(I(1, "x"), 200);

    network.execute(200);

    assertEqualDocs("xdefg", client, server);
  }

  /** first delete operation is completely before second operation. */
  @Test
  public void deleteRace() {
    client.sendOperation(D(0, "a"), 100);
    server.sendOperation(D(1, "bc"), 200);

    network.execute(200);

    assertEqualDocs("defg", client, server);
  }

  /** delete operation inside delete operation area */
  @Test
  public void deleteInsideDelete() {
    client.sendOperation(D(0, "abcd"), 100);
    server.sendOperation(D(1, "bc"), 200);

    network.execute(200);

    assertEqualDocs("efg", client, server);
  }

  /** delete operation starts before second delete operation and ends inside. */
  @Test
  public void deleteDeleteTimeOverlap() {
    client.sendOperation(D(0, "ab"), 100);
    server.sendOperation(D(1, "bcd"), 200);

    network.execute(200);

    assertEqualDocs("efg", client, server);
  }

  /** delete operation starts inside of delete operation area and ends after this. */
  @Test
  public void deleteInsideDeleteTimeOverlap() {
    client.sendOperation(D(1, "bcd"), 100);
    server.sendOperation(D(0, "abc"), 200);

    network.execute(200);

    assertEqualDocs("efg", client, server);
  }

  /** delete operation inside second delete operation area */
  @Test
  public void deleteAreaInsideDeleteArea() {
    client.sendOperation(D(1, "b"), 100);
    server.sendOperation(D(0, "abc"), 200);

    network.execute(200);

    assertEqualDocs("defg", client, server);
  }

  @Test
  public void overlappingSplitOperations() {
    client.sendOperation(S(D(1, "bcd"), I(0, "xyz")), 100);
    server.sendOperation(S(D(0, "abc"), I(0, "uvw")), 200);

    network.execute(200);

    assertEqualDocs("uvwxyzefg", client, server);
  }

  @Test
  public void splitOperations() {

    // ----01234567890123456789012345
    setUp("abcdefghijklmnopqrstuvwxyz");

    client.sendOperation(S(D(4, "efg"), I(4, "123")), 100);
    client.sendOperation(S(D(8, "ijk"), I(8, "789")), 200);
    server.sendOperation(S(D(6, "ghi"), I(6, "456")), 300);

    network.execute(300);

    assertEqualDocs("abcd123456789lmnopqrstuvwxyz", client, server);
  }

  @Test
  public void deleteOperations() {

    // ----01234567890123456789012345
    setUp("abcdefghijklmnopqrstuvwxyz");

    client.sendOperation(D(4, "efg"), 100);
    client.sendOperation(D(5, "ijk"), 200);
    server.sendOperation(D(6, "ghi"), 300);

    network.execute(300);

    assertEqualDocs("abcdlmnopqrstuvwxyz", client, server);
  }

  @Test
  public void deleteSplitOperations() {

    // ----01234567890123456789012345
    setUp("abcdefghijklmnopqrstuvwxyz");

    client.sendOperation(S(D(4, "efg"), I(4, "123")), 100);
    assertEqualDocs("abcd123hijklmnopqrstuvwxyz", client);
    client.sendOperation(D(8, "ijk"), 200);
    assertEqualDocs("abcd123hlmnopqrstuvwxyz", client);
    server.sendOperation(D(6, "ghi"), 300);
    assertEqualDocs("abcdefjklmnopqrstuvwxyz", server);

    network.execute(100);

    assertEqualDocs("abcd123jklmnopqrstuvwxyz", server);

    network.execute(200);

    assertEqualDocs("abcd123lmnopqrstuvwxyz", server);

    network.execute(300);

    assertEqualDocs("abcd123lmnopqrstuvwxyz", client, server);
  }

  @Test
  public void deleteSplitOperations2() {

    // ----01234567890123456789012345
    setUp("abcdefghijklmnopqrstuvwxyz");

    client.sendOperation(S(D(8, "ijk"), I(8, "789")), 100);
    assertEqualDocs("abcdefgh789lmnopqrstuvwxyz", client);
    client.sendOperation(D(4, "efg"), 200);
    assertEqualDocs("abcdh789lmnopqrstuvwxyz", client);
    server.sendOperation(D(6, "ghi"), 300);
    assertEqualDocs("abcdefjklmnopqrstuvwxyz", server);

    network.execute(300);

    assertEqualDocs("abcd789lmnopqrstuvwxyz", client, server);
  }

  @Test
  public void deleteInsertDelete() {

    // ----01234567890123456789012345
    setUp("abcdefghijklmnopqrstuvwxyz");

    client.sendOperation(D(4, "efg"), 100);
    client.sendOperation(I(4, "123"), 200);
    client.sendOperation(D(8, "ijk"), 300);
    server.sendOperation(D(6, "ghi"), 400);

    network.execute(400);

    assertEqualDocs("abcd123lmnopqrstuvwxyz", client, server);
  }

  @Test
  public void insertInsideDelete2() {

    // ----01234567890123456789012345
    setUp("abcdefghijklmnopqrstuvwxyz");

    client.sendOperation(D(4, "efgh"), 100);
    server.sendOperation(I(6, "12"), 200);

    network.execute();

    assertEqualDocs("abcd12ijklmnopqrstuvwxyz", client, server);
  }

  @Test
  public void nestedSplitOperation() {

    // ----01234567890123456789012345
    setUp("abcdefghijklmnopqrstuvwxyz");

    client.sendOperation((S(D(8, "ijk"), S(D(3, "def"), I(3, "replaced")))), 100);
    server.sendOperation(D(4, "efgh"), 200);
    network.execute();

    assertEqualDocs("abcreplacedlmnopqrstuvwxyz", client, server);
  }

  /**
   * The following test case failed during a real world stress Test and revealed an incorrect
   * handling of NoOperations.
   */
  @Test
  public void realworldFailure() throws Exception {

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

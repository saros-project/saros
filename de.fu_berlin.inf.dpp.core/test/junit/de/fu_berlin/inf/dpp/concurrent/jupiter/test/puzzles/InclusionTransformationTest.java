package de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles;

import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterSimulator;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterTestCase;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.TwoWayJupiterClientDocument;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.TwoWayJupiterServerDocument;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class contains all possible transformation of operations.
 *
 * @author orieger
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

  /**
   * insert before insert
   *
   * @throws Exception
   */
  @Test
  public void insertBeforeInsert() throws Exception {
    client.sendOperation(I(0, "x"), 100);
    server.sendOperation(I(0, "y"), 200);

    network.execute(300);

    assertEquals(client.getDocument(), server.getDocument());

    client.sendOperation(I(0, "x"), 400);
    server.sendOperation(I(1, "y"), 500);

    network.execute(700);

    assertEqualDocs("xyyxabcdefg", client, server);
  }

  /**
   * insert after insert
   *
   * @throws Exception
   */
  @Test
  public void insertAfterInsert() throws Exception {
    client.sendOperation(I(1, "xx"), 100);
    server.sendOperation(D(0, "abc"), 200);

    network.execute(300);

    assertEquals(client.getDocument(), server.getDocument());

    client.sendOperation(I(2, "x"), 400);
    server.sendOperation(I(1, "y"), 500);

    network.execute(700);

    assertEqualDocs("xyxxdefg", client, server);
  }

  /**
   * insert before delete operation
   *
   * @throws Exception
   */
  @Test
  public void insertBeforeDelete() throws Exception {
    client.sendOperation(I(1, "x"), 100);
    server.sendOperation(D(2, "c"), 200);

    network.execute(300);

    assertEquals(client.getDocument(), server.getDocument());

    client.sendOperation(I(0, "y"), 400);
    server.sendOperation(D(0, "a"), 500);

    network.execute();

    assertEqualDocs("yxbdefg", client, server);
  }

  /**
   * insert after delete operation
   *
   * @throws Exception
   */
  @Test
  public void insertAfterDelete() throws Exception {
    client.sendOperation(I(1, "x"), 100);
    server.sendOperation(D(0, "a"), 200);

    network.execute(200);

    assertEqualDocs("xbcdefg", client, server);
  }

  /**
   * insert operation inside delete operation area
   *
   * @throws Exception
   */
  @Test
  public void insertInsideDelete() throws Exception {
    client.sendOperation(I(1, "x"), 100);
    server.sendOperation(D(0, "abc"), 200);

    network.execute(200);

    assertEqualDocs("xdefg", client, server);
  }

  /**
   * insert operation after delete operation
   *
   * @throws Exception
   */
  @Test
  public void insertAfterDeleteClient() throws Exception {
    client.sendOperation(D(0, "a"), 100);
    server.sendOperation(I(1, "x"), 200);

    network.execute(200);

    assertEqualDocs("xbcdefg", client, server);
  }

  /**
   * insert operation at same position of delete operation
   *
   * @throws Exception
   */
  @Test
  public void insertDeleteSamePosition() throws Exception {
    client.sendOperation(D(0, "a"), 200);
    server.sendOperation(I(0, "x"), 100);

    network.execute(200);

    assertEqualDocs("xbcdefg", client, server);
  }

  /**
   * insert operation is in area of delete operation.
   *
   * @throws Exception
   */
  @Test
  public void insertAreaOfDelete() throws Exception {
    client.sendOperation(D(0, "abc"), 100);
    server.sendOperation(I(1, "x"), 200);

    network.execute(200);

    assertEqualDocs("xdefg", client, server);
  }

  /**
   * first delete operation is completely before second operation.
   *
   * @throws Exception
   */
  @Test
  public void deleteRace() throws Exception {
    client.sendOperation(D(0, "a"), 100);
    server.sendOperation(D(1, "bc"), 200);

    network.execute(200);

    assertEqualDocs("defg", client, server);
  }

  /**
   * delete operation inside delete operation area
   *
   * @throws Exception
   */
  @Test
  public void deleteInsideDelete() throws Exception {
    client.sendOperation(D(0, "abcd"), 100);
    server.sendOperation(D(1, "bc"), 200);

    network.execute(200);

    assertEqualDocs("efg", client, server);
  }

  /**
   * delete operation starts before second delete operation and ends inside.
   *
   * @throws Exception
   */
  @Test
  public void deleteDeleteTimeOverlap() throws Exception {
    client.sendOperation(D(0, "ab"), 100);
    server.sendOperation(D(1, "bcd"), 200);

    network.execute(200);

    assertEqualDocs("efg", client, server);
  }

  /**
   * delete operation starts inside of delete operation area and ends after this.
   *
   * @throws Exception
   */
  @Test
  public void deleteInsideDeleteTimeOverlap() throws Exception {
    client.sendOperation(D(1, "bcd"), 100);
    server.sendOperation(D(0, "abc"), 200);

    network.execute(200);

    assertEqualDocs("efg", client, server);
  }

  /**
   * delete operation inside second delete operation area
   *
   * @throws Exception
   */
  @Test
  public void deleteAreaInsideDeleteArea() throws Exception {
    client.sendOperation(D(1, "b"), 100);
    server.sendOperation(D(0, "abc"), 200);

    network.execute(200);

    assertEqualDocs("defg", client, server);
  }

  @Test
  public void overlappingSplitOperations() throws Exception {
    client.sendOperation(S(D(1, "bcd"), I(0, "xyz")), 100);
    server.sendOperation(S(D(0, "abc"), I(0, "uvw")), 200);

    network.execute(200);

    assertEqualDocs("uvwxyzefg", client, server);
  }

  @Test
  public void splitOperations() throws Exception {

    // ----01234567890123456789012345
    setUp("abcdefghijklmnopqrstuvwxyz");

    client.sendOperation(S(D(4, "efg"), I(4, "123")), 100);
    client.sendOperation(S(D(8, "ijk"), I(8, "789")), 200);
    server.sendOperation(S(D(6, "ghi"), I(6, "456")), 300);

    network.execute(300);

    assertEqualDocs("abcd123456789lmnopqrstuvwxyz", client, server);
  }

  @Test
  public void deleteOperations() throws Exception {

    // ----01234567890123456789012345
    setUp("abcdefghijklmnopqrstuvwxyz");

    client.sendOperation(D(4, "efg"), 100);
    client.sendOperation(D(5, "ijk"), 200);
    server.sendOperation(D(6, "ghi"), 300);

    network.execute(300);

    assertEqualDocs("abcdlmnopqrstuvwxyz", client, server);
  }

  @Test
  public void deleteSplitOperations() throws Exception {

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
  public void deleteSplitOperations2() throws Exception {

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
  public void deleteInsertDelete() throws Exception {

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
  public void insertInsideDelete2() throws Exception {

    // ----01234567890123456789012345
    setUp("abcdefghijklmnopqrstuvwxyz");

    client.sendOperation(D(4, "efgh"), 100);
    server.sendOperation(I(6, "12"), 200);

    network.execute();

    assertEqualDocs("abcd12ijklmnopqrstuvwxyz", client, server);
  }

  @Test
  public void nestedSplitOperation() throws Exception {

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

package saros.concurrent.jupiter.test.puzzles;

import static org.junit.Assert.assertEquals;
import static saros.test.util.OperationHelper.D;
import static saros.test.util.OperationHelper.EOL;
import static saros.test.util.OperationHelper.I;
import static saros.test.util.OperationHelper.S;

import org.junit.Before;
import org.junit.Test;
import saros.concurrent.jupiter.test.util.JupiterSimulator;
import saros.concurrent.jupiter.test.util.JupiterTestCase;
import saros.concurrent.jupiter.test.util.TwoWayJupiterClientDocument;
import saros.concurrent.jupiter.test.util.TwoWayJupiterServerDocument;

/**
 * Test class contains all possible transformation of insert and delete operations.
 *
 * @see saros.concurrent.jupiter.internal.text.GOTOInclusionTransformation
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

  /* Test insert & insert */

  /**
   * Tests the transformation of an insert operation with another insert operation. Both operations
   * are located in the same line.
   *
   * <p>The server side tests the transformation for cases where the remote insert operation to
   * transform is located before the local insert operation.
   *
   * <p>=> No adjustment necessary.
   *
   * <p>The client side tests the transformation for cases where the remote insert operation to
   * transform is located after the local insert operation.
   *
   * <p>=> The remote insert operation must be shifted right by the length of the local insert
   * operation.
   *
   * <p>Base setup where both operations don't contain multiple lines.
   */
  @Test
  public void testTransformInsertInsertSameLineNoEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(1, "1"), 100);
    server.sendOperation(I(2, "2"), 200);

    network.execute(300);
    assertEqualDocs("a1b2c" + EOL + "def" + EOL + "ghi" + EOL + "jkl", client, server);
  }

  /**
   * Setup where only the client insert operation contains multiple lines.
   *
   * @see #testTransformInsertInsertSameLineNoEOL()
   */
  @Test
  public void testTransformInsertInsertSameLineClientEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(2, 1, "12" + EOL), 100);
    server.sendOperation(I(2, 3, "2"), 200);

    network.execute(300);
    assertEqualDocs("abc" + EOL + "def" + EOL + "g12" + EOL + "hi2" + EOL + "jkl", client, server);
  }

  /**
   * Setup where only the server insert operation contains multiple lines.
   *
   * @see #testTransformInsertInsertSameLineNoEOL()
   */
  @Test
  public void testTransformInsertInsertSameLineServerEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(3, 1, "123"), 100);
    server.sendOperation(I(3, 2, "4" + EOL + "56"), 200);

    network.execute(300);
    assertEqualDocs(
        "abc" + EOL + "def" + EOL + "ghi" + EOL + "j123k4" + EOL + "56l", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * @see #testTransformInsertInsertSameLineNoEOL()
   */
  @Test
  public void testTransformInsertInsertSameLineBothEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(0, "12" + EOL + "3"), 100);
    server.sendOperation(I(3, "45" + EOL + "678"), 200);

    network.execute(300);
    assertEqualDocs(
        "12" + EOL + "3abc45" + EOL + "678" + EOL + "def" + EOL + "ghi" + EOL + "jkl",
        client,
        server);
  }

  /**
   * Tests the transformation of an insert operation with another insert operation. The two
   * operations are located in different lines.
   *
   * <p>The server side tests the transformation for cases where the remote insert operation to
   * transform is located before the local insert operation.
   *
   * <p>=> No adjustment necessary.
   *
   * <p>The client side tests the transformation for cases where the remote insert operation to
   * transform is located after the local insert operation.
   *
   * <p>=> The remote insert operation must be shifted right by the length of the local insert
   * operation.
   *
   * <p>Base setup where both operations don't contain multiple lines.
   */
  @Test
  public void testTransformInsertInsertDifferentLineNoEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(0, 1, "1"), 100);
    server.sendOperation(I(3, 2, "2"), 200);

    network.execute(300);
    assertEqualDocs("a1bc" + EOL + "def" + EOL + "ghi" + EOL + "jk2l", client, server);
  }

  /**
   * Setup where only the client insert operation contains multiple lines.
   *
   * @see #testTransformInsertInsertDifferentLineNoEOL()
   */
  @Test
  public void testTransformInsertInsertDifferentLineClientEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(1, 1, "1" + EOL), 100);
    server.sendOperation(I(3, 2, "2"), 200);

    network.execute(300);
    assertEqualDocs("abc" + EOL + "d1" + EOL + "ef" + EOL + "ghi" + EOL + "jk2l", client, server);
  }

  /**
   * Setup where only the server insert operation contains multiple lines.
   *
   * @see #testTransformInsertInsertDifferentLineNoEOL()
   */
  @Test
  public void testTransformInsertInsertDifferentLineServerEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(0, 0, "1234"), 100);
    server.sendOperation(I(3, 2, "567" + EOL + "890"), 200);

    network.execute(300);
    assertEqualDocs(
        "1234abc" + EOL + "def" + EOL + "ghi" + EOL + "jk567" + EOL + "890l", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * @see #testTransformInsertInsertDifferentLineNoEOL()
   */
  @Test
  public void testTransformInsertInsertDifferentLineBothEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(1, 1, EOL + "123"), 100);
    server.sendOperation(I(2, 0, "45" + EOL + "6"), 200);

    network.execute(300);
    assertEqualDocs(
        "abc" + EOL + "d" + EOL + "123ef" + EOL + "45" + EOL + "6ghi" + EOL + "jkl",
        client,
        server);
  }

  /* Test insert & delete */

  /**
   * Tests the transformation of an insert operation with a delete operation. The two operations are
   * located in the same line.
   *
   * <p>The server side tests the transformation for cases where the remote insert operation to
   * transform is located before the local delete operation.
   *
   * <p>=> No adjustment necessary.
   *
   * <p>The client side tests the transformation for cases where the remote delete operation to
   * transform is located after the local insert operation.
   *
   * <p>=> The remote delete operation must be shifted right by the length of the local insert
   * operation.
   *
   * <p>Base setup where both operations don't contain multiple lines.
   */
  @Test
  public void testTransformInsertDeleteSameLineBeforeStartNoEOL() {
    setUp("abcde" + EOL + "fghij" + EOL + "klmnop");

    client.sendOperation(I(1, "1"), 100);
    server.sendOperation(D(3, "d"), 200);

    network.execute(300);
    assertEqualDocs("a1bce" + EOL + "fghij" + EOL + "klmnop", client, server);
  }

  /**
   * Setup where only the client insert operation contains multiple lines.
   *
   * @see #testTransformInsertDeleteSameLineBeforeStartNoEOL()
   */
  @Test
  public void testTransformInsertDeleteSameLineBeforeStartClientEOL() {
    setUp("abcde" + EOL + "fghij" + EOL + "klmnop");

    client.sendOperation(I(1, 2, "12" + EOL + "34" + EOL), 100);
    server.sendOperation(D(1, 3, "ij"), 200);

    network.execute(300);
    assertEqualDocs(
        "abcde" + EOL + "fg12" + EOL + "34" + EOL + "h" + EOL + "klmnop", client, server);
  }

  /**
   * Setup where only the server delete operation contains multiple lines.
   *
   * @see #testTransformInsertDeleteSameLineBeforeStartNoEOL()
   */
  @Test
  public void testTransformInsertDeleteSameLineBeforeStartServerEOL() {
    setUp("abcde" + EOL + "fghij" + EOL + "klmnop");

    client.sendOperation(I(0, "123"), 100);
    server.sendOperation(D(2, "cde" + EOL + "fghij" + EOL), 200);

    network.execute(300);
    assertEqualDocs("123abklmnop", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * @see #testTransformInsertDeleteSameLineBeforeStartNoEOL()
   */
  @Test
  public void testTransformInsertDeleteSameLineBeforeStartBothEOL() {
    setUp("abcde" + EOL + "fghij" + EOL + "klmnop");

    client.sendOperation(I(1, 0, "12" + EOL), 100);
    server.sendOperation(D(1, 4, "j" + EOL + "klmn"), 200);

    network.execute(300);
    assertEqualDocs("abcde" + EOL + "12" + EOL + "fghiop", client, server);
  }

  /**
   * Tests the transformation of an insert operation with a delete operation. The two operations are
   * located in different lines.
   *
   * <p>The server side tests the transformation for cases where the remote insert operation to
   * transform is located before the local delete operation.
   *
   * <p>=> No adjustment necessary.
   *
   * <p>The client side tests the transformation for cases where the remote delete operation to
   * transform is located after the local insert operation.
   *
   * <p>=> The remote delete operation must be shifted right by the length of the local insert
   * operation.
   *
   * <p>Base setup where both operations don't contain multiple lines.
   */
  @Test
  public void testTransformInsertDeleteDifferentLineBeforeStartNoEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(0, 1, "1"), 100);
    server.sendOperation(D(1, 1, "ef"), 200);

    network.execute(300);
    assertEqualDocs("a1bc" + EOL + "d" + EOL + "ghi" + EOL + "jkl", client, server);
  }

  /**
   * Setup where only the client insert operation contains multiple lines.
   *
   * @see #testTransformInsertDeleteDifferentLineBeforeStartNoEOL()
   */
  @Test
  public void testTransformInsertDeleteDifferentLineBeforeStartClientEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(1, 0, "12" + EOL + "3" + EOL), 100);
    server.sendOperation(D(3, 1, "kl"), 200);

    network.execute(300);
    assertEqualDocs(
        "abc" + EOL + "12" + EOL + "3" + EOL + "def" + EOL + "ghi" + EOL + "j", client, server);
  }

  /**
   * Setup where only the server delete operation contains multiple lines.
   *
   * @see #testTransformInsertDeleteDifferentLineBeforeStartNoEOL()
   */
  @Test
  public void testTransformInsertDeleteDifferentLineBeforeStartServerEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(0, 2, "1234567"), 100);
    server.sendOperation(D(1, 0, "def" + EOL + "ghi" + EOL), 200);

    network.execute(300);
    assertEqualDocs("ab1234567c" + EOL + "jkl", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * @see #testTransformInsertDeleteDifferentLineBeforeStartNoEOL()
   */
  @Test
  public void testTransformInsertDeleteDifferentLineBeforeStartBothEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(1, 3, EOL + "12" + EOL + "34" + EOL), 100);
    server.sendOperation(D(2, 1, "hi" + EOL + "jkl"), 200);

    network.execute(300);
    assertEqualDocs(
        "abc" + EOL + "def" + EOL + "12" + EOL + "34" + EOL + EOL + "g", client, server);
  }

  /**
   * Tests the transformation of an insert operation with a delete operation. The two operations are
   * located in the same line.
   *
   * <p>The server side tests the transformation for cases where the remote insert operation to
   * transform is located after the end of the local delete operation.
   *
   * <p>=> The remove insert operation must be shifted left by the length of the local delete
   * operation.
   *
   * <p>The client side tests the transformation for cases where the end of the remote delete
   * operation to transform is located before the local insert operation.
   *
   * <p>=> No adjustment necessary.
   *
   * <p>Base setup where both operations don't contain multiple lines.
   */
  @Test
  public void testTransformInsertDeleteSameLineAfterEndNoEOL() {
    setUp("abcde" + EOL + "fghij" + EOL + "klmnop");

    client.sendOperation(I(1, 4, "1"), 100);
    server.sendOperation(D(1, 1, "ghi"), 200);

    network.execute(300);
    assertEqualDocs("abcde" + EOL + "f1j" + EOL + "klmnop", client, server);
  }

  /**
   * Setup where only the client insert operation contains multiple lines.
   *
   * @see #testTransformInsertDeleteSameLineAfterEndNoEOL()
   */
  @Test
  public void testTransformInsertDeleteSameLineAfterEndClientEOL() {
    setUp("abcde" + EOL + "fghij" + EOL + "klmnop");

    client.sendOperation(I(2, 6, EOL + "12" + EOL + "3456"), 100);
    server.sendOperation(D(2, 2, "mno"), 200);

    network.execute(300);
    assertEqualDocs(
        "abcde" + EOL + "fghij" + EOL + "klp" + EOL + "12" + EOL + "3456", client, server);
  }

  /**
   * Setup where only the server delete operation contains multiple lines.
   *
   * @see #testTransformInsertDeleteSameLineAfterEndNoEOL()
   */
  @Test
  public void testTransformInsertDeleteSameLineAfterEndServerEOL() {
    setUp("abcde" + EOL + "fghij" + EOL + "klmnop");

    client.sendOperation(I(1, 3, "123"), 100);
    server.sendOperation(D(0, 5, EOL + "fg"), 200);

    network.execute(300);
    assertEqualDocs("abcdeh123ij" + EOL + "klmnop", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * @see #testTransformInsertDeleteSameLineAfterEndNoEOL()
   */
  @Test
  public void testTransformInsertDeleteSameLineAfterEndBothEOL() {
    setUp("abcde" + EOL + "fghij" + EOL + "klmnop");

    client.sendOperation(I(2, 4, "123" + EOL + "456"), 100);
    server.sendOperation(D(0, 0, "abcde" + EOL + "fghij" + EOL), 200);

    network.execute(300);
    assertEqualDocs("klmn123" + EOL + "456op", client, server);
  }

  /**
   * Tests the transformation of an insert operation with a delete operation. The two operations are
   * located in different lines.
   *
   * <p>The server side tests the transformation for cases where the remote insert operation to
   * transform is located after the end of the local delete operation.
   *
   * <p>=> The remove insert operation must be shifted left by the length of the local delete
   * operation.
   *
   * <p>The client side tests the transformation for cases where the end of the remote delete
   * operation to transform is located before the local insert operation.
   *
   * <p>=> No adjustment necessary.
   *
   * <p>Base setup where both operations don't contain multiple lines.
   */
  @Test
  public void testTransformInsertDeleteDifferentLineAfterEndNoEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(2, 1, "1234"), 100);
    server.sendOperation(D(0, 0, "abc"), 200);

    network.execute(300);
    assertEqualDocs(EOL + "def" + EOL + "g1234hi" + EOL + "jkl", client, server);
  }

  /**
   * Setup where only the client insert operation contains multiple lines.
   *
   * @see #testTransformInsertDeleteDifferentLineAfterEndNoEOL()
   */
  @Test
  public void testTransformInsertDeleteDifferentLineAfterEndClientEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(3, 3, "123" + EOL + "456" + EOL), 100);
    server.sendOperation(D(1, 0, "def"), 200);

    network.execute(300);
    assertEqualDocs("abc" + EOL + EOL + "ghi" + EOL + "jkl123" + EOL + "456" + EOL, client, server);
  }

  /**
   * Setup where only the server delete operation contains multiple lines.
   *
   * @see #testTransformInsertDeleteDifferentLineAfterEndNoEOL()
   */
  @Test
  public void testTransformInsertDeleteDifferentLineAfterEndServerEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(3, 0, "1234"), 100);
    server.sendOperation(D(0, 0, "abc" + EOL + "def" + EOL + "ghi"), 200);

    network.execute(300);
    assertEqualDocs(EOL + "1234jkl", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * @see #testTransformInsertDeleteDifferentLineAfterEndNoEOL()
   */
  @Test
  public void testTransformInsertDeleteDifferentLineAfterEndBothEOL() {
    setUp("abc" + EOL + "def" + EOL + "ghi" + EOL + "jkl");

    client.sendOperation(I(2, 0, "123456" + EOL + "7890"), 100);
    server.sendOperation(D(0, 1, "bc" + EOL + "def"), 200);

    network.execute(300);
    assertEqualDocs("a" + EOL + "123456" + EOL + "7890ghi" + EOL + "jkl", client, server);
  }

  /**
   * Tests the transformation of an insert operation with a delete operation. The two operations are
   * located in the same line.
   *
   * <p>The server side tests the transformation for cases where the remote insert operation to
   * transform is located inside the area of the local delete operation.
   *
   * <p>=> The start position of the remove insert operation must be set to the start position of
   * the local delete operation.
   *
   * <p>The client side tests the transformation for cases where the area of the remote delete
   * operation to transform contains the position of the local insert operation.
   *
   * <p>=> The remote delete operation must be split at the start point of the local insert
   * operation. The second part of the insert operation must be shifted right by the length of the
   * local insert operation.
   *
   * <p>Base setup where both operations don't contain multiple lines.
   */
  @Test
  public void testTransformInsertDeleteInNoEOL() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(I(1, 3, "12"), 100);
    server.sendOperation(D(1, 2, "ij"), 200);

    network.execute(300);
    assertEqualDocs(
        "abcdef" + EOL + "gh12kl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where only the server delete operation contains multiple lines.
   *
   * <p>The server delete operation only contains line breaks after the split point.
   *
   * @see #testTransformInsertDeleteInNoEOL()
   */
  @Test
  public void testTransformInsertDeleteInNoEOL2() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(I(3, 5, "12"), 100);
    server.sendOperation(D(3, 1, "tuvwx" + EOL + "yz"), 200);

    network.execute(300);
    assertEqualDocs("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "s12", client, server);
  }

  /**
   * Setup where only the server delete operation contains multiple lines.
   *
   * <p>The server delete operation only contains line breaks before the split point.
   *
   * @see #testTransformInsertDeleteInNoEOL()
   */
  @Test
  public void testTransformInsertDeleteInNoEOL3() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(I(1, 1, "123"), 100);
    server.sendOperation(D(0, 4, "ef" + EOL + "ghijkl" + EOL + "mn"), 200);

    network.execute(300);
    assertEqualDocs("abcd123opqr" + EOL + "stuvwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where only the server delete operation contains multiple lines.
   *
   * <p>The server delete operation contains line breaks before and after the split point.
   *
   * @see #testTransformInsertDeleteInNoEOL()
   */
  @Test
  public void testTransformInsertDeleteInNoEOL4() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(I(1, 2, "1234"), 100);
    server.sendOperation(D(0, 3, "def" + EOL + "ghijkl" + EOL + "mnopq"), 200);

    network.execute(300);
    assertEqualDocs("abc1234r" + EOL + "stuvwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where only the client insert operation contains multiple lines.
   *
   * @see #testTransformInsertDeleteInNoEOL()
   */
  @Test
  public void testTransformInsertDeleteInInsertEOL() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(I(2, 4, "123" + EOL + "4" + EOL), 100);
    server.sendOperation(D(2, 1, "nopqr"), 200);

    network.execute(300);
    assertEqualDocs(
        "abcdef" + EOL + "ghijkl" + EOL + "m123" + EOL + "4" + EOL + EOL + "stuvwx" + EOL + "yz",
        client,
        server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * <p>The server delete operation only contains line breaks after the split point.
   *
   * @see #testTransformInsertDeleteInNoEOL()
   */
  @Test
  public void testTransformInsertDeleteInNoEOL5() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(I(1, 2, EOL + "123" + EOL + "45" + EOL), 100);
    server.sendOperation(D(1, 0, "ghijkl" + EOL + "mnopqr"), 200);

    network.execute(300);
    assertEqualDocs(
        "abcdef" + EOL + EOL + "123" + EOL + "45" + EOL + EOL + "stuvwx" + EOL + "yz",
        client,
        server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * <p>The server delete operation only contains line breaks before the split point.
   *
   * @see #testTransformInsertDeleteInNoEOL()
   */
  @Test
  public void testTransformInsertDeleteInNoEOL6() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(I(1, 1, EOL + "123456"), 100);
    server.sendOperation(D(0, 2, "cdef" + EOL + "ghi"), 200);

    network.execute(300);
    assertEqualDocs(
        "ab" + EOL + "123456jkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * <p>The server delete operation contains line breaks before and after the split point.
   *
   * @see #testTransformInsertDeleteInNoEOL()
   */
  @Test
  public void testTransformInsertDeleteInNoEOL7() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(I(2, 0, "1" + EOL + "2345" + EOL), 100);
    server.sendOperation(
        D(0, 0, "abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz"), 200);

    network.execute(300);
    assertEqualDocs("1" + EOL + "2345" + EOL, client, server);
  }

  /* Test delete & delete */

  /**
   * Tests the transformation of a delete operation with another delete operation. Both operations
   * are located in the same line.
   *
   * <p>The server side tests the transformation for cases where the end of the remote delete
   * operation to transform is located before start of the local delete operation.
   *
   * <p>=> No adjustment necessary.
   *
   * <p>The client side tests the transformation for cases where the start of the remote delete
   * operation to transform is located after the end of the local delete operation.
   *
   * <p>=> The remote delete operation must be shifted left by the length of the local delete
   * operation.
   *
   * <p>Base setup where both operations don't contain multiple lines.
   */
  @Test
  public void testTransformDeleteDeleteBeforeSameLineNoEOL() {
    setUp("abcde" + EOL + "fghij" + EOL + "klmnop");

    client.sendOperation(D(0, "ab"), 100);
    server.sendOperation(D(3, "de"), 200);

    network.execute(300);
    assertEqualDocs("c" + EOL + "fghij" + EOL + "klmnop", client, server);
  }

  /**
   * Setup where only the client delete operation contains multiple lines.
   *
   * @see #testTransformDeleteDeleteBeforeSameLineNoEOL()
   */
  @Test
  public void testTransformDeleteDeleteBeforeSameLineClientEOL() {
    setUp("abcde" + EOL + "fghij" + EOL + "klmnop");

    client.sendOperation(D(0, 2, "cde" + EOL + "f"), 100);
    server.sendOperation(D(1, 3, "ij"), 200);

    network.execute(300);
    assertEqualDocs("abgh" + EOL + "klmnop", client, server);
  }

  /**
   * Setup where only the server delete operation contains multiple lines.
   *
   * @see #testTransformDeleteDeleteBeforeSameLineNoEOL()
   */
  @Test
  public void testTransformDeleteDeleteBeforeSameLineServerEOL() {
    setUp("abcde" + EOL + "fghij" + EOL + "klmnop");

    client.sendOperation(D(1, 0, "fghi"), 100);
    server.sendOperation(D(1, 4, "j" + EOL + "klm"), 200);

    network.execute(300);
    assertEqualDocs("abcde" + EOL + "nop", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * @see #testTransformDeleteDeleteBeforeSameLineNoEOL()
   */
  @Test
  public void testTransformDeleteDeleteBeforeSameLineBothEOL() {
    setUp("abcde" + EOL + "fghij" + EOL + "klmnop");

    client.sendOperation(D(0, 0, "abcde" + EOL + "fg"), 100);
    server.sendOperation(D(1, 3, "ij" + EOL + "klmnop"), 200);

    network.execute(300);
    assertEqualDocs("h", client, server);
  }

  /**
   * Tests the transformation of a delete operation with another delete operation. The two
   * operations are located in different line.
   *
   * <p>The server side tests the transformation for cases where the end of the remote delete
   * operation to transform is located before start of the local delete operation.
   *
   * <p>=> No adjustment necessary.
   *
   * <p>The client side tests the transformation for cases where the start of the remote delete
   * operation to transform is located after the end of the local delete operation.
   *
   * <p>=> The remote delete operation must be shifted left by the length of the local delete
   * operation.
   *
   * <p>Base setup where both operations don't contain multiple lines.
   */
  @Test
  public void testTransformDeleteDeleteBeforeDifferentLineNoEOL() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(0, 0, "abcdef"), 100);
    server.sendOperation(D(4, 0, "yz"), 200);

    network.execute(300);
    assertEqualDocs(EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL, client, server);
  }

  /**
   * Setup where only the client delete operation contains multiple lines.
   *
   * @see #testTransformDeleteDeleteBeforeSameLineNoEOL()
   */
  @Test
  public void testTransformDeleteDeleteBeforeDifferentLineClientEOL() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(1, 2, "ijkl" + EOL + "m"), 100);
    server.sendOperation(D(3, 2, "uvw"), 200);

    network.execute(300);
    assertEqualDocs("abcdef" + EOL + "ghnopqr" + EOL + "stx" + EOL + "yz", client, server);
  }

  /**
   * Setup where only the server delete operation contains multiple lines.
   *
   * @see #testTransformDeleteDeleteBeforeSameLineNoEOL()
   */
  @Test
  public void testTransformDeleteDeleteBeforeDifferentLineServerEOL() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(2, 1, "nop"), 100);
    server.sendOperation(D(3, 1, "tuvwx" + EOL + "yz"), 200);

    network.execute(300);
    assertEqualDocs("abcdef" + EOL + "ghijkl" + EOL + "mqr" + EOL + "s", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * @see #testTransformDeleteDeleteBeforeSameLineNoEOL()
   */
  @Test
  public void testTransformDeleteDeleteBeforeDifferentLineBothEOL() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(0, 0, "abcdef" + EOL + "ghijk"), 100);
    server.sendOperation(D(2, 1, "nopqr" + EOL + "stuvwx" + EOL + "yz"), 200);

    network.execute(300);
    assertEqualDocs("l" + EOL + "m", client, server);
  }

  /**
   * Tests the transformation of a delete operation with another delete operation. The two
   * operations overlap.
   *
   * <p>The server side tests the transformation for cases where the end of the remote delete
   * operation to transform overlaps with the start of the local delete operation.
   *
   * <p>=> The remote delete operation must be adjusted by dropping the trailing overlap.
   *
   * <p>The client side tests the transformation for cases where the start of the remote delete
   * operation to transform overlaps with the end of the local delete operation.
   *
   * <p>=> The remote delete operation must be adjusted by dropping the leading overlap.
   *
   * <p>Base setup where both operations don't contain multiple lines.
   */
  @Test
  public void testTransformDeleteDeletePartialOverlap1() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(1, 1, "hij"), 100);
    server.sendOperation(D(1, 3, "jkl"), 200);

    network.execute(300);
    assertEqualDocs(
        "abcdef" + EOL + "g" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where only the client delete operation contains multiple lines.
   *
   * <p>The client delete operation only contains line breaks before the overlap.
   *
   * @see #testTransformDeleteDeletePartialOverlap1()
   */
  @Test
  public void testTransformDeleteDeletePartialOverlap2() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(0, 3, "def" + EOL + "ghijkl" + EOL + "mnop"), 100);
    server.sendOperation(D(2, 0, "mnopqr"), 200);

    network.execute(300);
    assertEqualDocs("abc" + EOL + "stuvwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * <p>The client delete operation only contains line breaks in the overlap.
   *
   * <p>The server delete operation only contains line breaks in the overlap.
   *
   * @see #testTransformDeleteDeletePartialOverlap1()
   */
  @Test
  public void testTransformDeleteDeletePartialOverlap3() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(1, 0, "ghijkl" + EOL + "mno"), 100);
    server.sendOperation(D(1, 3, "jkl" + EOL + "mnopqr"), 200);

    network.execute(300);
    assertEqualDocs("abcdef" + EOL + EOL + "stuvwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where only the server delete operation contains multiple lines.
   *
   * <p>The server delete operation only contains line breaks after the overlap.
   *
   * @see #testTransformDeleteDeletePartialOverlap1()
   */
  @Test
  public void testTransformDeleteDeletePartialOverlap4() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(0, 0, "abcde"), 100);
    server.sendOperation(D(0, 3, "def" + EOL + "ghij"), 200);

    network.execute(300);
    assertEqualDocs("kl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * <p>The client delete operation contains line breaks before and in the overlap.
   *
   * <p>The server delete operation only contains line breaks in the overlap.
   *
   * @see #testTransformDeleteDeletePartialOverlap1()
   */
  @Test
  public void testTransformDeleteDeletePartialOverlap5() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(0, 4, "ef" + EOL + "ghijkl" + EOL + "mn"), 100);
    server.sendOperation(D(1, 2, "ijkl" + EOL + "mnopq"), 200);

    network.execute(300);
    assertEqualDocs("abcdr" + EOL + "stuvwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * <p>The client delete operation only contains line breaks in the overlap.
   *
   * <p>The server delete operation contains line breaks in and after the overlap.
   *
   * @see #testTransformDeleteDeletePartialOverlap1()
   */
  @Test
  public void testTransformDeleteDeletePartialOverlap6() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(1, 0, "ghijkl" + EOL + "mnopqr" + EOL + "s"), 100);
    server.sendOperation(D(1, 6, EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz"), 200);

    network.execute(300);
    assertEqualDocs("abcdef" + EOL, client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * <p>The client delete operation only contains line breaks before the overlap.
   *
   * <p>The server delete operation only contains line breaks after the overlap.
   *
   * @see #testTransformDeleteDeletePartialOverlap1()
   */
  @Test
  public void testTransformDeleteDeletePartialOverlap7() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(1, 0, "ghijkl" + EOL + "mnopqr"), 100);
    server.sendOperation(D(2, 0, "mnopqr" + EOL + "stuvwx"), 200);

    network.execute(300);
    assertEqualDocs("abcdef" + EOL + EOL + "yz", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * <p>The client delete operation contains line breaks before and in the overlap.
   *
   * <p>The server delete operation contains line breaks in and after the overlap.
   *
   * @see #testTransformDeleteDeletePartialOverlap1()
   */
  @Test
  public void testTransformDeleteDeletePartialOverlap8() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(0, 1, "bcdef" + EOL + "ghijkl" + EOL + "mnopqr"), 100);
    server.sendOperation(D(1, 0, "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "y"), 200);

    network.execute(300);
    assertEqualDocs("az", client, server);
  }

  /**
   * Tests the transformation of a delete operation with another delete operation. One operation
   * completely contains the other.
   *
   * <p>The server side tests the transformation for cases where the area of the remote delete
   * operation to transform completely contains the local delete operation.
   *
   * <p>=> The remote delete operation must be adjusted by dropping the contained overlap.
   *
   * <p>The client side tests the transformation for cases where the remote delete operation to
   * transform is completely contained in the area of the local delete operation.
   *
   * <p>=> The remote delete operation becomes a NOP.
   *
   * <p>Base setup where both operations don't contain multiple lines.
   */
  @Test
  public void testTransformDeleteDeleteContained1() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(1, 0, "ghijkl"), 100);
    server.sendOperation(D(1, 2, "ij"), 200);

    network.execute(300);
    assertEqualDocs("abcdef" + EOL + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where only the client delete operation contains multiple lines.
   *
   * <p>The client delete operation only contains line breaks before the dropped content.
   *
   * @see #testTransformDeleteDeleteContained1()
   */
  @Test
  public void testTransformDeleteDeleteContained2() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(0, 2, "cdef" + EOL + "ghijkl" + EOL + "mnopq"), 100);
    server.sendOperation(D(2, 1, "nop"), 200);

    network.execute(300);
    assertEqualDocs("abr" + EOL + "stuvwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * <p>The client delete operation only contains line breaks in the dropped content.
   *
   * @see #testTransformDeleteDeleteContained1()
   */
  @Test
  public void testTransformDeleteDeleteContained3() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(0, 0, "abcdef" + EOL + "ghijkl" + EOL + "mnopqr"), 100);
    server.sendOperation(D(0, 3, "def" + EOL + "ghijkl" + EOL + "mno"), 200);

    network.execute(300);
    assertEqualDocs(EOL + "stuvwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where only the client delete operation contains multiple lines.
   *
   * <p>The client delete operation only contains line breaks after the dropped content.
   *
   * @see #testTransformDeleteDeleteContained1()
   */
  @Test
  public void testTransformDeleteDeleteContained4() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(2, 1, "nopqr" + EOL + "stuv"), 100);
    server.sendOperation(D(2, 2, "opqr"), 200);

    network.execute(300);
    assertEqualDocs("abcdef" + EOL + "ghijkl" + EOL + "mwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * <p>The client delete operation only contains line breaks before and in the dropped content.
   *
   * @see #testTransformDeleteDeleteContained1()
   */
  @Test
  public void testTransformDeleteDeleteContained5() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(0, 3, "def" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvw"), 100);
    server.sendOperation(D(2, 5, "r" + EOL + "st"), 200);

    network.execute(300);
    assertEqualDocs("abcx" + EOL + "yz", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * <p>The client delete operation only contains line breaks in and after the dropped content.
   *
   * @see #testTransformDeleteDeleteContained1()
   */
  @Test
  public void testTransformDeleteDeleteContained6() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(1, 1, "hijkl" + EOL + "mnopqr" + EOL + "stuvw"), 100);
    server.sendOperation(D(1, 3, "jkl" + EOL + "mno"), 200);

    network.execute(300);
    assertEqualDocs("abcdef" + EOL + "gx" + EOL + "yz", client, server);
  }

  /**
   * Setup where only the client delete operation contains multiple lines.
   *
   * <p>The client delete operation only contains line breaks before and after the dropped content.
   *
   * @see #testTransformDeleteDeleteContained1()
   */
  @Test
  public void testTransformDeleteDeleteContained7() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(D(0, 0, "abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL), 100);
    server.sendOperation(D(1, 0, "ghijk"), 200);

    network.execute(300);
    assertEqualDocs("stuvwx" + EOL + "yz", client, server);
  }

  /**
   * Setup where both operations contain multiple lines.
   *
   * <p>The client delete operation contains line breaks before, in, and after the dropped content.
   *
   * @see #testTransformDeleteDeleteContained1()
   */
  @Test
  public void testTransformDeleteDeleteContained8() {
    setUp("abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz");

    client.sendOperation(
        D(0, 0, "abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz"), 100);
    server.sendOperation(D(1, 0, "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx"), 200);

    network.execute(300);
    assertEqualDocs("", client, server);
  }

  /* Split operation & other tests */

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

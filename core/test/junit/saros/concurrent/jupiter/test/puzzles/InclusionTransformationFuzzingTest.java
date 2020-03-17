package saros.concurrent.jupiter.test.puzzles;

import static org.junit.Assert.assertEquals;
import static saros.test.util.OperationHelper.EOL;

import java.util.Objects;
import java.util.Random;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.internal.text.DeleteOperation;
import saros.concurrent.jupiter.internal.text.InsertOperation;
import saros.concurrent.jupiter.internal.text.SplitOperation;
import saros.concurrent.jupiter.test.util.JupiterTestCase;
import saros.concurrent.jupiter.test.util.TwoWayJupiterClientDocument;
import saros.concurrent.jupiter.test.util.TwoWayJupiterServerDocument;
import saros.editor.text.TextPosition;
import saros.editor.text.TextPositionUtils;

/**
 * Tests the inclusion transformation using randomly generated operations. As the test is not
 * deterministic in its default setup, it is not included in any test suite. It is rather meant as a
 * helpful tool to manually run to ensure that changes to the inclusion transformations did not
 * cause any more subtle issues that might have been missed by the normal unit tests in {@link
 * InclusionTransformationTest} and {@link GOTOInclusionTransformationTest}.
 *
 * <p>{@link #ROUNDS} specifies the number of rounds that are run. {@link #ROUND_LENGTH} specifies
 * the number of operations per round.
 *
 * <p>Each operation is randomly generated for either the server or the client as an Insert, Delete,
 * or Split operation with random content. See {@link
 * #generateOperation(TwoWayJupiterClientDocument, TwoWayJupiterServerDocument)}.
 *
 * <p>At the end of each round, all local operations of the server/client created during that round
 * are applied to the other side. Afterwards, it is checked that the server and client have
 * successfully arrived at the same document state.
 *
 * <p>Prints the used seed at the start of each run. This allows specific runs to be reproduced by
 * setting a specific seed in the {@link #setup()} method.
 *
 * @see saros.concurrent.jupiter.InclusionTransformation
 * @see saros.concurrent.jupiter.internal.text.GOTOInclusionTransformation
 */
public class InclusionTransformationFuzzingTest extends JupiterTestCase {

  /** Number of rounds. */
  private static final int ROUNDS = 200;

  /**
   * Number of operations per round.
   *
   * <p>As the time for the network synchronization (transforming and applying all operations of the
   * other side after a round) seems to increase exponentially with the number of operations per
   * round, this should probably not be set above 1000 (except if you have a LOT of time on your
   * hands).
   */
  private static final int ROUND_LENGTH = 100;

  TwoWayJupiterClientDocument client;
  TwoWayJupiterServerDocument server;

  Random random;

  @Override
  @Before
  public void setup() {
    super.setup();

    String initialText = "abcdef" + EOL + "ghijkl" + EOL + "mnopqr" + EOL + "stuvwx" + EOL + "yz";

    client = new TwoWayJupiterClientDocument(initialText, network);
    server = new TwoWayJupiterServerDocument(initialText, network);

    network.addClient(client);
    network.addClient(server);

    random = new Random();

    // set to known seed to be able to reproduce results
    long seed = random.nextLong();

    System.out.println("Set up fuzzing test; used seed: " + seed);

    random.setSeed(seed);
  }

  /** @see InclusionTransformationFuzzingTest */
  @Ignore("Meant to be run manually")
  @Test
  public void fuzzInclusionTransformation() {
    int lastTime = -1;

    for (int roundCount = 0; roundCount < ROUNDS; roundCount++) {
      for (int roundOperationCount = 0; roundOperationCount < ROUND_LENGTH; roundOperationCount++) {
        int time = roundCount * ROUND_LENGTH + roundOperationCount + 1;

        assert time > lastTime;
        lastTime = time;

        int side = random.nextInt(2);

        if (side == 0) {
          /* Create a client operation */

          client.sendOperation(generateOperation(client, null), time);

        } else if (side == 1) {
          /* Create a server operation */

          server.sendOperation(generateOperation(null, server), time);
        }
      }

      // synchronize server and client
      network.execute(lastTime);

      assertEquals(
          "Document synchronization failed at time " + lastTime,
          server.getDocument(),
          client.getDocument());

      System.out.println(
          "Documents correctly converged after round ("
              + (roundCount + 1)
              + "/"
              + ROUNDS
              + ") - op count: "
              + lastTime
              + " - document content: "
              + StringEscapeUtils.escapeJava(server.getDocument()));
    }
  }

  /**
   * Generates a random operation for the given client or server.
   *
   * <p>Can generate Insert, Delete, and (nested) Split operations.
   *
   * <p>Either the client or the server must be <code>null</code>.
   *
   * @param client the client to calculate an operation for
   * @param server the server to calculate an operation for
   * @return a random operation for the given client or server
   */
  private Operation generateOperation(
      TwoWayJupiterClientDocument client, TwoWayJupiterServerDocument server) {

    Pair<Operation, String> operation = generateOperationInternal(client, server, null);

    return operation.getLeft();
  }

  /**
   * Generates a random operation for the given client or server.
   *
   * <p>Internal method also accepting pre-adjusted content to be passed. This is necessary to
   * create (nested) split operations, as the later operations of a split operation are dependent on
   * the earlier operations of the split operation, meaning they have to be created based on the
   * already modified content state after all prior operations of the split operation were already
   * applied.
   *
   * <p>Can generate Insert, Delete, and (nested) Split operations.
   *
   * <p>Either the client or the server must be <code>null</code>.
   *
   * @param client the client to calculate an operation for
   * @param server the server to calculate an operation for
   * @param preAdjustedContent the already modified content to use for operation creation; necessary
   *     for split operations
   * @return a pair containing a random operation for the given client or server as the left element
   *     and the adjusted content after the returned operation was applied as the right element
   */
  private Pair<Operation, String> generateOperationInternal(
      TwoWayJupiterClientDocument client,
      TwoWayJupiterServerDocument server,
      String preAdjustedContent) {

    assert client != null || server != null;

    int kind = random.nextInt(10);

    String content;

    if (preAdjustedContent != null) {
      content = preAdjustedContent;
    } else if (client != null) {
      content = client.getDocument();
    } else {
      content = server.getDocument();
    }

    int contentLength = content.length();

    if (kind <= 1) {
      /*
       * Creates a random split operation. 20% chance
       *
       * Contained operations can also be split operations
       */

      Pair<Operation, String> first = generateOperationInternal(client, server, preAdjustedContent);
      Pair<Operation, String> second = generateOperationInternal(client, server, first.getRight());

      Operation split = new SplitOperation(first.getLeft(), second.getLeft());

      return new ImmutablePair<>(split, second.getRight());

    } else if (kind <= 5 && contentLength > 0) {
      /*
       * Creates a random delete operation. ~40% chance
       *
       * Replaced by an insert operation if there is no content to delete.
       */

      int position = random.nextInt(contentLength);

      int possibleMaxLength = contentLength - position;

      int length;

      if (possibleMaxLength == 1) {
        length = 1;

      } else if (possibleMaxLength <= 10) {
        /* Do deletion of length 1 to possibleMaxLength */
        length = random.nextInt(possibleMaxLength - 1) + 1;

      } else {
        int deletionCase = random.nextInt(11);

        /* Mostly do deletions of length 1 to 10 but sometimes also allow longer deletions cases. */
        if (deletionCase <= 9) {
          length = deletionCase + 1;

        } else {
          /* Do deletion of length 1 to possibleMaxLength */
          length = random.nextInt(possibleMaxLength - 1) + 1;
        }
      }

      String deletedContent = content.substring(position, position + length);

      TextPosition start = calculateTextPosition(content, position);
      Pair<Integer, Integer> deltas = TextPositionUtils.calculateDeltas(deletedContent, EOL);

      Operation delete =
          new DeleteOperation(start, deltas.getLeft(), deltas.getRight(), deletedContent);

      StringBuilder sb = new StringBuilder(content);
      sb.delete(position, position + length);

      return new ImmutablePair<>(delete, sb.toString());

    } else {
      /* Creates random insert operation. ~40% chance */

      int position = random.nextInt(contentLength + 1);

      /* Do insertions of length 1 to 10 */
      int length = random.nextInt(10) + 1;

      TextPosition start = calculateTextPosition(content, position);

      String addedContent = generateRandomString(length);

      Pair<Integer, Integer> deltas = TextPositionUtils.calculateDeltas(addedContent, EOL);

      String adjustedContent =
          content.substring(0, position) + addedContent + content.substring(position);

      Operation insert =
          new InsertOperation(start, deltas.getLeft(), deltas.getRight(), addedContent);

      return new ImmutablePair<>(insert, adjustedContent);
    }
  }

  /**
   * Generates a random string of the given length.
   *
   * <p>The string is build by randomly using the numbers 0-9 and newline character.
   *
   * @param length the length of the string to generate
   * @return a random string of the given length
   */
  private String generateRandomString(int length) {
    StringBuilder res = new StringBuilder();

    while (res.length() < length) {
      int type = random.nextInt(11);

      if (type <= 9) {
        res.append(type);
      } else {
        res.append(EOL);
      }
    }

    return res.toString();
  }

  /**
   * Calculates the text position for the given offset in the given document content.
   *
   * <p>Expects the document content to use Unix line endings.
   *
   * @param documentContent the document content in which to calculate the text position
   * @param offset the offset whose text position to calculate
   * @return the text position for the given offset in the given document content
   */
  public static TextPosition calculateTextPosition(String documentContent, int offset) {
    Objects.requireNonNull(documentContent, "Given document content must not be null");

    if (offset < 0) {
      throw new IllegalArgumentException("Given offset must not be negative");
    }

    if (offset > documentContent.length()) {
      throw new IllegalArgumentException("Given offset is larger than text length");
    }

    String lineSeparator = EOL;

    int lineNumber = 0;
    int lastLineStartOffset = 0;

    int currentOffset = 0;

    while (currentOffset < offset) {
      currentOffset = documentContent.indexOf(lineSeparator, lastLineStartOffset);

      if (currentOffset == -1) {
        break;
      }

      currentOffset += lineSeparator.length();

      if (currentOffset <= offset) {
        lastLineStartOffset = currentOffset;
        lineNumber++;
      }
    }

    int inLineOffset = offset - lastLineStartOffset;

    return new TextPosition(lineNumber, inLineOffset);
  }
}

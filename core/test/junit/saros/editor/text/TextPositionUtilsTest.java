package saros.editor.text;

import static org.junit.Assert.assertEquals;
import static saros.editor.text.TextPositionUtils.UNIX_LINE_SEPARATOR;
import static saros.editor.text.TextPositionUtils.WINDOWS_LINE_SEPARATOR;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Test;
import saros.concurrent.jupiter.internal.text.GOTOInclusionTransformation;

public class TextPositionUtilsTest {

  private static final String ESCAPED_UNIX_LINE_SEPARATOR = "\\n";
  private static final String ESCAPED_WINDOWS_LINE_SEPARATOR = "\\r\\n";

  private static final String UNIX_TEST_STRING =
      "" // line start offset:
          + "public class TestClass {\n" // 0
          + "  public static void main(String[] args) {\n" // 25
          + "    System.out.println(\"Hello World\");\n" // 68
          + "\n" // 107
          + "    ping();\n" // 108
          + "\n" // 120
          + "    pong();\n" // 121
          + "  }\n" // 133
          + "\n" // 137
          + "  private static void ping() {\n" // 138
          + "    System.out.println(\"PING!\");\n" // 169
          + "  }\n" // 202
          + "\n" // 206
          + "  private static void pong() {\n" // 207
          + "    System.out.println(\"PONG!\");\n" // 238
          + "  }\n" // 271
          + "}"; // 275

  private static final int[] UNIX_TEST_STRING_LINE_OFFSETS =
      new int[] {0, 25, 68, 107, 108, 120, 121, 133, 137, 138, 169, 202, 206, 207, 238, 271, 275};

  private static final String WINDOWS_TEST_STRING =
      "" // line start offset:
          + "public class TestClass {\r\n" // 0
          + "  public static void main(String[] args) {\r\n" // 26
          + "    System.out.println(\"Hello World\");\r\n" // 70
          + "\r\n" // 110
          + "    ping();\r\n" // 112
          + "\r\n" // 125
          + "    pong();\r\n" // 127
          + "  }\r\n" // 140
          + "\r\n" // 145
          + "  private static void ping() {\r\n" // 147
          + "    System.out.println(\"PING!\");\r\n" // 179
          + "  }\r\n" // 213
          + "\r\n" // 218
          + "  private static void pong() {\r\n" // 220
          + "    System.out.println(\"PONG!\");\r\n" // 252
          + "  }\r\n" // 286
          + "}"; // 291

  private static final int[] WINDOWS_TEST_STRING_LINE_OFFSETS =
      new int[] {0, 26, 70, 110, 112, 125, 127, 140, 145, 147, 179, 213, 218, 220, 252, 286, 291};

  @Ignore("Internal test that is not needed for normal test runs")
  @Test
  public void testOffsetCount() {
    for (int newLineOffset : UNIX_TEST_STRING_LINE_OFFSETS) {
      if (newLineOffset != 0) {
        String chars =
            UNIX_TEST_STRING.substring(newLineOffset - UNIX_LINE_SEPARATOR.length(), newLineOffset);
        assertEquals(
            "line offset " + newLineOffset + " is not correct",
            StringEscapeUtils.escapeJava(UNIX_LINE_SEPARATOR),
            StringEscapeUtils.escapeJava(chars));
      }
    }

    int unixTextLength = UNIX_TEST_STRING.length();
    int unixLastLineOffset =
        UNIX_TEST_STRING_LINE_OFFSETS[UNIX_TEST_STRING_LINE_OFFSETS.length - 1];

    assertEquals(unixTextLength, unixLastLineOffset + 1);

    for (int newLineOffset : WINDOWS_TEST_STRING_LINE_OFFSETS) {
      if (newLineOffset != 0) {
        String chars =
            WINDOWS_TEST_STRING.substring(
                newLineOffset - WINDOWS_LINE_SEPARATOR.length(), newLineOffset);
        assertEquals(
            "line offset " + newLineOffset + " is not correct",
            StringEscapeUtils.escapeJava(WINDOWS_LINE_SEPARATOR),
            StringEscapeUtils.escapeJava(chars));
      }
    }

    int windowsTextLength = WINDOWS_TEST_STRING.length();
    int windowsLastLineOffset =
        WINDOWS_TEST_STRING_LINE_OFFSETS[WINDOWS_TEST_STRING_LINE_OFFSETS.length - 1];

    assertEquals(windowsTextLength, windowsLastLineOffset + 1);
  }

  /**
   * Ease of use method that calculates the offset for the given text position using the Unix test
   * string and Unix line separator.
   *
   * @param textPosition the text position whose offset to calculate
   * @return the offset for the given text position
   * @see TextPositionUtils#calculateOffset(String, TextPosition, String)
   * @see #UNIX_TEST_STRING
   * @see TextPositionUtils#UNIX_LINE_SEPARATOR
   */
  private static int calculateUnixOffset(TextPosition textPosition) {
    return TextPositionUtils.calculateOffset(UNIX_TEST_STRING, textPosition, UNIX_LINE_SEPARATOR);
  }

  /**
   * Ease of use method that calculates the offset for the given text position using the Windows
   * test string and Windows line separator.
   *
   * @param textPosition the text position whose offset to calculate
   * @return the offset for the given text position
   * @see TextPositionUtils#calculateOffset(String, TextPosition, String)
   * @see #WINDOWS_TEST_STRING
   * @see TextPositionUtils#WINDOWS_LINE_SEPARATOR
   */
  private static int calculateWindowsOffset(TextPosition textPosition) {
    return TextPositionUtils.calculateOffset(
        WINDOWS_TEST_STRING, textPosition, WINDOWS_LINE_SEPARATOR);
  }

  @Test
  public void testUnixPositionCalculation() {
    int realUnixOffset;
    TextPosition position;
    int calculatedUnixOffset;

    // Test position at the start of the file
    realUnixOffset = 0;
    position = new TextPosition(0, 0);
    calculatedUnixOffset = calculateUnixOffset(position);

    assertEquals(
        "incorrect Unix offset calculation for text start", realUnixOffset, calculatedUnixOffset);

    // Test position at the end of the file
    realUnixOffset = UNIX_TEST_STRING.length() - 1;
    position = new TextPosition(16, 0);
    calculatedUnixOffset = calculateUnixOffset(position);

    assertEquals(
        "incorrect Unix offset calculation for text end", realUnixOffset, calculatedUnixOffset);

    // Test position in the middle of a line
    realUnixOffset = UNIX_TEST_STRING.indexOf("PING!");
    position = new TextPosition(10, 24);
    calculatedUnixOffset = calculateUnixOffset(position);

    assertEquals(
        "incorrect Unix offset calculation for middle of the line",
        realUnixOffset,
        calculatedUnixOffset);

    // Test position at the end of a line
    realUnixOffset = UNIX_TEST_STRING_LINE_OFFSETS[15] - UNIX_LINE_SEPARATOR.length();
    position = new TextPosition(14, 32);
    calculatedUnixOffset = calculateUnixOffset(position);

    assertEquals(
        "incorrect Unix offset calculation for end of the line",
        realUnixOffset,
        calculatedUnixOffset);
  }

  @Test
  public void testUnixLineStartPositionCalculation() {
    for (int i = 0; i < UNIX_TEST_STRING_LINE_OFFSETS.length; i++) {
      TextPosition lineStartPosition = new TextPosition(i, 0);

      int calculatedLineStartOffset = calculateUnixOffset(lineStartPosition);

      assertEquals(
          "incorrect Unix offset calculation for start of line " + i,
          UNIX_TEST_STRING_LINE_OFFSETS[i],
          calculatedLineStartOffset);
    }
  }

  /**
   * If this test fails, it could be a sign that an update of the Apache 'commons-lang3' library has
   * caused {@link StringUtils#ordinalIndexOf(CharSequence, CharSequence, int)} to no longer
   * correctly filters out escaped new lines.
   *
   * <p>If such a case, all usages of {@link StringUtils#lastIndexOf(CharSequence, CharSequence)}
   * and {@link StringUtils#ordinalIndexOf(CharSequence, CharSequence, int)} have to be adjusted.
   * See {@link GOTOInclusionTransformation}.
   */
  @Test
  public void testUnixPositionCalculationEscapedLineBreaks() {
    String text;
    int realUnixOffset;
    TextPosition position;
    int calculatedUnixOffset;

    // Test end position with only escaped lines endings
    text = UNIX_TEST_STRING.replace(UNIX_LINE_SEPARATOR, ESCAPED_UNIX_LINE_SEPARATOR);

    realUnixOffset = text.length();
    position = new TextPosition(0, text.length());

    calculatedUnixOffset = TextPositionUtils.calculateOffset(text, position, UNIX_LINE_SEPARATOR);

    assertEquals(
        "incorrect Unix offset calculation for only escaped line breaks",
        realUnixOffset,
        calculatedUnixOffset);

    // Test mixed escaped and normal line endings
    text =
        "0123"
            + UNIX_LINE_SEPARATOR
            + "456"
            + ESCAPED_UNIX_LINE_SEPARATOR
            + "789"
            + UNIX_LINE_SEPARATOR
            + ESCAPED_UNIX_LINE_SEPARATOR
            + UNIX_LINE_SEPARATOR
            + "0";

    realUnixOffset = text.length();
    position = new TextPosition(3, 1);

    calculatedUnixOffset = TextPositionUtils.calculateOffset(text, position, UNIX_LINE_SEPARATOR);

    assertEquals(
        "incorrect Unix offset calculation for mixed escaped line breaks",
        realUnixOffset,
        calculatedUnixOffset);
  }

  @Test
  public void testWindowsPositionCalculation() {
    int realWindowsOffset;
    TextPosition position;
    int calculatedWindowsOffset;

    // Test position at the start of the file
    realWindowsOffset = 0;
    position = new TextPosition(0, 0);
    calculatedWindowsOffset = calculateWindowsOffset(position);

    assertEquals(
        "incorrect Windows offset calculation for text start",
        realWindowsOffset,
        calculatedWindowsOffset);

    // Test position at the end of the file
    realWindowsOffset = WINDOWS_TEST_STRING.length() - 1;
    position = new TextPosition(16, 0);
    calculatedWindowsOffset = calculateWindowsOffset(position);

    assertEquals(
        "incorrect Windows offset calculation for text end",
        realWindowsOffset,
        calculatedWindowsOffset);

    // Test position in the middle of a line
    realWindowsOffset = WINDOWS_TEST_STRING.indexOf("PING!");
    position = new TextPosition(10, 24);
    calculatedWindowsOffset = calculateWindowsOffset(position);

    assertEquals(
        "incorrect Windows offset calculation for middle of the line",
        realWindowsOffset,
        calculatedWindowsOffset);

    // Test position at the end of a line
    realWindowsOffset = WINDOWS_TEST_STRING_LINE_OFFSETS[15] - WINDOWS_LINE_SEPARATOR.length();
    position = new TextPosition(14, 32);
    calculatedWindowsOffset = calculateWindowsOffset(position);

    assertEquals(
        "incorrect Windows offset calculation for end of the line",
        realWindowsOffset,
        calculatedWindowsOffset);
  }

  @Test
  public void testWindowsLineStartPositionCalculation() {
    for (int i = 0; i < WINDOWS_TEST_STRING_LINE_OFFSETS.length; i++) {
      TextPosition lineStartPosition = new TextPosition(i, 0);

      int calculatedLineStartOffset = calculateWindowsOffset(lineStartPosition);

      assertEquals(
          "incorrect Windows offset calculation for start of line " + i,
          WINDOWS_TEST_STRING_LINE_OFFSETS[i],
          calculatedLineStartOffset);
    }
  }

  /** @see #testUnixPositionCalculationEscapedLineBreaks() */
  @Test
  public void testWindowsPositionCalculationEscapedLineBreaks() {
    String text;
    int realWindowsOffset;
    TextPosition position;
    int calculatedWindowsOffset;

    // Test end position with only escaped lines endings
    text = UNIX_TEST_STRING.replace(WINDOWS_LINE_SEPARATOR, ESCAPED_WINDOWS_LINE_SEPARATOR);

    realWindowsOffset = text.length();
    position = new TextPosition(0, text.length());

    calculatedWindowsOffset =
        TextPositionUtils.calculateOffset(text, position, WINDOWS_LINE_SEPARATOR);

    assertEquals(
        "incorrect Windows offset calculation for only escaped line breaks",
        realWindowsOffset,
        calculatedWindowsOffset);

    // Test mixed escaped and normal line endings
    text =
        "0123"
            + WINDOWS_LINE_SEPARATOR
            + "456"
            + ESCAPED_WINDOWS_LINE_SEPARATOR
            + "789"
            + WINDOWS_LINE_SEPARATOR
            + ESCAPED_WINDOWS_LINE_SEPARATOR
            + WINDOWS_LINE_SEPARATOR
            + "0";

    realWindowsOffset = text.length();
    position = new TextPosition(3, 1);

    calculatedWindowsOffset =
        TextPositionUtils.calculateOffset(text, position, WINDOWS_LINE_SEPARATOR);

    assertEquals(
        "incorrect Windows offset calculation for mixed escaped line breaks",
        realWindowsOffset,
        calculatedWindowsOffset);
  }

  /**
   * Ease of use method that calculates the line and offset delta for the given text using the Unix
   * line separator.
   *
   * @param text the text whose deltas to calculate
   * @return the line and offset delta for the given text
   * @see TextPositionUtils#calculateDeltas(String, String)
   */
  private static Pair<Integer, Integer> calculateDeltas(String text) {
    return TextPositionUtils.calculateDeltas(text, UNIX_LINE_SEPARATOR);
  }

  @Test
  public void testCalculateDeltas() {
    Pair<Integer, Integer> realDeltas;
    Pair<Integer, Integer> calculatedDeltas;
    String text;

    // Test multi-line delta, multi-offset delta case
    realDeltas = new ImmutablePair<>(16, 1);
    text = UNIX_TEST_STRING;
    calculatedDeltas = calculateDeltas(text);

    assertEquals(
        "incorrect delta calculation for multi-line, multi-offset delta text",
        realDeltas,
        calculatedDeltas);

    // Test case ending in new-line
    realDeltas = new ImmutablePair<>(17, 0);
    text = UNIX_TEST_STRING + UNIX_LINE_SEPARATOR;
    calculatedDeltas = calculateDeltas(text);

    assertEquals(
        "incorrect delta calculation for multi-line, no offset delta text",
        realDeltas,
        calculatedDeltas);

    // Test single-line delta
    realDeltas = new ImmutablePair<>(0, 24);
    int splitPoint = UNIX_TEST_STRING_LINE_OFFSETS[1] - 1;
    text = UNIX_TEST_STRING.substring(0, splitPoint);
    calculatedDeltas = calculateDeltas(text);

    assertEquals(
        "incorrect delta calculation for no line, multi-offset delta text",
        realDeltas,
        calculatedDeltas);

    // Test different multi-line, multi-offset delta
    realDeltas = new ImmutablePair<>(10, 24);
    splitPoint = UNIX_TEST_STRING.indexOf("PING!");
    text = UNIX_TEST_STRING.substring(0, splitPoint);
    calculatedDeltas = calculateDeltas(text);

    assertEquals(
        "incorrect delta calculation for no line, multi-offset delta text",
        realDeltas,
        calculatedDeltas);
  }

  /**
   * If this test fails, it could be a sign that an update of the Apache 'commons-lang3' library has
   * caused {@link StringUtils#lastIndexOf(CharSequence, CharSequence)} to no longer correctly
   * filters out escaped new lines.
   *
   * <p>If such a case, all usages of {@link StringUtils#lastIndexOf(CharSequence, CharSequence)}
   * and {@link StringUtils#ordinalIndexOf(CharSequence, CharSequence, int)} have to be adjusted.
   * See {@link GOTOInclusionTransformation}.
   */
  @Test
  public void testCalculateDeltasEscapedNewLine() {
    Pair<Integer, Integer> realDeltas;
    Pair<Integer, Integer> calculatedDeltas;
    String text;

    // Test escaping line endings
    text = UNIX_TEST_STRING.replace(UNIX_LINE_SEPARATOR, ESCAPED_UNIX_LINE_SEPARATOR);

    realDeltas = new ImmutablePair<>(0, text.length());
    calculatedDeltas = calculateDeltas(text);

    assertEquals(
        "incorrect delta calculation for multi-line, multi-offset delta text",
        realDeltas,
        calculatedDeltas);

    // Test mixed escaped and normal line endings
    text =
        "0123"
            + UNIX_LINE_SEPARATOR
            + "456"
            + ESCAPED_UNIX_LINE_SEPARATOR
            + "789"
            + UNIX_LINE_SEPARATOR
            + ESCAPED_UNIX_LINE_SEPARATOR
            + UNIX_LINE_SEPARATOR
            + "0";

    realDeltas = new ImmutablePair<>(3, 1);
    calculatedDeltas = calculateDeltas(text);

    assertEquals(
        "incorrect delta calculation for multi-line, multi-offset delta text",
        realDeltas,
        calculatedDeltas);
  }
}

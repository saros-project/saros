package saros.editor.text;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/** Utility class offering methods to calculate text position related data. */
public class TextPositionUtils {

  /** The default Windows line separator. */
  public static final String WINDOWS_LINE_SEPARATOR = "\r\n";
  /** The default Unix line separator. */
  public static final String UNIX_LINE_SEPARATOR = "\n";

  private TextPositionUtils() {
    // NOP
  }

  /**
   * Calculates the offset of the given text position in the given text using the given line
   * separator.
   *
   * <p>Tries to guess the used line separator by checking for Windows (<code>\r\n</code>) or Unix
   * line separators (<code>\n</code>) in the text.
   *
   * @param text the text with which to calculate the offset
   * @param position the position for which to calculate the offset
   * @return the offset of the given text position in the given text
   * @throws NullPointerException if the given text position, text, or line separator is <code>null
   *     </code>
   * @throws IllegalArgumentException if the given text position is invalid
   * @throws IllegalStateException if the given text contains fewer lines than specified by the text
   *     position
   * @see #guessLineSeparator(String)
   */
  public static int calculateOffset(String text, TextPosition position) {

    Objects.requireNonNull(text, "The given text must not be null");

    String lineSeparator = guessLineSeparator(text);

    return calculateOffset(text, position, lineSeparator);
  }

  /**
   * Calculates the offset of the given text position in the given text using the given line
   * separator.
   *
   * @param text the text with which to calculate the offset
   * @param position the position for which to calculate the offset
   * @param lineSeparator the line separator used in the text
   * @return the offset of the given text position in the given text
   * @throws NullPointerException if the given text position, text, or line separator is <code>null
   *     </code>
   * @throws IllegalArgumentException if the given text position is invalid or if an empty string is
   *     passed as the line separator in combination with a text position that is not located in
   *     line 0
   * @throws IllegalStateException if the given text contains fewer lines than specified by the text
   *     position
   */
  public static int calculateOffset(String text, TextPosition position, String lineSeparator) {

    Objects.requireNonNull(text, "The given document content must not be null");
    Objects.requireNonNull(position, "The given text position must not be null");
    Objects.requireNonNull(lineSeparator, "The given line separator must not be null");

    if (!position.isValid()) {
      throw new IllegalArgumentException("The given position must not be invalid");
    }

    int lineNumber = position.getLineNumber();

    if (lineNumber == 0) {
      return position.getInLineOffset();

    } else if (lineSeparator.isEmpty()) {
      throw new IllegalArgumentException(
          "No line separator was passed for a position that expects a text having multiple lines: "
              + position);
    }

    int previousLineEndOffset = StringUtils.ordinalIndexOf(text, lineSeparator, lineNumber);

    if (previousLineEndOffset == -1) {
      throw new IllegalStateException(
          "The given text contains fewer lines than specified by the text position");
    }

    int lineStartOffset = previousLineEndOffset + lineSeparator.length();

    return lineStartOffset + position.getInLineOffset();
  }

  /**
   * Calculates the line and offset delta contained in the text, i.e. how many lines the text
   * contains and how many characters it contains in the last line.
   *
   * <p>Tries to guess the used line separator by checking for Windows (<code>\r\n</code>) or Unix
   * line separators (<code>\n</code>) in the text.
   *
   * @param text the text for which to calculate the deltas
   * @return a pair containing the line delta as the first/left element and the offset delta as the
   *     second/right element
   * @throws NullPointerException if the given text is <code>null</code>
   * @see #guessLineSeparator(String)
   */
  // TODO remove different line separator handling once internal normalization is set up
  public static Pair<Integer, Integer> calculateDeltas(String text) {
    Objects.requireNonNull(text, "The given text must not be null");

    String lineSeparator = guessLineSeparator(text);

    return calculateDeltas(text, lineSeparator);
  }

  /**
   * Calculates the line and offset delta contained in the text, i.e. how many lines the text
   * contains and how many characters it contains in the last line.
   *
   * @param text the text for which to calculate the deltas
   * @param lineSeparator the line separator contained in the text
   * @return a pair containing the line delta as the first/left element and the offset delta as the
   *     second/right element
   * @throws NullPointerException if the given text or line separator is <code>null</code>
   */
  public static Pair<Integer, Integer> calculateDeltas(String text, String lineSeparator) {
    Objects.requireNonNull(text, "The given text must not be null");
    Objects.requireNonNull(lineSeparator, "The given line separator must not be null");

    if (text.isEmpty()) {
      return new ImmutablePair<>(0, 0);
    }

    int lineDelta = StringUtils.countMatches(text, lineSeparator);

    int offsetDelta;

    if (lineDelta == 0) {
      offsetDelta = text.length();

    } else {
      int lastLineStart = StringUtils.lastIndexOf(text, lineSeparator) + lineSeparator.length();

      offsetDelta = text.length() - lastLineStart;
    }

    return new ImmutablePair<>(lineDelta, offsetDelta);
  }

  /**
   * Tries to figure out the line separator used in the text by whether it contains the Windows or
   * Unix line separator.
   *
   * @param text the text to check
   * @return the used line separator or an empty string if no line separator could be found
   * @see #WINDOWS_LINE_SEPARATOR
   * @see #UNIX_LINE_SEPARATOR
   */
  public static String guessLineSeparator(String text) {

    // Windows line ending must be tested first as the Unix line ending is a substring of it
    if (text.contains(WINDOWS_LINE_SEPARATOR)) {
      return WINDOWS_LINE_SEPARATOR;
    }

    if (text.contains(UNIX_LINE_SEPARATOR)) {
      return UNIX_LINE_SEPARATOR;
    }

    return "";
  }
}

package saros.util;

import java.util.Objects;

/**
 * Utility class offering methods to normalize and denormalize text by replacing the used line
 * separators.
 *
 * <p>The default line separator used for normalized content is the Unix line separator.
 *
 * @see #NORMALIZED_LINE_SEPARATOR
 */
public class LineSeparatorNormalizationUtil {

  /**
   * The line separator used in normalized content.
   *
   * <p>The default Unix line separator.
   */
  public static final String NORMALIZED_LINE_SEPARATOR = "\n";

  private LineSeparatorNormalizationUtil() {
    // NOP
  }

  /**
   * Normalizes the line endings in the given text by replacing all occurrences of the passed line
   * separator with Unix line separator.
   *
   * <p>Does nothing if the given line separator is the Unix line separator or is not contained in
   * the given text.
   *
   * @param text the text to normalize
   * @param usedLineSeparator the line separator used in the given text
   * @return the normalized text containing only Unix line endings
   * @throws NullPointerException if the given text or line separator to use is <code>null</code>
   * @throws IllegalArgumentException if the given line separator to use is empty
   * @see #NORMALIZED_LINE_SEPARATOR
   */
  public static String normalize(String text, String usedLineSeparator) {
    Objects.requireNonNull(text, "The given text must not be null");
    Objects.requireNonNull(usedLineSeparator, "The given line separator must not be null");

    if (usedLineSeparator.isEmpty()) {
      throw new IllegalArgumentException("The given line separator must not be empty.");
    }

    if (text.isEmpty() || usedLineSeparator.equals(NORMALIZED_LINE_SEPARATOR)) {
      return text;
    }

    return text.replace(usedLineSeparator, NORMALIZED_LINE_SEPARATOR);
  }

  /**
   * Reverts the line separator normalization by replacing all occurrences of the Unix line
   * separator with the given line separator.
   *
   * <p>Does nothing if the passed line separator is the Unix line separator.
   *
   * @param text the text whose line ending normalization to revert
   * @param lineSeparatorToUse the line separator to use in the text
   * @return the denormalized text using only the given line separator
   * @throws NullPointerException if the given text or line separator to use is <code>null</code>
   * @throws IllegalArgumentException if the given line separator to use is empty
   * @see #NORMALIZED_LINE_SEPARATOR
   */
  public static String revertNormalization(String text, String lineSeparatorToUse) {
    Objects.requireNonNull(text, "The given text must not be null");
    Objects.requireNonNull(lineSeparatorToUse, "The given line separator must not be null");

    if (lineSeparatorToUse.isEmpty()) {
      throw new IllegalArgumentException("The given line separator must not be empty.");
    }

    if (text.isEmpty() || lineSeparatorToUse.equals(NORMALIZED_LINE_SEPARATOR)) {
      return text;
    }

    return text.replace(NORMALIZED_LINE_SEPARATOR, lineSeparatorToUse);
  }
}

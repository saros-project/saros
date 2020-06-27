package saros.util;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Utility class to escape, unescape, and split strings for a given delimiter and escape character.
 */
public class EscapeUtil {
  private final String delimiter;
  private final String escapeSequence;
  private final Pattern splitPattern;

  public EscapeUtil(String delimiter, String escapeCharacter) {
    this.delimiter = delimiter;
    this.escapeSequence = escapeCharacter + delimiter;
    this.splitPattern =
        Pattern.compile("(?<!" + Pattern.quote(escapeCharacter) + ")" + Pattern.quote(delimiter));
  }

  /**
   * Returns a split pattern for the {@link #delimiter}. The patter splits at every occurrence of
   * the {@link #delimiter} that is not escaped.
   *
   * @return a split pattern for the {@link #delimiter}
   * @see #delimiter
   * @see #escapeSequence
   */
  public Pattern getSplitPattern() {
    return splitPattern;
  }

  /**
   * Splits the given character sequence using the contained split pattern and returns the result.
   *
   * @param charSequence the character sequence to split
   * @return an array containing the split results
   * @see Pattern#split(CharSequence)
   */
  public String[] split(CharSequence charSequence) {
    return splitPattern.split(charSequence);
  }

  /**
   * Splits the given character sequence using the contained split pattern and returns a stream
   * containing the results.
   *
   * @param charSequence the character sequence to split
   * @return a stream containing the split results
   * @see Pattern#splitAsStream(CharSequence)
   */
  public Stream<String> splitAsStream(CharSequence charSequence) {
    return splitPattern.splitAsStream(charSequence);
  }

  /**
   * Escapes the given string by replacing all occurrences of the {@link #delimiter} with the {@link
   * #escapeSequence}.
   *
   * @param string the string to escape
   * @return the given string where all occurrences of the {@link #delimiter} were replaced with the
   *     {@link #escapeSequence}
   */
  public String escape(String string) {
    return string.replace(delimiter, escapeSequence);
  }

  /**
   * Unescapes the given string by replacing all occurrences of the {@link #escapeSequence} with the
   * {@link #delimiter}.
   *
   * @param string the string to unescape
   * @return the given string where all occurrences of the {@link #escapeSequence} were replaced
   *     with the {@link #delimiter}
   */
  public String unescape(String string) {
    return string.replace(escapeSequence, delimiter);
  }
}

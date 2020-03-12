package saros.editor.text;

import java.util.Objects;

/** Immutable representation of a text position through a line number and an in-line offset. */
public class TextPosition implements Comparable<TextPosition> {
  private final int lineNumber;
  private final int inLineOffset;

  /**
   * An empty/invalid text position.
   *
   * <p>Empty/invalid positions are represented as having the line number and in-line offset value
   * of -1.
   *
   * @see #isValid()
   */
  public static final TextPosition INVALID_TEXT_POSITION = new TextPosition();

  /**
   * Creates a new text position.
   *
   * <p>Both the line number and the in-line offset must be greater than or equal to zero.
   *
   * <p>For an empty/invalid text position, use {@link #INVALID_TEXT_POSITION}.
   *
   * @param lineNumber the line number
   * @param inLineOffset the in-line offset
   * @throws IllegalArgumentException if the line number or in-line offset is negative
   */
  public TextPosition(int lineNumber, int inLineOffset) {
    if (lineNumber < 0 || inLineOffset < 0) {
      throw new IllegalArgumentException(
          "The line number and in-line offset must be greater than or equal to zero - l: "
              + lineNumber
              + ", o: "
              + inLineOffset);
    }

    this.lineNumber = lineNumber;
    this.inLineOffset = inLineOffset;
  }

  /** Private constructor for invalid text position objects. */
  private TextPosition() {
    this.lineNumber = -1;
    this.inLineOffset = -1;
  }

  /**
   * Returns the line number.
   *
   * <p>The first line of the document has the number 0.
   *
   * @return the line number
   */
  public int getLineNumber() {
    return lineNumber;
  }

  /**
   * Returns the in-line offset.
   *
   * <p>The first character in the line has the offset 0.
   *
   * @return the in-line offset
   */
  public int getInLineOffset() {
    return inLineOffset;
  }

  /**
   * Returns whether the text position is valid.
   *
   * <p>Invalid/empty text positions have a line number and in-line offset of -1.
   *
   * @return whether the text position is valid
   * @see #INVALID_TEXT_POSITION
   */
  public boolean isValid() {
    return this != INVALID_TEXT_POSITION;
  }

  @Override
  public int compareTo(TextPosition other) {
    if (lineNumber < other.lineNumber) {
      return -1;
    }

    if (lineNumber > other.lineNumber) {
      return 1;
    }

    return Integer.compare(inLineOffset, other.inLineOffset);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lineNumber, inLineOffset);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TextPosition that = (TextPosition) o;

    return this.lineNumber == that.lineNumber && this.inLineOffset == that.inLineOffset;
  }

  @Override
  public String toString() {
    return "["
        + this.getClass().getSimpleName()
        + " - line number: "
        + lineNumber
        + ", in-line offset: "
        + inLineOffset
        + "]";
  }
}

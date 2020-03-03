package saros.editor.text;

import java.util.Objects;

/**
 * Immutable representation of a text selection through two text positions.
 *
 * @see TextPosition
 */
public class TextSelection {
  private final TextPosition startPosition;
  private final TextPosition endPosition;

  /**
   * An empty text selection.
   *
   * <p>A selection of length 0 is <b>not</b> an empty text selection as it describes, e.g., the
   * cursor position in a viewer.
   *
   * <p>Empty text selections are represented as having invalid text positions for the start and end
   * position.
   *
   * @see TextPosition#INVALID_TEXT_POSITION
   * @see #isEmpty()
   */
  public static final TextSelection EMPTY_SELECTION = new TextSelection();

  /**
   * Creates a text selection for the given parameters. This selection object describes a generic
   * text range.
   *
   * <p>Both the start and end position must not be null and must be valid.
   *
   * <p>For an empty selection, use {@link #EMPTY_SELECTION}.
   *
   * @param startPosition the starting position of the selection
   * @param endPosition the end position of the selection
   * @throws NullPointerException if the given start position or end position is <code>null</code>
   * @throws IllegalArgumentException if the given start position or end position is not valid
   * @see TextPosition#isValid()
   */
  public TextSelection(TextPosition startPosition, TextPosition endPosition) {
    Objects.requireNonNull(startPosition, "Starting position must not be null");
    Objects.requireNonNull(endPosition, "End position must not be null");

    if (!(startPosition.isValid() && endPosition.isValid())) {
      throw new IllegalArgumentException(
          "Both the start and end position must be valid. s: "
              + startPosition
              + ", e:"
              + endPosition);
    }

    this.startPosition = startPosition;
    this.endPosition = endPosition;
  }

  /** Private constructor for empty text selection objects. */
  private TextSelection() {
    this.startPosition = TextPosition.INVALID_TEXT_POSITION;
    this.endPosition = TextPosition.INVALID_TEXT_POSITION;
  }

  /**
   * Returns the starting text position of the selection.
   *
   * @return the starting text position of the selection
   */
  public TextPosition getStartPosition() {
    return startPosition;
  }

  /**
   * Returns the end text position of the selection.
   *
   * @return the end text position of the selection
   */
  public TextPosition getEndPosition() {
    return endPosition;
  }

  /**
   * Returns whether this text selection is an empty selection.
   *
   * <p>A selection of length 0 is <b>not</b> an empty text selection as it describes, e.g., the
   * cursor position in a viewer.
   *
   * @return <code>true</code> if this selection is empty
   * @see #EMPTY_SELECTION
   */
  public boolean isEmpty() {
    return this == EMPTY_SELECTION;
  }

  @Override
  public String toString() {
    return "["
        + this.getClass().getSimpleName()
        + " - start position: "
        + startPosition
        + ", end position: "
        + endPosition
        + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(startPosition, endPosition);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TextSelection that = (TextSelection) o;

    return this.startPosition.equals(that.startPosition)
        && this.endPosition.equals(that.endPosition);
  }
}

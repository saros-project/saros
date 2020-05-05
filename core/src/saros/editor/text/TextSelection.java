package saros.editor.text;

import java.util.Objects;

/**
 * Immutable representation of a text selection through two text positions.
 *
 * <p>Optionally, the selection can be declared as a backwards selection through the usage of {@link
 * #isBackwardsSelection}. A backwards selection is a selection where the cursor is located at the
 * start of the selection.
 *
 * @see TextPosition
 */
public class TextSelection {
  private final TextPosition startPosition;
  private final TextPosition endPosition;
  private final boolean isBackwardsSelection;

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
   * Calls {@link TextSelection#TextSelection(TextPosition, TextPosition, boolean)} with <code>
   * isBackwardsSelection=false</code>.
   */
  public TextSelection(TextPosition startPosition, TextPosition endPosition) {
    this(startPosition, endPosition, false);
  }

  /**
   * Creates a text selection for the given parameters. This selection object describes a generic
   * text range.
   *
   * <p>Both the start and end position must not be null, must be valid, and the start position must
   * not be after the end position.
   *
   * <p>For an empty selection, use {@link #EMPTY_SELECTION}.
   *
   * @param startPosition the starting position of the selection
   * @param endPosition the end position of the selection
   * @param isBackwardsSelection whether the selection is a backwards selection
   * @throws NullPointerException if the given start position or end position is <code>null</code>
   * @throws IllegalArgumentException if the given start position or end position is not valid or
   *     the start position is located after the end position
   * @see TextPosition#isValid()
   */
  public TextSelection(
      TextPosition startPosition, TextPosition endPosition, boolean isBackwardsSelection) {
    Objects.requireNonNull(startPosition, "Starting position must not be null");
    Objects.requireNonNull(endPosition, "End position must not be null");

    if (!(startPosition.isValid() && endPosition.isValid())) {
      throw new IllegalArgumentException(
          "Both the start and end position must be valid. s: "
              + startPosition
              + ", e:"
              + endPosition);
    }

    if (startPosition.compareTo(endPosition) > 0) {
      throw new IllegalStateException(
          "The given selection range must be forward facing (start<=end). s: "
              + startPosition
              + ", e: "
              + endPosition
              + " - for backwards selections, use the parameter isBackwardsSelection instead");
    }

    this.startPosition = startPosition;
    this.endPosition = endPosition;
    this.isBackwardsSelection = isBackwardsSelection;
  }

  /** Private constructor for empty text selection objects. */
  private TextSelection() {
    this.startPosition = TextPosition.INVALID_TEXT_POSITION;
    this.endPosition = TextPosition.INVALID_TEXT_POSITION;
    this.isBackwardsSelection = false;
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
   * Returns whether this selection is a backwards selection.
   *
   * <p>A backwards selection is a selection where the cursor is located at the start of the
   * selection.
   *
   * @return whether this selection is a backwards selection
   */
  public boolean isBackwardsSelection() {
    return isBackwardsSelection;
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
        + ", is backwards: "
        + isBackwardsSelection
        + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(startPosition, endPosition, isBackwardsSelection);
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
        && this.endPosition.equals(that.endPosition)
        && this.isBackwardsSelection == that.isBackwardsSelection;
  }
}

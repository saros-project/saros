package saros.editor.text;

/** Container to hold text selection values. */
public class TextSelection {
  private final int offset;
  private final int length;

  private TextSelection() {
    this(-1, -1);
  }

  /**
   * Creates a text selection for the given range. This selection object describes generically a
   * text range.
   *
   * @param offset the offset of the range. selection is empty if negative
   * @param length the length of the range. selection is empty if negative
   */
  public TextSelection(int offset, int length) {
    this.offset = offset;
    this.length = length;
  }

  /**
   * Returns the position of the character which marks the beginning of the selection. Selections
   * including the first character have offset 0.
   */
  public int getOffset() {
    return offset;
  }

  /**
   * Returns the number of characters contained in the selection. Length 0 represents a cursor at
   * position {@link #getOffset() offset}.
   */
  public int getLength() {
    return length;
  }

  /**
   * Returns whether this text selection is an empty selection.
   *
   * <p>A selection of length 0 is not an empty text selection as it describes, e.g., the cursor
   * position in a viewer.
   *
   * @return <code>true</code> if this selection is empty
   */
  public boolean isEmpty() {
    return offset < 0 || length < 0;
  }

  /** Returns an empty text selection. */
  public static TextSelection emptySelection() {
    return new TextSelection();
  }
}

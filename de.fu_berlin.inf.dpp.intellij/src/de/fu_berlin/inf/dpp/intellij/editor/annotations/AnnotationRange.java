package de.fu_berlin.inf.dpp.intellij.editor.annotations;

import com.intellij.openapi.editor.markup.RangeHighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a highlighted area of text used as part of an annotation. The highlighting is done
 * through a {@link RangeHighlighter}. If there is currently no local representation of this
 * highlighted text, meaning the editor the text belongs to is closed, this <code>RangeHighlighter
 * </code> will be <code>null</code>.
 *
 * <p>A <code>RangeHighlighter</code> is automatically updated by Intellij while the editor it
 * belong to is open, meaning the held state of an <code>AnnotationRange</code> should be updated
 * through {@link #updateRange()} when the editor containing the highlighted text is closed.
 *
 * @see RangeHighlighter
 */
class AnnotationRange {
  private int start;
  private int end;

  private RangeHighlighter rangeHighlighter;

  /**
   * Creates a new <code>AnnotationRange</code> with the given position. If there is currently no
   * local representation for the <code>AnnotationRange</code>, <code>null</code> should be passe as
   * the <code>RangeHighlighter</code>.
   *
   * <p>Both the given start and end point have to be positive integers. Furthermore, the start
   * position has to be smaller as or equal to the end position. If a <code>RangeHighlighter</code>
   * is given, it must match the given start and end position.
   *
   * @param start the start point of the annotation range
   * @param end the end point of the annotation range
   * @param rangeHighlighter the representation of the annotated text in a local editor
   */
  AnnotationRange(int start, int end, @Nullable RangeHighlighter rangeHighlighter) {

    if (start < 0 || end < 0) {
      throw new IllegalArgumentException(
          "The start and end of the annotation must not be negative "
              + "values. start: "
              + start
              + ", end: "
              + end);
    }

    if (start > end) {
      throw new IllegalArgumentException(
          "The start of the annotation must not be after the end of the "
              + "annotation. start: "
              + start
              + ", end: "
              + end);
    }

    if (rangeHighlighter != null
        && (start != rangeHighlighter.getStartOffset() || end != rangeHighlighter.getEndOffset())) {

      throw new IllegalArgumentException(
          "The range of the RangeHighlighter does not match the given " + "start and end value");
    }

    this.start = start;
    this.end = end;
    this.rangeHighlighter = rangeHighlighter;
  }

  /**
   * Creates an <code>AnnotationRange</code> with the given position without a local representation.
   *
   * @param start the start point of the annotation range
   * @param end the end point of the annotation range
   */
  AnnotationRange(int start, int end) {
    this(start, end, null);
  }

  /**
   * Adds a local representation to this <code>AnnotationRange</code>. The given <code>
   * RangeHighlighter</code> must match the held start and end position.
   *
   * @param rangeHighlighter the RangeHighlighter for the annotation range
   */
  void addRangeHighlighter(@NotNull RangeHighlighter rangeHighlighter) {

    if (start != rangeHighlighter.getStartOffset() || end != rangeHighlighter.getEndOffset()) {

      throw new IllegalArgumentException(
          "The range of the RangeHighlighter does not match the given " + "start and end value");
    }

    this.rangeHighlighter = rangeHighlighter;
  }

  /** Removes the local representation from this <code>AnnotationRange</code>. */
  void removeRangeHighlighter() {
    rangeHighlighter = null;
  }

  /**
   * Updates the current start and end point of the <code>AnnotationRange</code> with the current
   * position of the held <code>RangeHighlighter</code>.
   *
   * <p>This method does nothing if no <code>RangeHighlighter</code> is present.
   */
  void updateRange() {
    if (rangeHighlighter == null || !rangeHighlighter.isValid()) {
      return;
    }

    start = rangeHighlighter.getStartOffset();
    end = rangeHighlighter.getEndOffset();
  }

  int getStart() {
    return start;
  }

  int getEnd() {
    return end;
  }

  /**
   * Returns the length of the <code>AnnotationRange</code>.
   *
   * @return the length of the <code>AnnotationRange</code>
   */
  int getLength() {
    return end - start;
  }

  @Nullable
  RangeHighlighter getRangeHighlighter() {
    return rangeHighlighter;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "[start="
        + start
        + ", end="
        + end
        + ", rangeHighlighter="
        + rangeHighlighter
        + "]";
  }
}

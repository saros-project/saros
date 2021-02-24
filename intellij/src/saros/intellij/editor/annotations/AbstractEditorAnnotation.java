package saros.intellij.editor.annotations;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Computable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IFile;
import saros.intellij.runtime.EDTExecutor;
import saros.session.User;

/**
 * Base class for Saros annotations. An annotation is a highlighted section of text. Annotations are
 * used by Saros to mark actions of other session participants in the local editor in order to
 * distinguish them from local actions.
 *
 * <p>Each annotation belongs to a {@link User} and an {@link IFile}. The highlighted sections of
 * text are held in a list of {@link AnnotationRange}. If the file the annotations belongs to is
 * currently open, the annotations also holds the current {@link Editor} representing the file. The
 * held <code>IFile</code> is used to determine the file the annotation belongs to, even when there
 * is currently no editor available for the file.
 *
 * <p>The different annotation layers used by Saros are declared in {@link
 * AnnotationHighlighterLayers}.
 *
 * @see AnnotationRange
 */
abstract class AbstractEditorAnnotation {
  private static final Logger log = Logger.getLogger(AbstractEditorAnnotation.class);

  private final User user;
  private IFile file;
  private final List<AnnotationRange> annotationRanges;

  private Editor editor;

  /**
   * Creates an annotation with the given arguments.
   *
   * <p>An annotation needs to contain at least one <code>AnnotationRange</code>.
   *
   * <p>If the given <code>Editor</code> is null, the given annotation ranges <b>must not</b>
   * contain a <code>RangeHighlighter</code>. If the given editor is not null, the given annotation
   * ranges <b>must</b> contain a <code>RangeHighlighter</code>. The created annotation holds a
   * <b>copy</b> of the passed list of annotation ranges instead of the passed list itself. This is
   * done to prevent external sources from directly changing the internal state of the annotation by
   * modifying the passed list.
   *
   * @param user the user the annotation belongs to and whose color is used
   * @param file the file the annotation belongs to
   * @param editor the editor the annotation is displayed in
   * @param annotationRanges the highlighted sections of text belonging to the annotation
   */
  AbstractEditorAnnotation(
      @NotNull User user,
      @NotNull IFile file,
      @Nullable Editor editor,
      @NotNull List<AnnotationRange> annotationRanges) {

    if (annotationRanges.isEmpty()) {
      throw new IllegalArgumentException("An annotation has to have at least one AnnotationRange.");
    }

    if (editor != null) {
      annotationRanges.forEach(
          annotationRange -> {
            if (annotationRange.getRangeHighlighter() == null) {
              throw new IllegalArgumentException(
                  "Found AnnotationRange without RangeHighlighter when "
                      + "using the constructor with a local "
                      + "representation: "
                      + annotationRange);
            }
          });

    } else {
      annotationRanges.forEach(
          annotationRange -> {
            if (annotationRange.getRangeHighlighter() != null) {
              throw new IllegalArgumentException(
                  "Found AnnotationRange with RangeHighlighter when "
                      + "using the constructor without a local "
                      + "representation: "
                      + annotationRange);
            }
          });
    }

    this.user = user;
    this.file = file;
    this.editor = editor;
    this.annotationRanges = new ArrayList<>(annotationRanges);
  }

  /**
   * Checks whether the given start and end offset form a valid range.
   *
   * <p>The following conditions must hold true:
   *
   * <ul>
   *   <li><code>start &ge; 0</code>
   *   <li><code>end &ge; 0</code>
   *   <li><code>start &le; end</code>
   * </ul>
   *
   * @param start the start position
   * @param end the end position
   * @throws IllegalStateException if <code>start &lt; 0</code>, <code>end &lt; 0</code>, or <code>
   *     start &gt; end</code>
   */
  static void checkRange(int start, int end) {
    if (start < 0 || end < 0) {
      throw new IllegalArgumentException(
          "The start and end of the given range must not be a negative value. start: "
              + start
              + ", end: "
              + end);
    }

    if (start > end) {
      throw new IllegalArgumentException(
          "The start of the given range must not be after the end of the range. start: "
              + start
              + ", end: "
              + end);
    }
  }

  /**
   * Adds the given <code>Editor</code> to the annotation.
   *
   * <p>This method should be used when adding the local representation of the annotation when an
   * editor for the corresponding file is opened.
   *
   * @param newEditor the new <code>Editor</code> for the annotation
   */
  void addEditor(@NotNull Editor newEditor) {

    if (newEditor.isDisposed()) {
      throw new IllegalArgumentException("The given editor is already disposed.");
    }

    editor = newEditor;
  }

  /**
   * Adds the given editor to this annotations. Creates and adds the RangeHighlighters for all
   * contained annotation ranges.
   *
   * @param editor the editor to create RangeHighlighters in
   */
  abstract void addLocalRepresentation(@NotNull Editor editor);

  /**
   * Removes all held range highlighters from the held editor and drops the held <code>Editor</code>
   * and all held <code>RangeHighlighter</code> references.
   *
   * <p>Does nothing if no editor is present.
   *
   * <p>This method should be used to remove the local representation of the annotation when the
   * editor for the corresponding file is closed.
   */
  void removeLocalRepresentation() {
    if (editor == null) {
      return;
    }

    for (AnnotationRange annotationRange : annotationRanges) {
      RangeHighlighter rangeHighlighter = annotationRange.getRangeHighlighter();

      annotationRange.removeRangeHighlighter();

      if (rangeHighlighter == null || !rangeHighlighter.isValid()) {
        continue;
      }

      removeRangeHighlighter(editor, rangeHighlighter);
    }

    editor = null;
  }

  /**
   * Synchronizes the state of the displayed <code>RangeHighlighter</code> with the saved boundaries
   * for every <code>AnnotationRange</code>. This is needed as the position of a <code>
   * RangeHighlighter</code> can be changed by Intellij.
   *
   * <p>If a <code>RangeHighlighter</code> has become invalid, the <code>AnnotationRange</code> is
   * removed from the annotation.
   *
   * <p><b>NOTE:</b> It is possible that a annotation does not contain any annotation ranges after
   * this operation. Such annotations are not removed automatically and should therefore be removed
   * from the annotation store by the caller.
   *
   * @see AnnotationRange
   * @see AnnotationRange#updateRange()
   */
  void updateBoundaries() {
    annotationRanges.removeIf(
        annotationRange ->
            annotationRange.getRangeHighlighter() != null
                && !annotationRange.getRangeHighlighter().isValid());

    annotationRanges.forEach(AnnotationRange::updateRange);
  }

  /**
   * Returns an <b>unmodifiable copy</b> of the held list of annotation ranges. The returned list
   * can not be used to modify the internally held list. To modify the internal list of annotation
   * ranges, please use {@link #replaceAnnotationRange(AnnotationRange, AnnotationRange)}.
   *
   * @return an unmodifiable copy of the held list of annotation ranges.
   */
  @NotNull
  List<AnnotationRange> getAnnotationRanges() {
    return Collections.unmodifiableList(new ArrayList<>(annotationRanges));
  }

  /**
   * Replaces the given current annotation range with the given new annotation range if present.
   * Does <b>not</b> add the new annotation range to the annotation if the given current annotation
   * range object is not contained in the current list of annotation ranges.
   *
   * <p><b>NOTE:</b> The given current annotation range object has to be one of the actually held
   * annotation range objects. It is not enough if it just contains the same position and <code>
   * RangeHighlighter</code> as an object in the list.
   *
   * @param currentAnnotationRange the old annotation range to replace
   * @param newAnnotationRange the new annotation range
   */
  void replaceAnnotationRange(
      @NotNull AnnotationRange currentAnnotationRange,
      @NotNull AnnotationRange newAnnotationRange) {

    int index = annotationRanges.indexOf(currentAnnotationRange);

    if (index == -1) {
      return;
    }

    annotationRanges.set(index, newAnnotationRange);
  }

  /**
   * Changes the file the annotation belongs to.
   *
   * @param newFile the new file of the annotation
   */
  void updateFile(@NotNull IFile newFile) {

    file = newFile;
  }

  @NotNull
  User getUser() {
    return user;
  }

  @NotNull
  IFile getFile() {
    return file;
  }

  @Nullable
  Editor getEditor() {
    return editor;
  }

  /**
   * Updates the position of the annotation according to the given addition boundaries.
   *
   * <p>If there are no range highlighters or editors present: Moves the given annotations back by
   * the length of the addition if they are located behind the added text. Elongates the annotations
   * by the length of the addition if they overlap with the added text.
   *
   * <p>Does nothing if the annotation has a local representation (an editor or range highlighters)
   * as this will be done automatically by the internal Intellij logic.
   *
   * @param additionStart the star position of the added text
   * @param additionEnd the end position of the added text
   * @see AnnotationManager#moveAnnotationsAfterAddition(IFile, int, int)
   */
  void moveAfterAddition(int additionStart, int additionEnd) {
    if (editor != null) {
      return;
    }

    checkRange(additionStart, additionEnd);

    int offset = additionEnd - additionStart;

    for (AnnotationRange annotationRange : annotationRanges) {
      int currentStart = annotationRange.getStart();
      int currentEnd = annotationRange.getEnd();

      if (annotationRange.getRangeHighlighter() != null || currentEnd <= additionStart) {

        continue;
      }

      AnnotationRange newAnnotationRange;

      if (currentStart >= additionStart) {
        newAnnotationRange = new AnnotationRange(currentStart + offset, currentEnd + offset);

      } else {
        newAnnotationRange = new AnnotationRange(currentStart, currentEnd + offset);
      }

      replaceAnnotationRange(annotationRange, newAnnotationRange);
    }
  }

  /**
   * Updates the position of the annotation according to the given deletion boundaries. Returns
   * whether the annotation is invalid after the deletion and should therefor be removed from the
   * annotation store.
   *
   * <p>If there are no range highlighters or editors present: Moves all given annotations for the
   * given file forward by the length of the removal if they are located behind the removed text.
   * Shortens the annotations if they partially overlap with the removed text.
   *
   * <p>Does nothing if the annotation has a local representation (an editor or range highlighters)
   * as this will be done automatically by the internal Intellij logic.
   *
   * @param deletionStart the start position of the deleted text
   * @param deletionEnd the end position of the deleted text
   * @return whether the annotation is invalid after the deletion
   * @see AnnotationManager#moveAnnotationsAfterDeletion(IFile, int, int)
   */
  boolean moveAfterDeletion(int deletionStart, int deletionEnd) {
    if (editor != null) {
      return false;
    }

    checkRange(deletionStart, deletionEnd);

    int offset = deletionEnd - deletionStart;

    for (Iterator<AnnotationRange> iterator = annotationRanges.iterator(); iterator.hasNext(); ) {
      AnnotationRange annotationRange = iterator.next();

      int currentStart = annotationRange.getStart();
      int currentEnd = annotationRange.getEnd();

      if (annotationRange.getRangeHighlighter() != null || currentEnd <= deletionStart) {

        continue;
      }

      AnnotationRange newAnnotationRange;

      if (currentStart >= deletionEnd) {
        newAnnotationRange = new AnnotationRange(currentStart - offset, currentEnd - offset);

      } else if (currentStart < deletionStart) {
        if (currentEnd <= deletionEnd) {
          newAnnotationRange = new AnnotationRange(currentStart, deletionStart);

        } else {
          newAnnotationRange = new AnnotationRange(currentStart, currentEnd - offset);
        }

      } else {
        if (currentEnd <= deletionEnd) {
          iterator.remove();

          continue;

        } else {
          newAnnotationRange = new AnnotationRange(deletionStart, currentEnd - offset);
        }
      }

      replaceAnnotationRange(annotationRange, newAnnotationRange);
    }

    return annotationRanges.isEmpty();
  }

  /** Tears down the annotation, removing any remaining range highlighters. */
  protected void dispose() {
    for (AnnotationRange annotationRange : annotationRanges) {
      RangeHighlighter rangeHighlighter = annotationRange.getRangeHighlighter();

      if (rangeHighlighter == null || !rangeHighlighter.isValid()) {
        continue;
      }

      if (editor != null) {
        removeRangeHighlighter(editor, rangeHighlighter);

      } else {
        log.warn(
            "Could not remove range highlighter during teardown of range "
                + annotationRange
                + " as no editor is present. annotation: "
                + this);
      }
    }
  }

  /**
   * Creates a RangeHighlighter with the given position and text attributes for the given editor.
   *
   * <p>The returned <code>RangeHighlighter</code> can not be modified through the API but is
   * automatically updated by Intellij if there are changes to the editor.
   *
   * @param start the start of the highlighted area
   * @param end the end of the highlighted area
   * @param editor the editor to create the highlighter for
   * @param textAttributes the text attributes defining the look of the range highlighter
   * @param highlighterLayer the highlighter layer of the range highlighter
   * @param file the file for the editor
   * @return a RangeHighlighter with the given parameters or <code>null</code> if the given end
   *     position is located after the document end
   */
  @Nullable
  static RangeHighlighter addRangeHighlighter(
      int start,
      int end,
      @NotNull Editor editor,
      @NotNull TextAttributes textAttributes,
      int highlighterLayer,
      @NotNull IFile file) {

    int documentLength = editor.getDocument().getTextLength();

    if (documentLength < end) {
      log.warn(
          "The creation of a range highlighter with the bounds ("
              + start
              + ", "
              + end
              + ") for the file "
              + file.getReferencePoint().getName()
              + " - "
              + file.getReferencePointRelativePath()
              + " failed as the given end position is located after the "
              + "document end. document length: "
              + documentLength
              + ", end position: "
              + end);

      return null;
    }

    return EDTExecutor.invokeAndWait(
        (Computable<RangeHighlighter>)
            () ->
                editor
                    .getMarkupModel()
                    .addRangeHighlighter(
                        start,
                        end,
                        highlighterLayer,
                        textAttributes,
                        HighlighterTargetArea.EXACT_RANGE),
        ModalityState.defaultModalityState());
  }

  /**
   * Removes the given range highlighter from the given editor.
   *
   * @param editor the editor from which to remove the range highlighter
   * @param rangeHighlighter the range highlighter to remove
   */
  static void removeRangeHighlighter(
      @NotNull Editor editor, @NotNull RangeHighlighter rangeHighlighter) {
    EDTExecutor.invokeAndWait(
        () -> editor.getMarkupModel().removeHighlighter(rangeHighlighter),
        ModalityState.defaultModalityState());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "[user="
        + user
        + ", file="
        + file
        + ", editor="
        + editor
        + ", annotationRanges="
        + annotationRanges
        + "]";
  }
}

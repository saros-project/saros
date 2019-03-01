package saros.intellij.editor.annotations;

import com.intellij.openapi.editor.Editor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IFile;
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
 * @see AnnotationRange
 */
abstract class AbstractEditorAnnotation {
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
   * Removes the <code>Editor</code> and <code>RangeHighlighter</code> from the annotation.
   *
   * <p>This method should be used to remove the local representation of the annotation when the
   * editor for the corresponding file is closed.
   */
  void removeLocalRepresentation() {
    editor = null;
    annotationRanges.forEach(AnnotationRange::removeRangeHighlighter);
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
   * Checks whether the annotation has a local representation.
   *
   * @return whether the held editor is not <code>null</code> and all annotation ranges contain a
   *     <code>RangeHighlighter</code>
   */
  boolean hasLocalRepresentation() {
    return editor != null
        && annotationRanges
            .stream()
            .noneMatch(annotationRange -> annotationRange.getRangeHighlighter() == null);
  }

  /**
   * Returns an <b>unmodifiable copy</b> of the held list of annotation ranges. The returned list
   * can not be used to modify the internally held list. To modify the internal list of annotation
   * ranges, please use {@link #replaceAnnotationRange(AnnotationRange, AnnotationRange)} and {@link
   * #removeAnnotationRange(AnnotationRange)}.
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
   * Removes the given <code>AnnotationRange</code> from the held list of annotation ranges if
   * present.
   *
   * @param annotationRange the annotation range to remove
   */
  void removeAnnotationRange(@NotNull AnnotationRange annotationRange) {

    annotationRanges.remove(annotationRange);
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

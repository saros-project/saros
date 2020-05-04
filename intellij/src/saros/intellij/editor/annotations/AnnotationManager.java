package saros.intellij.editor.annotations;

import com.intellij.openapi.editor.Editor;
import java.util.List;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IFile;
import saros.repackaged.picocontainer.Disposable;
import saros.session.User;

/** Annotation manager used to create, delete and manage annotations for a Saros session. */
// TODO move local selections affected by changes while editor is closed; see issue #116
public class AnnotationManager implements Disposable {

  private static final Logger log = Logger.getLogger(AnnotationManager.class);

  public static final int MAX_CONTRIBUTION_ANNOTATIONS =
      Integer.getInteger("saros.intellij.MAX_CONTRIBUTION_ANNOTATIONS", 50);

  private final AnnotationStore<SelectionAnnotation> selectionAnnotationStore;
  private final AnnotationQueue<ContributionAnnotation> contributionAnnotationQueue;

  @Override
  public void dispose() {
    removeAllAnnotations();
  }

  public AnnotationManager() {
    this.selectionAnnotationStore = new AnnotationStore<>();
    this.contributionAnnotationQueue = new AnnotationQueue<>(MAX_CONTRIBUTION_ANNOTATIONS);
  }

  /**
   * Removes the current selection annotation for the given file and user combination if present. If
   * the new annotation is at least one character long (start < end), it subsequently adds a
   * selection annotation with the given parameters to the given file and stores it.
   *
   * <p>If no valid editor is given, the method assumes that the file the annotation belongs to is
   * not open locally. The annotation will still be stored so that it can be applied if the given
   * file is opened later on.
   *
   * <p>The given start position must not be after the given end position: start <= end
   *
   * @param user the user the annotation belongs to
   * @param file the file the annotation belongs to
   * @param start the starting position of the annotation
   * @param end the ending position of the annotation
   * @param editor the editor the annotation is applied to
   */
  public void addSelectionAnnotation(
      @NotNull User user, @NotNull IFile file, int start, int end, @Nullable Editor editor) {

    removeSelectionAnnotation(user, file);

    checkRange(start, end);

    if (start == end) {
      return;
    }

    SelectionAnnotation selectionAnnotation;

    try {
      selectionAnnotation = new SelectionAnnotation(user, file, start, end, editor);

    } catch (IllegalStateException e) {
      log.warn(
          "Failed to add contribution annotation for file "
              + file
              + " and user "
              + user
              + " at position("
              + start
              + ","
              + end
              + ").",
          e);

      return;
    }

    selectionAnnotationStore.addAnnotation(selectionAnnotation);
  }

  /**
   * Removes the current selection annotation for the given file and user combination if present.
   *
   * @param user the user the annotation belongs to
   * @param file the file the annotation belongs to
   */
  public void removeSelectionAnnotation(@NotNull User user, @NotNull IFile file) {
    List<SelectionAnnotation> currentSelectionAnnotation =
        selectionAnnotationStore.removeAnnotations(user, file);

    for (SelectionAnnotation annotation : currentSelectionAnnotation) {
      AbstractEditorAnnotation.removeRangeHighlighter(
          annotation.getEditor(), annotation.getAnnotationRanges());
    }
  }

  /**
   * Applies a contribution annotations to the given editor with the given parameters and stores it.
   *
   * <p>If no valid editor is given, the method assumes that the file the annotation belongs to is
   * not open locally. The annotation will still be stored so that it can be applied if the given
   * file is opened later on.
   *
   * <p>The given start position must not be after the given end position: start <= end
   *
   * @param user the user the annotation belongs to
   * @param file the file the annotation belongs to
   * @param editor the editor the annotation is applied to
   * @param start the starting position of the annotation
   * @param end the ending position of the annotation
   */
  public void addContributionAnnotation(
      @NotNull User user, @NotNull IFile file, int start, int end, @Nullable Editor editor) {

    checkRange(start, end);

    if (start == end) {
      return;
    }

    ContributionAnnotation contributionAnnotation;

    try {
      contributionAnnotation = new ContributionAnnotation(user, file, start, end, editor);

    } catch (IllegalStateException e) {
      log.warn(
          "Failed to add contribution annotation for file "
              + file
              + " and user "
              + user
              + " at position("
              + start
              + ","
              + end
              + ").",
          e);

      return;
    }

    ContributionAnnotation dequeuedAnnotation = contributionAnnotationQueue.removeIfFull();

    if (dequeuedAnnotation != null) {
      AbstractEditorAnnotation.removeRangeHighlighter(
          dequeuedAnnotation.getEditor(), dequeuedAnnotation.getAnnotationRanges());
    }

    contributionAnnotationQueue.addAnnotation(contributionAnnotation);
  }

  /**
   * Moves all annotations for the given file back by the length of the addition if they are located
   * behind the added text. Elongates the annotations by the length of the addition if they overlap
   * with the added text.
   *
   * <p>If a RangeHighlighter is present, this will all be done automatically by the internal
   * Intellij logic. This is why {@link AnnotationManager#updateAnnotationStore(IFile)} should
   * <b>always</b> be called before an editor is closed to synchronize the boundaries of the saved
   * annotations with the currently displayed boundaries in the editor annotation model and remove
   * invalid annotations from the annotation store.
   *
   * <p>This method should be used to adjust the position of all annotations after text was added to
   * a currently closed file.
   *
   * @param file the file text was added to
   * @param additionStart the start position of added text
   * @param additionEnd the end position of the added text
   */
  public void moveAnnotationsAfterAddition(
      @NotNull IFile file, int additionStart, int additionEnd) {

    if (additionStart == additionEnd) {
      return;
    }

    checkRange(additionStart, additionEnd);

    for (SelectionAnnotation annotation : selectionAnnotationStore.getAnnotations(file)) {
      annotation.moveAfterAddition(additionStart, additionEnd);
    }

    for (ContributionAnnotation annotation : contributionAnnotationQueue.getAnnotations(file)) {
      annotation.moveAfterAddition(additionStart, additionEnd);
    }
  }

  /**
   * Moves all annotations for the given file forward by the length of the removal if they are
   * located behind the removed text. Shortens the annotations if they partially overlap with the
   * removed text. Removes all annotations that were completely contained in the removed text.
   *
   * <p>If a RangeHighlighter is present, this will all be done automatically by the internal
   * Intellij logic. This is why {@link AnnotationManager#updateAnnotationStore(IFile)} should
   * <b>always</b> be called before an editor is closed to synchronize the boundaries of the saved
   * annotations with the currently displayed boundaries in the editor annotation model and remove
   * invalid annotations from the annotation store.
   *
   * <p>This method should be used to adjust the position of all annotations after text was added to
   * a currently closed file.
   *
   * @param file the file text was removed from
   * @param deletionStart the start position of removed text
   * @param deletionEnd the end position of the removed text
   */
  public void moveAnnotationsAfterDeletion(
      @NotNull IFile file, int deletionStart, int deletionEnd) {

    if (deletionStart == deletionEnd) {
      return;
    }

    checkRange(deletionStart, deletionEnd);

    for (SelectionAnnotation annotation : selectionAnnotationStore.getAnnotations(file)) {
      boolean isInvalid = annotation.moveAfterDeletion(deletionStart, deletionEnd);

      if (isInvalid) {
        selectionAnnotationStore.removeAnnotation(annotation);
      }
    }

    for (ContributionAnnotation annotation : contributionAnnotationQueue.getAnnotations(file)) {
      boolean isInvalid = annotation.moveAfterDeletion(deletionStart, deletionEnd);

      if (isInvalid) {
        contributionAnnotationQueue.removeAnnotation(annotation);
      }
    }
  }

  /**
   * Applies all stored annotations for the given file to the given editor and adds the given editor
   * to the annotations.
   *
   * <p>This method does nothing if the given editor is already disposed.
   *
   * <p>This method should be used to add existing annotations to a newly opened editor.
   *
   * @param file the file that was opened in an editor
   * @param editor the new <code>Editor</code> for the annotation
   * @see AbstractEditorAnnotation#addLocalRepresentation(Editor)
   * @see Editor#isDisposed()
   */
  public void applyStoredAnnotations(@NotNull IFile file, @NotNull Editor editor) {

    for (SelectionAnnotation annotation : selectionAnnotationStore.getAnnotations(file)) {
      annotation.addLocalRepresentation(editor);
    }

    for (ContributionAnnotation annotation : contributionAnnotationQueue.getAnnotations(file)) {
      annotation.addLocalRepresentation(editor);
    }
  }

  /**
   * Updates all annotations for the given file in all annotation stores by checking if an editor
   * for the annotation is present and then updating the stored annotation range if it has changed.
   * If the annotation is marked as not valid by the editor, it is removed from the annotation
   * store.
   *
   * <p>This method must be called <b>before</b> the editor is closed.
   *
   * <p>This method should be called before {@link #removeLocalRepresentation(IFile)} to update all
   * annotations.
   *
   * @param file the file to update
   */
  public void updateAnnotationStore(@NotNull IFile file) {

    updateAnnotationStore(selectionAnnotationStore, file);
    updateAnnotationStore(contributionAnnotationQueue, file);
  }

  /**
   * Updates the given annotation stores for the given file by checking if an editor for the
   * annotation is present and then updating the stored annotation range if it has changed. If the
   * annotation is marked as not valid by the editor, it is removed from the annotation store.
   *
   * @param annotationStore the annotation store to update
   * @param file the file to update
   * @param <E> the type of annotations stored in the given annotation store
   */
  private <E extends AbstractEditorAnnotation> void updateAnnotationStore(
      @NotNull AnnotationStore<E> annotationStore, @NotNull IFile file) {

    List<E> annotations = annotationStore.getAnnotations(file);

    annotations.forEach(
        annotation -> {
          annotation.updateBoundaries();

          if (annotation.getAnnotationRanges().isEmpty()) {
            annotationStore.removeAnnotation(annotation);
          }
        });
  }

  /**
   * Removes the local representation of the annotation from all stored annotations for the given
   * file.
   *
   * <p>This method should be used to remove the local representation of the annotations when the
   * editor for the corresponding file is closed.
   *
   * <p>{@link #updateAnnotationStore(IFile)} should <b>always</b> be called before this method to
   * update the local state of the annotations with the ranges of the currently displayed
   * highlighters.
   *
   * @param file the file whose editor was closed
   * @see AbstractEditorAnnotation#removeLocalRepresentation()
   */
  public void removeLocalRepresentation(@NotNull IFile file) {

    selectionAnnotationStore
        .getAnnotations(file)
        .forEach(AbstractEditorAnnotation::removeLocalRepresentation);

    contributionAnnotationQueue
        .getAnnotations(file)
        .forEach(AbstractEditorAnnotation::removeLocalRepresentation);
  }

  /**
   * Reloads all annotations in all held annotation stores.
   *
   * <p>This method should be used to react to changes in the IDE theme, which could cause the
   * appearance (e.g. color) of the annotation to change.
   */
  public void reloadAnnotations() {
    reloadAnnotations(selectionAnnotationStore);

    reloadAnnotations(contributionAnnotationQueue);
  }

  /**
   * Reloads all annotations in the passed annotation store.
   *
   * <p>This is done by first updating the store annotations to ensure that the new local
   * representations span the correct area of text. Then, for each annotation, the local
   * representation is removed and re-added.
   *
   * @param annotationStore the annotation store whose annotations to reload
   * @param <E> the annotation type
   * @see AbstractEditorAnnotation#updateBoundaries()
   * @see AbstractEditorAnnotation#removeRangeHighlighter(Editor, List)
   * @see AbstractEditorAnnotation#removeLocalRepresentation()
   * @see AbstractEditorAnnotation#addLocalRepresentation(Editor)
   */
  private <E extends AbstractEditorAnnotation> void reloadAnnotations(
      @NotNull AnnotationStore<E> annotationStore) {

    for (E annotation : annotationStore.getAnnotations()) {
      Editor editor = annotation.getEditor();

      if (editor == null) {
        continue;
      }

      annotation.updateBoundaries();

      AbstractEditorAnnotation.removeRangeHighlighter(
          annotation.getEditor(), annotation.getAnnotationRanges());

      annotation.removeLocalRepresentation();

      annotation.addLocalRepresentation(editor);
    }
  }

  /**
   * Removes all annotations belonging to the given user from all annotation stores and from all
   * open editors.
   *
   * <p>This method should be used to remove all annotations belonging to a user that left the
   * session.
   *
   * @param user the user whose annotations to remove
   */
  public void removeAnnotations(@NotNull User user) {

    for (SelectionAnnotation annotation : selectionAnnotationStore.removeAnnotations(user)) {
      AbstractEditorAnnotation.removeRangeHighlighter(
          annotation.getEditor(), annotation.getAnnotationRanges());
    }

    for (ContributionAnnotation annotation : contributionAnnotationQueue.removeAnnotations(user)) {
      AbstractEditorAnnotation.removeRangeHighlighter(
          annotation.getEditor(), annotation.getAnnotationRanges());
    }
  }

  /**
   * Removes all annotations belonging to the given file from all annotation stores and from the
   * editor for the file.
   *
   * <p>This method should be used when a file is deleted or removed from the session scope.
   *
   * @param file the file to remove from the annotation store
   */
  public void removeAnnotations(@NotNull IFile file) {

    for (SelectionAnnotation selectionAnnotation : selectionAnnotationStore.getAnnotations(file)) {

      AbstractEditorAnnotation.removeRangeHighlighter(
          selectionAnnotation.getEditor(), selectionAnnotation.getAnnotationRanges());

      selectionAnnotationStore.removeAnnotation(selectionAnnotation);
    }

    for (ContributionAnnotation contributionAnnotation :
        contributionAnnotationQueue.getAnnotations(file)) {

      AbstractEditorAnnotation.removeRangeHighlighter(
          contributionAnnotation.getEditor(), contributionAnnotation.getAnnotationRanges());

      contributionAnnotationQueue.removeAnnotation(contributionAnnotation);
    }
  }

  /**
   * Removes all annotations from all open editors and removes all the stored annotations from all
   * annotation stores.
   */
  private void removeAllAnnotations() {
    for (SelectionAnnotation annotation : selectionAnnotationStore.removeAllAnnotations()) {
      AbstractEditorAnnotation.removeRangeHighlighter(
          annotation.getEditor(), annotation.getAnnotationRanges());
    }

    for (ContributionAnnotation annotation : contributionAnnotationQueue.removeAllAnnotations()) {
      AbstractEditorAnnotation.removeRangeHighlighter(
          annotation.getEditor(), annotation.getAnnotationRanges());
    }
  }

  /**
   * Sets the given new file as the file for all annotations belonging to the given old file and
   * updates the mapping of all annotation stores.
   *
   * <p>This method should be used when a file is moved.
   *
   * <p><b>NOTE:</b> If the move was caused by a received Saros activity, the local representation
   * has to be removed from the corresponding annotations. This method assumes that such local
   * representations were already removed if necessary. This can be done by closing the old editor
   * before calling this method. The local representation for all annotations will then be
   * re-created once an editor for the new file is opened. If the move was caused by a local action,
   * the editor and range highlighters contained in the stored annotations can still be used as they
   * will get updated by the internal Intellij logic.
   *
   * @param oldFile the old file of the annotations
   * @param newFile the new file of the annotations
   */
  public void updateAnnotationPath(@NotNull IFile oldFile, @NotNull IFile newFile) {

    updateAnnotationPath(newFile, selectionAnnotationStore.getAnnotations(oldFile));

    selectionAnnotationStore.updateAnnotationPath(oldFile, newFile);

    updateAnnotationPath(newFile, contributionAnnotationQueue.getAnnotations(oldFile));

    contributionAnnotationQueue.updateAnnotationPath(oldFile, newFile);
  }

  /**
   * Sets the given file as the new file for the given annotations to correctly store the new path
   * of a moved file.
   *
   * <p><b>NOTE:</b> If the move was caused by a received Saros activity, the local representation
   * has to be removed from the corresponding annotations. This method assumes that such local
   * representations were already removed if necessary.
   *
   * @param newFile the new file of the annotations
   * @param oldAnnotations the annotations for the old file
   * @param <E> the type of annotations stored in the given annotation store
   */
  private <E extends AbstractEditorAnnotation> void updateAnnotationPath(
      @NotNull IFile newFile, @NotNull List<E> oldAnnotations) {

    for (E oldAnnotation : oldAnnotations) {
      oldAnnotation.updateFile(newFile);
    }
  }

  /**
   * Checks whether the given start and end point form a valid range.
   *
   * <p>The following conditions must hold true:
   *
   * <ul>
   *   <li>start >= 0
   *   <li>end >= 0
   *   <li>start <= end
   * </ul>
   *
   * Throws an <code>IllegalArgumentException</code> otherwise.
   *
   * @param start the start position
   * @param end the end position
   */
  private void checkRange(int start, int end) {
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
  }
}

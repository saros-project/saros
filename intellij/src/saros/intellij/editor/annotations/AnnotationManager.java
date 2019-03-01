package de.fu_berlin.inf.dpp.intellij.editor.annotations;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorManager;
import de.fu_berlin.inf.dpp.session.User;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Annotation manager used to create, delete and manage annotations for a Saros session. */
// TODO save local selection before editor is closed
// TODO move saved local selections affected by changes while editor is closed
// TODO adjust position of local selection when editor is re-opened
public class AnnotationManager {

  private static final Logger LOG = Logger.getLogger(AnnotationManager.class);

  /** Enum containing the possible annotation types. */
  public enum AnnotationType {
    SELECTION_ANNOTATION,
    CONTRIBUTION_ANNOTATION
  }

  private static final int MAX_CONTRIBUTION_ANNOTATIONS =
      Integer.getInteger("saros.intellij.MAX_CONTRIBUTION_ANNOTATIONS", 50);

  private final AnnotationStore<SelectionAnnotation> selectionAnnotationStore;
  private final AnnotationQueue<ContributionAnnotation> contributionAnnotationQueue;

  private final Application application;

  public AnnotationManager() {
    this.selectionAnnotationStore = new AnnotationStore<>();
    this.contributionAnnotationQueue = new AnnotationQueue<>(MAX_CONTRIBUTION_ANNOTATIONS);

    this.application = ApplicationManager.getApplication();
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

    List<SelectionAnnotation> currentSelectionAnnotation =
        selectionAnnotationStore.removeAnnotations(user, file);

    currentSelectionAnnotation.forEach(this::removeRangeHighlighter);

    checkRange(start, end);

    if (start == end) {
      return;
    }

    AnnotationRange annotationRange;

    if (editor != null) {
      RangeHighlighter rangeHighlighter =
          addRangeHighlighter(user, start, end, editor, AnnotationType.SELECTION_ANNOTATION, file);

      if (rangeHighlighter == null) {
        return;
      }

      annotationRange = new AnnotationRange(start, end, rangeHighlighter);

    } else {
      annotationRange = new AnnotationRange(start, end);
    }

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(user, file, editor, Collections.singletonList(annotationRange));

    selectionAnnotationStore.addAnnotation(selectionAnnotation);
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

    List<AnnotationRange> annotationRanges = new ArrayList<>();

    for (int i = 0; i < end - start; i++) {
      int currentStart = start + i;
      int currentEnd = start + i + 1;

      AnnotationRange annotationRange;

      if (editor != null) {
        RangeHighlighter rangeHighlighter =
            addRangeHighlighter(
                user,
                currentStart,
                currentEnd,
                editor,
                AnnotationType.CONTRIBUTION_ANNOTATION,
                file);

        if (rangeHighlighter == null) {
          return;
        }

        annotationRange = new AnnotationRange(currentStart, currentEnd, rangeHighlighter);

      } else {
        annotationRange = new AnnotationRange(currentStart, currentEnd);
      }

      annotationRanges.add(annotationRange);
    }

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(user, file, editor, annotationRanges);

    ContributionAnnotation dequeuedAnnotation = contributionAnnotationQueue.removeIfFull();

    if (dequeuedAnnotation != null) {
      removeRangeHighlighter(dequeuedAnnotation);
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
   * @param start the start position of added text
   * @param end the end position of the added text
   */
  public void moveAnnotationsAfterAddition(@NotNull IFile file, int start, int end) {

    if (start == end) {
      return;
    }

    checkRange(start, end);

    moveAnnotationsAfterAddition(selectionAnnotationStore.getAnnotations(file), start, end);
    moveAnnotationsAfterAddition(contributionAnnotationQueue.getAnnotations(file), start, end);
  }

  /**
   * If there are not range highlighters or editors present: Moves the given annotations back by the
   * length of the addition if they are located behind the added text. Elongates the annotations by
   * the length of the addition if they overlap with the added text.
   *
   * <p>Does nothing if the annotation has a local representation (an editor or range highlighters).
   *
   * @param annotations the annotations to move
   * @param additionStart the star position of the added text
   * @param additionEnd the end position of the added text
   * @param <E> the annotation type
   * @see #moveAnnotationsAfterAddition(IFile, int, int)
   */
  private <E extends AbstractEditorAnnotation> void moveAnnotationsAfterAddition(
      @NotNull List<E> annotations, int additionStart, int additionEnd) {

    int offset = additionEnd - additionStart;

    annotations.forEach(
        annotation -> {
          if (annotation.getEditor() != null) {
            return;
          }

          annotation
              .getAnnotationRanges()
              .forEach(
                  annotationRange -> {
                    int currentStart = annotationRange.getStart();
                    int currentEnd = annotationRange.getEnd();

                    if (annotationRange.getRangeHighlighter() != null
                        || currentEnd <= additionStart) {

                      return;
                    }

                    AnnotationRange newAnnotationRange;

                    if (currentStart >= additionStart) {
                      newAnnotationRange =
                          new AnnotationRange(currentStart + offset, currentEnd + offset);

                    } else {
                      newAnnotationRange = new AnnotationRange(currentStart, currentEnd + offset);
                    }

                    annotation.replaceAnnotationRange(annotationRange, newAnnotationRange);
                  });
        });
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
   * @param start the start position of removed text
   * @param end the end position of the removed text
   */
  public void moveAnnotationsAfterDeletion(@NotNull IFile file, int start, int end) {

    if (start == end) {
      return;
    }

    checkRange(start, end);

    List<SelectionAnnotation> emptySelectionAnnotations =
        moveAnnotationsAfterDeletion(selectionAnnotationStore.getAnnotations(file), start, end);

    emptySelectionAnnotations.forEach(selectionAnnotationStore::removeAnnotation);

    List<ContributionAnnotation> emptyContributionAnnotations =
        moveAnnotationsAfterDeletion(contributionAnnotationQueue.getAnnotations(file), start, end);

    emptyContributionAnnotations.forEach(contributionAnnotationQueue::removeAnnotation);
  }

  /**
   * If there are not range highlighters or editors present: Moves all given annotations for the
   * given file forward by the length of the removal if they are located behind the removed text.
   * Shortens the annotations if they partially overlap with the removed text. Returns a list of
   * annotations that were completely contained in the removed text.
   *
   * <p>Does nothing if the annotation has a local representation (an editor or range highlighters).
   *
   * @param annotations the annotations to adjust
   * @param deletionStart the start position of the deleted text
   * @param deletionEnd the end position of the deleted text
   * @param <E> the annotation type
   * @return the list of deleted annotations
   * @see #moveAnnotationsAfterDeletion(IFile, int, int)
   */
  @NotNull
  private <E extends AbstractEditorAnnotation> List<E> moveAnnotationsAfterDeletion(
      @NotNull List<E> annotations, int deletionStart, int deletionEnd) {

    int offset = deletionEnd - deletionStart;

    List<E> emptyAnnotations = new ArrayList<>();

    annotations.forEach(
        annotation -> {
          if (annotation.getEditor() != null) {
            return;
          }

          annotation
              .getAnnotationRanges()
              .forEach(
                  annotationRange -> {
                    int currentStart = annotationRange.getStart();
                    int currentEnd = annotationRange.getEnd();

                    if (annotationRange.getRangeHighlighter() != null
                        || currentEnd <= deletionStart) {

                      return;
                    }

                    AnnotationRange newAnnotationRange;

                    if (currentStart >= deletionEnd) {
                      newAnnotationRange =
                          new AnnotationRange(currentStart - offset, currentEnd - offset);

                    } else if (currentStart < deletionStart) {
                      if (currentEnd <= deletionEnd) {
                        newAnnotationRange = new AnnotationRange(currentStart, deletionStart);

                      } else {
                        newAnnotationRange = new AnnotationRange(currentStart, currentEnd - offset);
                      }

                    } else {
                      if (currentEnd <= deletionEnd) {
                        annotation.removeAnnotationRange(annotationRange);

                        return;

                      } else {
                        newAnnotationRange =
                            new AnnotationRange(deletionStart, currentEnd - offset);
                      }
                    }

                    annotation.replaceAnnotationRange(annotationRange, newAnnotationRange);
                  });

          if (annotation.getAnnotationRanges().isEmpty()) {
            emptyAnnotations.add(annotation);
          }
        });

    return emptyAnnotations;
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
   * @see Editor#isDisposed()
   */
  public void applyStoredAnnotations(@NotNull IFile file, @NotNull Editor editor) {

    addLocalRepresentationToAnnotations(selectionAnnotationStore.getAnnotations(file), editor);

    addLocalRepresentationToAnnotations(contributionAnnotationQueue.getAnnotations(file), editor);
  }

  /**
   * Creates RangeHighlighters for the given annotations and adds the given editor and the matching
   * created RangeHighlighters to each given annotation.
   *
   * @param annotations the annotations to add a local representation to
   * @param editor the editor to create RangeHighlighters in
   * @param <E> the annotation type
   * @see #addRangeHighlighter(User, int, int, Editor, AnnotationType, IFile)
   */
  private <E extends AbstractEditorAnnotation> void addLocalRepresentationToAnnotations(
      @NotNull List<E> annotations, @NotNull Editor editor) {

    annotations.forEach(
        annotation -> {
          AnnotationType annotationType;

          if (annotation instanceof SelectionAnnotation) {
            annotationType = AnnotationType.SELECTION_ANNOTATION;

          } else if (annotation instanceof ContributionAnnotation) {
            annotationType = AnnotationType.CONTRIBUTION_ANNOTATION;

          } else {
            throw new IllegalArgumentException("Unknown annotation type " + annotation.getClass());
          }

          User user = annotation.getUser();
          List<AnnotationRange> annotationRanges = annotation.getAnnotationRanges();

          annotation.addEditor(editor);

          IFile file = annotation.getFile();

          annotationRanges.forEach(
              annotationRange -> {
                int start = annotationRange.getStart();
                int end = annotationRange.getEnd();

                RangeHighlighter rangeHighlighter =
                    addRangeHighlighter(user, start, end, editor, annotationType, file);

                if (rangeHighlighter != null) {
                  annotationRange.addRangeHighlighter(rangeHighlighter);
                }
              });
        });
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
   * Removes all annotations belonging to the given user from all annotation stores and from all
   * open editors.
   *
   * <p>This method should be used to remove all annotations belonging to a user that left the
   * session.
   *
   * @param user the user whose annotations to remove
   */
  public void removeAnnotations(@NotNull User user) {

    selectionAnnotationStore.removeAnnotations(user).forEach(this::removeRangeHighlighter);

    contributionAnnotationQueue.removeAnnotations(user).forEach(this::removeRangeHighlighter);
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

      removeRangeHighlighter(selectionAnnotation);
      selectionAnnotationStore.removeAnnotation(selectionAnnotation);
    }

    for (ContributionAnnotation contributionAnnotation :
        contributionAnnotationQueue.getAnnotations(file)) {

      removeRangeHighlighter(contributionAnnotation);
      contributionAnnotationQueue.removeAnnotation(contributionAnnotation);
    }
  }

  /**
   * Removes all annotations from all open editors and removes all the stored annotations from all
   * annotation stores.
   */
  public void removeAllAnnotations() {
    selectionAnnotationStore.removeAllAnnotations().forEach(this::removeRangeHighlighter);

    contributionAnnotationQueue.removeAllAnnotations().forEach(this::removeRangeHighlighter);
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

  /**
   * Creates a RangeHighlighter with the given position for the given editor.
   *
   * <p>The color of the highlighter is determined by the given user and annotation type. Valid
   * types are defined in the enum <code>AnnotationType</code>. The returned <code>RangeHighlighter
   * </code> can not be modified through the API but is automatically updated by Intellij if there
   * are changes to the editor.
   *
   * @param user the user whose color to use
   * @param start the start of the highlighted area
   * @param end the end of the highlighted area
   * @param editor the editor to create the highlighter for
   * @param annotationType the type of annotation
   * @param file the file the annotation belongs to
   * @return a RangeHighlighter with the given parameters or <code>null</code> if the given end
   *     position is located after the document end
   */
  @Nullable
  private RangeHighlighter addRangeHighlighter(
      @NotNull User user,
      int start,
      int end,
      @NotNull Editor editor,
      @NotNull AnnotationType annotationType,
      @NotNull IFile file) {

    int documentLength = editor.getDocument().getTextLength();

    if (documentLength < end) {
      LOG.warn(
          "The creation of a range highlighter with the bounds ("
              + start
              + ", "
              + end
              + ") for the file "
              + file.getProject().getName()
              + " - "
              + file.getProjectRelativePath()
              + " failed as the given end position is located after the "
              + "document end. document length: "
              + documentLength
              + ", end position: "
              + end);

      return null;
    }

    Color color;
    switch (annotationType) {
      case SELECTION_ANNOTATION:
        color = ColorManager.getColorModel(user.getColorID()).getSelectColor();
        break;

      case CONTRIBUTION_ANNOTATION:
        color = ColorManager.getColorModel(user.getColorID()).getEditColor();
        break;

      default:
        throw new IllegalArgumentException("Unknown annotation type: " + annotationType);
    }

    TextAttributes textAttr = new TextAttributes();
    textAttr.setBackgroundColor(color);

    AtomicReference<RangeHighlighter> result = new AtomicReference<>();

    application.invokeAndWait(
        () ->
            result.set(
                editor
                    .getMarkupModel()
                    .addRangeHighlighter(
                        start,
                        end,
                        HighlighterLayer.LAST,
                        textAttr,
                        HighlighterTargetArea.EXACT_RANGE)),
        ModalityState.defaultModalityState());

    return result.get();
  }

  /**
   * Removes all existing RangeHighlighters for the given annotation from the editor of the
   * annotation. This does <b>not</b> affect the stored values in the given annotation, meaning the
   * objects for the RangeHighlighters will still remain stored in the annotation.
   *
   * @param annotation the annotation whose highlighters to remove
   */
  private void removeRangeHighlighter(@NotNull AbstractEditorAnnotation annotation) {

    Editor editor = annotation.getEditor();

    if (editor == null) {
      return;
    }

    List<AnnotationRange> annotationRanges = annotation.getAnnotationRanges();

    annotationRanges.forEach(
        annotationRange -> {
          RangeHighlighter rangeHighlighter = annotationRange.getRangeHighlighter();

          if (rangeHighlighter == null || !rangeHighlighter.isValid()) {
            return;
          }

          application.invokeAndWait(
              () -> editor.getMarkupModel().removeHighlighter(rangeHighlighter),
              ModalityState.defaultModalityState());
        });
  }
}

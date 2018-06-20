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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Annotation manager used to create, delete and manage annotations for a Saros
 * session.
 */
//TODO save local selection before editor is closed
//TODO move saved local selections affected by changes while editor is closed
//TODO adjust position of local selection when editor is re-opened
public class AnnotationManager {
    /**
     * Enum containing the possible annotation types.
     */
    public enum AnnotationType {
        SELECTION_ANNOTATION, CONTRIBUTION_ANNOTATION
    }

    private final AnnotationStore<SelectionAnnotation> selectionAnnotationStore;
    private final AnnotationStore<ContributionAnnotation> contributionAnnotationStore;

    private final Application application;

    public AnnotationManager() {
        this.selectionAnnotationStore = new AnnotationStore<>();
        this.contributionAnnotationStore = new AnnotationStore<>();

        this.application = ApplicationManager.getApplication();
    }

    /**
     * Removes the current selection annotation for the given file and user
     * combination if present. If the new annotation is at least one character
     * long (start < end), it subsequently adds a selection annotation with the
     * given parameters to the given file and stores it.
     * <p></p>
     * If no valid editor is given, the method assumes that the file the
     * annotation belongs to is not open locally. The annotation will still be
     * stored so that it can be applied if the given file is opened later on.
     * <p></p>
     * The given start position must not be after the given end position:
     * start <= end
     *
     * @param user   the user the annotation belongs to
     * @param file   the file the annotation belongs to
     * @param start  the starting position of the annotation
     * @param end    the ending position of the annotation
     * @param editor the editor the annotation is applied to
     */
    public void addSelectionAnnotation(
        @NotNull
            User user,
        @NotNull
            IFile file, int start, int end,
        @Nullable
            Editor editor) {

        List<SelectionAnnotation> currentSelectionAnnotation = selectionAnnotationStore
            .removeAnnotations(user, file);

        currentSelectionAnnotation.forEach(this::removeRangeHighlighter);

        checkRange(start, end);

        if (start == end) {
            return;
        }

        List<AnnotationRange> annotationRanges = new ArrayList<>();
        AnnotationRange annotationRange;

        if (editor != null) {
            RangeHighlighter rangeHighlighter = addRangeHighlighter(user, start,
                end, editor, AnnotationType.SELECTION_ANNOTATION);

            annotationRange = new AnnotationRange(start, end, rangeHighlighter);

        } else {
            annotationRange = new AnnotationRange(start, end);
        }

        annotationRanges.add(annotationRange);

        SelectionAnnotation selectionAnnotation = new SelectionAnnotation(user,
            file, editor, annotationRanges);

        selectionAnnotationStore.addAnnotation(selectionAnnotation);
    }

    /**
     * Applies a contribution annotations to the given editor with the given
     * parameters and stores it.
     * <p></p>
     * If no valid editor is given, the method assumes that the file the
     * annotation belongs to is not open locally. The annotation will still be
     * stored so that it can be applied if the given file is opened later on.
     * <p></p>
     * The given start position must not be after the given end position:
     * start <= end
     *
     * @param user   the user the annotation belongs to
     * @param file   the file the annotation belongs to
     * @param editor the editor the annotation is applied to
     * @param start  the starting position of the annotation
     * @param end    the ending position of the annotation
     */
    //TODO only save last X contribution annotations, rotate out older ones
    public void addContributionAnnotation(
        @NotNull
            User user,
        @NotNull
            IFile file, int start, int end,
        @Nullable
            Editor editor) {

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
                RangeHighlighter rangeHighlighter = addRangeHighlighter(user,
                    currentStart, currentEnd, editor,
                    AnnotationType.CONTRIBUTION_ANNOTATION);

                annotationRange = new AnnotationRange(currentStart, currentEnd,
                    rangeHighlighter);

            } else {
                annotationRange = new AnnotationRange(currentStart, currentEnd);
            }

            annotationRanges.add(annotationRange);
        }

        ContributionAnnotation contributionAnnotation = new ContributionAnnotation(
            user, file, editor, annotationRanges);

        contributionAnnotationStore.addAnnotation(contributionAnnotation);
    }

    /**
     * Moves all annotations for the given file back by the length of the
     * addition if they are located behind the added text. Elongates the
     * annotations by the length of the addition if they overlap with the added
     * text.
     * <p>
     * If a RangeHighlighter is present, this will all be done automatically by
     * the internal Intellij logic. This is why
     * {@link AnnotationManager#updateAnnotationStore(IFile)} should
     * <b>always</b> be called before an editor is closed to synchronize the
     * boundaries of the saved annotations with the currently displayed
     * boundaries in the editor annotation model and remove invalid
     * annotations from the annotation store.
     * </p>
     * <p></p>
     * This method should be used to adjust the position of all annotations
     * after text was added to a currently closed file.
     *
     * @param file  the file text was added to
     * @param start the start position of added text
     * @param end   the end position of the added text
     */
    public void moveAnnotationsAfterAddition(
        @NotNull
            IFile file, int start, int end) {

        if (start == end) {
            return;
        }

        checkRange(start, end);

        moveAnnotationsAfterAddition(
            selectionAnnotationStore.getAnnotations(file), start, end);
        moveAnnotationsAfterAddition(
            contributionAnnotationStore.getAnnotations(file), start, end);
    }

    /**
     * <p>
     * If there are not range highlighters or editors present:
     * </p>
     * Moves the given annotations back by the length of the addition if they
     * are located behind the added text. Elongates the annotations by the
     * length of the addition if they overlap with the added text.
     * <p></p>
     * Does nothing if the annotation has a local representation (an editor or
     * range highlighters).
     *
     * @param annotations   the annotations to move
     * @param additionStart the star position of the added text
     * @param additionEnd   the end position of the added text
     * @param <E>           the annotation type
     * @see #moveAnnotationsAfterAddition(IFile, int, int)
     */
    private <E extends AbstractEditorAnnotation> void moveAnnotationsAfterAddition(
        @NotNull
            List<E> annotations, int additionStart, int additionEnd) {

        int offset = additionEnd - additionStart;

        annotations.forEach(annotation -> {
            if (annotation.getEditor() != null) {
                return;
            }

            annotation.getAnnotationRanges().forEach(annotationRange -> {
                int currentStart = annotationRange.getStart();
                int currentEnd = annotationRange.getEnd();

                if (annotationRange.getRangeHighlighter() != null
                    || currentEnd <= additionStart) {

                    return;
                }

                AnnotationRange newAnnotationRange;

                if (currentStart >= additionStart) {
                    newAnnotationRange = new AnnotationRange(
                        currentStart + offset, currentEnd + offset);

                } else {
                    newAnnotationRange = new AnnotationRange(currentStart,
                        currentEnd + offset);
                }

                annotation.replaceAnnotationRange(annotationRange,
                    newAnnotationRange);
            });
        });
    }

    /**
     * Moves all annotations for the given file forward by the length of the
     * removal if they are located behind the removed text. Shortens the
     * annotations if they partially overlap with the removed text. Removes all
     * annotations that were completely contained in the removed text.
     * <p>
     * If a RangeHighlighter is present, this will all be done automatically by
     * the internal Intellij logic. This is why
     * {@link AnnotationManager#updateAnnotationStore(IFile)} should
     * <b>always</b> be called before an editor is closed to synchronize the
     * boundaries of the saved annotations with the currently displayed
     * boundaries in the editor annotation model and remove invalid
     * annotations from the annotation store.
     * </p>
     * <p></p>
     * This method should be used to adjust the position of all annotations
     * after text was added to a currently closed file.
     *
     * @param file  the file text was removed from
     * @param start the start position of removed text
     * @param end   the end position of the removed text
     */
    public void moveAnnotationsAfterDeletion(
        @NotNull
            IFile file, int start, int end) {

        if (start == end) {
            return;
        }

        checkRange(start, end);

        List<SelectionAnnotation> emptySelectionAnnotations = moveAnnotationsAfterDeletion(
            selectionAnnotationStore.getAnnotations(file), start, end);

        emptySelectionAnnotations
            .forEach(selectionAnnotationStore::removeAnnotation);

        List<ContributionAnnotation> emptyContributionAnnotations = moveAnnotationsAfterDeletion(
            contributionAnnotationStore.getAnnotations(file), start, end);

        emptyContributionAnnotations
            .forEach(contributionAnnotationStore::removeAnnotation);
    }

    /**
     * <p>
     * If there are not range highlighters or editors present:
     * </p>
     * Moves all given annotations for the given file forward by the length of
     * the removal if they are located behind the removed text. Shortens the
     * annotations if they partially overlap with the removed text. Returns a
     * list of annotations that were completely contained in the removed text.
     * <p></p>
     * Does nothing if the annotation has a local representation (an editor or
     * range highlighters).
     *
     * @param annotations   the annotations to adjust
     * @param deletionStart the start position of the deleted text
     * @param deletionEnd   the end position of the deleted text
     * @param <E>           the annotation type
     * @return the list of deleted annotations
     * @see #moveAnnotationsAfterDeletion(IFile, int, int)
     */
    @NotNull
    private <E extends AbstractEditorAnnotation> List<E> moveAnnotationsAfterDeletion(
        @NotNull
            List<E> annotations, int deletionStart, int deletionEnd) {

        int offset = deletionEnd - deletionStart;

        List<E> emptyAnnotations = new ArrayList<>();

        annotations.forEach(annotation -> {
            if (annotation.getEditor() != null) {
                return;
            }

            annotation.getAnnotationRanges().forEach(annotationRange -> {
                int currentStart = annotationRange.getStart();
                int currentEnd = annotationRange.getEnd();

                if (annotationRange.getRangeHighlighter() != null
                    || currentEnd <= deletionStart) {

                    return;
                }

                AnnotationRange newAnnotationRange;

                if (currentStart >= deletionEnd) {
                    newAnnotationRange = new AnnotationRange(
                        currentStart - offset, currentEnd - offset);

                } else if (currentStart < deletionStart) {
                    if (currentEnd <= deletionEnd) {
                        newAnnotationRange = new AnnotationRange(currentStart,
                            deletionStart);

                    } else {
                        newAnnotationRange = new AnnotationRange(currentStart,
                            currentEnd - offset);
                    }

                } else {
                    if (currentEnd <= deletionEnd) {
                        annotation.removeAnnotationRange(annotationRange);

                        return;

                    } else {
                        newAnnotationRange = new AnnotationRange(deletionStart,
                            currentEnd - offset);
                    }
                }

                annotation.replaceAnnotationRange(annotationRange,
                    newAnnotationRange);
            });

            if (annotation.getAnnotationRanges().isEmpty()) {
                emptyAnnotations.add(annotation);
            }
        });

        return emptyAnnotations;
    }

    /**
     * Applies all stored annotations for the given file to the given editor and
     * adds the given editor to the annotations.
     * <p>
     * This method does nothing if the given editor is already disposed.
     * </p>
     * <p></p>
     * This method should be used to add existing annotations to a newly opened
     * editor.
     *
     * @param file   the file that was opened in an editor
     * @param editor the new <code>Editor</code> for the annotation
     * @see Editor#isDisposed()
     */
    public void applyStoredAnnotations(
        @NotNull
            IFile file,
        @NotNull
            Editor editor) {

        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Updates all annotations for the given file in all annotation stores by
     * checking if an editor for the annotation is present and then updating the
     * stored annotation range if it has changed. If the annotation is marked as
     * not valid by the editor, it is removed from the annotation store.
     * <p></p>
     * This method must be called <b>before</b> the editor is closed.
     * <p></p>
     * This method should be called before
     * {@link #removeLocalRepresentation(IFile)} to update all annotations.
     *
     * @param file the file to update
     */
    public void updateAnnotationStore(
        @NotNull
            IFile file) {

        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Removes the local representation of the annotation from all
     * stored annotations for the given file.
     * <p></p>
     * This method should be used to remove the local representation of the
     * annotations when the editor for the corresponding file is closed.
     * <p></p>
     * {@link #updateAnnotationStore(IFile)} should <b>always</b> be called
     * before this method to update the local state of the annotations with the
     * ranges of the currently displayed highlighters.
     *
     * @param file the file whose editor was closed
     * @see AbstractEditorAnnotation#removeLocalRepresentation()
     */
    public void removeLocalRepresentation(
        @NotNull
            IFile file) {

        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Removes all annotations belonging to the given user from all annotation
     * stores and from all open editors.
     * <p></p>
     * This method should be used to remove all annotations belonging to a user
     * that left the session.
     *
     * @param user the user whose annotations to remove
     */
    public void removeAnnotations(
        @NotNull
            User user) {

        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Removes all annotations from all open editors and removes all the stored
     * annotations from all annotation stores.
     */
    public void removeAllAnnotations() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Checks whether the given start and end point form a valid range.
     * <p>
     * The following conditions must hold true:
     * </p>
     * <ul>
     * <li>start >= 0</li>
     * <li>end >= 0</li>
     * <li>start <= end</li>
     * </ul>
     * Throws an <code>IllegalArgumentException</code> otherwise.
     *
     * @param start the start position
     * @param end   the end position
     */
    private void checkRange(int start, int end) {
        if (start < 0 || end < 0) {
            throw new IllegalArgumentException(
                "The start and end of the annotation must not be negative "
                    + "values. start: " + start + ", end: " + end);
        }

        if (start > end) {
            throw new IllegalArgumentException(
                "The start of the annotation must not be after the end of the "
                    + "annotation. start: " + start + ", end: " + end);

        }
    }

    /**
     * Creates a RangeHighlighter with the given position for the given
     * editor.
     * <p>
     * The color of the highlighter is determined by the given user and
     * annotation type. Valid types are defined in the enum
     * <code>AnnotationType</code>.
     * </p>
     * The returned <code>RangeHighlighter</code> can not be modified through
     * the API but is automatically updated by Intellij if there are changes to
     * the editor.
     *
     * @param user           the user whose color to use
     * @param start          the start of the highlighted area
     * @param end            the end of the highlighted area
     * @param editor         the editor to create the highlighter for
     * @param annotationType the type of annotation
     * @return a RangeHighlighter with the given parameters.
     */
    @NotNull
    private RangeHighlighter addRangeHighlighter(
        @NotNull
            User user, int start, int end,
        @NotNull
            Editor editor,
        @NotNull
            AnnotationType annotationType) {

        Color color;
        switch (annotationType) {

        case SELECTION_ANNOTATION:
            color = ColorManager.getColorModel(user.getColorID())
                .getSelectColor();
            break;

        case CONTRIBUTION_ANNOTATION:
            color = ColorManager.getColorModel(user.getColorID())
                .getEditColor();
            break;

        default:
            throw new IllegalArgumentException(
                "Unknown annotation type: " + annotationType);
        }

        TextAttributes textAttr = new TextAttributes();
        textAttr.setBackgroundColor(color);

        AtomicReference<RangeHighlighter> result = new AtomicReference<>();

        application.invokeAndWait(() -> result.set(editor.getMarkupModel()
                .addRangeHighlighter(start, end, HighlighterLayer.LAST, textAttr,
                    HighlighterTargetArea.EXACT_RANGE)),
            ModalityState.defaultModalityState());

        return result.get();
    }

    /**
     * Removes all existing RangeHighlighters for the given annotation from
     * the editor of the annotation. This does <b>not</b> affect the stored
     * values in the given annotation, meaning the objects for the
     * RangeHighlighters will still remain stored in the annotation.
     *
     * @param annotation the annotation whose highlighters to remove
     */
    private void removeRangeHighlighter(
        @NotNull
            AbstractEditorAnnotation annotation) {

        Editor editor = annotation.getEditor();

        if (editor == null) {
            return;
        }

        List<AnnotationRange> annotationRanges = annotation
            .getAnnotationRanges();

        annotationRanges.forEach(annotationRange -> {

            RangeHighlighter rangeHighlighter = annotationRange
                .getRangeHighlighter();

            if (rangeHighlighter == null || !rangeHighlighter.isValid()) {
                return;
            }

            application.invokeAndWait(() -> editor.getMarkupModel()
                    .removeHighlighter(rangeHighlighter),
                ModalityState.defaultModalityState());
        });

    }
}

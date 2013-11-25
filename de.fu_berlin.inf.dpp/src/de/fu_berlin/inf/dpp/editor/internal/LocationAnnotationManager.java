package de.fu_berlin.inf.dpp.editor.internal;

import org.apache.log4j.Logger;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.SelectionAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.ViewportAnnotation;
import de.fu_berlin.inf.dpp.util.Predicate;

/**
 * This class is responsible for managing annotations related with other users'
 * locations, i.e. their cursors, selections, and viewports.
 */
public class LocationAnnotationManager {

    private static final Logger LOG = Logger
        .getLogger(LocationAnnotationManager.class);

    private AnnotationModelHelper annotationModelHelper;

    public LocationAnnotationManager() {
        annotationModelHelper = new AnnotationModelHelper();
    }

    /**
     * Create or update the annotations that represent a user's viewport
     * (visible lines of code).
     * 
     * @param user
     *            The remote user whose viewport should be visualized.
     * @param editorPart
     *            The {@link IEditorPart} which shows the file that the remote
     *            user has opened, too.
     * @param lineRange
     *            The {@link ILineRange} that is visible to the remote user.
     */
    public void setViewportForUser(final User user, IEditorPart editorPart,
        ILineRange lineRange) {

        ITextViewer viewer = EditorAPI.getViewer(editorPart);
        if (!(viewer instanceof ISourceViewer)) {
            return;
        }

        IDocument document = viewer.getDocument();
        IAnnotationModel model = ((ISourceViewer) viewer).getAnnotationModel();

        if (model == null) {
            return;
        }

        int top = lineRange.getStartLine();
        int bottom = top + lineRange.getNumberOfLines();

        // Clean-up. Remove any existing ViewportAnnotations of the given user
        annotationModelHelper.removeAnnotationsFromModel(model,
            new Predicate<Annotation>() {
                @Override
                public boolean evaluate(Annotation annotation) {
                    return (annotation instanceof ViewportAnnotation)
                        && ((SarosAnnotation) annotation).getSource().equals(
                            user);
                }
            });

        // Calculate the position of the new ViewportAnnotation and then add it
        // to the model
        try {
            int lines = document.getNumberOfLines();
            top = Math.max(0, Math.min(lines - 1, top));
            bottom = Math.max(0, Math.min(lines - 1, bottom));

            int start = document.getLineOffset(top);
            int end = document.getLineOffset(bottom);

            if (start == -1)
                throw new BadLocationException("Start line -1");

            if (end == -1 || end < start)
                throw new BadLocationException("End line -1 or less than start");

            ViewportAnnotation va = new ViewportAnnotation(user);

            if (lines > 1)
                va.setMoreThanOneLine(true);

            // TODO Is there a reason *not* to use "va"?
            SarosAnnotation annotation = va;
            Position position = new Position(start, end - start);
            model.addAnnotation(annotation, position);
        } catch (BadLocationException e) {
            LOG.warn("Internal Error:", e);
        }
    }

    /**
     * Remove any existing viewport-related annotations of a user that might
     * exist inside the given {@link IEditorPart}.
     * 
     * @param user
     * @param editorPart
     */
    public void clearViewportForUser(final User user, IEditorPart editorPart) {
        IAnnotationModel model = annotationModelHelper
            .retrieveAnnotationModel(editorPart);

        if (model == null) {
            return;
        }

        annotationModelHelper.removeAnnotationsFromModel(model,
            new Predicate<Annotation>() {
                @Override
                public boolean evaluate(Annotation annotation) {
                    return (annotation instanceof ViewportAnnotation)
                        && ((SarosAnnotation) annotation).getSource().equals(
                            user);
                }
            });
    }

    /**
     * Create or update annotations related to text selections made by remote
     * users.
     * 
     * In case there is no actual selection (just a blinking cursor) the
     * annotation will be one character wide.
     * 
     * @param source
     *            The remote user who made the text selection (or to whom the
     *            text cursor belongs).
     * @param selection
     *            The selection itself.
     * @param editorPart
     *            {@link IEditorPart} that displays the opened document of which
     *            the annotations should be updated.
     */
    public void setSelection(IEditorPart editorPart, ITextSelection selection,
        User source) {

        if (!(editorPart instanceof ITextEditor))
            return;

        ITextEditor textEditor = (ITextEditor) editorPart;
        IDocumentProvider docProvider = textEditor.getDocumentProvider();

        if (docProvider == null)
            return;

        IEditorInput input = textEditor.getEditorInput();
        IAnnotationModel model = docProvider.getAnnotationModel(input);

        if (model == null)
            return;

        if (selection.isEmpty()) {
            clearSelectionForUser(source, editorPart);
            return;
        }

        int offset = selection.getOffset();
        int length = selection.getLength();
        boolean isCursor = length == 0;

        if (isCursor) {
            if (offset >= 1) {
                /*
                 * Highlight the character before the cursor in the light color
                 * of the user. Does nothing if offset is the beginning of an
                 * line
                 */
                setSelectionAnnotation(source, isCursor, new Position(
                    offset - 1, 1), model);
            } else {
                /*
                 * We have to draw this highlighting of the character before the
                 * cursor even if it isn't visible at all to prevent ghosting of
                 * the highlight when jumping to the beginning of the file
                 */
                setSelectionAnnotation(source, isCursor, new Position(0, 0),
                    model);
            }
        } else {
            /*
             * Highlight the last character in a selection of a remote user in
             * the remote user's light color.
             */
            setSelectionAnnotation(source, isCursor, new Position(offset,
                length), model);
        }
    }

    /**
     * Removes all selection-related annotations of a user inside the given
     * {@link IEditorPart}.
     * 
     * @param user
     *            The originator of the annotations to be deleted.
     * @param editorPart
     */
    private void clearSelectionForUser(final User user, IEditorPart editorPart) {
        IAnnotationModel model = annotationModelHelper
            .retrieveAnnotationModel(editorPart);

        if (model == null) {
            return;
        }

        annotationModelHelper.removeAnnotationsFromModel(model,
            new Predicate<Annotation>() {
                @Override
                public boolean evaluate(Annotation annotation) {
                    return (annotation instanceof SelectionAnnotation)
                        && ((SarosAnnotation) annotation).getSource().equals(
                            user);
                }
            });
    }

    /**
     * Helper function to create and add an annotation that highlights the
     * selected text (see {@link SelectionAnnotation}).
     */
    private void setSelectionAnnotation(User user, boolean isCursor,
        Position position, IAnnotationModel annotationModel) {

        setAnnotationForSelection(new SelectionAnnotation(user, isCursor),
            position, annotationModel);
    }

    /**
     * Sets annotations related to selections made by remote users.
     * 
     * @param newAnnotation
     *            {@link SarosAnnotation} that is set during this call.
     * @param position
     *            {@link Position} at which the annotation is replaced, removed,
     *            or updated.
     * @param model
     *            {@link IAnnotationModel} that maintains the annotations for
     *            the opened document.
     */
    private void setAnnotationForSelection(SarosAnnotation newAnnotation,
        Position position, IAnnotationModel model) {

        if (newAnnotation == null || position == null) {
            throw new IllegalArgumentException(
                "Both newAnnotation and position must not be null");
        }

        final User user = newAnnotation.getSource();

        // Clean up.
        annotationModelHelper.removeAnnotationsFromModel(model,
            new Predicate<Annotation>() {
                @Override
                public boolean evaluate(Annotation annotation) {
                    return (annotation instanceof SelectionAnnotation)
                        && ((SarosAnnotation) annotation).getSource().equals(
                            user);
                }
            });

        // Add new annotation.
        model.addAnnotation(newAnnotation, position);
    }

}

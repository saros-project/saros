package de.fu_berlin.inf.dpp.intellij.editor.annotations;

import com.intellij.openapi.editor.Editor;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.session.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Annotation manager used to create, delete and manage annotations for a Saros
 * session.
 */
//TODO save local selection before editor is closed
//TODO move saved local selections affected by changes while editor is closed
//TODO adjust position of local selection when editor is re-opened
public class AnnotationManager {

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

        throw new UnsupportedOperationException("Not yet implemented.");
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

        throw new UnsupportedOperationException("Not yet implemented.");
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

        throw new UnsupportedOperationException("Not yet implemented.");
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

        throw new UnsupportedOperationException("Not yet implemented.");
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
}

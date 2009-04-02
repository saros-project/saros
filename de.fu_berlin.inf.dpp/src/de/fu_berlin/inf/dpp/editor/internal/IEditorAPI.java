package de.fu_berlin.inf.dpp.editor.internal;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.ui.IEditorPart;

import de.fu_berlin.inf.dpp.editor.EditorManager;

/**
 * An humble interface that is responsible for editor functionality. The idea
 * behind this interface is to only capsulates the least possible amount of
 * functionality - the one that can't be easily tested. All higher logic can be
 * found in {@link EditorManager}.
 * 
 * @author rdjemili
 */
public interface IEditorAPI {
    /**
     * Sets the editor manager that uses this IEditorAPI. The given editor
     * manager will receive the callbacks.
     */
    public void setEditorManager(EditorManager editorManager);

    /**
     * Opens the editor with given file. Needs to be called from an UI thread.
     * 
     * @return the opened editor or <code>null</code> if the editor couldn't be
     *         opened.
     */
    public IEditorPart openEditor(IFile file);

    /**
     * Closes the given editorpart.
     * 
     * Needs to be called from an UI thread.
     */
    public void closeEditor(IEditorPart part);

    /**
     * @return the editor that is currently activated.
     */
    public IEditorPart getActiveEditor();

    /**
     * @return all editors that are currently opened.
     */
    public Set<IEditorPart> getOpenEditors();

    /**
     * Sets the text selection in given editor.
     */
    public void setSelection(IEditorPart editorPart, ITextSelection selection,
        String source, boolean follow);

    /**
     * Returns the current text selection for given editor.
     * 
     * @param editorPart
     *            the editorPart for which to get the text selection.
     * @return the current text selection. Returns
     *         {@link TextSelection#emptySelection()} if no text selection
     *         exists.
     * 
     */
    public ITextSelection getSelection(IEditorPart editorPart);

    /**
     * @return the path of the file the given editor is displaying or null if
     *         the given editor is not showing a file or the file is not
     *         referenced via a path in the project.
     */
    public IPath getEditorPath(IEditorPart editorPart);

    /**
     * Show an viewport annotation in the given editorPart at the given viewport
     * for the given user.
     */
    public void setViewportAnnotation(IEditorPart editorPart, ILineRange viewport,
        String user);

    /**
     * Try to make sure the given viewport is visible in the given editor
     */
    public void reveal(IEditorPart editorPart, ILineRange viewport);

    /**
     * @return Return the viewport for given editor or null, if this editorPart
     *         does not have ITextViewer associated.
     */
    public LineRange getViewport(IEditorPart editorPart);

    /**
     * Enables/disables the ability to edit the document in given editor.
     */
    public void setEditable(IEditorPart editorPart, boolean editable);

    /**
     * Attaches listeners to the given editor that will fire the
     * {@link IEditorListener} methods on the editor manager set with
     * {@link #setEditorManager(EditorManager)}.
     * 
     * Connecting to an editorPart multiple times, will result in multiple
     * events. The caller is responsible for organizing connections.
     * 
     * @swt Needs to be called from the SWT-UI thread.
     */
    public void addSharedEditorListener(IEditorPart editorPart);

    /**
     * Syntactic sugar for getting the path of the IEditorPart returned by
     * getActiveEditor()
     */
    public IPath getActiveEditorPath();

    /**
     * Returns the file currently displayed in the given editorPart, this maybe
     * null if the given editor is not operating on a file or has several files
     * (TeamEditor for instance).
     */
    public IResource getEditorResource(IEditorPart editorPart);
}

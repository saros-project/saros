package de.fu_berlin.inf.dpp.editor.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.text.LineRange;

/**
 * A humble interface that is responsible for editor functionality. The idea
 * behind this interface is to only capsulates the least possible amount of
 * functionality - the one that can't be easily tested. All higher logic can be
 * found in {@link EditorManager}.
 * 
 * @author rdjemili
 */
public interface IEditorAPI {

    /**
     * Opens the editor with given path. Needs to be called from an UI thread.
     * 
     * @return the opened editor or <code>null</code> if the editor couldn't be
     *         opened.
     */
    public IEditorPart openEditor(SPath path);

    /**
     * Opens the editor with given path. Needs to be called from an UI thread.
     * 
     * @param activate
     *            <code>true</code>, if editor should get focus, otherwise
     *            <code>false</code>
     * @return the opened editor or <code>null</code> if the editor couldn't be
     *         opened.
     */
    public IEditorPart openEditor(SPath path, boolean activate);

    /**
     * Opens the given editor part.
     * 
     * Needs to be called from an UI thread.
     * 
     * @return <code>true</code> if the editor part was successfully opened,
     *         <code>false</code> otherwise
     */
    public boolean openEditor(IEditorPart part);

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
    public SPath getEditorPath(IEditorPart editorPart);

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
     * Syntactic sugar for getting the path of the IEditorPart returned by
     * getActiveEditor()
     */
    public SPath getActiveEditorPath();

    /**
     * Returns the resource currently displayed in the given editorPart.
     * 
     * @return Can be <code>null</code>, e.g. if the given editorPart is not
     *         operating on a resource, or has several resources.
     */
    public IResource getEditorResource(IEditorPart editorPart);

    /**
     * Returns the {@link IDocumentProvider} of the given {@link IEditorInput}.
     * This method analyzes the file extension of the {@link IFile} associated
     * with the given {@link IEditorInput}. Depending on the file extension it
     * returns file-types responsible {@link IDocumentProvider}.
     * 
     * @param input
     *            the {@link IEditorInput} for which {@link IDocumentProvider}
     *            is needed
     * 
     * @return IDocumentProvider of the given input
     */
    public IDocumentProvider getDocumentProvider(IEditorInput input);

    /**
     * Returns the document for the given editor part.
     * 
     * @param editorPart
     *            editor part to retrieve the document
     * @return the document for the given editor part
     */
    public IDocument getDocument(IEditorPart editorPart);

}

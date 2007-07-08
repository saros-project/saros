package de.fu_berlin.inf.dpp.editor.internal;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

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
	 * @return the opened editor or <code>null</code> if the editor couldn't
	 *         be opened.
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
	public void setSelection(IEditorPart editorPart, ITextSelection selection, String source);

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
	 * @return the file path that given editor is displaying.
	 */
	public IResource getEditorResource(IEditorPart editorPart);

	public void setViewport(IEditorPart editorPart, boolean jumpTo, int top, int bottom, String source);

	/**
	 * Return the viewport for given editor.
	 * 
	 * @param editorPart
	 *            the editor for which to get the viewport
	 * @return the viewport. Never <code>null</code>.
	 */
	public ILineRange getViewport(IEditorPart editorPart);

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
	 * Needs to be called from a UI thread.
	 */
	public void addSharedEditorListener(IEditorPart editorPart);

	public IDocument getDocument(IEditorPart editorPart);

	public IDocumentProvider getDocumentProvider(IEditorInput editorInput);
}

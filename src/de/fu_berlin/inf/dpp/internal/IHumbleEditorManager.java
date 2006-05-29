
package de.fu_berlin.inf.dpp.internal;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;

import de.fu_berlin.inf.dpp.EditorManager;
import de.fu_berlin.inf.dpp.listeners.ISharedEditorListener;

public interface IHumbleEditorManager {
    /**
     * Sets the editor manager that uses this IHumbleEditorManager. The given
     * editor manager will receive the callbacks.
     */
    public void setEditorManager(EditorManager editorManager);

    /**
     * Opens the editor with given file.
     */
    public void openEditor(IFile file);

    /**
     * Sets/Replaces text in given file.
     */
    public void setText(IFile file, int offset, int replace, String text);

    /**
     * Sets the text selection in given editor.
     */
    public void setSelection(IEditorPart editorPart, ITextSelection selection);

    /**
     * @return the editor that is currently activated.
     */
    public IEditorPart getActiveEditor();

    /**
     * @return all editors that are currently opened.
     */
    public List<IEditorPart> getOpenedEditors();

    /**
     * @return the file path that given editor is displaying.
     */
    public IPath getEditorPath(IEditorPart editorPart);

    /**
     * Enables/disables the ability to edit the document in given editor.
     */
    public void setEditable(IEditorPart editorPart, boolean editable);

    /**
     * Attaches listeners to the given editor that will fire the
     * {@link ISharedEditorListener} methods on the editor manager set with
     * {@link #setEditorManager(EditorManager)}.
     */
    public void connect(IEditorPart editorPart);
}

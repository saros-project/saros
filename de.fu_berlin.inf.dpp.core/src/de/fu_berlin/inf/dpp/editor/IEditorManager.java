package de.fu_berlin.inf.dpp.editor;

import java.util.Set;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;

/**
 * This interface gives access to managed editors during a running session. An
 * editor is in the state of being <tt>managed</tt> when it is part of the
 * current sharing settings, i.e modifying its contents will be recognized and
 * broadcasted to other session users.
 */
public interface IEditorManager {

    /**
     * Returns the paths of all shared files for which an editor is currently
     * open locally.
     * 
     * @return paths of locally open shared files
     */
    public Set<SPath> getLocallyOpenEditors();

    /**
     * Returns the paths of all shared files for which an editor is currently
     * open at a remote site.
     * 
     * @return paths of remotely open shared files
     */
    public Set<SPath> getRemotelyOpenEditors();

    /**
     * Returns the text content of the local editor associated with the
     * specified file, or the content of the file itself if there is currently
     * no open editor for it.
     * <p>
     * This method must be called on the UI thread.
     * </p>
     * 
     * @param path
     *            path of the file whose content should be returned
     * @return the text content of the matching local editor or file, or
     *         <code>null</code> if no file with the given path exists locally
     */
    public String getContent(SPath path);

    /**
     * Saves all currently managed editors that belong to the given project. If
     * the given project is <code>null</code> all currently managed editors will
     * be saved.
     * 
     * @param project
     *            the project which editors should be saved or <code>null</code>
     */
    public void saveEditors(IProject project);

    /**
     * Adds an {@link ISharedEditorListener} to listen for changes such as
     * editors getting opened, closed or their content changed.
     * 
     * @param listener
     *            editor listener to add
     */
    public void addSharedEditorListener(ISharedEditorListener listener);

    /**
     * Removes an {@link ISharedEditorListener} that was previously added with
     * {@link #addSharedEditorListener(ISharedEditorListener)}.
     * 
     * @param listener
     *            editor listener to remove
     */
    public void removeSharedEditorListener(ISharedEditorListener listener);
}

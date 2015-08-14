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
     * Saves all currently managed editors that belong to the given project. If
     * the given project is <code>null</code> all currently managed editors will
     * be saved.
     * 
     * @param project
     *            the project which editors should be saved or <code>null</code>
     */
    public void saveEditors(IProject project);
}

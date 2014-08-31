package de.fu_berlin.inf.dpp.editor;

import de.fu_berlin.inf.dpp.filesystem.IProject;

/**
 * This interface gives access to managed editors during a running session. An
 * editor is in the state of being <tt>managed</tt> when it is part of the
 * current sharing settings, i.e modifying its contents will be recognized and
 * broadcasted to other session users.
 */
public interface IEditorManager {

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

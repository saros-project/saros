package de.fu_berlin.inf.dpp.editor;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.User;

/**
 * Empty abstract implementation of the ISharedEditorListener interface
 */
public abstract class AbstractSharedEditorListener implements
    ISharedEditorListener {

    public void activeEditorChanged(User user, IPath path) {
        // does nothing
    }

    public void editorRemoved(User user, IPath path) {
        // does nothing
    }

    public void driverEditorSaved(IPath path, boolean replicated) {
        // does nothing
    }

    public void followModeChanged(User user) {
        // does nothing
    }
}

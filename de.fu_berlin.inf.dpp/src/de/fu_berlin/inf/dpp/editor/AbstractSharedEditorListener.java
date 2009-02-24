package de.fu_berlin.inf.dpp.editor;

import org.eclipse.core.runtime.IPath;

/**
 * Empty abstract implementation of the SharedEditorListener interface
 */
public abstract class AbstractSharedEditorListener implements
    ISharedEditorListener {

    public void activeDriverEditorChanged(IPath path, boolean replicated) {
        // does nothing
    }

    public void driverEditorRemoved(IPath path, boolean replicated) {
        // does nothing
    }

    public void driverEditorSaved(IPath path, boolean replicated) {
        // does nothing
    }

    public void followModeChanged(boolean enabled) {
        // does nothing
    }
}

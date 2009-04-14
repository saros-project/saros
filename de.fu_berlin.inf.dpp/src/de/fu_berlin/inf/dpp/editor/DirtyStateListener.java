package de.fu_berlin.inf.dpp.editor;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IElementStateListener;

/**
 * Listener registered on Editors to be informed about their dirty state.
 */
public class DirtyStateListener implements IElementStateListener {

    private final EditorManager editorManager;

    DirtyStateListener(EditorManager editorManager) {
        this.editorManager = editorManager;
    }

    public boolean enabled = true;

    public void elementDirtyStateChanged(Object element, boolean isDirty) {

        if (!enabled)
            return;

        if (isDirty)
            return;

        if (!this.editorManager.isDriver)
            return;

        if (!(element instanceof FileEditorInput))
            return;

        IFile file = ((FileEditorInput) element).getFile();

        if (!ObjectUtils.equals(file.getProject(), editorManager.sharedProject
            .getProject())) {
            return;
        }

        // Only trigger save events for files managed in the editor pool
        if (!editorManager.isConnected(file)) {
            return;
        }

        EditorManager.log.debug("Dirty state reset for: " + file.toString());

        IPath path = file.getProjectRelativePath();
        this.editorManager.sendEditorActivitySaved(path);
    }

    public void elementContentAboutToBeReplaced(Object element) {
        // ignore
    }

    public void elementContentReplaced(Object element) {
        // ignore
    }

    public void elementDeleted(Object element) {
        // ignore
    }

    public void elementMoved(Object originalElement, Object movedElement) {
        // ignore
    }
}
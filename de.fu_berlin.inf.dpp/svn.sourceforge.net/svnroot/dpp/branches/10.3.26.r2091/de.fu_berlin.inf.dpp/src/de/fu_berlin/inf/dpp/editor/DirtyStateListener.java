package de.fu_berlin.inf.dpp.editor;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IElementStateListener;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Listener registered on Editors to be informed about their dirty state.
 * 
 * There is one global DirtyStateListener for all editors!
 */
public class DirtyStateListener implements IElementStateListener {

    private static final Logger log = Logger
        .getLogger(DirtyStateListener.class);

    protected final EditorManager editorManager;

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

        final IFile file = ((FileEditorInput) element).getFile();
        final ISharedProject sharedProject = editorManager.sharedProject;

        if (sharedProject == null || !sharedProject.isShared(file.getProject())) {
            return;
        }

        Util.runSafeSWTSync(log, new Runnable() {

            public void run() {

                // Only trigger save events for files managed in the editor pool
                if (!editorManager.isConnected(file)) {
                    return;
                }

                EditorManager.log.debug("Dirty state reset for: "
                    + file.toString());
                editorManager.sendEditorActivitySaved(new SPath(file));
            }
        });
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
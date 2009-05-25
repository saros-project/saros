/**
 * 
 */
package de.fu_berlin.inf.dpp.editor.internal;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import de.fu_berlin.inf.dpp.editor.EditorManager;

public class EditorPartListener implements IPartListener2 {

    protected EditorManager editorManager;

    public EditorPartListener(EditorManager editorManager) {
        this.editorManager = editorManager;
    }

    public void partActivated(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);

        if ((part != null) && (part instanceof IEditorPart)) {
            IEditorPart editor = (IEditorPart) part;
            editorManager.partActivated(editor);
        }
    }

    public void partOpened(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);

        if ((part != null) && (part instanceof IEditorPart)) {
            IEditorPart editor = (IEditorPart) part;
            editorManager.partOpened(editor);
        }
    }

    public void partClosed(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);

        if ((part != null) && (part instanceof IEditorPart)) {
            IEditorPart editor = (IEditorPart) part;
            editorManager.partClosed(editor);
        }
    }

    /**
     * We need to catch partBroughtToTop events because partActivate events are
     * missing if Editors are opened programmatically.
     */
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);

        if ((part != null) && (part instanceof IEditorPart)) {
            IEditorPart editor = (IEditorPart) part;
            editorManager.partActivated(editor);
        }
    }

    public void partDeactivated(IWorkbenchPartReference partRef) {
        // do nothing
    }

    public void partHidden(IWorkbenchPartReference partRef) {
        // do nothing
    }

    public void partVisible(IWorkbenchPartReference partRef) {
        // do nothing
    }

    /**
     * Called for instance when a file was renamed. We just close and open the
     * editor.
     */
    public void partInputChanged(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);

        if ((part != null) && (part instanceof IEditorPart)) {
            IEditorPart editor = (IEditorPart) part;
            editorManager.partInputChanged(editor);
        }
    }
}
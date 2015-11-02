package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import de.fu_berlin.inf.dpp.activities.SPath;

/**
 * IntelliJ editor selection listener
 */
public class StoppableSelectionListener extends AbstractStoppableListener
    implements SelectionListener {

    public StoppableSelectionListener(EditorManager manager) {
        super(manager);
    }

    /**
     * Calls {@link EditorManager#generateSelection(SPath, SelectionEvent)}.
     *
     * @param event
     */
    @Override
    public void selectionChanged(SelectionEvent event) {
        if (!enabled) {
            return;
        }

        SPath path = editorManager.getEditorPool()
            .getFile(event.getEditor().getDocument());
        if (path != null) {
            editorManager.generateSelection(path, event);
        }
    }
}

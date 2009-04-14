package de.fu_berlin.inf.dpp.editor;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

/**
 * A DocumentListener which informs the given EditorManager of changes before
 * they occur in a document (using documentAboutToBeChanged).
 * 
 * This implementation can be made to stop sending events using the enabled
 * field.
 */
public class StoppableDocumentListener implements IDocumentListener {

    private final EditorManager editorManager;

    StoppableDocumentListener(EditorManager editorManager) {
        this.editorManager = editorManager;
    }

    boolean enabled = true;

    public void documentAboutToBeChanged(final DocumentEvent event) {

        if (enabled) {
            String text = event.getText() == null ? "" : event.getText();
            this.editorManager.textAboutToBeChanged(event.getOffset(), text,
                event.getLength(), event.getDocument());
        }
    }

    public void documentChanged(final DocumentEvent event) {
        // do nothing. We handeled everything in documentAboutToBeChanged
    }
}
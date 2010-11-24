package de.fu_berlin.inf.dpp.editor;

import org.apache.log4j.Logger;
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

    protected static Logger log = Logger
        .getLogger(StoppableDocumentListener.class.getName());

    private final EditorManager editorManager;

    StoppableDocumentListener(EditorManager editorManager) {
        this.editorManager = editorManager;
    }

    /**
     * While paused the StoppableDocumentManager doesn't call the EditorManager
     */
    private boolean pause = false;

    boolean enabled = true;

    public void documentAboutToBeChanged(final DocumentEvent event) {

        if (enabled) {
            if (pause) {
                log.warn("document changes while listener paused");
                return;
            }
            String text = event.getText() == null ? "" : event.getText();
            this.editorManager.textAboutToBeChanged(event.getOffset(), text,
                event.getLength(), event.getDocument());
        }
    }

    public void documentChanged(final DocumentEvent event) {
        // do nothing. We handled everything in documentAboutToBeChanged
    }

    public void setPause(boolean stop) {
        this.pause = stop;
    }
}
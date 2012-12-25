package de.fu_berlin.inf.dpp.editor;

import org.apache.log4j.Logger;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

/**
 * A DocumentListener which informs the given EditorManager of changes before
 * they occur in a document (using documentAboutToBeChanged).
 * 
 * The StoppableDocumentListener (actually TextChangeListener) notifies the
 * EditorManager about local text changes in a shared document. The
 * EditorManager then transmits the changes to all listening session
 * participants. The EditorManager temporariliy disables this listener when it
 * locally applies remote changes to a file to avoid an endless cycle of
 * notification (because the change would also trigger a text change event).
 * 
 * This implementation can be made to stop sending events using the enabled
 * field.
 * 
 * @author awaldmann, anarw and nwarnatsch
 */
public class StoppableDocumentListener implements IDocumentListener {

    private final static Logger log = Logger
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

    @Override
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

    @Override
    public void documentChanged(final DocumentEvent event) {
        // do nothing. We handled everything in documentAboutToBeChanged
    }

    public void setPause(boolean stop) {
        this.pause = stop;
    }
}
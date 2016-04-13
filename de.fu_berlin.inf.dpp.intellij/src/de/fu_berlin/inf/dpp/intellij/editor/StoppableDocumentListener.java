package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import de.fu_berlin.inf.dpp.activities.SPath;
import org.apache.log4j.Logger;

/**
 * Tracks modifications of documents opened in editors.
 * <p/>
 * Notify its EditorManager of modifications in an opened document, before
 * the modification has actually happened.
 *
 * @see DocumentListener#beforeDocumentChange(DocumentEvent)
 */
public class StoppableDocumentListener extends AbstractStoppableListener
    implements DocumentListener {

    private static final Logger LOG = Logger
        .getLogger(StoppableDocumentListener.class);

    public StoppableDocumentListener(EditorManager editorManager) {
        super(editorManager);
        super.setEnabled(false);
    }

    /**
     * Calls
     * {@link EditorManager#generateTextEdit(int, String, String, SPath)}
     *
     * @param event
     */
    @Override
    public void beforeDocumentChange(DocumentEvent event) {
        // We rely on the editor pool to filter files that are not shared.
        SPath path = editorManager.getEditorPool().getFile(event.getDocument());
        if (path == null) {
            LOG.debug("Event for document " + event.getDocument()
                    + " ignored: document is not known to the editor pool");
            return;
        }

        String newText = event.getNewFragment().toString();
        String replacedText = event.getOldFragment().toString();

        editorManager
            .generateTextEdit(event.getOffset(), newText, replacedText, path);
    }

    /**
     * Does nothing.
     *
     * @param event
     */
    @Override
    public void documentChanged(DocumentEvent event) {
        // do nothing. We handled everything in documentAboutToBeChanged
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            startListening();
        } else {
            stopListening();
        }
    }

    /**
     * Stop listening for document modifications.
     */
    public void stopListening() {
        if (enabled) {
            EditorFactory.getInstance().getEventMulticaster()
                .removeDocumentListener(this);
            LOG.debug("Stopped listening for document events");
        }

        super.setEnabled(false);
    }

    /**
     * Start listening for document modifications.
     */
    public void startListening() {
        if (!enabled) {
            EditorFactory.getInstance().getEventMulticaster()
                .addDocumentListener(this);
            LOG.debug("Started listening for document events");
        }

        super.setEnabled(true);
    }
}

package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import de.fu_berlin.inf.dpp.activities.SPath;
import org.apache.log4j.Logger;

/**
 * A document listener which informs the given EditorManager of changes before
 * they occur in a document (using documentAboutToBeChanged). The DocumentListener
 * is only added to the document that is currently being edited.
 * <p/>
 */
public class StoppableDocumentListener extends AbstractStoppableListener
    implements DocumentListener {

    private Document document;

    private static final Logger LOG = Logger
        .getLogger(StoppableDocumentListener.class);

    public StoppableDocumentListener(EditorManager editorManager) {
        super(editorManager);
    }

    /**
     * Calls
     * {@link EditorManager#generateTextEdit(int, String, String, SPath)}
     *
     * @param event
     */
    @Override
    public void beforeDocumentChange(DocumentEvent event) {
        if (!enabled) {
            return;
        }
        FileDocumentManager.getInstance().getFile(event.getDocument());
        SPath path = editorManager.getEditorPool().getFile(event.getDocument());
        if (path == null) {
            LOG.warn("Could not find path for editor " + event.getDocument());
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

    public Document getDocument() {
        return document;
    }

    /**
     * Removes this listener from the document.
     */
    public void stopListening() {
        if (document != null) {
            document.removeDocumentListener(this);
            document = null;
        }
    }

    /**
     * Removes itself from previously listened documents and registers itself with the new document.
     *
     * @param newDocument
     */
    public void startListening(Document newDocument) {
        if (document == null) {
            document = newDocument;
            document.addDocumentListener(this);
            //TODO: replace by equals?
        } else if (document != newDocument) {
            document.removeDocumentListener(this);
            document = newDocument;
            document.addDocumentListener(this);
        } else {
            //do nothing, as we already listen to that document
        }
    }

}
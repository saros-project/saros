package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.SPath;
import org.apache.log4j.Logger;

/**
 * Tracks modifications of Documents and triggers matching TextEditActivities.
 * These activities are created before the modification are actually applied to
 * the document.
 *
 * @see DocumentListener#beforeDocumentChange(DocumentEvent)
 */
public class StoppableDocumentListener extends AbstractStoppableListener
    implements DocumentListener {

    private static final Logger LOG = Logger
        .getLogger(StoppableDocumentListener.class);

    private final VirtualFileConverter virtualFileConverter;

    public StoppableDocumentListener(EditorManager editorManager,
        VirtualFileConverter virtualFileConverter) {

        super(editorManager);
        super.setEnabled(false);

        this.virtualFileConverter = virtualFileConverter;
    }

    /**
     * Generates and dispatches a <code>TextEditActivity</code> for the given
     * <code>DocumentEvent</code>.
     *
     * @param event the <code>DocumentEvent</code> to react to
     */
    @Override
    public void beforeDocumentChange(DocumentEvent event) {
        Document document = event.getDocument();

        SPath path = editorManager.getEditorPool().getFile(document);

        if (path == null) {
            VirtualFile virtualFile = FileDocumentManager.getInstance()
                .getFile(document);

            if (virtualFile == null) {
                LOG.trace("Ignoring event for document " + document
                    + " - document is not known to the editor pool and a "
                    + "VirtualFile for the document could not be found");

                return;
            }

            path = virtualFileConverter.convertToPath(virtualFile);

            if (path == null) {
                LOG.trace("Ignoring Event for document " + document
                        + " - document is not shared");

                return;
            }
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
        if (!this.enabled && enabled) {
            LOG.debug("Started listening for document events");

            EditorFactory.getInstance().getEventMulticaster()
                    .addDocumentListener(this);

            this.enabled = true;

        } else if (this.enabled && !enabled) {

            LOG.debug("Stopped listening for document events");

            EditorFactory.getInstance().getEventMulticaster()
                    .removeDocumentListener(this);

            this.enabled = false;
        }
    }
}

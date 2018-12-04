package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.intellij.filesystem.VirtualFileConverter;
import de.fu_berlin.inf.dpp.intellij.session.SessionUtils;
import org.apache.log4j.Logger;

/** Parent class for stoppable document listeners. */
public abstract class AbstractStoppableDocumentListener extends AbstractStoppableListener
    implements DocumentListener {

  private static final Logger LOG = Logger.getLogger(AbstractStoppableDocumentListener.class);

  /**
   * Uses the constructor provided by AbstractStoppableListener and sets the internal listener state
   * flag to be disabled by default. The default state is set to false as the document listener is
   * only registered to the IntelliJ API when {@link #setEnabled(boolean)} is called.
   *
   * @param editorManager the EditorManager instance
   */
  protected AbstractStoppableDocumentListener(EditorManager editorManager) {
    super(editorManager);
    super.setEnabled(false);
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!this.enabled && enabled) {
      LOG.debug("Started listening for document events");

      EditorFactory.getInstance().getEventMulticaster().addDocumentListener(this);

      this.enabled = true;

    } else if (this.enabled && !enabled) {

      LOG.debug("Stopped listening for document events");

      EditorFactory.getInstance().getEventMulticaster().removeDocumentListener(this);

      this.enabled = false;
    }
  }

  /**
   * Returns the SPath for the given document.
   *
   * @param document the document to get an SPath for
   * @return the SPath for the given document or <code>null</code> if no VirtualFile for the
   *     document could be found, the found VirtualFile could not be converted to an SPath or the
   *     resources represented by the given document is not shared
   * @see VirtualFileConverter#convertToSPath(VirtualFile)
   */
  protected final SPath getSPath(Document document) {
    SPath path = editorManager.getEditorPool().getFile(document);

    if (path != null) {
      return path;
    }

    VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);

    if (virtualFile == null) {
      if (LOG.isTraceEnabled()) {
        LOG.trace(
            "Ignoring event for document "
                + document
                + " - document is not known to the editor pool and a "
                + "VirtualFile for the document could not be found");
      }

      return null;
    }

    path = VirtualFileConverter.convertToSPath(virtualFile);

    if (path == null || !SessionUtils.isShared(path)) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Ignoring Event for document " + document + " - document is not shared");
      }

      return null;
    }

    return path;
  }
}

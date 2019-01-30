package de.fu_berlin.inf.dpp.intellij.eventhandler.editor.document;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;

/**
 * Tracks modifications of Documents and triggers matching TextEditActivities. These activities are
 * created before the modification are actually applied to the document.
 *
 * @see DocumentListener#beforeDocumentChange(DocumentEvent)
 */
public class LocalDocumentModificationHandler extends AbstractLocalDocumentModificationHandler {

  private final DocumentListener documentListener =
      new DocumentListener() {
        @Override
        public void beforeDocumentChange(DocumentEvent event) {
          generateTextEditActivity(event);
        }
      };

  public LocalDocumentModificationHandler(EditorManager editorManager) {
    super(editorManager);
  }

  /**
   * Generates and dispatches a <code>TextEditActivity</code> for the given <code>DocumentEvent
   * </code>.
   *
   * @param event the event to react to
   * @see DocumentListener#beforeDocumentChange(DocumentEvent)
   */
  private void generateTextEditActivity(DocumentEvent event) {
    Document document = event.getDocument();

    SPath path = getSPath(document);

    if (path == null) {
      return;
    }

    String newText = event.getNewFragment().toString();
    String replacedText = event.getOldFragment().toString();

    editorManager.generateTextEdit(event.getOffset(), newText, replacedText, path);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled, documentListener);
  }
}

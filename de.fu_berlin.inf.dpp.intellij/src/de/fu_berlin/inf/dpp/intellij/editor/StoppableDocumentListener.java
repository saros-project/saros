package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import de.fu_berlin.inf.dpp.activities.SPath;

/**
 * Tracks modifications of Documents and triggers matching TextEditActivities. These activities are
 * created before the modification are actually applied to the document.
 *
 * @see DocumentListener#beforeDocumentChange(DocumentEvent)
 */
public class StoppableDocumentListener extends AbstractStoppableDocumentListener {

  StoppableDocumentListener(EditorManager editorManager) {
    super(editorManager);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Generates and dispatches a <code>TextEditActivity</code> for the given <code>DocumentEvent
   * </code>.
   *
   * @param event {@inheritDoc}
   */
  @Override
  public void beforeDocumentChange(DocumentEvent event) {
    Document document = event.getDocument();

    SPath path = getSPath(document);

    if (path == null) {
      return;
    }

    String newText = event.getNewFragment().toString();
    String replacedText = event.getOldFragment().toString();

    editorManager.generateTextEdit(event.getOffset(), newText, replacedText, path);
  }
}

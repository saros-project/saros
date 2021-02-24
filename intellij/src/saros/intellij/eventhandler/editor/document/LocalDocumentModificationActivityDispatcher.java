package saros.intellij.eventhandler.editor.document;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import saros.filesystem.IFile;
import saros.intellij.editor.EditorManager;
import saros.session.ISarosSession;

/**
 * Tracks modifications of Documents and triggers matching TextEditActivities. These activities are
 * created before the modification are actually applied to the document.
 *
 * @see DocumentListener#beforeDocumentChange(DocumentEvent)
 */
public class LocalDocumentModificationActivityDispatcher
    extends AbstractLocalDocumentModificationHandler {

  private final DocumentListener documentListener =
      new DocumentListener() {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
          generateTextEditActivity(event);
        }
      };

  public LocalDocumentModificationActivityDispatcher(
      Project project, EditorManager editorManager, ISarosSession sarosSession) {

    super(project, editorManager, sarosSession);
  }

  /**
   * Generates and dispatches a <code>TextEditActivity</code> for the given <code>DocumentEvent
   * </code>.
   *
   * @param event the event to react to
   * @see DocumentListener#beforeDocumentChange(DocumentEvent)
   */
  private void generateTextEditActivity(@NotNull DocumentEvent event) {
    Document document = event.getDocument();

    IFile file = getFile(document);

    if (file == null) {
      return;
    }

    String newText = event.getNewFragment().toString();
    String replacedText = event.getOldFragment().toString();

    editorManager.generateTextEdit(event.getOffset(), newText, replacedText, file, document);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled, documentListener);
  }
}

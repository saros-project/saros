package saros.intellij.eventhandler.editor.document;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import saros.filesystem.IFile;
import saros.intellij.editor.EditorManager;
import saros.intellij.editor.ProjectAPI;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.session.ISarosSession;

/**
 * Tracks modifications of Documents and adjusts the local annotations accordingly if the document
 * is not currently open in an editor.
 *
 * @see com.intellij.openapi.editor.event.DocumentListener
 */
public class LocalClosedEditorModificationHandler extends AbstractLocalDocumentModificationHandler {
  private final AnnotationManager annotationManager;

  private final DocumentListener documentListener =
      new DocumentListener() {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
          cleanUpAnnotations(event);
        }
      };

  public LocalClosedEditorModificationHandler(
      Project project,
      EditorManager editorManager,
      ISarosSession sarosSession,
      AnnotationManager annotationManager) {

    super(project, editorManager, sarosSession);

    this.annotationManager = annotationManager;
  }

  /**
   * Adjusts the annotations for the resource represented by the changed document if it is not
   * currently open in an editor. If it is currently open in an editor, this will be done
   * automatically by Intellij.
   *
   * @param event the event to react to
   * @see DocumentListener#beforeDocumentChange(DocumentEvent)
   */
  private void cleanUpAnnotations(@NotNull DocumentEvent event) {
    Document document = event.getDocument();

    IFile file = getFile(document);

    if (file == null) {
      return;
    }

    int offset = event.getOffset();
    String newText = event.getNewFragment().toString();
    String replacedText = event.getOldFragment().toString();

    boolean isOpenInEditor = ProjectAPI.isOpen(project, document);

    int replacedTextLength = replacedText.length();
    if (replacedTextLength > 0) {
      if (isOpenInEditor) {
        // ensure that annotations invalidated by deletion are pruned
        annotationManager.updateAnnotationStore(file);

      } else {
        annotationManager.moveAnnotationsAfterDeletion(file, offset, offset + replacedTextLength);
      }
    }

    int newTextLength = newText.length();
    if (!isOpenInEditor && newTextLength > 0) {
      annotationManager.moveAnnotationsAfterAddition(file, offset, offset + newTextLength);
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled, documentListener);
  }
}

package de.fu_berlin.inf.dpp.intellij.eventhandler.editor.document;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.ProjectAPI;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.AnnotationManager;
import org.jetbrains.annotations.NotNull;

/**
 * Tracks modifications of Documents and adjusts the local annotations accordingly if the document
 * is not currently open in an editor.
 *
 * @see com.intellij.openapi.editor.event.DocumentListener
 */
public class LocalClosedEditorModificationHandler extends AbstractLocalDocumentModificationHandler {
  private final ProjectAPI projectAPI;
  private final AnnotationManager annotationManager;

  private final DocumentListener documentListener =
      new DocumentListener() {
        @Override
        public void beforeDocumentChange(DocumentEvent event) {
          cleanUpAnnotations(event);
        }
      };

  public LocalClosedEditorModificationHandler(
      EditorManager editorManager, ProjectAPI projectAPI, AnnotationManager annotationManager) {

    super(editorManager);

    this.projectAPI = projectAPI;
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

    SPath path = getSPath(document);

    if (path == null) {
      return;
    }

    int offset = event.getOffset();
    String newText = event.getNewFragment().toString();
    String replacedText = event.getOldFragment().toString();

    if (!projectAPI.isOpen(document)) {
      IFile file = path.getFile();

      int replacedTextLength = replacedText.length();
      if (replacedTextLength > 0) {
        annotationManager.moveAnnotationsAfterDeletion(file, offset, offset + replacedTextLength);
      }

      int newTextLength = newText.length();
      if (newTextLength > 0) {
        annotationManager.moveAnnotationsAfterAddition(file, offset, offset + newTextLength);
      }
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled, documentListener);
  }
}

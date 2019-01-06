package de.fu_berlin.inf.dpp.intellij.editor.annotations;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.intellij.editor.AbstractLocalDocumentModificationHandler;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.ProjectAPI;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJReferencePointManager;
import de.fu_berlin.inf.dpp.intellij.filesystem.VirtualFileConverter;
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
  private final IntelliJReferencePointManager intelliJReferencePointManager;

  private final DocumentListener documentListener =
      new DocumentListener() {
        @Override
        public void beforeDocumentChange(DocumentEvent event) {
          cleanUpAnnotations(event);
        }
      };

  public LocalClosedEditorModificationHandler(
      @NotNull EditorManager editorManager,
      @NotNull ProjectAPI projectAPI,
      @NotNull AnnotationManager annotationManager,
      @NotNull IntelliJReferencePointManager intelliJReferencePointManager) {

    super(editorManager);

    this.projectAPI = projectAPI;
    this.annotationManager = annotationManager;
    this.intelliJReferencePointManager = intelliJReferencePointManager;
  }

  /**
   * Adjusts the annotations for the resource represented by the changed document if it is not
   * currently open in an editor. If it is currently open in an editor, this will be done
   * automatically by Intellij.
   *
   * @param event the event to react to
   * @see DocumentListener#beforeDocumentChange(DocumentEvent)
   */
  private void cleanUpAnnotations(DocumentEvent event) {
    Document document = event.getDocument();

    SPath path = getSPath(document);

    if (path == null) {
      return;
    }

    int offset = event.getOffset();
    String newText = event.getNewFragment().toString();
    String replacedText = event.getOldFragment().toString();

    if (!projectAPI.isOpen(document)) {
      IFile file = getFile(path);

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

  private IFile getFile(SPath path) {
    IReferencePoint referencePoint = path.getReferencePoint();
    IPath referencePointRelativePath = path.getReferencePointRelativePath();

    VirtualFile virtualFile =
        intelliJReferencePointManager.getResource(referencePoint, referencePointRelativePath);

    return (IFile) VirtualFileConverter.convertToResource(virtualFile);
  }
}

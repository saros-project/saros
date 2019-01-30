package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.ITextOperation;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import de.fu_berlin.inf.dpp.editor.text.TextSelection;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.AnnotationManager;
import de.fu_berlin.inf.dpp.intellij.filesystem.VirtualFileConverter;
import de.fu_berlin.inf.dpp.intellij.project.SharedResourcesManager;
import de.fu_berlin.inf.dpp.intellij.session.SessionUtils;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.session.User;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/** This class applies the logic for activities that were received from remote. */
public class LocalEditorManipulator {

  private static final Logger LOG = Logger.getLogger(LocalEditorManipulator.class);

  private final ProjectAPI projectAPI;
  private final EditorAPI editorAPI;
  private final AnnotationManager annotationManager;

  /** This is just a reference to {@link EditorManager}'s editorPool and not a separate pool. */
  private EditorPool editorPool;

  private EditorManager manager;

  public LocalEditorManipulator(
      ProjectAPI projectAPI, EditorAPI editorAPI, AnnotationManager annotationManager) {
    this.projectAPI = projectAPI;
    this.editorAPI = editorAPI;
    this.annotationManager = annotationManager;
  }

  /** Initializes all fields that require an EditorManager. */
  void initialize(EditorManager editorManager) {
    editorPool = editorManager.getEditorPool();
    manager = editorManager;
  }

  /**
   * Opens an editor for the passed virtualFile, adds it to the pool of currently open editors and
   * calls {@link EditorManager#startEditor(Editor)} with it.
   *
   * <p><b>Note:</b> This does only work for shared resources.
   *
   * @param path path of the file to open
   * @param activate activate editor after opening
   * @return the editor for the given path, or <code>null</code> if the file does not exist or is
   *     not shared
   */
  public Editor openEditor(SPath path, boolean activate) {
    if (!SessionUtils.isShared(path.getResource())) {
      LOG.warn("Ignored open editor request for path " + path + " as it is not shared");

      return null;
    }

    VirtualFile virtualFile = VirtualFileConverter.convertToVirtualFile(path);

    if (virtualFile == null || !virtualFile.exists()) {
      LOG.warn(
          "Could not open Editor for path "
              + path
              + " as a "
              + "matching VirtualFile does not exist or could not be found");

      return null;
    }

    Editor editor = projectAPI.openEditor(virtualFile, activate);

    manager.startEditor(editor);
    editorPool.add(path, editor);

    LOG.debug("Opened Editor " + editor + " for file " + virtualFile);

    return editor;
  }

  /**
   * Closes the editor under the given path.
   *
   * @param path the path of the file for which to close the editor
   */
  public void closeEditor(SPath path) {
    editorPool.removeEditor(path);

    LOG.debug("Removed editor for path " + path + " from EditorPool");

    VirtualFile virtualFile = VirtualFileConverter.convertToVirtualFile(path);

    if (virtualFile == null || !virtualFile.exists()) {
      LOG.warn(
          "Could not close Editor for path "
              + path
              + " as a "
              + "matching VirtualFile does not exist or could not be found");

      return;
    }

    if (projectAPI.isOpen(virtualFile)) {
      projectAPI.closeEditor(virtualFile);
    }

    LOG.debug("Closed editor for file " + virtualFile);
  }

  /**
   * Applies the text operations to the document for the given path and adds matching contribution
   * annotations if necessary.
   *
   * @param path the path of the file whose document should be modified
   * @param operations the operations to apply to the document
   */
  void applyTextOperations(SPath path, Operation operations) {
    Document doc = editorPool.getDocument(path);

    /*
     * If the document was not opened in an editor yet, it is not in the
     * editorPool so we have to create it temporarily here.
     */
    if (doc == null) {
      VirtualFile virtualFile = VirtualFileConverter.convertToVirtualFile(path);

      if (virtualFile == null || !virtualFile.exists()) {
        LOG.warn(
            "Could not apply TextOperations "
                + operations
                + " as the VirtualFile for path "
                + path
                + " does not exist or could not be found");

        return;
      }

      doc = projectAPI.getDocument(virtualFile);

      if (doc == null) {
        LOG.warn(
            "Could not apply TextOperations "
                + operations
                + " as the Document for VirtualFile "
                + virtualFile
                + " could not be found");

        return;
      }
    }

    try {
      /*
       * Disable documentListener temporarily to avoid being notified of
       * the change
       */
      manager.disableDocumentHandlers();

      for (ITextOperation op : operations.getTextOperations()) {
        if (op instanceof DeleteOperation) {
          editorAPI.deleteText(doc, op.getPosition(), op.getPosition() + op.getTextLength());
        } else {
          boolean writePermission = doc.isWritable();
          if (!writePermission) {
            doc.setReadOnly(false);
          }
          editorAPI.insertText(doc, op.getPosition(), op.getText());
          if (!writePermission) {
            doc.setReadOnly(true);
          }
        }
      }

    } finally {
      manager.enableDocumentHandlers();
    }
  }

  /**
   * Adjusts the viewport of the given editor. Focus is set on the center of the given range. If no
   * range is given, focus is set to the center of the given selection instead. Either range or
   * selection can be <code>null</code>, but not both.
   *
   * <p><b>NOTE:</b> This class is meant for internal use only and should generally not be used
   * outside the editor package. If you still need to access this method, please consider whether
   * your class should rather be located in the editor package.
   *
   * @param editor the editor to adjust
   * @param range viewport of the followed user; can be <code>null</code> if selection is not <code>
   *     null</code>
   * @param selection text selection of the followed user; can be <code>null</code> if range is not
   *     <code>null</code>
   * @see IEditorManager#adjustViewport(SPath, LineRange, TextSelection)
   */
  public void adjustViewport(@NotNull Editor editor, LineRange range, TextSelection selection) {
    if (selection == null && range == null) {
      VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());

      LOG.warn(
          "Could not adjust viewport for "
              + file
              + " as no target location was given: given line range and text selection were null.");

      return;
    }

    LineRange localViewport = editorAPI.getLocalViewPortRange(editor);

    int localStartLine = localViewport.getStartLine();
    int localEndLine = localViewport.getStartLine() + localViewport.getNumberOfLines();

    int remoteStartLine;
    int remoteEndLine;

    if (range != null) {
      remoteStartLine = range.getStartLine();
      remoteEndLine = range.getStartLine() + range.getNumberOfLines();

    } else {
      int startOffset = selection.getOffset();
      int endOffset = selection.getOffset() + selection.getLength();

      LineRange selectionRange = editorAPI.getLineRange(editor, startOffset, endOffset);

      remoteStartLine = selectionRange.getStartLine();
      remoteEndLine = selectionRange.getStartLine() + selectionRange.getNumberOfLines();
    }

    if (localStartLine <= remoteStartLine && localEndLine >= remoteEndLine) {
      if (LOG.isTraceEnabled()) {
        LOG.trace(
            "Ignoring viewport change request as given viewport is already completely visible."
                + " local viewport: "
                + localStartLine
                + " - "
                + localEndLine
                + ", given viewport: "
                + remoteStartLine
                + " - "
                + remoteEndLine);
      }

      return;
    }

    int remoteViewPortCenter = findCenter(remoteStartLine, remoteEndLine);

    editorAPI.scrollToViewPortCenter(editor, remoteViewPortCenter);
  }

  /**
   * Returns the center file of the given line range.
   *
   * <p>IntelliJ sets the center position of an editor at 1/3 of the visible line range. This is
   * taken into account for the calculations.
   *
   * @param startLine the first line of the section
   * @param endLine the last line of the section
   * @return the center line of the section
   */
  private int findCenter(int startLine, int endLine) {
    int offset = (endLine - startLine) / 3;

    return startLine + offset;
  }

  /**
   * Replaces the content of the given file with the given content. This is done on the Document
   * level.
   *
   * <p><b>NOTE:</b> This method should only be used as part of the recovery process.
   *
   * @param path the path of the file to recover
   * @param content the new content of the file
   * @param encoding the encoding of the content
   * @param source the user that send the recovery action
   * @see Document
   * @see SharedResourcesManager#handleFileRecovery(FileActivity)
   */
  public void handleContentRecovery(SPath path, byte[] content, String encoding, User source) {
    VirtualFile virtualFile = VirtualFileConverter.convertToVirtualFile(path);
    if (virtualFile == null) {
      LOG.warn(
          "Could not recover file content of "
              + path
              + " as it could not be converted to a VirtualFile.");

      return;
    }

    Document document = projectAPI.getDocument(virtualFile);
    if (document == null) {
      LOG.warn(
          "Could not recover file content of "
              + path
              + " as no valid Document representation was returned by the IntelliJ API.");

      return;
    }

    int documentLength = document.getTextLength();

    IFile file = path.getFile();
    annotationManager.removeAnnotations(file);

    String text;
    try {
      text = new String(content, encoding);
    } catch (UnsupportedEncodingException e) {
      LOG.warn("Could not decode text using given encoding. Using default instead.", e);

      text = new String(content);

      NotificationPanel.showWarning(
          String.format(
              Messages.LocalEditorManipulator_incompatible_encoding_message,
              file.getLocation(),
              encoding,
              Charset.defaultCharset()),
          Messages.LocalEditorManipulator_incompatible_encoding_title);
    }

    boolean wasReadOnly = !document.isWritable();
    boolean wasDocumentListenerEnabled = manager.isDocumentModificationHandlerEnabled();

    try {
      // TODO distinguish if read-only was set by the StopManager, abort otherwise
      if (wasReadOnly) {
        document.setReadOnly(false);
      }

      if (wasDocumentListenerEnabled) {
        manager.disableDocumentHandlers();
      }

      editorAPI.deleteText(document, 0, documentLength);
      editorAPI.insertText(document, 0, text);

    } finally {

      if (wasDocumentListenerEnabled) {
        manager.enableDocumentHandlers();
      }

      if (wasReadOnly) {
        document.setReadOnly(true);
      }
    }

    Editor editor = null;
    if (projectAPI.isOpen(virtualFile)) {
      editor = projectAPI.openEditor(virtualFile, false);
    }

    annotationManager.addContributionAnnotation(source, file, 0, documentLength, editor);
  }
}

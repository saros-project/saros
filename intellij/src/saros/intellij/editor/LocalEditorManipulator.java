package saros.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.editor.IEditorManager;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.filesystem.IntellijReferencePoint;
import saros.intellij.filesystem.VirtualFileConverter;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.NotificationPanel;
import saros.session.ISarosSession;
import saros.session.User;

/** This class applies the logic for activities that were received from remote. */
public class LocalEditorManipulator {

  private static final Logger log = Logger.getLogger(LocalEditorManipulator.class);

  private final AnnotationManager annotationManager;
  private final ISarosSession sarosSession;

  /** This is just a reference to {@link EditorManager}'s editorPool and not a separate pool. */
  private final EditorPool editorPool;

  private final EditorManager manager;

  public LocalEditorManipulator(
      AnnotationManager annotationManager,
      EditorManager editorManager,
      ISarosSession sarosSession) {

    this.annotationManager = annotationManager;
    this.manager = editorManager;
    this.sarosSession = sarosSession;

    this.editorPool = manager.getEditorPool();
  }

  /**
   * Opens an editor for the passed file.
   *
   * <p>If the editor is a text editor, it is also added to the pool of currently open editors and
   * {@link EditorManager#startEditor(Editor)} is called with it.
   *
   * <p><b>Note:</b> This does only work for shared resources.
   *
   * @param file the file to open
   * @param activate activate editor after opening
   * @return the editor for the given file, or <code>null</code> if the file does not exist or is
   *     not shared or can not be represented by a text editor
   */
  public Editor openEditor(@NotNull IFile file, boolean activate) {
    if (!sarosSession.isShared(file)) {
      log.warn("Ignored open editor request for file " + file + " as it is not shared");

      return null;
    }

    VirtualFile virtualFile = VirtualFileConverter.convertToVirtualFile(file);

    if (virtualFile == null || !virtualFile.exists()) {
      log.warn(
          "Could not open Editor for file "
              + file
              + " as a matching virtual file does not exist or could not be found");

      return null;
    }

    Project project = ((IntellijReferencePoint) file.getReferencePoint()).getProject();

    Editor editor = ProjectAPI.openEditor(project, virtualFile, activate);

    if (editor == null) {
      log.debug("Ignoring non-text editor for file " + virtualFile);

      return null;
    }

    manager.startEditor(editor);
    editorPool.add(file, editor);

    log.debug("Opened Editor " + editor + " for file " + virtualFile);

    return editor;
  }

  /**
   * Closes the editor for the given file.
   *
   * @param file the file for which to close the editor
   */
  public void closeEditor(IFile file) {
    editorPool.removeEditor(file);

    log.debug("Removed editor for file " + file + " from EditorPool");

    VirtualFile virtualFile = VirtualFileConverter.convertToVirtualFile(file);

    if (virtualFile == null || !virtualFile.exists()) {
      log.warn(
          "Could not close Editor for file "
              + file
              + " as a matching virtual file does not exist or could not be found");

      return;
    }

    Project project = ((IntellijReferencePoint) file.getReferencePoint()).getProject();

    if (ProjectAPI.isOpen(project, virtualFile)) {
      ProjectAPI.closeEditor(project, virtualFile);
    }

    log.debug("Closed editor for file " + virtualFile);
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
   * @see IEditorManager#adjustViewport(IFile, LineRange, TextSelection)
   */
  public void adjustViewport(
      @NotNull Editor editor, @Nullable LineRange range, @Nullable TextSelection selection) {

    if ((selection == null || selection.isEmpty()) && range == null) {
      VirtualFile file = DocumentAPI.getVirtualFile(editor.getDocument());

      log.warn(
          "Could not adjust viewport for "
              + file
              + " as no target location was given: given line range and text selection were null.");

      return;
    }

    LineRange localViewport = EditorAPI.getLocalViewPortRange(editor);

    int localStartLine = localViewport.getStartLine();
    int localEndLine = localViewport.getStartLine() + localViewport.getNumberOfLines();

    int remoteStartLine;
    int remoteEndLine;

    if (range != null) {
      remoteStartLine = range.getStartLine();
      remoteEndLine = range.getStartLine() + range.getNumberOfLines();

    } else {
      remoteStartLine = selection.getStartPosition().getLineNumber();
      remoteEndLine = selection.getEndPosition().getLineNumber();
    }

    if (localStartLine <= remoteStartLine && localEndLine >= remoteEndLine) {
      if (log.isTraceEnabled()) {
        log.trace(
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

    EditorAPI.scrollToViewPortCenter(editor, remoteViewPortCenter);
  }

  /**
   * Returns the center file of the given line range.
   *
   * <p>From experimentation, it looks like Intellij sets the center position of an editor at 1/3 of
   * the visible line range. This is taken into account for the calculations.
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
   * @param file the file to recover
   * @param content the new content of the file
   * @param encoding the encoding of the content
   * @param source the user that send the recovery action
   * @see Document
   */
  public void handleContentRecovery(IFile file, byte[] content, String encoding, User source) {
    VirtualFile virtualFile = VirtualFileConverter.convertToVirtualFile(file);
    if (virtualFile == null) {
      log.warn(
          "Could not recover file content of "
              + file
              + " as it could not be converted to a virtual file.");

      return;
    }

    Project project = ((IntellijReferencePoint) file.getReferencePoint()).getProject();

    Document document = DocumentAPI.getDocument(virtualFile);
    if (document == null) {
      log.warn(
          "Could not recover file content of "
              + file
              + " as no valid document representation was returned by the Intellij API.");

      return;
    }

    int documentLength = document.getTextLength();

    annotationManager.removeAnnotations(file);

    String text;
    try {
      text = new String(content, encoding);
    } catch (UnsupportedEncodingException e) {
      log.warn("Could not decode text using given encoding. Using default instead.", e);

      text = new String(content);

      String qualifiedResource =
          file.getReferencePoint().getName() + " - " + file.getReferencePointRelativePath();

      NotificationPanel.showWarning(
          String.format(
              Messages.LocalEditorManipulator_incompatible_encoding_message,
              qualifiedResource,
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
        manager.setLocalDocumentModificationHandlersEnabled(false);
      }

      DocumentAPI.replaceText(project, document, 0, documentLength, text);

    } finally {

      if (wasDocumentListenerEnabled) {
        manager.setLocalDocumentModificationHandlersEnabled(true);
      }

      if (wasReadOnly) {
        document.setReadOnly(true);
      }
    }

    Editor editor = null;
    if (ProjectAPI.isOpen(project, virtualFile)) {
      editor = ProjectAPI.openEditor(project, virtualFile, false);

      if (editor == null) {
        log.warn("Could not obtain text editor for open, recovered file " + virtualFile);

        return;
      }
    }

    annotationManager.addContributionAnnotation(source, file, 0, documentLength, editor);
  }
}

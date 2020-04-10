package saros.intellij.editor;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.intellij.runtime.FilesystemRunner;

/**
 * Wrapper for interacting with the Intellij document API.
 *
 * @see Document
 */
public class DocumentAPI {
  private static final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
  private static final CommandProcessor commandProcessor = CommandProcessor.getInstance();

  private DocumentAPI() {
    // NOP
  }

  /**
   * Returns a document for the given file.
   *
   * @param virtualFile the <code>VirtualFile</code> for which the document is requested
   * @return a <code>Document</code> for the given file or <code>null</code> if the file does not
   *     exist, could not be read, is a directory, or is to large
   */
  @Nullable
  public static Document getDocument(@NotNull final VirtualFile virtualFile) {
    if (!virtualFile.exists()) {
      return null;
    }

    return FilesystemRunner.runReadAction(() -> fileDocumentManager.getDocument(virtualFile));
  }

  /**
   * Returns the VirtualFile for the given document.
   *
   * @param document the Document whose VirtualFile to get
   * @return the VirtualFile for the given document or <code>null</code> if the document was not
   *     created from a VirtualFile
   */
  @Nullable
  public static VirtualFile getVirtualFile(@NotNull final Document document) {
    return fileDocumentManager.getFile(document);
  }

  /**
   * Returns whether the document corresponding to the given file has unsaved changes.
   *
   * <p>Resources that don't have a matching document (i.e. that can't be opened in a text editor)
   * are always seen as unmodified.
   *
   * @param file the file to check for unsaved changes
   * @return whether the document corresponding to the given file has unsaved changes
   */
  public static boolean hasUnsavedChanges(@NotNull VirtualFile file) {
    return fileDocumentManager.isFileModified(file);
  }

  /**
   * Returns whether the given document has unsaved changes.
   *
   * @param document the document to check for unsaved changes
   * @return whether the given document has unsaved changes
   */
  public static boolean hasUnsavedChanges(@NotNull Document document) {
    return fileDocumentManager.isDocumentUnsaved(document);
  }

  /**
   * Saves the given document in the UI thread.
   *
   * @param document the document to save.
   */
  public static void saveDocument(final Document document) {
    FilesystemRunner.runWriteAction(
        () -> fileDocumentManager.saveDocument(document), ModalityState.NON_MODAL);
  }

  /** Saves all documents in the UI thread. */
  public static void saveAllDocuments() {
    FilesystemRunner.runWriteAction(fileDocumentManager::saveAllDocuments, ModalityState.NON_MODAL);
  }

  /**
   * Inserts the specified text at the specified offset in the document. Line breaks in the inserted
   * text must be normalized as '\n'.
   *
   * <p>The insertion will be wrapped in a command processor action. The action will be assigned to
   * the passed project. This means the action will be registered with the undo-buffer of the given
   * project.
   *
   * @param project the project to assign the resulting insertion action to
   * @param document the document to insert the text into
   * @param offset the offset to insert the text at
   * @param text the text to insert
   * @see Document#insertString(int, CharSequence)
   * @see CommandProcessor
   */
  static void insertText(
      @NotNull Project project,
      @NotNull final Document document,
      final int offset,
      final String text) {

    Runnable insertCommand =
        () -> {
          Runnable insertString = () -> document.insertString(offset, text);

          String commandName = "Saros text insertion at index " + offset + " of \"" + text + "\"";

          commandProcessor.executeCommand(
              project,
              insertString,
              commandName,
              commandProcessor.getCurrentCommandGroupId(),
              UndoConfirmationPolicy.REQUEST_CONFIRMATION,
              false);
        };

    FilesystemRunner.runWriteAction(insertCommand, ModalityState.defaultModalityState());
  }

  /**
   * Deletes the specified range of text from the given document.
   *
   * <p>The deletion will be wrapped in a command processor action. The action will be assigned to
   * the passed project. This means the action will be registered with the undo-buffer of the given
   * project.
   *
   * @param project the project to assign the resulting deletion action to
   * @param document the document to delete text from
   * @param start the start offset of the range to delete
   * @param end the end offset of the range to delete
   * @see Document#deleteString(int, int)
   * @see CommandProcessor
   */
  static void deleteText(
      @NotNull Project project, @NotNull final Document document, final int start, final int end) {

    Runnable deletionCommand =
        () -> {
          Runnable deleteRange = () -> document.deleteString(start, end);

          String commandName = "Saros text deletion from index " + start + " to " + end;

          commandProcessor.executeCommand(
              project,
              deleteRange,
              commandName,
              commandProcessor.getCurrentCommandGroupId(),
              UndoConfirmationPolicy.REQUEST_CONFIRMATION,
              false);
        };

    FilesystemRunner.runWriteAction(deletionCommand, ModalityState.defaultModalityState());
  }

  /**
   * Replaces the specified range of text in the given document with the given text. Line breaks in
   * the given text must be normalized as '\n'.
   *
   * <p>The replacement will be wrapped in a command processor action. The action will be assigned
   * to the passed project. This means the action will be registered with the undo-buffer of the
   * given project.
   *
   * @param project the project to assign the resulting deletion action to
   * @param document the document to delete text from
   * @param startOffset the start offset of the range to replace
   * @param endOffset the end offset of the range to replace
   * @param text the text to replace the current text with
   * @see Document#replaceString(int, int, CharSequence)
   * @see CommandProcessor
   */
  static void replaceText(
      @NotNull Project project,
      @NotNull Document document,
      int startOffset,
      int endOffset,
      @NotNull String text) {
    Runnable insertCommand =
        () -> {
          Runnable replaceText = () -> document.replaceString(startOffset, endOffset, text);

          String commandName =
              "Saros text replacement from index "
                  + startOffset
                  + " to "
                  + endOffset
                  + " with \""
                  + text
                  + "\"";

          commandProcessor.executeCommand(
              project,
              replaceText,
              commandName,
              commandProcessor.getCurrentCommandGroupId(),
              UndoConfirmationPolicy.REQUEST_CONFIRMATION,
              false);
        };

    FilesystemRunner.runWriteAction(insertCommand, ModalityState.defaultModalityState());
  }
}

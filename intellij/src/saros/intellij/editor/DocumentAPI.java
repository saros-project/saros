package saros.intellij.editor;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.intellij.filesystem.Filesystem;

/**
 * Wrapper for interacting with the Intellij document API.
 *
 * @see Document
 */
public class DocumentAPI {
  private static final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();

  private DocumentAPI() {
    // NOP
  }

  /**
   * Returns a document for the given file.
   *
   * @param file the <code>VirtualFile</code> for which the document is requested
   * @return a <code>Document</code> for the given file or <code>null</code> if the file does not
   *     exist, could not be read, is a directory, or is to large
   */
  @Nullable
  public static Document getDocument(@NotNull final VirtualFile file) {
    if (!file.exists()) {
      return null;
    }

    return Filesystem.runReadAction(
        new Computable<Document>() {

          @Override
          public Document compute() {
            return fileDocumentManager.getDocument(file);
          }
        });
  }

  /**
   * Saves the given document in the UI thread.
   *
   * @param doc
   */
  public static void saveDocument(final Document doc) {
    Filesystem.runWriteAction(
        new Runnable() {

          @Override
          public void run() {
            fileDocumentManager.saveDocument(doc);
          }
        },
        ModalityState.NON_MODAL);
  }

  /**
   * Reloads the current document in the UI thread.
   *
   * @param doc
   */
  public static void reloadFromDisk(final Document doc) {
    Filesystem.runReadAction(
        new Runnable() {

          @Override
          public void run() {
            fileDocumentManager.reloadFromDisk(doc);
          }
        });
  }

  /** Saves all documents in the UI thread. */
  public static void saveAllDocuments() {
    Filesystem.runWriteAction(
        new Runnable() {

          @Override
          public void run() {
            fileDocumentManager.saveAllDocuments();
          }
        },
        ModalityState.NON_MODAL);
  }
}

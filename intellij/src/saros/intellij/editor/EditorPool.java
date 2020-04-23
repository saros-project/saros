package saros.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IFile;

/**
 * The Intellij editor pool. It is used to store a mapping of <code>IFile</code>s onto <code>Editor
 * </code>s for all shared files that are open locally.
 */
class EditorPool {
  private final Map<IFile, Editor> editors;
  private final BackgroundEditorPool backgroundEditorPool;

  EditorPool(BackgroundEditorPool backgroundEditorPool) {
    this.editors = new HashMap<>();

    this.backgroundEditorPool = backgroundEditorPool;
  }

  /**
   * Adds the given <code>IFile</code> <code>Editor</code> mapping to the editor pool.
   *
   * <p>Also closes any existing background editors for the file as they are no longer needed.
   *
   * @param file the file
   * @param editor the editor for the file
   * @see BackgroundEditorPool
   */
  void add(@NotNull IFile file, @NotNull Editor editor) {

    editors.put(file, editor);

    backgroundEditorPool.dropBackgroundEditor(file);
  }

  /**
   * Removes the given file from the editor pool.
   *
   * @param file the file that is removed from the editor pool
   */
  void removeEditor(@NotNull IFile file) {

    editors.remove(file);
  }

  /**
   * Replaces the file of the editor pool mapping for the old file with the new file. Does nothing
   * if the editor pool does not contain a mapping for the given old file.
   *
   * @param oldFile the old file
   * @param newFile the new file
   */
  void replaceFile(@NotNull IFile oldFile, @NotNull IFile newFile) {

    Editor editor = editors.remove(oldFile);

    if (editor != null) {
      editors.put(newFile, editor);
    }
  }

  /** Sets all editors in the editor pool to read/write. */
  void unlockAllDocuments() {
    for (Editor editor : editors.values()) {
      editor.getDocument().setReadOnly(false);
    }
  }

  /** Sets all editors in the editor pool to read only. */
  void lockAllDocuments() {
    for (Editor editor : editors.values()) {
      editor.getDocument().setReadOnly(true);
    }
  }

  /**
   * Returns the <code>Document</code> for the given file.
   *
   * @param file the <code>IFile</code> to get the document for
   * @return the <code>Document</code> for the given file or <code>null</code> if the given file is
   *     not contained in the editor pool
   */
  @Nullable
  Document getDocument(@NotNull IFile file) {

    Editor editor = editors.get(file);

    return editor == null ? null : editor.getDocument();
  }

  /**
   * Returns the <code>Editor</code> for the given file.
   *
   * @param file the <code>IFile</code> to get the editor for
   * @return the <code>Editor</code> for the given file or <code>null</code> if the given file is
   *     not contained in the editor pool
   */
  @Nullable
  Editor getEditor(@NotNull IFile file) {

    return editors.get(file);
  }

  /**
   * Returns the <code>IFile</code> for the given document.
   *
   * @param doc the <code>Document</code> to get the file for
   * @return the <code>IFile</code> for the given document or <code>null</code> if no matching file
   *     could be found in the editor pool
   */
  @Nullable
  IFile getFile(@Nullable Document doc) {

    for (Map.Entry<IFile, Editor> entry : editors.entrySet()) {
      if (entry.getValue().getDocument().equals(doc)) {
        return entry.getKey();
      }
    }

    return null;
  }

  /**
   * Return all editors contained in the editor pool.
   *
   * @return all editors contained in the editor pool
   */
  @NotNull
  Collection<Editor> getEditors() {
    return editors.values();
  }

  /**
   * Returns all files contained in the editor pool.
   *
   * @return all files contained in the editor pool
   */
  @NotNull
  Set<IFile> getFiles() {
    return editors.keySet();
  }

  /**
   * Returns an unmodifiable representation of the held editor mapping. The mapping will still
   * reflect any changes made to the editor pool.
   *
   * @return an unmodifiable representation of the held editor mapping
   */
  @NotNull
  Map<IFile, Editor> getMapping() {
    return Collections.unmodifiableMap(editors);
  }

  /** Removes all mappings from the editor pool. */
  void clear() {
    editors.clear();
  }
}

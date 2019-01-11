package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import de.fu_berlin.inf.dpp.activities.SPath;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Intellij editor pool. It is used to store a mapping of <code>SPath</code>s onto <code>Editor
 * </code>s for all shared files that are open locally.
 */
public class EditorPool {
  private final Map<SPath, Editor> editors;

  public EditorPool() {
    this.editors = new HashMap<>();
  }

  /**
   * Adds the given <code>SPath</code> <code>Editor</code> mapping to the editor pool.
   *
   * @param file the path for the file
   * @param editor the editor for the file
   */
  public void add(@NotNull SPath file, @NotNull Editor editor) {

    editors.put(file, editor);
  }

  /**
   * Removes the given path from the editor pool.
   *
   * @param file the path that is removed from the editor pool
   */
  public void removeEditor(@NotNull SPath file) {

    editors.remove(file);
  }

  /**
   * Replaces the path of the editor pool mapping for the old path with the new path. Does nothing
   * if the editor pool does not contain a mapping for the given old path.
   *
   * @param oldPath the old path
   * @param newPath the new path
   */
  public void replacePath(@NotNull SPath oldPath, @NotNull SPath newPath) {

    Editor editor = editors.remove(oldPath);

    if (editor != null) {
      editors.put(newPath, editor);
    }
  }

  /** Sets all editors in the editor pool to read/write. */
  public void unlockAllDocuments() {
    for (Editor editor : editors.values()) {
      editor.getDocument().setReadOnly(false);
    }
  }

  /** Sets all editors in the editor pool to read only. */
  public void lockAllDocuments() {
    for (Editor editor : editors.values()) {
      editor.getDocument().setReadOnly(true);
    }
  }

  /**
   * Returns the <code>Document</code> for the given path.
   *
   * @param file the <code>SPath</code> to get the document for
   * @return the <code>Document</code> for the given path or <code>null</code> if the given path is
   *     not contained in the editor pool
   */
  @Nullable
  public Document getDocument(@NotNull SPath file) {

    Editor editor = editors.get(file);

    return editor == null ? null : editor.getDocument();
  }

  /**
   * Returns the <code>Editor</code> for the given path.
   *
   * @param file the <code>SPath</code> to get the editor for
   * @return the <code>Editor</code> for the given path or <code>null</code> if the given path is
   *     not contained in the editor pool
   */
  @Nullable
  public Editor getEditor(@NotNull SPath file) {

    return editors.get(file);
  }

  /**
   * Returns the <code>SPath</code> for the given document.
   *
   * @param doc the <code>Document</code> to get the path for
   * @return the <code>SPath</code> for the given document or <code>null</code> if no matching path
   *     could be found in the editor pool
   */
  @Nullable
  public SPath getFile(@NotNull Document doc) {

    for (Map.Entry<SPath, Editor> entry : editors.entrySet()) {
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
  public Collection<Editor> getEditors() {
    return editors.values();
  }

  /**
   * Returns all paths contained in the editor pool.
   *
   * @return all paths contained in the editor pool
   */
  @NotNull
  public Set<SPath> getFiles() {
    return editors.keySet();
  }

  /** Removes all mappings from the editor pool. */
  public void clear() {
    editors.clear();
  }
}

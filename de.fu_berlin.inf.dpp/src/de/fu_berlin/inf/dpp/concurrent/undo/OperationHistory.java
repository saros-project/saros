package de.fu_berlin.inf.dpp.concurrent.undo;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * The Operation History is the data structure for saving operations to calculate undo and redo
 * operations. It contains histories for each editor.
 */
public class OperationHistory {

  /** determines how many operations can be saved in history per editor */
  // TODO: has to be dependent on the Eclipse properties
  public final int MAX_SIZE = 1000;

  protected HashMap<SPath, LinkedList<EditorHistoryEntry>> history =
      new HashMap<SPath, LinkedList<EditorHistoryEntry>>();

  /**
   * An operation can have three types. A local operation can be undone. A remote operation is not
   * interesting for us. A redoable operation is a local operation that was undone and can now be
   * redone.
   */
  public enum Type {
    LOCAL,
    REMOTE,
    REDOABLE
  }

  public class EditorHistoryEntry {
    protected final Type type;
    protected final Operation operation;

    public EditorHistoryEntry(Type type, Operation operation) {
      this.type = type;
      this.operation = operation;
    }

    public Type getType() {
      return this.type;
    }

    public Operation getOperation() {
      return this.operation;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + OperationHistory.this.hashCode();
      result = prime * result + ((operation == null) ? 0 : operation.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      EditorHistoryEntry other = (EditorHistoryEntry) obj;
      if (operation == null) {
        if (other.operation != null) return false;
      } else if (!operation.equals(other.operation)) return false;
      if (type == null) {
        if (other.type != null) return false;
      } else if (!type.equals(other.type)) return false;
      return true;
    }
  }

  /**
   * Adds an Operation to the history. Too old elements are removed if the list is full.
   *
   * @param editor in which the operation was executed
   */
  public void add(SPath editor, Type type, Operation operation) {

    LinkedList<EditorHistoryEntry> editorHistory = history.get(editor);

    if (editorHistory == null) {
      editorHistory = new LinkedList<EditorHistoryEntry>();
      history.put(editor, editorHistory);
    }

    // history shouldn't overflow, remove too old elements
    for (int i = editorHistory.size(); i >= MAX_SIZE; i--) {
      editorHistory.removeLast();
    }

    EditorHistoryEntry entry = new EditorHistoryEntry(type, operation);
    editorHistory.add(0, entry);
  }

  /** @return the latest local Operation in the editor's history, null if there is none */
  public Operation getLatestLocal(SPath editor) {

    return getLatestOfType(Type.LOCAL, editor);
  }

  /**
   * @return the latest Operation in the editor's history that can be redone, null if there is none
   */
  public Operation getLatestRedoable(SPath editor) {

    return getLatestOfType(Type.REDOABLE, editor);
  }

  protected Operation getLatestOfType(Type type, SPath editor) {

    List<EditorHistoryEntry> editorHistory = history.get(editor);
    if (editorHistory == null) return null;

    for (EditorHistoryEntry entry : editorHistory) {
      if (entry.getType() == type) return entry.getOperation();
    }
    return null;
  }

  /**
   * @return all entries up to the latest local Operation (exclusive) in reverse order (oldest
   *     first)
   */
  public List<EditorHistoryEntry> entriesToLatestLocal(SPath editor) {
    return entriesToLatestOfType(Type.LOCAL, editor);
  }

  /**
   * @return all entries up to the latest redoable Operation (exclusive) in reverse order (oldest
   *     first)
   */
  public List<EditorHistoryEntry> entriesToLatestRedoable(SPath editor) {
    return entriesToLatestOfType(Type.REDOABLE, editor);
  }

  /**
   * @return all entries up to the latest operation of the given type (exclusive) in reverse order
   *     (oldest first)
   */
  protected List<EditorHistoryEntry> entriesToLatestOfType(Type type, SPath editor) {

    List<EditorHistoryEntry> result = new LinkedList<EditorHistoryEntry>();

    List<EditorHistoryEntry> editorHistory = history.get(editor);
    if (editorHistory == null) return result;

    for (EditorHistoryEntry entry : editorHistory) {
      if (entry.getType() != type) {
        result.add(0, entry);
      } else {
        return result;
      }
    }
    return result;
  }

  public void replaceType(SPath editor, Operation operation, Type oldType, Type newType) {

    EditorHistoryEntry oldEntry = new EditorHistoryEntry(oldType, operation);
    EditorHistoryEntry newEntry = new EditorHistoryEntry(newType, operation);

    LinkedList<EditorHistoryEntry> editorHistory = history.get(editor);

    if (editorHistory == null)
      throw new IllegalArgumentException(
          "Cannot replace type of " + operation + ", history empty for editor " + editor);

    ListIterator<EditorHistoryEntry> it = editorHistory.listIterator(0);

    while (it.hasNext()) {
      if (it.next().equals(oldEntry)) {
        it.set(newEntry);
        return;
      }
    }

    throw new IllegalArgumentException("Cannot replace type of " + operation + ", not in history");
  }

  public void clearEditorHistory(SPath editor) {
    history.remove(editor);
  }

  public void clear() {
    history.clear();
  }

  public List<EditorHistoryEntry> getAllEntries(SPath editor) {
    return (history.get(editor) != null)
        ? history.get(editor)
        : new LinkedList<EditorHistoryEntry>();
  }

  public boolean canUndo(SPath editor) {
    return getLatestLocal(editor) != null;
  }

  /** Redo should only be possible if there was executed an undo. */
  public boolean canRedo(SPath editor) {
    return getLatestRedoable(editor) != null;
  }
}

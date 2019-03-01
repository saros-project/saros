package saros.whiteboard.gef.commands;

import java.util.LinkedList;
import java.util.List;
import saros.whiteboard.gef.model.LayoutElementRecord;
import saros.whiteboard.sxe.records.DocumentRecord;
import saros.whiteboard.sxe.records.IRecord;

/**
 * Base class for creation commands.
 *
 * <p>An undo of a creation corresponds to a delete, thus a redo to a undo-delete. To undo a delete,
 * a record is "recreated" by means of the <code>recreate()</code> method. Thus the references in
 * the <code>CommandStack</code> may remain but to the peer another RID (thus another record) will
 * be sent.
 *
 * @author jurke
 */
public abstract class AbstractElementRecordCreateCommand extends SXECommand {
  private LayoutElementRecord parent;
  protected String newChildName;
  private LayoutElementRecord child;

  /**
   * protected because subclasses may be specialized to a specific type only (like polylines)
   *
   * @param name
   */
  protected void setChildName(String name) {
    this.newChildName = name;
  }

  public void setParent(Object e) {
    if (e instanceof LayoutElementRecord) this.parent = (LayoutElementRecord) e;
  }

  protected LayoutElementRecord getNewChild() {
    return child;
  }

  @Override
  public List<IRecord> getRecords() {
    List<IRecord> records = new LinkedList<IRecord>();

    child = (LayoutElementRecord) parent.createNewElementRecord(null, newChildName);

    records.add(child);
    records.addAll(getAttributeRecords(child));

    return records;
  }

  protected abstract List<IRecord> getAttributeRecords(LayoutElementRecord child);

  @Override
  public List<IRecord> getUndoRecords() {
    List<IRecord> records = new LinkedList<IRecord>();
    records.add(child.getRemoveRecord());

    return records;
  }

  @Override
  public List<IRecord> getRedoRecords() {
    List<IRecord> records = new LinkedList<IRecord>();
    records.add(child.getRecreateRecord());
    return records;
  }

  @Override
  public DocumentRecord getDocumentRecord() {
    return parent.getDocumentRecord();
  }

  @Override
  protected boolean canExecuteSXECommand() {
    if (newChildName == null || parent == null || !parent.isComposite()) return false;

    return parent.isPartOfVisibleDocument();
  }

  @Override
  protected boolean canUndoSXECommand() {
    return child.isPartOfVisibleDocument();
  }

  @Override
  public void dispose() {
    newChildName = null;
    parent = null;
    child = null;
  }
}

package saros.whiteboard.gef.commands;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import saros.whiteboard.gef.model.LayoutElementRecord;
import saros.whiteboard.sxe.records.DocumentRecord;
import saros.whiteboard.sxe.records.ElementRecord;
import saros.whiteboard.sxe.records.IRecord;
import saros.whiteboard.sxe.util.HierarchicalRecordSet;

/**
 * This command allows to delete a arbitrary group of <code>ElementRecord</code> s by only creating
 * <code>RemoveRecord</code> for the top-most elements.
 *
 * <p>To undo a delete, a record is "recreated" by means of the <code>recreate()</code> method. Thus
 * the references in the <code>CommandStack</code> may remain but to the peer another RID (thus
 * another record) will be sent.
 *
 * @author jurke
 */
public class DeleteRecordsCommand extends SXECommand {

  /** all records to delete */
  private HierarchicalRecordSet recordSet = new HierarchicalRecordSet();

  private DocumentRecord documentRecord;

  public void addRecordToDelete(ElementRecord record) {
    if (record.getParent() == null) return;
    if (documentRecord == null) documentRecord = record.getDocumentRecord();
    recordSet.insertRecord(record);
  }

  @Override
  protected boolean canExecuteSXECommand() {
    if (recordSet.getRootRecords().isEmpty()) return false;

    for (ElementRecord er : recordSet.getRootRecords()) {
      if (er.isPartOfVisibleDocument()) return true;
    }
    return false;
  }

  @Override
  public List<IRecord> getRecords() {
    List<IRecord> records = new LinkedList<IRecord>();

    for (ElementRecord er : recordSet.getRootRecords()) {
      records.add(er.getRemoveRecord());
    }

    return records;
  }

  @Override
  public List<IRecord> getUndoRecords() {

    List<IRecord> records = new ArrayList<IRecord>();

    for (ElementRecord e : recordSet.getRootRecords()) {
      records.add(e.getRecreateRecord());
    }

    return records;
  }

  @Override
  public DocumentRecord getDocumentRecord() {
    return documentRecord;
  }

  @Override
  protected boolean canUndoSXECommand() {
    LayoutElementRecord parent;

    if (recordSet.getRootRecords().isEmpty()) return false;

    try {
      for (ElementRecord e : recordSet.getRootRecords()) {
        parent = (LayoutElementRecord) e.getParent();
        if (!parent.isPartOfVisibleDocument()) return false;
        if (!parent.isComposite()) return false;
        /*
         * also if existent, let's just set it visible again. Will be
         * ignored by the controller then.
         */
      }
    } catch (ClassCastException e) {
      return false;
    }

    return true;
  }

  @Override
  public void dispose() {
    super.dispose();
    recordSet.clear();
    recordSet = null;
    documentRecord = null;
  }

  void setDocumentRecord(DocumentRecord document) {
    this.documentRecord = document;
  }
}

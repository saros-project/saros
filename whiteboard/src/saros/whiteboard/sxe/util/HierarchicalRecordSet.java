package de.fu_berlin.inf.dpp.whiteboard.sxe.util;

import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * Use this class to divide a bunch of records in root records and records that are descendants of
 * top records, subtree records.
 *
 * <p>Especially useful to achieve the records to delete or to copy because subtrees will be
 * deleted/copied with the parent.
 *
 * @author jurke
 */
public class HierarchicalRecordSet {

  protected LinkedList<ElementRecord> rootRecords = new LinkedList<ElementRecord>();
  protected TreeSet<ElementRecord> subtreeRecords = new TreeSet<ElementRecord>();

  /**
   * Inserts all records of the targets collection of type {@link ElementRecord} to this set.
   *
   * @param targets
   */
  public void insertElementRecords(Collection<IRecord> targets) {
    for (IRecord r : targets) {
      if (r instanceof ElementRecord) {
        insertRecord((ElementRecord) r);
      }
    }
  }

  /**
   * If any of the passed record's parents or itself is already contained there is nothing to do.
   * </br> Else it will be inserted as a new root record. Furthermore all its descendants will be
   * added to subtree records and removed from root records if applicable.
   *
   * @param record
   */
  public void insertRecord(ElementRecord record) {

    if (subtreeRecords.contains(record) || rootRecords.contains(record)) {
      return;
    } else {
      ElementRecord tmp;
      Collection<ElementRecord> children = record.getAllVisibleDescendantElements();

      Iterator<ElementRecord> it = rootRecords.iterator();
      while (it.hasNext()) {
        tmp = it.next();
        if (children.contains(tmp)) {
          subtreeRecords.add(tmp);
          it.remove();
        }
      }

      rootRecords.add(record);
      subtreeRecords.addAll(children);
    }
  }

  /** @return all records so that */
  public List<ElementRecord> getRootRecords() {
    return rootRecords;
  }

  public Collection<ElementRecord> getSubtreeRecords() {
    return subtreeRecords;
  }

  public boolean isEmpty() {
    return rootRecords.isEmpty();
  }

  public void clear() {
    rootRecords.clear();
    subtreeRecords.clear();
  }
}

package de.fu_berlin.inf.dpp.whiteboard.sxe.records;

import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * This class offers convenient methods to notify clients (like GUI) about applied changes and
 * conflicts in the SXE document.
 *
 * <p>Therefore it allows to cache child and attribute changes (order, create or remove) as well as
 * conflicts until the notification takes place clearing the cache.</br>
 *
 * <p>These changes can only occur when applying records in this package so respective methods are
 * not public.
 *
 * <p>The listeners are notified after a whole operation or message is applied.
 *
 * @see de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController#notifyLocalListeners()
 * @see de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord#apply(DocumentRecord)
 * @author jurke
 */
public class ChildRecordChangeCache {

  private static final Logger log = Logger.getLogger(ChildRecordChangeCache.class);

  /**
   * Informs about child element and attribute changes as well as conflicts of child nodes by
   * passing the causative records.
   *
   * @author jurke
   * @hidden // ignore this UMLGraph tag
   */
  public static interface ChildRecordChangeListener {

    public void childElementRecordChanged(List<IRecord> records);

    public void attributeRecordChanged(List<IRecord> records);

    public void childRecordConflict(Map<NodeRecord, Set<SetRecord>> conflicts);
  }

  protected List<ChildRecordChangeListener> listeners = new ArrayList<ChildRecordChangeListener>();

  protected ElementRecord record;

  protected List<IRecord> attrs = new LinkedList<IRecord>();
  protected List<IRecord> elements = new LinkedList<IRecord>();
  protected Map<NodeRecord, Set<SetRecord>> conflicts =
      new LinkedHashMap<NodeRecord, Set<SetRecord>>();

  protected SXEController controller;

  /**
   * @param record the ElementRecord for which to track events
   * @param controller the controller that handles record applying
   */
  public ChildRecordChangeCache(ElementRecord record, SXEController controller) {
    if (controller == null || record == null) throw new NullPointerException();
    this.record = record;
    this.controller = controller;
  }

  public void clear() {
    attrs = new LinkedList<IRecord>();
    elements = new LinkedList<IRecord>();

    /*
     * If introducing a conflict handler (like a possibility for the user to
     * choose one out of conflicting SetRecords) "conflicts" should be an
     * extra data structure with a listener to inform the handler about new
     * conflicts arrived while choosing
     */
    conflicts = new LinkedHashMap<NodeRecord, Set<SetRecord>>();
  }

  public void add(ChildRecordChangeListener listener) {
    listeners.add(listener);
  }

  public void remove(ChildRecordChangeListener listener) {
    listeners.remove(listener);
  }

  /**
   * Add the record that caused a creation, removal or change of a child element
   *
   * @param record
   */
  void childElementChange(IRecord record) {
    elements.add(record);
    controller.addChildRecordChange(this);
  }

  /**
   * Add the record that caused a creation, removal or change of an attribute
   *
   * @param record
   */
  void attributeChange(IRecord record) {
    attrs.add(record);
    controller.addChildRecordChange(this);
  }

  /**
   * Add two conflicting SetRecords and the concerning Record
   *
   * @param child the child that contains the resulting (reverted) state
   * @param previousState the SetRecord that used to be valid before the conflict
   * @param remote the SetRecord causing the conflict
   */
  void addChildRecordConflict(NodeRecord child, SetRecord previousState, SetRecord remote) {
    Set<SetRecord> set = conflicts.get(child);
    if (set == null) {
      set = new HashSet<SetRecord>();
      conflicts.put(child, set);
    }
    set.add(previousState);
    set.add(remote);
    controller.addChildRecordChange(this);
  }

  /** Notifies all listeners about change and conflict events and clears the cache. */
  public void notifyListeners() {
    if (!record.isCommitted()) {
      log.warn("Tried to notify listeners of uncommitted records!");
      return;
    }
    log.trace(
        "Notify Listener: " + record + " \nAttributes: " + attrs + " \nElements: " + elements);
    for (ChildRecordChangeListener l : listeners) {
      l.attributeRecordChanged(attrs);
    }

    for (ChildRecordChangeListener l : listeners) l.childElementRecordChanged(elements);
    for (ChildRecordChangeListener l : listeners) l.childRecordConflict(conflicts);
    clear();
  }
}

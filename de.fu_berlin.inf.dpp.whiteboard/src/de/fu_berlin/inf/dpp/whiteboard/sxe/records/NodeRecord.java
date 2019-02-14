package de.fu_berlin.inf.dpp.whiteboard.sxe.records;

import com.google.gson.annotations.Expose;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.NodeType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordEntry;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions.CommittedRecordException;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable.NewRecordDataObject;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable.RecordDataObject;
import de.fu_berlin.inf.dpp.whiteboard.sxe.util.SetRecordList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 * Abstract implementation of a DOM node in context of Shared XML Editing XEP-0284 (SXE).
 *
 * <p>It maintains the common mutable and immutable fields of attribute and element nodes. By this a
 * list of setRecords, sorted by version, and the possibility to apply SetRecords and RemoveRecords.
 *
 * <p>Note: In difference to SXE, name and namespace are seen an immutable fields because this does
 * not make sense respective SVG whiteboarding.
 *
 * <p>The primary-weight defines the position of this record in its parent's child node list. If
 * left empty it is set automatically when applying to add this record to the end.
 *
 * @author jurke
 */
public abstract class NodeRecord extends AbstractRecord implements Comparable<NodeRecord> {

  private static final Logger log = Logger.getLogger(NodeRecord.class);

  /** Java Random to generate a unique Record ID */
  private static final Random RANDOM = new Random();

  /* immutable fields */
  @Expose private String rid;
  @Expose private final NodeType type;
  @Expose protected String name;
  protected int version;
  protected String ns;
  protected String creator;

  /* mutable fields */
  @Expose protected boolean visible = true;
  protected Float currentPrimaryWeight;

  // Don't expose parent, avoids circular references
  protected ElementRecord currentParent;

  /** a initial SetRecord as initial state * */
  protected SetRecord initialSet;

  /** specialized List that ensures increasing versions * */
  protected SetRecordList setRecords = new SetRecordList();

  /** the document reference * */
  private DocumentRecord documentRecord;

  /**
   * Constructor for locally created records with version == 0.
   *
   * @param documentRecord
   * @param type
   */
  protected NodeRecord(DocumentRecord documentRecord, NodeType nodeType) {
    this(documentRecord, nodeType, 0);
  }

  /**
   * Constructor for received remote records
   *
   * @param documentRecord
   * @param type
   * @param version
   */
  protected NodeRecord(DocumentRecord documentRecord, NodeType nodeType, int version) {
    this.rid = getNextRandomUniqueID();
    this.type = nodeType;
    this.documentRecord = documentRecord;
    this.version = version;
    initialSet = new SetRecord(this, version);
    initialSet.setSetVisibilityTo(true);
  }

  /**
   * Generate a random Identifier that is most likely unique
   *
   * @return a numerical String representing a random id
   */
  public String getNextRandomUniqueID() {
    // if needed, increase the random number.
    return (System.currentTimeMillis()) + "" + RANDOM.nextInt(100 * 1000);
  }

  public DocumentRecord getDocumentRecord() {
    return documentRecord;
  }

  protected void setDocumentRecord(DocumentRecord documentRecord) {
    this.documentRecord = documentRecord;
  }

  @Override
  public boolean isCommitted() {
    // this can happen when called in the constructor
    if (getDocumentRecord() == null) return false;

    if (getDocumentRecord().contains(this)) return true;

    return false;
  }

  public boolean isVisible() {
    return visible;
  }

  @Override
  public boolean isPartOfVisibleDocument() {
    if (!isCommitted()) return false;

    if (!visible) return false;

    if (this == documentRecord.getRoot()) return true;

    if (currentParent == null) return false;

    return currentParent.isPartOfVisibleDocument();
  }

  public SetRecord getRemoveRecord() {
    SetRecord remove = new SetRecord(this);
    remove.setSetVisibilityTo(false);
    return remove;
  }

  public SetRecord getRecreateRecord() {
    SetRecord recreate = new SetRecord(this);
    recreate.setSetVisibilityTo(true);
    return recreate;
  }

  /**
   * Returns the list of non-discarded SetRecords.
   *
   * <p>Clients should not modify.
   *
   * @return the list of applied SetRecords
   */
  public LinkedList<SetRecord> getSetRecords() {
    return setRecords;
  }

  /**
   * Important: For new-records, the target is this.
   *
   * @return this reference
   */
  @Override
  public NodeRecord getTarget() {
    return this;
  }

  @Override
  public RecordType getRecordType() {
    return RecordType.NEW;
  }

  /**
   * The type of node to be inserted.
   *
   * @see NodeType
   */
  public NodeType getNodeType() {
    return type;
  }

  public String getRid() {
    return rid;
  }

  public void setRid(String rid) {
    if (isCommitted()) throw new CommittedRecordException();
    this.rid = rid;
  }

  /** @return the current version due to SetRecords */
  public int getVersion() {
    return version;
  }

  /**
   * Beware of using this method. Should only be used during start synchronization to skip missing
   * versions. This is expected because due to conflicts SetRecords may have been discarded.</br>
   *
   * <p>However in a running session this must be an error.
   *
   * @throws IllegalArgumentException if the passed version is smaller than the current one
   */
  public void repairVersion(int version) {
    if (this.version > version)
      throw new IllegalArgumentException("Can only skip version, not set it back.");
    this.version = version;
  }

  /**
   * Method for remote records initial version
   *
   * @param version the initial version
   */
  public void setVersion(int version) {
    if (isCommitted()) throw new CommittedRecordException();
    this.version = version;
  }

  public String getNs() {
    return ns;
  }

  public void setNs(String ns) {
    if (isCommitted()) throw new CommittedRecordException();
    this.ns = ns;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (isCommitted()) throw new CommittedRecordException();
    this.name = name;
  }

  /* mutable record attributes */

  public ElementRecord getParent() {
    return currentParent;
  }

  public void setParent(ElementRecord parent) {
    if (isCommitted()) throw new CommittedRecordException();
    documentRecord = parent.getDocumentRecord();
    currentParent = parent;
    initialSet.setParentToChange(parent);
  }

  public Float getPrimaryWeight() {
    return currentPrimaryWeight;
  }

  public void setPrimaryWeight(Float primaryWeight) {
    if (isCommitted()) throw new CommittedRecordException();
    currentPrimaryWeight = primaryWeight;
    initialSet.setPrimaryWeight(primaryWeight);
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    if (isCommitted()) throw new CommittedRecordException();
    this.creator = creator;
  }

  @Override
  public String getLastModifiedBy() {
    if (setRecords.isEmpty()) return initialSet.getLastModifiedBy();
    return setRecords.getLast().getLastModifiedBy();
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    if (isCommitted()) throw new CommittedRecordException();
    initialSet.setLastModifiedBy(lastModifiedBy);
  }

  /* Util */

  /** compares the primary and secondary-weight (RID) */
  @Override
  public int compareTo(NodeRecord record) {
    if (this == record) return 0;
    if (getPrimaryWeight() < record.getPrimaryWeight()) return -1;
    if (getPrimaryWeight() > record.getPrimaryWeight()) return 1;
    return getRid().compareTo(record.getRid());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRecord)) return false;
    return equals((NodeRecord) o);
  }

  public boolean equals(NodeRecord r) {
    return getRid().equals(r.getRid());
  }

  /**
   * If successful, this method sets the mutable fields to these defined in the provided SetRecord
   * and increments the version attribute.
   *
   * @param setRecord
   * @return if applying was successful
   */
  public final boolean applySetRecord(SetRecord setRecord) {

    if (setRecords.contains(setRecord)) {
      // This may happen during start synchronization
      log.debug("Duplicate set record: " + setRecord);
      return false;
    }

    // cannot add a parent to root
    if (getParent() == null && setRecord.getParentToChange() != null) return false;

    SetRecord oldState = getCurrentMutableFields();
    SetRecord newState;

    // conflict
    if (version + 1 != setRecord.getVersion()) {
      newState = getNewStateAndRevertHistory(setRecord.getVersion());
      getParent().notifyChildConflict(this, oldState, setRecord);
      log.debug(
          "reverting record from version "
              + (getVersion() - 1)
              + " to "
              + (setRecord.getVersion() - 1)
              + ": "
              + newState.toString());
      setValuesTo(newState);
    } else {
      newState = setRecord;
      setValuesTo(newState);
      setRecords.add(setRecord);
    }

    version++;

    fireRecordChanged(oldState, newState);
    return true;
  }

  /** @return the current mutable fields as SetRecord */
  protected SetRecord getCurrentMutableFields() {
    SetRecord currentState = new SetRecord(this);
    currentState.setParentToChange(currentParent);
    currentState.setPrimaryWeight(currentPrimaryWeight);
    return currentState;
  }

  /**
   * @param version
   * @return the mutable fields before the version as SetRecord
   */
  protected SetRecord getNewStateAndRevertHistory(int version) {
    if (setRecords.isEmpty()) return initialSet;
    ListIterator<SetRecord> it = setRecords.listIterator(setRecords.size());
    SetRecord previous;
    SetRecord setTo = new SetRecord(this);

    /*
     * First, remove/discard all previously applied SetRecords before
     * version. Then iterate further until we found a previous value for
     * every mutable field.
     */

    while (it.hasPrevious()) {
      previous = it.previous();
      if (previous.getVersion() >= version) {
        it.remove();
      } else {
        setTo.fillEmptyMutableFieldsFrom(previous);
        if (setTo.setsAllMutableFields())
          // we found a record that changes all mutable values
          return setTo;
      }
    }
    // if not complete, fill by initial values;
    if (!setTo.setsAllMutableFields()) setTo.fillEmptyMutableFieldsFrom(initialSet);
    return setTo;
  }

  /**
   * Set mutable fields to these provided by setRecord. Does nothing for fields == null.
   *
   * <p>On primary-weight change, this record is re-attached to its parent (to achieve correct
   * ordering).
   *
   * @param setRecord
   */
  protected void setValuesTo(SetRecord setRecord) {

    boolean parentOrChildOrderChange = false;

    if (setRecord.getParentToChange() != null
        && !setRecord.getParentToChange().equals(currentParent)) {
      currentParent.removeChild(this);
      currentParent = setRecord.getParentToChange();
      parentOrChildOrderChange = true;
    }

    if (setRecord.getPrimaryWeight() != null
        && !setRecord.getPrimaryWeight().equals(currentPrimaryWeight)) {
      currentPrimaryWeight = setRecord.getPrimaryWeight();
      parentOrChildOrderChange = true;
    }

    if (setRecord.getSetVisibilityTo() != null && !setRecord.getSetVisibilityTo().equals(visible)) {
      visible = setRecord.getSetVisibilityTo();
    }

    // Because of SortedSet functionality we have to re-attach a child on
    // primaryWeight change
    if (parentOrChildOrderChange) getParent().reattachChild(this);
  }

  /**
   * Returns a SetRecord that changes this records parent and primary-weight
   *
   * @param newParent the new parent, may be null
   * @param primaryWeight the new primary-weight
   * @return the SetRecord to move this record
   */
  public SetRecord createMoveRecord(ElementRecord newParent, Float primaryWeight) {
    SetRecord r = new SetRecord(this);
    if (newParent != null) r.setParentToChange(newParent);
    r.setPrimaryWeight(primaryWeight);
    return r;
  }

  /**
   * @param newParent
   * @return the next primary-weight to append this record on top
   */
  protected abstract Float nextPrimaryWeight(ElementRecord newParent);

  /**
   * Returns a SetRecord that appends this record to another parent as last child.
   *
   * @param newParent
   * @return the set-record to move this record
   */
  public SetRecord createMoveRecord(ElementRecord newParent) {
    return createMoveRecord(newParent, nextPrimaryWeight(newParent));
  }

  public void clear() {
    if (isCommitted()) throw new CommittedRecordException();
    setRecords.clear();
  }

  @Override
  public RecordDataObject getRecordDataObject() {
    RecordDataObject rdo = new NewRecordDataObject();

    rdo.putValue(RecordEntry.TYPE, getNodeType().toString());
    rdo.putValue(RecordEntry.RID, getRid());
    rdo.putValue(RecordEntry.VERSION, initialSet.getVersion());
    if (getParent() != null) rdo.putValue(RecordEntry.PARENT, getParent().getRid());
    rdo.putValue(RecordEntry.VISIBLE, isVisible());
    rdo.putValue(RecordEntry.PRIMARY_WEIGHT, initialSet.getPrimaryWeight());
    rdo.putValue(RecordEntry.NAME, getName());
    rdo.putValue(RecordEntry.NS, getNs());
    rdo.putValue(RecordEntry.CHDATA, initialSet.getChdata());

    return rdo;
  }

  /** @return a new-record as copy of this one, without set-records and with a new RID */
  public abstract NodeRecord getCopy();

  /* notification */

  /** A helper method to notify the parent about changes on this record */
  protected void fireRecordChanged(SetRecord oldState, SetRecord newState) {

    if (newState.getParentToChange() != null
        && !newState.getParentToChange().equals(oldState.getParentToChange())) {
      // notify old parent
      oldState.getParentToChange().notifyChildChange(this);
      getParent().notifyChildChange(newState);
      return;
    }
    if (newState.getPrimaryWeight() != null
        && !newState.getPrimaryWeight().equals(oldState.getPrimaryWeight())) {
      getParent().notifyChildChange(newState);
      return;
    }

    if (newState.getChdata() != null && !newState.getChdata().equals(oldState.getChdata())) {
      getParent().notifyChildChange(newState);
      return;
    }

    if (newState.getSetVisibilityTo() != null
        && !newState.getSetVisibilityTo().equals(oldState.getSetVisibilityTo())) {
      getParent().notifyChildChange(newState);
      return;
    }
  }

  @Override
  public boolean canApply() {
    try {
      if (!getDocumentRecord().contains(getParent())) return false;
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String toString() {
    return "attr(" + rid + ") name=" + name;
  }
}

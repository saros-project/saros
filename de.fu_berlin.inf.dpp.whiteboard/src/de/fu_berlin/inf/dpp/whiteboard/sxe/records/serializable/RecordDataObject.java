package de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable;

import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.NodeType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordEntry;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions.MalformedRecordException;
import de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions.MissingRecordException;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base class for serializable records. This class uses the enum {@link RecordEntry} to maintain the
 * records' fields and offers respective setter methods.
 *
 * <p>As a record only consists of attributes the fields are stored in a HashMap.
 *
 * @author jurke
 */
public abstract class RecordDataObject implements Serializable {

  private static final long serialVersionUID = -4821813704707487311L;

  private final RecordType type;
  private NodeType nodeType;
  private final Map<RecordEntry, String> valuePairs = new HashMap<RecordEntry, String>();

  public RecordDataObject(RecordType recordType) {
    if (recordType == null) throw new NullPointerException();
    this.type = recordType;
  }

  public RecordType getRecordType() {
    return type;
  }

  public void setSenderIfAbsent(String sender) {
    if (getString(RecordEntry.SENDER) == null) putValue(RecordEntry.SENDER, sender);
  }

  /**
   * Returns the target RID entry or the RID of the new-record. Like this it works as complement to
   * Record.getTarget() and clients can assume that it will never return null.
   *
   * @return the target-entry or the rid of the new-record
   * @throws MalformedRecordException if there is no RID this data object does not correspond to a
   *     valid record
   */
  public abstract String getTargetRid() throws MalformedRecordException;

  public void putValue(RecordEntry entry, String value) {
    if (value != null) valuePairs.put(entry, value);
  }

  public void putValue(RecordEntry entry, Float value) {
    if (value != null) valuePairs.put(entry, String.valueOf(value));
  }

  public void putValue(RecordEntry entry, Boolean value) {
    if (value != null) valuePairs.put(entry, String.valueOf(value));
  }

  public void putValue(RecordEntry entry, int value) {
    putValue(entry, String.valueOf(value));
  }

  public String getString(RecordEntry name) {
    return valuePairs.get(name);
  }

  public Boolean getBoolean(RecordEntry name) {
    String value = getString(name);
    if (value == null) return null;
    return Boolean.valueOf(value);
  }

  public Integer getInt(RecordEntry name) {
    String value = getString(name);
    if (value == null) return null;
    return Integer.valueOf(value);
  }

  public Float getFloat(RecordEntry name) {
    String value = getString(name);
    if (value == null) return null;
    return Float.valueOf(value);
  }

  public String getSender() {
    return getString(RecordEntry.SENDER);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(type + " ");

    for (Entry<RecordEntry, String> e : valuePairs.entrySet()) {
      sb.append(e.getKey() + "=" + e.getValue() + " ");
    }
    if (!valuePairs.isEmpty()) sb.delete(sb.length() - 1, sb.length());

    return sb.toString();
  }

  public void setNodeType(NodeType nodeType) {
    this.nodeType = nodeType;
  }

  public NodeType getNodeType() {
    return nodeType;
  }

  public Map<RecordEntry, String> getValuePairs() {
    return valuePairs;
  }

  /**
   * Note: under certain conditions this record may return a different type than defined in the
   * RecordType field due to concurrent edits.
   *
   * <p>Subclasses have to ensure to provide a record that sets the document in a synchronized state
   * or throw a MissingRecordException.
   *
   * @param document
   * @return a record to set the document in a synchronized state
   * @throws MissingRecordException if a field references a missing record
   */
  public abstract IRecord getIRecord(DocumentRecord document) throws MissingRecordException;

  /**
   * @param document the document record where it may have been applied to
   * @return whether the corresponding IRecord was already applied locally
   * @throws MissingRecordException if a target (i.e. SetRecord, RemoveRecord) is missing completely
   */
  public abstract boolean isAlreadyApplied(DocumentRecord document) throws MissingRecordException;
}

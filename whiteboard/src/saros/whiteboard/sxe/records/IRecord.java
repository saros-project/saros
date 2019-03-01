package saros.whiteboard.sxe.records;

import saros.whiteboard.sxe.constants.RecordType;
import saros.whiteboard.sxe.records.serializable.RecordDataObject;

/**
 * The most generalized version of an SXE record.
 *
 * <p>All SXE edit classes (records) should implement this.
 *
 * @author jurke
 */
public interface IRecord {

  /* existence */

  public boolean isPartOfVisibleDocument();

  /* applying */

  /**
   * Tries to apply this record to our document.
   *
   * @return if applying was successful, including conflict resolution
   */
  public boolean apply(DocumentRecord document);

  /** @return whether the record can be applied */
  public boolean canApply();

  /* common methods */

  /** The record type */
  public RecordType getRecordType();

  /**
   * The target of this record.
   *
   * <p>Note that it should return a this-reference in case of a new-record, thus is a convenient
   * method to access the record and the next parent ElementRecord by getTarget().getParent()
   */
  public NodeRecord getTarget();

  // only needed if P2P
  /** the last modifier */
  public String getLastModifiedBy();

  /* serialization */

  /** @return the serializable record data object */
  public RecordDataObject getRecordDataObject();

  /** @return the sender of this record */
  public String getSender();

  /**
   * Sets the sender of this record
   *
   * @param sender
   * @throws saros.whiteboard.sxe.exceptions.CommittedRecordException
   */
  public void setSender(String sender);
}

package saros.whiteboard.sxe.exceptions;

import saros.whiteboard.sxe.constants.RecordEntry;

/**
 * Used if a RecordDataObject is received that does not correspond to a valid record or that lacks a
 * required attribute.
 *
 * @author jurke
 */
public class MalformedRecordException extends RuntimeException {

  private static final long serialVersionUID = -1572530830099480598L;

  public MalformedRecordException(String record) {
    super("Malformed record " + record);
  }

  public MalformedRecordException(RecordEntry entry) {
    super("Missing record attribute: " + entry.toString());
  }
}

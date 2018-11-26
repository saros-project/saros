package de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions;

/**
 * A record committed to the document cannot be changed directly, SetRecords have to be used else a
 * CommittedRecordException will be thrown.
 *
 * @author jurke
 */
public class CommittedRecordException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CommittedRecordException() {
    super("Cannot change a record that is already commited. Create a new one to change XML data.");
  }
}

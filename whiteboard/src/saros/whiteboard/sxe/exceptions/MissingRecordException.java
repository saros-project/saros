package saros.whiteboard.sxe.exceptions;

/**
 * Used if a received RecordDataObject contains a reference to a local record that does not exist.
 *
 * @author jurke
 */
public class MissingRecordException extends Exception {

  private static final long serialVersionUID = 249900151513504486L;

  private final String rid;

  public MissingRecordException(String rid) {
    super("Missing record " + rid);
    this.rid = rid;
  }

  public MissingRecordException(String rid, String msg) {
    super("Missing record " + rid + ": " + msg);
    this.rid = rid;
  }

  public String getMissingRid() {
    return rid;
  }
}

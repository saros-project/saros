package saros.whiteboard.sxe.records;

import saros.whiteboard.sxe.exceptions.CommittedRecordException;

public abstract class AbstractRecord implements IRecord {

  protected String sender;

  @Override
  public String getSender() {
    return sender;
  }

  @Override
  public void setSender(String sender) {
    if (this.sender != null && isCommitted()) throw new CommittedRecordException();
    this.sender = sender;
  }

  protected abstract boolean isCommitted();
}

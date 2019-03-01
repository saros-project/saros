package de.fu_berlin.inf.dpp.whiteboard.sxe.records;

import de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions.CommittedRecordException;

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

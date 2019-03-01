package de.fu_berlin.inf.dpp.whiteboard.gef.commands;

import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.SetRecord;
import java.util.List;

public class ConflictingSXECommand extends SXECommand {

  private final SXECommand delegate;

  public ConflictingSXECommand(SXECommand delegate) {
    this.delegate = delegate;
  }

  @Override
  public List<IRecord> getRecords() {
    return makeConflicting(delegate.getRecords());
  }

  @Override
  public List<IRecord> getUndoRecords() {
    return makeConflicting(delegate.getUndoRecords());
  }

  @Override
  public List<IRecord> getRedoRecords() {
    return makeConflicting(delegate.getRedoRecords());
  }

  @Override
  public DocumentRecord getDocumentRecord() {
    return delegate.getDocumentRecord();
  }

  @Override
  protected boolean canExecuteSXECommand() {
    return delegate.canExecuteSXECommand();
  }

  @Override
  protected boolean canUndoSXECommand() {
    return delegate.canUndoSXECommand();
  }

  private List<IRecord> makeConflicting(List<IRecord> records) {
    for (IRecord r : records) {
      if (r instanceof SetRecord) makeConflicting((SetRecord) r);
    }
    return records;
  }

  private void makeConflicting(SetRecord setRecord) {
    setRecord.setVersion(setRecord.getTarget().getVersion());
  }
}

package saros.whiteboard.sxe.records.serializable;

import saros.whiteboard.sxe.constants.RecordEntry;
import saros.whiteboard.sxe.constants.RecordType;
import saros.whiteboard.sxe.exceptions.MalformedRecordException;
import saros.whiteboard.sxe.exceptions.MissingRecordException;
import saros.whiteboard.sxe.records.DocumentRecord;
import saros.whiteboard.sxe.records.ElementRecord;
import saros.whiteboard.sxe.records.IRecord;
import saros.whiteboard.sxe.records.NodeRecord;
import saros.whiteboard.sxe.records.SetRecord;

public class SetRecordDataObject extends RecordDataObject {

  private static final long serialVersionUID = 5665050716333165567L;

  public SetRecordDataObject() {
    super(RecordType.SET);
  }

  /**
   * Usually returns the respective SetRecord.
   *
   * <p>However, a RemoveRecord is returned if the respective SetRecord would change the target's
   * parent to a deleted record.</br>
   */
  @Override
  public IRecord getIRecord(DocumentRecord document) throws MissingRecordException {
    String tmp;

    tmp = getString(RecordEntry.TARGET);
    if (tmp == null) throw new MalformedRecordException("target rid missing");

    NodeRecord target = document.getRecordById(tmp);
    int version = getInt(RecordEntry.VERSION);

    ElementRecord parent = null;

    tmp = getString(RecordEntry.PARENT);

    if (tmp != null) {
      parent = document.getElementRecordById(tmp);
    }

    SetRecord record = new SetRecord(target, version);

    Boolean visible = getBoolean(RecordEntry.VISIBLE);
    if (visible != null) record.setSetVisibilityTo(visible);

    tmp = getString(RecordEntry.CHDATA);
    if (tmp != null) record.setChdata(tmp);

    if (parent != null) record.setParentToChange(parent);

    Float pw = getFloat(RecordEntry.PRIMARY_WEIGHT);
    if (pw != null) record.setPrimaryWeight(pw);

    record.setSender(getSender());

    return record;
  }

  @Override
  public boolean isAlreadyApplied(DocumentRecord document) throws MissingRecordException {
    NodeRecord record = document.getRecordById(getTargetRid());
    IRecord setRecord = getIRecord(document);
    return record.getSetRecords().contains(setRecord);
  }

  @Override
  public String getTargetRid() {
    String rid = getString(RecordEntry.TARGET);
    if (rid == null) throw new MalformedRecordException(RecordEntry.TARGET);
    return rid;
  }
}

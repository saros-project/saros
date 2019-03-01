package saros.whiteboard.sxe.records.serializable;

import saros.whiteboard.sxe.constants.NodeType;
import saros.whiteboard.sxe.constants.RecordEntry;
import saros.whiteboard.sxe.constants.RecordType;
import saros.whiteboard.sxe.exceptions.MalformedRecordException;
import saros.whiteboard.sxe.exceptions.MissingRecordException;
import saros.whiteboard.sxe.records.AttributeRecord;
import saros.whiteboard.sxe.records.DocumentRecord;
import saros.whiteboard.sxe.records.IRecord;
import saros.whiteboard.sxe.records.ISXERecordFactory;
import saros.whiteboard.sxe.records.NodeRecord;

public class NewRecordDataObject extends RecordDataObject {

  private static final long serialVersionUID = -4340512066154895188L;

  public NewRecordDataObject() {
    super(RecordType.NEW);
  }

  @Override
  public IRecord getIRecord(DocumentRecord document) throws MissingRecordException {
    NodeType nodeType = NodeType.fromString(getString(RecordEntry.TYPE));

    NodeRecord record;
    String tmp, rid;

    ISXERecordFactory factory = document.getController().getRecordFactory();

    String parentRid = getString(RecordEntry.PARENT);
    if (parentRid == null) {
      if (!document.isEmpty())
        throw new MalformedRecordException(
            "Received non-root record without parent: " + getString(RecordEntry.RID));
    }

    String name = getString(RecordEntry.NAME);
    String ns = getString(RecordEntry.NS);

    switch (nodeType) {
      case ATTR:
        String chdata = getString(RecordEntry.CHDATA);
        AttributeRecord attribute = factory.createAttributeRecord(document, ns, name, chdata);
        if (chdata != null) attribute.setChdata(chdata);
        record = attribute;
        break;
      case ELEMENT:
        if (parentRid == null) record = factory.createRoot(document);
        else record = factory.createElementRecord(document, ns, name);
        break;
      default:
        throw new RuntimeException("Unknown node type " + getRecordType());
    }

    rid = getString(RecordEntry.RID);
    record.setRid(rid);

    tmp = getString(RecordEntry.VERSION);
    if (tmp != null) record.setVersion(Integer.valueOf(tmp));
    else record.setVersion(0);

    if (parentRid != null) record.setParent(document.getElementRecordById(parentRid));

    Float pw = getFloat(RecordEntry.PRIMARY_WEIGHT);
    if (pw == null) pw = 0f;
    record.setPrimaryWeight(pw);

    tmp = getString(RecordEntry.NAME);
    if (tmp != null) record.setName(tmp);

    tmp = getString(RecordEntry.NS);
    if (tmp != null) record.setNs(tmp);

    // if no creator specified set sender as creator
    // TODO maybe creator tag is obsolete
    tmp = getString(RecordEntry.CREATOR);
    if (tmp != null) record.setCreator(tmp);
    else record.setCreator(getSender());

    tmp = getString(RecordEntry.LAST_MODIFIED_BY);
    if (tmp != null) record.setLastModifiedBy(tmp);

    record.setSender(getSender());

    return record;
  }

  @Override
  public boolean isAlreadyApplied(DocumentRecord document) {
    try {
      document.getRecordById(getTargetRid());
      return true;
    } catch (MissingRecordException e) {
      return false;
    }
  }

  @Override
  public String getTargetRid() {
    String rid = getString(RecordEntry.RID);
    if (rid == null) throw new MalformedRecordException(RecordEntry.RID);
    return rid;
  }
}

package de.fu_berlin.inf.dpp.whiteboard.sxe.records;

import com.google.gson.annotations.Expose;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.NodeType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions.CommittedRecordException;

/**
 * Implementation of a DOM attribute in context of Shared XML Editing XEP-0284 (SXE).
 *
 * <p>Respectively it extends the {@link NodeRecord} functionality by a chdata attribute.
 *
 * @author jurke
 */
/*
 * Note: can be base for TextRecord and CommentRecord with the difference that
 * they share the same primary-weight with ElementRecords
 */
public class AttributeRecord extends NodeRecord {

  @Expose protected String chdata;

  public AttributeRecord(DocumentRecord documentRecord) {
    super(documentRecord, NodeType.ATTR);
  }

  public String getChdata() {
    return chdata;
  }

  public void setChdata(String chdata) {
    if (isCommitted()) throw new CommittedRecordException();

    this.chdata = chdata;
    initialSet.setChdata(chdata);
  }

  @Override
  public void setValuesTo(SetRecord setRecord) {
    if (setRecord.getChdata() != null && !setRecord.getChdata().equals(chdata)) {
      chdata = setRecord.getChdata();
    }
    super.setValuesTo(setRecord);
  }

  @Override
  public AttributeRecord getCopy() {
    ISXERecordFactory factory = this.getDocumentRecord().getController().getRecordFactory();
    AttributeRecord ar =
        factory.createAttributeRecord(getDocumentRecord(), getNs(), getName(), getChdata());
    ar.setName(getName());
    ar.setNs(getNs());
    ar.setChdata(getChdata());
    return ar;
  }

  public SetRecord createSetRecord(String chdata) {
    if (getNodeType().equals(NodeType.ELEMENT))
      throw new UnsupportedOperationException("Cannot change chdata of an element.");
    SetRecord r = new SetRecord(this);
    r.setChdata(chdata);
    return r;
  }

  /** Inserts this record to the SXE tree and DocumentRecord. */
  @Override
  public boolean apply(DocumentRecord document) {

    if (this.getParent() == null) return false; // no parent

    if (document.contains(this)) return false;

    getParent().getAttributes().add(this);

    document.insert(this);

    getParent().notifyChildChange(this);

    return true;
  }

  @Override
  protected Float nextPrimaryWeight(ElementRecord newParent) {
    return newParent.getAttributes().nextPrimaryWeight();
  }

  @Override
  protected SetRecord getCurrentMutableFields() {
    SetRecord currentState = super.getCurrentMutableFields();
    currentState.setChdata(chdata);
    return currentState;
  }
}

package saros.whiteboard.sxe.records;

import saros.whiteboard.sxe.constants.NodeType;
import saros.whiteboard.sxe.constants.RecordEntry;
import saros.whiteboard.sxe.constants.RecordType;
import saros.whiteboard.sxe.exceptions.CommittedRecordException;
import saros.whiteboard.sxe.records.serializable.RecordDataObject;
import saros.whiteboard.sxe.records.serializable.SetRecordDataObject;

/**
 * A record that can change the target {@link NodeRecord}'s mutable fields.
 *
 * <p>If the version is left free it is automatically set to target.getVersion()+1 when applying.
 * This helps to ensure that locally created set-records can always be applied.
 *
 * @author jurke
 */
public class SetRecord extends AbstractRecord {

  protected NodeRecord target;
  protected int version = -1;

  // to set
  private String chdata;
  private ElementRecord parentToChange;
  private Float primaryWeight;
  private Boolean setVisible;

  private String lastModifiedBy;

  /** Constructor for locally created set records */
  public SetRecord(NodeRecord target) {
    this.target = target;
  }

  /** Constructor for remote set records */
  public SetRecord(NodeRecord target, int version) {
    this.target = target;
    this.version = version;
  }

  @Override
  public RecordType getRecordType() {
    return RecordType.SET;
  }

  public String getChdata() {
    return chdata;
  }

  public void setChdata(String chdata) {
    this.chdata = chdata;
  }

  @Override
  public boolean apply(DocumentRecord document) {
    if (!target.isCommitted()) return false;
    if (!changesTargetState()) return false;
    // Note: if version not set, it's a local record that should always
    // apply
    if (version == -1) version = getTarget().getVersion() + 1;
    return getTarget().applySetRecord(this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("set ");
    sb.append("target=" + getTarget().getRid());
    sb.append("(v" + getTarget().getVersion() + ")");

    sb.append(" name=" + getTarget().getName());
    if (getParentToChange() != null) sb.append(" parent=" + getParentToChange().getRid());
    if (getPrimaryWeight() != null) sb.append(" primary-weight=" + getPrimaryWeight());
    if (getChdata() != null) sb.append(" chdata=" + getChdata());
    if (setVisible != null) sb.append(" visible=" + setVisible);
    if (version != -1) sb.append(" version=" + version);
    else sb.append(" version=(current)");
    return sb.toString();
  }

  @Override
  public boolean isCommitted() {
    if (getTarget() == null) return false;
    return getTarget().getSetRecords().contains(this);
  }

  @Override
  public RecordDataObject getRecordDataObject() {
    RecordDataObject rdo = new SetRecordDataObject();
    rdo.putValue(RecordEntry.TARGET, getTarget().getRid());
    rdo.putValue(RecordEntry.VERSION, getVersion());
    rdo.putValue(RecordEntry.VISIBLE, getSetVisibilityTo());
    rdo.putValue(RecordEntry.CHDATA, getChdata());
    rdo.putValue(RecordEntry.PRIMARY_WEIGHT, getPrimaryWeight());
    if (getParentToChange() != null) rdo.putValue(RecordEntry.PARENT, getParentToChange().getRid());
    return rdo;
  }

  public ElementRecord getParentToChange() {
    return parentToChange;
  }

  public void setParentToChange(ElementRecord parentToChange) {
    this.parentToChange = parentToChange;
  }

  public Float getPrimaryWeight() {
    return primaryWeight;
  }

  public void setPrimaryWeight(Float primaryWeight) {
    this.primaryWeight = primaryWeight;
  }

  @Override
  public NodeRecord getTarget() {
    return target;
  }

  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    if (this.isCommitted()) throw new CommittedRecordException();
    this.version = version;
  }

  /** @return the difference between this and the target version (will be one if it can apply) */
  public int getVersionDifference() {
    if (version == -1) return 1;
    return version - target.getVersion();
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /**
   * Utility for local records whether they change anything and should be applied
   *
   * @return whether the target changes applying this set record
   */
  public boolean changesTargetState() {
    if (chdata != null
        && target instanceof AttributeRecord
        && !chdata.equals(((AttributeRecord) target).getChdata())) return true;
    if (parentToChange != null && !parentToChange.equals(target.getParent())) return true;
    if (primaryWeight != null && !primaryWeight.equals(target.getPrimaryWeight())) return true;
    if (setVisible != null && !setVisible.equals(target.isVisible())) return true;
    /*
     * Remote records always must change something if not contained, even if
     * it is the version only.
     */
    if (version != -1) return true;
    return false;
  }

  /** Utility method to distinguish whether this record sets all fields */
  public boolean setsAllMutableFields() {
    if (chdata == null && target.getNodeType() == NodeType.ATTR) return false;
    if (parentToChange == null) return false;
    if (primaryWeight == null) return false;
    if (setVisible == null) return false;
    return true;
  }

  /** Utility method to fill all this local fields that are empty from the provieded SetRecord */
  public void fillEmptyMutableFieldsFrom(SetRecord previous) {
    if (parentToChange == null && previous.getParentToChange() != null) {
      setParentToChange(previous.getParentToChange());
    }
    if (primaryWeight == null && previous.getPrimaryWeight() != null) {
      setPrimaryWeight(previous.getPrimaryWeight());
    }
    if (chdata == null && previous.getChdata() != null) {
      setChdata(previous.getChdata());
    }
    if (setVisible == null && previous.getSetVisibilityTo() != null) {
      setSetVisibilityTo(previous.getSetVisibilityTo());
    }
    if (version == -1) version = previous.getVersion();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SetRecord) return equals((SetRecord) o);
    return false;
  }

  public boolean equals(SetRecord r) {
    if (r.getVersion() != version) return false;

    if (sender == null && r.getSender() != null) return false;

    if (sender != null) if (!(sender.equals(r.getSender()))) return false;

    if (!target.equals(r.getTarget())) return false;

    if (chdata != null) if (!(chdata.equals(r.getChdata()))) return false;

    if (parentToChange != null) if (!(parentToChange.equals(r.getParentToChange()))) return false;

    if (primaryWeight != null) if (!(primaryWeight.equals(r.getPrimaryWeight()))) return false;
    if (setVisible != null) if (!(setVisible.equals(r.getSetVisibilityTo()))) return false;

    return true;
  }

  @Override
  public boolean canApply() {
    try {

      if (!getTarget().getDocumentRecord().contains(target)) return false;
      if (target.getSetRecords().contains(this)) return false;
      if (version == -1) return true;
      else return version == target.getVersion() + 1;

    } catch (Exception e) {
      return false;
    }
  }

  public Boolean getSetVisibilityTo() {
    return setVisible;
  }

  public void setSetVisibilityTo(boolean setVisible) {
    this.setVisible = setVisible;
  }

  @Override
  public boolean isPartOfVisibleDocument() {
    return target.isPartOfVisibleDocument();
  }
}

package de.fu_berlin.inf.dpp.whiteboard.sxe.records;

import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordEntry;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable.RecordDataObject;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable.RemoveRecordDataObject;

/**
 * <p>
 * A record to remove the target from the SXE document.
 * </p>
 * 
 * <p>
 * Note that the {@link Record.recreate()} method might change the target RID.
 * To ensure proper behavior of isCommitted() and canApply() the target rid is
 * saved separately, accessible by getTargetRid(). <br/>
 * For SetRecords this does not apply at they are not undone/redone changing the
 * target RID.
 * </p>
 * 
 * 
 * @author jurke
 */
public class RemoveRecord implements IRecord {

	private final NodeRecord target;
	private String lastModifiedBy;

	public RemoveRecord(NodeRecord target) {
		this(target, target.getLastModifiedBy());
	}

	public RemoveRecord(NodeRecord target, String lastModifiedBy) {
		this.target = target;
		this.lastModifiedBy = lastModifiedBy;
	}

	@Override
	public RecordType getRecordType() {
		return RecordType.REMOVE;
	}

	@Override
	public NodeRecord getTarget() {
		return target;
	}

	@Override
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		if (this.lastModifiedBy != null)
			throw new IllegalArgumentException("Cannot change last modifier.");
		this.lastModifiedBy = lastModifiedBy;
	}

	@Override
	public boolean apply(DocumentRecord document) {
		if (target.getParent() == null)
			return false;
		if (!target.isCommitted())
			return false;
		if (document.isRemoved(getTarget().getRid()))
			return false;
		return target.applyRemoveRecord(this);
	}

	@Override
	public boolean isCommitted() {
		return target.getDocumentRecord().isRemoved(getTarget().getRid());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("remove ");
		sb.append("target=" + getTarget().getNodeType() + "("
				+ getTarget().getRid() + ")");
		return sb.toString();
	}

	@Override
	public RecordDataObject getRecordDataObject() {
		RecordDataObject rdo = new RemoveRecordDataObject();

		rdo.putValue(RecordEntry.TARGET, getTarget().getRid());
		rdo.putValue(RecordEntry.LAST_MODIFIED_BY, getLastModifiedBy());

		return rdo;
	}

	@Override
	public boolean canApply() {
		try {
			return getTarget().getDocumentRecord().contains(target);
		} catch (Exception e) {
			return false;
		}
	}
}

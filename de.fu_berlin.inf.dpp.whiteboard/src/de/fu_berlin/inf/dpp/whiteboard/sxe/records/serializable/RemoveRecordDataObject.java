package de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable;

import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordEntry;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions.MalformedRecordException;
import de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions.MissingRecordException;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.NodeRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.RemoveRecord;

public class RemoveRecordDataObject extends RecordDataObject {

	private static final long serialVersionUID = -4906755658226197928L;

	public RemoveRecordDataObject() {
		super(RecordType.REMOVE);
	}

	@Override
	public IRecord getIRecord(DocumentRecord document)
			throws MissingRecordException {

		String targetRid = getString(RecordEntry.TARGET);
		if (targetRid == null)
			throw new MalformedRecordException("target rid missing");
		NodeRecord target = document.getRecordById(targetRid);

		String lastModifiedBy = getString(RecordEntry.LAST_MODIFIED_BY);

		RemoveRecord record = new RemoveRecord(target, lastModifiedBy);

		return record;
	}

	@Override
	public boolean isAlreadyApplied(DocumentRecord document)
			throws MissingRecordException {
		if (document.isRemoved(getTargetRid()))
			return true;
		// test if target exists
		document.getRecordById(getTargetRid());
		return false;
	}

	@Override
	public String getTargetRid() {
		String rid = getString(RecordEntry.TARGET);
		if (rid == null)
			throw new MalformedRecordException(RecordEntry.TARGET);
		return rid;
	}

}

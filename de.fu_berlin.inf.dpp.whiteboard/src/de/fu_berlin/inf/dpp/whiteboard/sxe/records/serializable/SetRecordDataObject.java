package de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordEntry;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions.MalformedRecordException;
import de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions.MissingRecordException;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.NodeRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.SetRecord;

public class SetRecordDataObject extends RecordDataObject {

	private static final long serialVersionUID = 5665050716333165567L;

	private static final Logger log = Logger
			.getLogger(SetRecordDataObject.class);

	public SetRecordDataObject() {
		super(RecordType.SET);
	}

	/**
	 * <p>
	 * Usually returns the respective SetRecord.
	 * </p>
	 * <p>
	 * However, a RemoveRecord is returned if the respective SetRecord would
	 * change the target's parent to a deleted record.</br>
	 * </p>
	 */
	/*
	 * TODO: A new-Record is returned if any of the target's parents was deleted
	 * concurrently but this set would move him out of the hierarchy. It will
	 * exactly return a reference to the record which was deleted by its
	 * parent's delete after applying the setRecord to it.
	 */
	@Override
	public IRecord getIRecord(DocumentRecord document)
			throws MissingRecordException {
		String tmp;

		tmp = getString(RecordEntry.TARGET);
		if (tmp == null)
			throw new MalformedRecordException("target rid missing");

		NodeRecord target = document.getRecordById(tmp);
		int version = getInt(RecordEntry.VERSION);

		ElementRecord parent = null;

		tmp = getString(RecordEntry.PARENT);

		if (tmp != null) {
			parent = document.getElementRecordById(tmp);
		}

		SetRecord record = new SetRecord(target, version);

		Boolean visible = getBoolean(RecordEntry.VISIBLE);
		if (visible != null)
			record.setSetVisibilityTo(visible);

		tmp = getString(RecordEntry.CHDATA);
		if (tmp != null)
			record.setChdata(tmp);

		if (parent != null)
			record.setParentToChange(parent);

		Float pw = getFloat(RecordEntry.PRIMARY_WEIGHT);
		if (pw != null)
			record.setPrimaryWeight(pw);

		return record;
	}

	@Override
	public boolean isAlreadyApplied(DocumentRecord document)
			throws MissingRecordException {
		NodeRecord record = document.getRecordById(getTargetRid());
		IRecord setRecord = getIRecord(document);
		return record.getSetRecords().contains(setRecord);
	}

	@Override
	public String getTargetRid() {
		String rid = getString(RecordEntry.TARGET);
		if (rid == null)
			throw new MalformedRecordException(RecordEntry.TARGET);
		return rid;
	}

}

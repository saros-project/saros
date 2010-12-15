package de.fu_berlin.inf.dpp.whiteboard.gef.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import de.fu_berlin.inf.dpp.whiteboard.gef.model.LayoutElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.RemoveRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.util.HierarchicalRecordSet;

/**
 * <p>
 * This command allows to delete a arbitrary group of <code>ElementRecord</code>
 * s by only creating <code>RemoveRecord</code> for the top-most elements.
 * </p>
 * 
 * <p>
 * To undo a delete, a record is "recreated" by means of the
 * <code>recreate()</code> method. Thus the references in the
 * <code>CommandStack</code> may remain but to the peer another RID (thus
 * another record) will be sent.
 * </p>
 * 
 * @author jurke
 * 
 */
public class DeleteRecordsCommand extends SXECommand {

	/** all records to delete */
	private HierarchicalRecordSet recordSet = new HierarchicalRecordSet();
	/** Maps records to their previous parents */
	private LinkedHashMap<ElementRecord, ElementRecord> recordMap = new LinkedHashMap<ElementRecord, ElementRecord>();
	private DocumentRecord documentRecord;

	public void addRecordToDelete(ElementRecord record) {
		if (record.getParent() == null)
			return;
		if (documentRecord == null)
			documentRecord = record.getDocumentRecord();
		recordSet.insertRecord(record);
	}

	@Override
	protected boolean canExecuteSXECommand() {
		if (recordSet.getRootRecords().isEmpty())
			return false;
		Iterator<ElementRecord> it = recordSet.getRootRecords().iterator();

		// remove records that are already removed
		while (it.hasNext()) {
			if (!it.next().isCommitted())
				it.remove();
		}

		return !recordSet.getRootRecords().isEmpty();
	}

	@Override
	public List<IRecord> getRecords() {
		List<IRecord> records = new LinkedList<IRecord>();

		recordMap.clear();

		for (ElementRecord er : recordSet.getRootRecords()) {
			recordMap.put(er, er.getParent());
			records.add(new RemoveRecord(er));
		}

		return records;
	}

	@Override
	public List<IRecord> getUndoRecords() {

		List<IRecord> records = new ArrayList<IRecord>();

		for (Entry<ElementRecord, ElementRecord> e : recordMap.entrySet()) {
			// first achieve the records from the tree in the command stack
			records.add(e.getKey());
			records.addAll(e.getKey().getAllDescendantNodes());
			/*
			 * then recreate - clears the child references to ensure proper
			 * applying.
			 */
			e.getKey().recreate(e.getValue(), true);
		}

		return records;
	}

	@Override
	public DocumentRecord getDocumentRecord() {
		return documentRecord;
	}

	@Override
	protected boolean canUndoSXECommand() {
		LayoutElementRecord parent;

		if (recordMap.isEmpty())
			return false;

		try {
			for (Entry<ElementRecord, ElementRecord> e : recordMap.entrySet()) {
				parent = (LayoutElementRecord) e.getValue();
				if (!parent.isCommitted())
					return false;
				if (!parent.isComposite())
					return false;
			}
		} catch (ClassCastException e) {
			return false;
		}

		return true;
	}

	@Override
	public void dispose() {
		super.dispose();
		recordSet.clear();
		recordSet = null;
		recordMap.clear();
		recordMap = null;
		documentRecord = null;
	}

	void setDocumentRecord(DocumentRecord document) {
		this.documentRecord = document;
	}

}

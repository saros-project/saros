package de.fu_berlin.inf.dpp.whiteboard.gef.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ui.actions.Clipboard;

import de.fu_berlin.inf.dpp.whiteboard.gef.model.LayoutElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.util.LayoutUtils;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.AttributeRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.NodeRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.RemoveRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.util.AttributeSet;

/**
 * <p>
 * Achieves a list of LayoutElementRecord from the Clipboard. The list should
 * not contain children of any other contained record.
 * </p>
 * 
 * <p>
 * For every execution, the records of this list is copied deeply and the
 * position (all concerning attributes) are shifted a certain amount to the
 * right bottom to give feedback about successful execution.</br>
 * 
 * Note, that copies inserted on the same place would just hide the originals,
 * making it difficult to see any effect.
 * </p>
 * 
 * <p>
 * An undo corresponds to a delete, thus a redo to a undo-delete. To undo a
 * delete, a record is "recreated" by means of the <code>recreate()</code>
 * method. Thus the references in the <code>CommandStack</code> may remain but
 * to the peer another RID (thus another record) will be sent.</br>
 * </p>
 * 
 * 
 * @author jurke
 * 
 */
public class AlternatePasteRecordCommand extends SXECommand {
	private static Logger log = Logger.getLogger(AlternatePasteRecordCommand.class);

	public static final int SHIFT_FOR_COPY = 50;

	/** the copies in the clipboard */
	private List<LayoutElementRecord> clonedTopRecords;
	/** the locally created records with shifted layou attributes */
	private List<IRecord> recordsToApply;
	/** the remove records to unde */
	private List<IRecord> undoRecords;
	/** the map of elements to their parents to redo (undo delete) */
	private LinkedHashMap<ElementRecord, ElementRecord> recordMap = new LinkedHashMap<ElementRecord, ElementRecord>();

	private final int shiftCount;

	/**
	 * 
	 * @param shiftCount
	 *            how many times the nodes have been pasted already
	 */
	public AlternatePasteRecordCommand(int shiftCount) {
		this.shiftCount = shiftCount;
	}

	@Override
	public List<IRecord> getRecords() {
		return recordsToApply;
	}

	@Override
	public List<IRecord> getUndoRecords() {
		recordMap = new LinkedHashMap<ElementRecord, ElementRecord>();

		for (IRecord r : undoRecords) {
			recordMap.put((ElementRecord) r.getTarget(), r.getTarget()
					.getParent());
		}
		return undoRecords;
	}

	@Override
	public List<IRecord> getRedoRecords() {
		// recordsToApply.clear();
		List<IRecord> records = new ArrayList<IRecord>();
		undoRecords = new LinkedList<IRecord>();

		for (Entry<ElementRecord, ElementRecord> e : recordMap.entrySet()) {
			// child does not exist but parent exists
			if (!e.getKey().isCommitted() && e.getValue().isCommitted()) {
				records.add(e.getKey());
				records.addAll(e.getKey().getAllDescendantNodes());
				e.getKey().recreate(e.getValue(), true);
				undoRecords.add(new RemoveRecord(e.getKey()));
			}
		}

		return records;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DocumentRecord getDocumentRecord() {
		clonedTopRecords = (List<LayoutElementRecord>) Clipboard.getDefault()
				.getContents();
		if (clonedTopRecords == null || clonedTopRecords.isEmpty())
			return null;
		return ((NodeRecord) clonedTopRecords.get(0)).getDocumentRecord();
	}

	/*
	 * on first call it instantiates the records to create (the shifted clones)
	 * and the undo
	 */
	@Override
	protected boolean canExecuteSXECommand() {
		try {
			if (recordsToApply == null) {
				undoRecords = new LinkedList<IRecord>();
				recordsToApply = new LinkedList<IRecord>();
				LayoutElementRecord copy;
				for (LayoutElementRecord er : clonedTopRecords) {
					copy = (LayoutElementRecord) er.getCopy();
					addElement(er, copy, er.getParent());
					undoRecords.add(new RemoveRecord(copy));
				}
			}
			if (recordsToApply.isEmpty())
				return false;
			return true;
		} catch (Exception e) {
			log.debug("Cannot paste because of " + e.getMessage());
		}
		return false;
	}

	@Override
	protected boolean canUndoSXECommand() {

		Iterator<IRecord> it = undoRecords.iterator();

		IRecord tmp;
		while (it.hasNext()) {
			tmp = it.next();
			if (!tmp.canApply())
				it.remove();
		}

		return !undoRecords.isEmpty();
	}

	protected List<NodeRecord> createCopyRecords(LayoutElementRecord toCopy) {
		Rectangle layout = toCopy.getLayout();
		LayoutElementRecord parent = (LayoutElementRecord) toCopy.getParent();
		parent = LayoutUtils.translateToAndGetRoot(layout, parent);

		LayoutElementRecord copy = (LayoutElementRecord) toCopy.getCopy(true);
		copy.setParent(parent);

		AttributeRecord attr;
		AttributeRecord copysAttr;

		AttributeSet attributes = copy.getAttributes();

		for (IRecord r : copy.createLayoutRecords(layout, true)) {
			attr = (AttributeRecord) r;
			copysAttr = attributes.remove(attr.getName());
			attr.setPrimaryWeight(copysAttr.getPrimaryWeight());
			attributes.add(attr);
		}

		LinkedList<NodeRecord> records = new LinkedList<NodeRecord>();
		records.add(copy);
		records.addAll(copy.getAllDescendantNodes());

		for (NodeRecord r : records) {
			r.clear();
		}
		return records;
	}

	/**
	 * Adds recursively the copy to the parent. Furthermore creates and appends
	 * copies of all child nodes from orig to copy.
	 * 
	 * @param orig
	 * @param copy
	 * @param parent
	 */
	protected void addElement(LayoutElementRecord orig,
			LayoutElementRecord copy, ElementRecord parent) {
		copy.setParent(parent);
		recordsToApply.add(copy);
		addAttributeValues(orig, copy);

		ElementRecord childCopy;
		for (ElementRecord child : orig.getChildElements()) {
			childCopy = child.getCopy(true);
			childCopy.setParent(copy);
			recordsToApply.add(childCopy);
			recordsToApply.addAll(childCopy.getAllDescendantNodes());
		}
	}

	/**
	 * Appends copies of all the attributes of orig to clone but shifts position
	 * records due to not overlay the original.
	 * 
	 * @param orig
	 * @param clone
	 */
	protected void addAttributeValues(LayoutElementRecord orig,
			LayoutElementRecord clone) {
		Rectangle layout = orig.getLayout();
		layout.translate(shiftCount * SHIFT_FOR_COPY, shiftCount
				* SHIFT_FOR_COPY);

		List<String> layoutTags = new LinkedList<String>();
		AttributeRecord attr;

		for (IRecord r : orig.createLayoutRecords(layout, true)) {
			attr = (AttributeRecord) r;
			attr.setParent(clone);
			layoutTags.add(attr.getName());
			recordsToApply.add(attr);
		}

		for (AttributeRecord r : orig.getAttributes()) {
			if (!layoutTags.contains(r.getName())) {
				attr = r.getCopy();
				attr.setParent(clone);
				recordsToApply.add(attr);
			}
		}
	}

}

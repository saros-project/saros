package saros.whiteboard.gef.commands;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ui.actions.Clipboard;
import saros.whiteboard.gef.model.LayoutElementRecord;
import saros.whiteboard.gef.util.LayoutUtils;
import saros.whiteboard.sxe.records.AttributeRecord;
import saros.whiteboard.sxe.records.DocumentRecord;
import saros.whiteboard.sxe.records.ElementRecord;
import saros.whiteboard.sxe.records.IRecord;
import saros.whiteboard.sxe.records.NodeRecord;

/**
 * Achieves a list of LayoutElementRecord from the Clipboard. The list should be obtained by the
 * root records of a {@link HierarichalRecordList} and thus not contain any descendant of any other
 * contained element.
 *
 * <p>For the execution, the records and all their descendants are copied while the position (all
 * concerning layout attributes) are shifted <code>SHIFT_FOR_COPY</code> points to the right bottom
 * to give feedback about successful execution.</br>
 *
 * <p>Note, that copies inserted on the same place would just hide the originals, making it
 * difficult to see any effect.
 *
 * <p>An undo corresponds to a delete, a redo to an undo-delet. For better reuse a {@link
 * DeleteRecordsCommand} is used as member.
 *
 * @author jurke
 */
public class PasteRecordCommand extends SXECommand {

  public static final int SHIFT_FOR_COPY = 50;

  /** the copies in the clipboard */
  private List<LayoutElementRecord> clonedTopRecords;

  private DeleteRecordsCommand deleteCommand;

  private final int shiftCount;

  /** @param shiftCount how many times the nodes have been pasted already */
  public PasteRecordCommand(int shiftCount) {
    this.shiftCount = shiftCount;
  }

  /*
   * creates clones with descendants (createCopyRecords(LayoutElementRecord))
   * and adds the copied root to the wrapped delete command
   */
  @Override
  public List<IRecord> getRecords() {
    List<IRecord> records = new LinkedList<IRecord>();
    List<IRecord> copies;
    deleteCommand = new DeleteRecordsCommand();
    deleteCommand.setDocumentRecord(getDocumentRecord());

    for (LayoutElementRecord e : clonedTopRecords) {
      copies = createCopyRecords(e);
      deleteCommand.addRecordToDelete((ElementRecord) copies.get(0));
      records.addAll(copies);
    }

    return records;
  }

  /*
   * wraps a delete command execution
   */
  @Override
  public List<IRecord> getUndoRecords() {
    return deleteCommand.getRecords();
  }

  /*
   * wraps a delete command undo
   */
  @Override
  public List<IRecord> getRedoRecords() {
    return deleteCommand.getUndoRecords();
  }

  @SuppressWarnings("unchecked")
  @Override
  public DocumentRecord getDocumentRecord() {
    clonedTopRecords = (List<LayoutElementRecord>) Clipboard.getDefault().getContents();
    if (clonedTopRecords == null || clonedTopRecords.isEmpty()) return null;
    return ((NodeRecord) clonedTopRecords.get(0)).getDocumentRecord();
  }

  @Override
  protected boolean canExecuteSXECommand() {
    if (deleteCommand == null) return !clonedTopRecords.isEmpty();
    return deleteCommand.canUndo();
  }

  @Override
  protected boolean canUndoSXECommand() {
    return deleteCommand.canExecute();
  }

  /**
   * Short helper that returns that checks whether the passed AttributeRecord <code>attribute</code>
   * is a layout record (contained by the layout records) if so, it will return the layout record
   * els the passed AttributeRecord itself.
   *
   * @param attribute
   * @param layout layout attributes
   * @return the attribute if it's name is not beyond the AttributeRecords of the layout attributes
   *     else the respective layout Attribute.
   */
  protected IRecord mergeAttribute(AttributeRecord attribute, List<IRecord> layout) {
    Iterator<IRecord> it = layout.iterator();
    IRecord tmp;
    while (it.hasNext()) {
      tmp = it.next();
      if (attribute.getName().equals(tmp.getTarget().getName())) {
        it.remove();
        return tmp;
      }
    }
    return attribute;
  }

  /**
   * Returns the layout records plus all the records of the AttributeSet that are no layout records.
   */
  protected List<IRecord> getMergedAttributes(
      List<AttributeRecord> attributes, List<IRecord> layout) {
    List<IRecord> records = new LinkedList<IRecord>();
    for (AttributeRecord attr : attributes) {
      records.add(mergeAttribute(attr, layout));
    }
    records.addAll(layout);
    return records;
  }

  /**
   * Copies the passed LayoutElementRecord, shifts the layout records and copies all other
   * descendants (attributes, child elements and their child nodes) to finally return them all as a
   * single list.
   *
   * @param toCopy
   * @return a list of copies of the passes record plus copies if its descendants
   */
  protected List<IRecord> createCopyRecords(LayoutElementRecord toCopy) {
    Rectangle layout = toCopy.getLayout();
    // add copies to root only
    LayoutElementRecord parent = (LayoutElementRecord) toCopy.getParent();
    parent = LayoutUtils.translateToAndGetRoot(layout, parent);

    // shift layout
    layout.translate(shiftCount * SHIFT_FOR_COPY, shiftCount * SHIFT_FOR_COPY);

    // copy
    LayoutElementRecord copy = (LayoutElementRecord) toCopy.getCopy();
    copy.setParent(parent);

    List<IRecord> records = new LinkedList<IRecord>();

    List<IRecord> layoutAttributes = toCopy.createLayoutRecords(layout, true);
    for (IRecord r : layoutAttributes) {
      ((AttributeRecord) r).setParent(copy);
    }

    // add all attributes from the copy but the layout ones
    records.add(copy);
    records.addAll(getMergedAttributes(toCopy.getVisibleAttributes(), layoutAttributes));

    // add all other sub nodes
    ElementRecord childCopy;
    for (ElementRecord er : toCopy.getVisibleChildElements()) {
      childCopy = er.getCopy();
      childCopy.setParent(copy);
      records.add(childCopy);
      records.addAll(er.getCopiedSubtreeRecords(childCopy));
    }

    return records;
  }

  @Override
  public void dispose() {
    super.dispose();
    clonedTopRecords = null;
    deleteCommand.dispose();
    deleteCommand = null;
  }
}

package saros.whiteboard.gef.commands;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.draw2d.geometry.Rectangle;
import saros.whiteboard.gef.model.LayoutElementRecord;
import saros.whiteboard.sxe.records.DocumentRecord;
import saros.whiteboard.sxe.records.IRecord;

/**
 * A command to remove an <code>ElementRecord</code> from its parent to append it as last child to
 * another one.
 *
 * @author jurke
 */
public class ElementRecordAddCommand extends SXECommand {
  private LayoutElementRecord parent;
  private LayoutElementRecord oldParent;
  private LayoutElementRecord record;
  private Rectangle layout;
  private Rectangle oldLayout;
  private Float oldPrimaryWeight;

  public ElementRecordAddCommand() {
    super();
    parent = null;
    record = null;
  }

  public void setElementModel(Object s) {
    if (s instanceof LayoutElementRecord) this.record = (LayoutElementRecord) s;
  }

  public void setParent(Object e) {
    if (e instanceof LayoutElementRecord) this.parent = (LayoutElementRecord) e;
  }

  public void setLayout(Rectangle constraint) {
    this.layout = constraint;
  }

  public Rectangle getConstraint() {
    return layout;
  }

  @Override
  public List<IRecord> getRecords() {
    List<IRecord> records = new LinkedList<IRecord>();

    oldParent = (LayoutElementRecord) record.getParent();
    oldLayout = record.getLayout();
    oldPrimaryWeight = record.getPrimaryWeight();

    records.add(record.createMoveRecord(parent));
    records.addAll(record.getChangeLayoutRecords(layout));

    return records;
  }

  @Override
  public List<IRecord> getUndoRecords() {
    List<IRecord> records = new LinkedList<IRecord>();

    records.add(record.createMoveRecord(oldParent, oldPrimaryWeight));
    records.addAll(record.getChangeLayoutRecords(oldLayout));

    return records;
  }

  @Override
  public DocumentRecord getDocumentRecord() {
    return record.getDocumentRecord();
  }

  @Override
  protected boolean canExecuteSXECommand() {
    if (record == null || parent == null || layout == null) return false;
    // this is important: don't allow a non well-formed XML document
    if (record.isCircularRelationship(parent)) return false;

    return record.isVisible() && parent.isPartOfVisibleDocument();
  }

  @Override
  protected boolean canUndoSXECommand() {
    if (record == null || oldParent == null || oldLayout == null) return false;
    // this is important: don't allow a non well-formed XML document
    if (record.isCircularRelationship(oldParent)) return false;

    return record.isVisible() && oldParent.isPartOfVisibleDocument();
  }

  @Override
  public void dispose() {
    super.dispose();
    parent = null;
    oldParent = null;
    record = null;
    layout = null;
    oldLayout = null;
    oldPrimaryWeight = null;
  }
}

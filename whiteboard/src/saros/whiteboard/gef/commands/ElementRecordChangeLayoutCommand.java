package saros.whiteboard.gef.commands;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.draw2d.geometry.Rectangle;
import saros.whiteboard.gef.model.LayoutElementRecord;
import saros.whiteboard.sxe.records.DocumentRecord;
import saros.whiteboard.sxe.records.IRecord;

/**
 * A command to change the position and size of an <code>ElementRecord</code> by setting its
 * attributes.
 *
 * @author jurke
 */
public class ElementRecordChangeLayoutCommand extends SXECommand {

  private LayoutElementRecord record;
  private Rectangle layout;
  private Rectangle oldLayout;

  public void setConstraint(Rectangle rect) {
    this.layout = rect;
  }

  public void setModel(Object model) {
    this.record = (LayoutElementRecord) model;
  }

  @Override
  public List<IRecord> getRecords() {
    List<IRecord> records = new LinkedList<IRecord>();
    oldLayout = record.getLayout();
    records.addAll(record.getChangeLayoutRecords(layout));

    return records;
  }

  @Override
  public List<IRecord> getUndoRecords() {
    List<IRecord> records = new LinkedList<IRecord>();
    records.addAll(record.getChangeLayoutRecords(oldLayout));

    return records;
  }

  @Override
  public DocumentRecord getDocumentRecord() {
    return record.getDocumentRecord();
  }

  @Override
  protected boolean canExecuteSXECommand() {
    if (record == null || layout == null) return false;
    return record.isPartOfVisibleDocument();
  }

  @Override
  protected boolean canUndoSXECommand() {
    if (record == null || oldLayout == null) return false;
    return record.isPartOfVisibleDocument();
  }

  @Override
  public void dispose() {
    super.dispose();
    layout = null;
    oldLayout = null;
    record = null;
  }
}

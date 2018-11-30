package de.fu_berlin.inf.dpp.whiteboard.gef.commands;

import de.fu_berlin.inf.dpp.whiteboard.gef.model.LayoutElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import java.util.List;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Default command to create arbitrary <code>ElementRecord</code> instances with a certain position
 * and size only.
 *
 * @author jurke
 */
public class ElementRecordCreateCommand extends AbstractElementRecordCreateCommand {

  private Rectangle layout;

  public void setLayout(Rectangle layout) {
    this.layout = layout;
  }

  /** Overridden to be public */
  @Override
  public void setChildName(String name) {
    super.setChildName(name);
  }

  public String getChildName() {
    return this.newChildName;
  }

  @Override
  protected List<IRecord> getAttributeRecords(LayoutElementRecord child) {
    return child.getChangeLayoutRecords(layout);
  }

  @Override
  protected boolean canExecuteSXECommand() {
    if (layout == null) return false;
    return super.canExecuteSXECommand();
  }

  @Override
  public void dispose() {
    super.dispose();
    layout = null;
  }
}

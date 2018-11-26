package de.fu_berlin.inf.dpp.whiteboard.gef.commands;

import de.fu_berlin.inf.dpp.whiteboard.gef.model.LayoutElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGPolylineRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import java.util.List;
import org.apache.batik.util.SVGConstants;
import org.eclipse.draw2d.geometry.PointList;

/**
 * A specialized version of a create command for point lists as location and size wouldn't suffice.
 *
 * @author jurke
 */
public class PolylineRecordCreateCommand extends AbstractElementRecordCreateCommand {

  private PointList points;

  public PolylineRecordCreateCommand() {
    setChildName(SVGConstants.SVG_POLYLINE_TAG);
  }

  public void setPointList(PointList r) {
    points = r;
  }

  @Override
  protected List<IRecord> getAttributeRecords(LayoutElementRecord child) {
    return ((SVGPolylineRecord) getNewChild()).createPointsRecord(points);
  }

  @Override
  protected boolean canExecuteSXECommand() {
    if (points == null) return false;
    return super.canExecuteSXECommand();
  }

  @Override
  public void dispose() {
    super.dispose();
    points.removeAllPoints();
    points = null;
  }
}

package saros.whiteboard.gef.commands;

import java.util.List;
import org.apache.batik.util.SVGConstants;
import org.eclipse.draw2d.geometry.PointList;
import saros.whiteboard.gef.model.LayoutElementRecord;
import saros.whiteboard.gef.model.SVGPolylineRecord;
import saros.whiteboard.sxe.records.IRecord;

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

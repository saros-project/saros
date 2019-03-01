package saros.whiteboard.gef.request;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.gef.requests.CreateRequest;
import saros.whiteboard.gef.editpolicy.XYLayoutWithFreehandEditPolicy;

/**
 * Simple request class to create a line
 *
 * @author jurke
 */
public class CreatePointlistRequest extends CreateRequest {

  private PointList points = null;

  public CreatePointlistRequest() {
    setType(XYLayoutWithFreehandEditPolicy.REQ_CREATE_POINTLIST);
  }

  public PointList getPoints() {
    return points;
  }

  /**
   * Updates the last point in the pointlist
   *
   * @param p
   */
  public void updateEndPoint(Point p) {
    if (points == null) points = new PointList();

    if (points.size() > 1) {
      points.setPoint(p, points.size() - 1);
    } else points.addPoint(p);
  }

  /**
   * Updates the last point in the pointlist
   *
   * @param p
   */
  public void updateArrowPoint(Point p) {
    if (points == null) {
      points = new PointList();
      points.addPoint(p);
      return;
    }

    Point p1 = points.getPoint(0);
    Point p2 = p;
    points.removeAllPoints();

    double width = p2.x - p1.x;
    double height = p2.y - p1.y;

    double arrow_end_x = p2.x;
    double arrow_end_y = p2.y;

    double angle = -Math.atan2(height, width) + Math.PI / 2.0;

    // length of the arrow
    double length = Math.sqrt(width * width + height * height);

    // length of the head
    double minLengthArrowHead = 5d;
    double maxLengthArrowHead = 15d;
    double lengthArrowHead =
        Math.max(minLengthArrowHead, Math.min(maxLengthArrowHead, length / 8.0));

    // arrow head points calculation
    double arrow_left_x = arrow_end_x - lengthArrowHead * Math.sin(angle - Math.PI / 4.0);
    double arrow_left_y = arrow_end_y - lengthArrowHead * Math.cos(angle - Math.PI / 4.0);
    double arrow_right_x = arrow_end_x - lengthArrowHead * Math.sin(angle + Math.PI / 4.0);
    double arrow_right_y = arrow_end_y - lengthArrowHead * Math.cos(angle + Math.PI / 4.0);

    Point a_left = new Point((int) arrow_left_x, (int) arrow_left_y);
    Point a_right = new Point((int) arrow_right_x, (int) arrow_right_y);

    // creation of the arrow point list model
    points.addPoint(p1);
    points.addPoint(p2);
    points.addPoint(a_left);
    points.addPoint(p2);
    points.addPoint(a_right);
  }

  public void addPoint(Point p) {
    if (points == null) points = new PointList();
    points.addPoint(p);
  }

  @Override
  public Point getLocation() {
    if (points == null) return null;
    return points.getBounds().getLocation();
  }

  @Override
  public void setLocation(Point location) {
    clear();
    addPoint(location);
  }

  @Override
  public Dimension getSize() {
    return points.getBounds().getSize();
  }

  public void clear() {
    points = null;
  }
}

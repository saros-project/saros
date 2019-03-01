package saros.whiteboard.gef.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.util.SVGConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import saros.whiteboard.gef.util.ColorUtils;
import saros.whiteboard.sxe.records.DocumentRecord;
import saros.whiteboard.sxe.records.IRecord;

/**
 * This class maintains a cache of the SVG points attribute. Parsing the String on every access
 * would be pretty expensive because this attribute quickly grows to some thousand points.
 *
 * @author jurke
 */
public class SVGPolylineRecord extends LayoutElementRecord {

  protected PointList points = null;
  protected String rawPoints = null;

  public SVGPolylineRecord(DocumentRecord documentRecord) {
    super(documentRecord);
    setName(SVGConstants.SVG_POLYLINE_TAG);
  }

  @Override
  public Rectangle getLayout() {
    generatePointList();
    return points.getBounds().getCopy();
  }

  /**
   * Returns the record to add the points attribute to this ElementRecord.
   *
   * @param points
   * @return the points record
   */
  public List<IRecord> createPointsRecord(PointList points) {
    List<IRecord> l = new LinkedList<IRecord>();
    String newPoints = generatePointsAttribute(points);
    l.add(createNewOrSetAttributeRecord(null, SVGConstants.SVG_POINTS_ATTRIBUTE, newPoints, false));

    l.add(
        createNewOrSetAttributeRecord(
            null,
            SVGConstants.SVG_COLOR_ATTRIBUTE,
            String.valueOf(ColorUtils.getForegroundColor()),
            false));
    return l;
  }

  protected static boolean isOdd(int x) {
    return (x & 1) == 1;
  }

  public PointList getPoints() {
    generatePointList();
    return points;
  }

  /** initializes the local PointList cache */
  protected void generatePointList() {
    if (points != null)
      if (rawPoints == getAttributeValue(SVGConstants.SVG_POINTS_ATTRIBUTE)) return;

    points = new PointList();
    rawPoints = getAttributeValue(SVGConstants.SVG_POINTS_ATTRIBUTE);
    if (rawPoints == null) return;

    String[] rawList = rawPoints.split(" ");
    String[] tmpPoint = null;
    for (String s : rawList) {
      tmpPoint = s.split(",");
      if (tmpPoint.length > 1)
        points.addPoint(Integer.parseInt(tmpPoint[0]), Integer.parseInt(tmpPoint[1]));
    }
  }

  protected static int scaleMaintainPosition(int anchor, int x, double scale) {
    if (scale == 1) return x;
    x -= anchor;
    x = (int) Math.floor(x * scale);
    x += anchor;
    return x;
  }

  protected static PointList performScale(PointList points, double sx, double sy) {
    Point anchor = points.getBounds().getLocation();
    int[] rawPoints = points.toIntArray();
    PointList ps = new PointList(points.size());
    int x, y;

    for (int i = 0; i < rawPoints.length; i += 2) {
      x = scaleMaintainPosition(anchor.x, rawPoints[i], sx);
      y = scaleMaintainPosition(anchor.y, rawPoints[i + 1], sy);
      ps.addPoint(x, y);
    }
    return ps;
  }

  /**
   * @param points
   * @return the PointList as SVG point attribute String
   */
  protected static String generatePointsAttribute(PointList points) {
    int[] rawInts = points.toIntArray();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < rawInts.length; i++) {
      sb.append(rawInts[i]);
      if (isOdd(i)) sb.append(" ");
      else sb.append(",");
    }
    return sb.toString();
  }

  /**
   * Returns the default points for empty PointList (when using drag and drop)
   *
   * @param bounds
   * @return
   */
  protected PointList getDefaultPoints(Rectangle bounds) {
    // let's add a vertical line if no points provided (i.e. drag and drop)
    PointList points = new PointList();
    points.addPoint(bounds.x, bounds.y);
    points.addPoint(bounds.x, bounds.y + bounds.height);
    return points;
  }

  /**
   * Returns a AttributeRecord or SetRecord to change the points attribute respectively the provided
   * layout rectangle
   */
  @Override
  public List<IRecord> createLayoutRecords(Rectangle layout, boolean onlyCreateNewRecords) {
    generatePointList();

    PointList points = this.points.getCopy();
    if (points.size() == 0) points.addAll(getDefaultPoints(layout));

    Rectangle bounds = points.getBounds();
    // calculate difference
    Dimension delta = bounds.getLocation().getDifference(layout.getLocation()).negate();

    // perform translate
    points.translate(delta.width, delta.height);

    // calculate scale
    double sx = ((double) layout.width) / ((double) bounds.width);
    double sy = ((double) layout.height) / ((double) bounds.height);

    // perform scale
    points = performScale(points, sx, sy);

    String newPoints = generatePointsAttribute(points);
    List<IRecord> records = new ArrayList<IRecord>(1);
    records.add(
        createNewOrSetAttributeRecord(
            null, SVGConstants.SVG_POINTS_ATTRIBUTE, newPoints, onlyCreateNewRecords));
    records.add(
        createNewOrSetAttributeRecord(
            null,
            SVGConstants.SVG_COLOR_ATTRIBUTE,
            String.valueOf(ColorUtils.getForegroundColor()),
            onlyCreateNewRecords));
    return records;
  }

  @Override
  public Point getLocation() {
    generatePointList();
    return points.getBounds().getLocation();
  }

  @Override
  public Dimension getSize() {
    generatePointList();
    return points.getBounds().getSize();
  }
}

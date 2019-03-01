package saros.whiteboard.gef.editpolicy;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.AbstractPointListShape;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.swt.graphics.Color;
import saros.whiteboard.gef.request.CreateLineRequest;
import saros.whiteboard.gef.request.CreatePointlistRequest;
import saros.whiteboard.gef.request.CreateTextBoxRequest;
import saros.whiteboard.gef.util.ColorUtils;

/**
 * This edit policy extends the XYLayout by the freehand drawing feature
 *
 * @author jurke
 */
public abstract class XYLayoutWithFreehandEditPolicy extends XYLayoutEditPolicy {
  private static final Logger log = Logger.getLogger(XYLayoutWithFreehandEditPolicy.class);

  public static final String REQ_CREATE_POINTLIST = "create pointlist";
  public static final String REQ_CREATE_LINE = "create line";
  public static final String REQ_CREATE_ARROW = "create arrow";
  public static final String REQ_CREATE_TEXTBOX = "create textbox";
  /** */
  private static final int PIXEL_TOLERANCE = 3;

  /** feedback figure */
  protected AbstractPointListShape shape = null;

  /** Returns a PointListCreateCommand if the request corresponds to a point list create request. */
  @Override
  public Command getCommand(Request request) {
    if (REQ_CREATE_POINTLIST.equals(request.getType())
        || REQ_CREATE_LINE.equals(request.getType())) {
      return getCreatePointListCommand((CreatePointlistRequest) request);
    }
    if (REQ_CREATE_TEXTBOX.equals(request.getType()))
      return getCreateTextboxCommand((CreateTextBoxRequest) request);
    return super.getCommand(request);
  }

  protected abstract Command getCreateTextboxCommand(CreateTextBoxRequest request);

  /**
   * @param request
   * @return a command to create a new point list element (i.e. line)
   */
  protected abstract Command getCreatePointListCommand(CreatePointlistRequest request);

  /** Shows the feedback while creating a figure */
  @Override
  public void showTargetFeedback(Request request) {
    if (REQ_CREATE_POINTLIST.equals(request.getType()))
      showPointlistCreationFeedback((CreatePointlistRequest) request);
    else if (REQ_CREATE_LINE.equals(request.getType()))
      showLineCreationFeedback((CreateLineRequest) request);
    if (REQ_CREATE_TEXTBOX.equals(request.getType())) {
      showLayoutTargetFeedback(request);
      CreateRequest createReq = (CreateRequest) request;
      if (createReq.getSize() != null) {
        test(createReq);
      }
      // [FIXME] for annotation request implement a resize of the image
      // that fits to the bounds of the feedback layout
    }
    super.showTargetFeedback(request);
  }

  void test(CreateRequest r) {
    Point p = new Point(r.getLocation().getCopy());
    Dimension size = r.getSize().getCopy();
    Rectangle feedbackBounds = new Rectangle(p, size);
    IFigure feedback = getSizeOnDropFeedback(r);
    feedback.setSize(50, 50);
    feedback.setLocation(p);
    feedback.setBounds(feedbackBounds.expand(getCreationFeedbackOffset(r)));
  }

  /** @return a polyline figure, already being initialized to the feedback layer */
  protected AbstractPointListShape getPolylineFeedback() {
    if (shape == null) {
      shape = new Polyline();
      shape.setForegroundColor(new Color(null, ColorUtils.getRGBForegroundColor()));
      shape.setBackgroundColor(new Color(null, ColorUtils.getRGBBackgroundColor()));
      addFeedback(shape);
    }
    return shape;
  }

  /**
   * Structure parallel to {@link #createSizeOnDropFeedback}.</br>
   *
   * <p>Override to provide a custom feedback respectively the request, else the standard shape from
   * {@link #getPolylineFeedback()} will be used.
   *
   * @param request
   * @return
   */
  protected AbstractPointListShape createPolylineFeedback(CreatePointlistRequest request) {
    return null;
  }

  protected AbstractPointListShape getPolylineFeedback(CreatePointlistRequest request) {
    if (shape == null) shape = createPolylineFeedback(request);
    return getPolylineFeedback();
  }

  /**
   * Extends (or creates if applicable) the feedback and adds the last point.
   *
   * @param request
   */
  protected void showPointlistCreationFeedback(CreatePointlistRequest request) {
    Point p = new Point(request.getPoints().getLastPoint().getCopy());
    AbstractPointListShape feedback = getPolylineFeedback(request);

    /*
     * For efficiency reasons we don't use getPointListFor() and don't
     * normalize the feedback
     */
    feedback.translateToRelative(p);
    if (feedback.getPoints().size() == 0) {
      Point start = request.getPoints().getFirstPoint().getCopy();
      feedback.translateToRelative(start);
      feedback.addPoint(start);
    }
    feedback.addPoint(p);
  }

  /**
   * Extends (or creates if applicable) the feedback and adds the last point.
   *
   * @param request
   */
  protected void showLineCreationFeedback(CreatePointlistRequest request) {
    getPolylineFeedback(request).setPoints(request.getPoints());
  }

  @Override
  public EditPart getTargetEditPart(Request request) {
    if (REQ_CREATE_POINTLIST.equals(request.getType()) || REQ_CREATE_LINE.equals(request.getType()))
      return getHost();
    if (REQ_CREATE_TEXTBOX.equals(request.getType())) return getHost();
    return super.getTargetEditPart(request);
  }

  @Override
  public void eraseTargetFeedback(Request request) {
    if (REQ_CREATE_POINTLIST.equals(request.getType()) || REQ_CREATE_LINE.equals(request.getType()))
      erasePointlistCreationFeedback(request);
    if (REQ_CREATE_TEXTBOX.equals(request.getType())) {
      eraseLayoutTargetFeedback(request);
      eraseSizeOnDropFeedback(request);
    }
    super.eraseTargetFeedback(request);
  }

  protected void erasePointlistCreationFeedback(Request request) {
    removeFeedback(shape);
    shape = null;
  }

  /**
   * A simple mechanism to reduce the amount of points at expense of similarity between drawn and
   * resulting point list
   */
  protected static int getPixelTolerance() {
    return PIXEL_TOLERANCE;
  }

  /**
   * Tests if the three points are collinear
   *
   * <p>However, we use a tolerance value to reduce the amount of points further, especially useful
   * on diagonal lines (rounding issues) or when a lot of points are next to each other.</br>
   *
   * <p>But beware, higher values may result in choppy curves on high zoom levels.
   *
   * @param p1
   * @param p2
   * @param p3
   * @return whether the points are collinear
   */
  /*
   * From the straight line function y = m * x + b
   *
   * set in p1 and p2 m = (y2-y1) / (x2-x1) and b = y1 - m * x1. => y =
   * (y2-y1)/(x2-x1)*x + y1 - (y2-y1)/(x2-x1)*x1
   *
   * normalize and set in p3
   *
   * (x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1) == 0 if collinear
   *
   * In this case, 0 +/- tolerance
   */
  public static boolean collinear(int[] raw, int p1, int p2, int p3, int tolerance) {
    int res =
        (raw[p2] - raw[p1]) * (raw[p3 + 1] - raw[p1 + 1])
            - (raw[p3] - raw[p1]) * (raw[p2 + 1] - raw[p1 + 1]);
    return (res >= -tolerance && res <= tolerance);
  }

  /**
   * Removes doubled entries and collinear points.
   *
   * @param points
   * @return normalized point list
   */
  protected static PointList getNormalizedPointList(PointList points) {
    PointList ps = new PointList(points.size());
    int[] rawPoints = points.toIntArray();
    // don't do overhead for a small list of points
    if (points.size() < 20) {
      for (int i = 0; i < rawPoints.length; i += 2) ps.addPoint(rawPoints[i], rawPoints[i + 1]);
      return ps;
    }
    if (rawPoints.length == 0) return ps;
    ps.addPoint(rawPoints[0], rawPoints[1]);
    if (rawPoints.length == 2) return ps;
    ps.addPoint(rawPoints[2], rawPoints[3]);
    if (rawPoints.length == 4) {
      return ps;
    }

    int p1 = 0, p2 = 2, p3 = 4;

    while (true) {
      if (!collinear(rawPoints, p1, p2, p3, getPixelTolerance())) {
        ps.addPoint(rawPoints[p1], rawPoints[p1 + 1]);
        p1 = p2;
      } else {
        // even if collinear we have to check whether p1 and p3 are on
        // different sides
        if (rawPoints[p1] < rawPoints[p2] && rawPoints[p3] < rawPoints[p2]
            || rawPoints[p1] > rawPoints[p2] && rawPoints[p3] > rawPoints[p2]
            || rawPoints[p1 + 1] < rawPoints[p2 + 1] && rawPoints[p3 + 1] < rawPoints[p2 + 1]
            || rawPoints[p1 + 1] > rawPoints[p2 + 1] && rawPoints[p3 + 1] > rawPoints[p2 + 1]) {
          // if so, add the middle point and increment a first time
          ps.addPoint(rawPoints[p2], rawPoints[p2 + 1]);
          p1 = p3;
          p2 = p3;
          p3 += 2;
          if (p3 >= rawPoints.length) break;
        }
      }
      p2 = p3;
      p3 += 2;
      if (p3 >= rawPoints.length) break;
    }
    // If the last check was collinear (if and only if p2-p1>4) we still
    // have to add p1
    if (p2 - p1 > 4) {
      ps.addPoint(rawPoints[p1], rawPoints[p1 + 1]);
    }
    ps.addPoint(rawPoints[p2], rawPoints[p2 + 1]);

    return ps;
  }

  /**
   * Convenient method parallel to getConstraintFor() to translate and normalize the points from the
   * request
   *
   * @param request
   * @return the translated PointList from the request
   */
  protected PointList getPointListFor(CreatePointlistRequest request) {
    IFigure figure = getLayoutContainer();

    PointList points = getNormalizedPointList(request.getPoints());
    // PointList points = request.getPoints();
    figure.translateToRelative(points);
    figure.translateFromParent(points);
    points.translate(getLayoutOrigin().getNegated());

    return points;
  }
}

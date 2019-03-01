package saros.whiteboard.gef.editpolicy;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import junit.framework.Assert;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.junit.Before;
import org.junit.Test;

public class ScalingTest {

  String resizeDataMethodName = "recalculateResizeData";
  private static final String PERCENTAGE_HEIGHT = "percentageHeight";
  private static final String PERCENTAGE_WIDTH = "percentageWidth";
  ChangeBoundsRequest request;
  GraphicalEditPart child;
  EditPolicy resizableEditPolicy;
  Method recalculateResizeData;

  /*
   * This method creates a simple ChangeBoundsRequest including the given
   * child, direction and resize delta. The delta values are percentage values
   * and hence get stored in the extended data map of the request.
   */
  @SuppressWarnings("unchecked")
  private static ChangeBoundsRequest createRequest(
      EditPart child, int direction, double sizeDeltaX, double sizeDeltaY) {
    ChangeBoundsRequest req = new ChangeBoundsRequest(RequestConstants.REQ_RESIZE_CHILDREN);
    req.setEditParts(child);
    req.getExtendedData().put(PERCENTAGE_HEIGHT, sizeDeltaY);
    req.getExtendedData().put(PERCENTAGE_WIDTH, sizeDeltaX);
    req.setResizeDirection(direction);
    return req;
  }

  /*
   * This method creates a simple GraphicalEditPart
   */
  private static GraphicalEditPart createEditPart(final IFigure partFigure) {
    GraphicalEditPart editPart =
        new AbstractGraphicalEditPart() {

          @Override
          protected IFigure createFigure() {
            return partFigure;
          }

          @Override
          protected void createEditPolicies() {
            // TODO Auto-generated method stub

          }
        };
    return editPart;
  }

  @Before
  public void setup() {
    ElementModelLayoutEditPolicy policy = new ElementModelLayoutEditPolicy();
    resizableEditPolicy = policy.createChildEditPolicy(null);
    Class<?> c = resizableEditPolicy.getClass();
    recalculateResizeData = null;
    for (Method m : c.getDeclaredMethods()) {
      if (m.getName().equals(resizeDataMethodName)) recalculateResizeData = m;
    }
    assertNotNull("wrong method name for recalculating delta values", recalculateResizeData);

    IFigure figure = new RectangleFigure();
    Point initialLocation = new Point(50, 50);
    Dimension initialSize = new Dimension(100, 200);

    // TestModel model = new TestModel(initialLocation, initialSize);
    child = createEditPart(figure);
    // child.setModel(model);
    child.getFigure().setSize(initialSize);
    child.getFigure().setLocation(initialLocation);
  }

  @Test
  public void scalingCalculation() {
    double sizeDeltaX;
    double sizeDeltaY;

    double[][] deltaValues = {
      {100, 0}, {0, 100}, {100, 100}, {0, 0}, {50, 50}, {20, 20}, {33, 66}, {44.4, 4}, {-101, -1}
    };
    int[] directions = {
      PositionConstants.SOUTH,
      PositionConstants.SOUTH_WEST,
      PositionConstants.WEST,
      PositionConstants.NORTH_WEST,
      PositionConstants.NORTH,
      PositionConstants.NORTH_EAST,
      PositionConstants.EAST,
      PositionConstants.SOUTH_EAST
    };
    // resize with all combinations of directions and delta values
    for (int direction : directions) {
      for (double[] sizeDelta : deltaValues) {
        sizeDeltaX = sizeDelta[0];
        sizeDeltaY = sizeDelta[1];
        request = createRequest(child, direction, sizeDeltaX, sizeDeltaY);
        // run the recalculation method with the given parameters
        // altering request data
        try {
          recalculateResizeData.invoke(resizableEditPolicy, request, child);
        } catch (Exception e) {
          e.printStackTrace();
          fail(e.getMessage());
        }
        /*
         * size section
         */
        // desired values
        int deltaWidth = (int) (sizeDeltaX * child.getFigure().getSize().width) / 100;
        int deltaHeight = ((int) (sizeDeltaY * child.getFigure().getSize().height) / 100);
        // checking actual values
        Assert.assertEquals(
            "wrong percentage calculation (width)", deltaWidth, request.getSizeDelta().width);
        Assert.assertEquals(
            "wrong percentage calculation (height)", deltaHeight, request.getSizeDelta().height);
        /*
         * move section
         */
        // actual values
        int moveX = request.getMoveDelta().x;
        int moveY = request.getMoveDelta().y;

        // checking actual values
        switch (request.getResizeDirection()) {
            // WEST direction: x adjustment
          case PositionConstants.SOUTH_WEST:
          case PositionConstants.WEST:
            Assert.assertEquals("wrong positioning (WEST)", moveX, -deltaWidth);
            break;
            // NORTH direction: y adjustment
          case PositionConstants.NORTH_EAST:
          case PositionConstants.NORTH:
            Assert.assertEquals("wrong positioning (NORTH)", moveY, -deltaHeight);
            break;
            // NORTH and WEST direction: x and y adjustment
          case PositionConstants.NORTH_WEST:
            Assert.assertEquals("wrong positioning (WEST)", moveX, -deltaWidth);
            Assert.assertEquals("wrong positioning (NORTH)", moveY, -deltaHeight);
            break;
            // SOUTH or EAST direction: no adjustment
          case PositionConstants.SOUTH_EAST:
          case PositionConstants.EAST:
          case PositionConstants.SOUTH:
          default:
            break;
        }
      }
    }
  }

  /*
   * a simple model for testing purposes
   */
  @SuppressWarnings("unused")
  private class TestModel {

    Point location = null;
    Dimension size = null;

    public TestModel(Point location, Dimension size) {
      this.location = location;
      this.size = size;
    }

    public void setSize(Dimension size) {
      this.size = size;
    }

    public Dimension getSize() {
      return size;
    }

    public Point getLocation() {
      return location;
    }

    public void setLocation(Point location) {
      this.location = location;
    }
  }
}

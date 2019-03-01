package de.fu_berlin.inf.dpp.whiteboard.gef.editpolicy;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.handles.NonResizableHandleKit;
import org.eclipse.gef.handles.ResizableHandleKit;
import org.eclipse.gef.handles.ResizeHandle;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.ResizeTracker;

/**
 * This class is an extended ResizableEditPolicy with ratio scaling when dealing with multiple
 * selections.
 *
 * @author hdegener
 */
public class ResizableRatioScalingEditPolicy extends ResizableEditPolicy {
  protected final String PERCENTAGE_HEIGHT = "percentageHeight";
  protected final String PERCENTAGE_WIDTH = "percentageWidth";

  /**
   * Calculates Delta values for the given child from the percentage values of the extended map of
   * the given request.
   *
   * @param request
   * @param child
   */
  protected void recalculateResizeData(ChangeBoundsRequest request, GraphicalEditPart child) {
    if (request.getExtendedData().containsKey(PERCENTAGE_HEIGHT)
        && request.getExtendedData().containsKey(PERCENTAGE_WIDTH)) {
      Dimension originalSize = child.getFigure().getSize();
      // calculating size delta values
      double percentageHeight = (Double) request.getExtendedData().get(PERCENTAGE_HEIGHT);
      double percentageWidth = (Double) request.getExtendedData().get(PERCENTAGE_WIDTH);
      int deltaHeight = (int) (originalSize.height * percentageHeight / 100);
      int deltaWidth = (int) (originalSize.width * percentageWidth / 100);
      // calculating move delta values
      int moveX = 0;
      int moveY = 0;

      // note: SOUTH and EAST directions don't need a move delta
      switch (request.getResizeDirection()) {
          // WEST direction: x adjustment
        case PositionConstants.SOUTH_WEST:
        case PositionConstants.WEST:
          moveX = -deltaWidth;
          break;
          // NORTH direction: y adjustment
        case PositionConstants.NORTH_EAST:
        case PositionConstants.NORTH:
          moveY = -deltaHeight;
          break;
          // NORTH and WEST direction: x and y adjustment
        case PositionConstants.NORTH_WEST:
          moveX = -deltaWidth;
          moveY = -deltaHeight;
          break;
          // SOUTH or EAST direction: no adjustment
        case PositionConstants.SOUTH_EAST:
        case PositionConstants.EAST:
        case PositionConstants.SOUTH:
        default:
          break;
      }
      // applying delta values
      request.setSizeDelta(new Dimension(deltaWidth, deltaHeight));
      request.setMoveDelta(new Point(moveX, moveY));
    }
  }

  /*
   * Overridden to adapt the feedback to the new percentage oriented resizing
   * mechanism.
   */
  @Override
  protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
    recalculateResizeData(request, (GraphicalEditPart) getHost());
    super.showChangeBoundsFeedback(request);
  }

  /**
   * This method applies the percentage information on the given request for the host of this edit
   * policy. Note that it is necessary to recalculate the size information for every child since a
   * request only stores one global delta information for multiple EditParts.
   */
  @Override
  protected Command getResizeCommand(ChangeBoundsRequest request) {
    recalculateResizeData(request, (GraphicalEditPart) getHost());
    return super.getResizeCommand(request);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  protected List createSelectionHandles() {
    List list = new ArrayList();
    int directions = getResizeDirections();
    if (directions == 0) NonResizableHandleKit.addHandles((GraphicalEditPart) getHost(), list);
    else if (directions != -1) {
      ResizableHandleKit.addMoveHandle((GraphicalEditPart) getHost(), list);
      if ((directions & PositionConstants.EAST) != 0)
        list.add(new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.EAST));
      else
        NonResizableHandleKit.addHandle(
            (GraphicalEditPart) getHost(), list, PositionConstants.EAST);
      if ((directions & PositionConstants.SOUTH_EAST) == PositionConstants.SOUTH_EAST)
        list.add(
            new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.SOUTH_EAST));
      else
        NonResizableHandleKit.addHandle(
            (GraphicalEditPart) getHost(), list, PositionConstants.SOUTH_EAST);
      if ((directions & PositionConstants.SOUTH) != 0)
        list.add(new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.SOUTH));
      else
        NonResizableHandleKit.addHandle(
            (GraphicalEditPart) getHost(), list, PositionConstants.SOUTH);
      if ((directions & PositionConstants.SOUTH_WEST) == PositionConstants.SOUTH_WEST)
        list.add(
            new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.SOUTH_WEST));
      else
        NonResizableHandleKit.addHandle(
            (GraphicalEditPart) getHost(), list, PositionConstants.SOUTH_WEST);
      if ((directions & PositionConstants.WEST) != 0)
        list.add(new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.WEST));
      else
        NonResizableHandleKit.addHandle(
            (GraphicalEditPart) getHost(), list, PositionConstants.WEST);
      if ((directions & PositionConstants.NORTH_WEST) == PositionConstants.NORTH_WEST)
        list.add(
            new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.NORTH_WEST));
      else
        NonResizableHandleKit.addHandle(
            (GraphicalEditPart) getHost(), list, PositionConstants.NORTH_WEST);
      if ((directions & PositionConstants.NORTH) != 0)
        list.add(new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.NORTH));
      else
        NonResizableHandleKit.addHandle(
            (GraphicalEditPart) getHost(), list, PositionConstants.NORTH);
      if ((directions & PositionConstants.NORTH_EAST) == PositionConstants.NORTH_EAST)
        list.add(
            new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.NORTH_EAST));
      else
        NonResizableHandleKit.addHandle(
            (GraphicalEditPart) getHost(), list, PositionConstants.NORTH_EAST);
    } else {
      ResizableHandleKit.addMoveHandle((GraphicalEditPart) getHost(), list);
      list.add(new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.EAST));
      list.add(new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.SOUTH_EAST));
      list.add(new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.SOUTH));
      list.add(new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.SOUTH_WEST));
      list.add(new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.WEST));
      list.add(new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.NORTH_WEST));
      list.add(new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.NORTH));
      list.add(new CustomResizeHandle((GraphicalEditPart) getHost(), PositionConstants.NORTH_EAST));
    }
    return list;
  }

  // A {@link ResizeHandle} using a custom DragTracker for percentage based
  // resizing
  class CustomResizeHandle extends ResizeHandle {

    int direction;

    public CustomResizeHandle(GraphicalEditPart owner, int direction) {
      super(owner, direction);
      this.direction = direction;
    }

    /**
     * Provides a custom {@link ResizeTracker} saving additional resize percentage information from
     * the resize source in the source request later dispatched to respective EditParts.
     */
    @Override
    protected DragTracker createDragTracker() {
      return new ResizeTracker(getOwner(), direction) {
        @SuppressWarnings("unchecked")
        @Override
        protected void updateSourceRequest() {
          super.updateSourceRequest();
          ChangeBoundsRequest request = (ChangeBoundsRequest) getSourceRequest();
          // note that the getOwner() method is the crucial
          // method to determine the figure that is currently
          // resized and that serves as basis for further
          // percentage calculations
          GraphicalEditPart dragSource = getOwner();
          Dimension sizeDelta = request.getSizeDelta();
          Dimension originalSize = dragSource.getFigure().getSize();
          // calculating percentage resize values, for maximum
          // precision we save the result as double value
          double percentageHeight = 100d * sizeDelta.height / originalSize.height;
          double percentageWidth = 100d * sizeDelta.width / originalSize.width;
          // saving percentage information
          request.getExtendedData().put(PERCENTAGE_HEIGHT, percentageHeight);
          request.getExtendedData().put(PERCENTAGE_WIDTH, percentageWidth);
        }
      };
    }
  }
}

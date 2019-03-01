package saros.whiteboard.gef.tools;

import org.eclipse.gef.Request;
import saros.whiteboard.gef.editpolicy.XYLayoutWithFreehandEditPolicy;
import saros.whiteboard.gef.request.CreatePointlistRequest;

/**
 * A tool to create a point list by dragging.
 *
 * <p>Instead of creating a rectangle out of start and current location, on every handle drag the
 * current point is added to the point list of the create request.
 *
 * @see saros.whiteboard.gef.request.CreatePointlistRequest
 * @see saros.whiteboard.gef.editpolicy.XYLayoutWithFreehandEditPolicy
 * @author jurke
 */
public class PointlistCreationTool extends CreationToolWithoutSelection {

  @Override
  protected Request createTargetRequest() {
    CreatePointlistRequest request = new CreatePointlistRequest();
    request.setFactory(getFactory());
    return request;
  }

  @Override
  protected String getCommandName() {
    return XYLayoutWithFreehandEditPolicy.REQ_CREATE_POINTLIST;
  }

  protected CreatePointlistRequest getCreatePointlistRequest() {
    return (CreatePointlistRequest) getTargetRequest();
  }

  @Override
  protected String getDebugName() {
    return "Pointlist Creation Tool";
  }

  /** Adds the current location to the point list of the CreatePointlistRequest. */
  @Override
  protected void updateTargetRequest() {
    CreatePointlistRequest req = getCreatePointlistRequest();
    if (isInState(STATE_DRAG)) {
      req.addPoint(getLocation());
    } else {
      req.clear();
      req.setLocation(getLocation());
    }
  }

  /** handles the drag to update the target request and to show the line feedback */
  @Override
  protected boolean handleDrag() {
    if (isInState(STATE_DRAG)) {
      updateTargetRequest();
      setCurrentCommand(getCommand());
      showTargetFeedback();
    }
    return true;
  }

  @Override
  protected boolean handleDragStarted() {
    // don't switch to STATE_DRAG_IN_PROGRESS
    return false;
  }

  @Override
  protected boolean handleDragInProgress() {
    // handled by handleDrag only
    return false;
  }

  @Override
  protected void showTargetFeedback() {
    if (isInState(STATE_DRAG)) {
      super.showTargetFeedback();
    }
  }
}

package de.fu_berlin.inf.dpp.whiteboard.gef.tools;

import de.fu_berlin.inf.dpp.whiteboard.gef.editpolicy.XYLayoutWithFreehandEditPolicy;
import de.fu_berlin.inf.dpp.whiteboard.gef.request.CreateLineRequest;
import de.fu_berlin.inf.dpp.whiteboard.gef.request.CreatePointlistRequest;
import org.eclipse.gef.Request;

/**
 * A tool to create a point list by dragging.
 *
 * <p>Instead of creating a rectangle out of start and current location, on every handle drag the
 * current point is added to the point list of the create request.
 *
 * @see de.fu_berlin.inf.dpp.whiteboard.gef.request.CreatePointlistRequest
 * @see de.fu_berlin.inf.dpp.whiteboard.gef.editpolicy.XYLayoutWithFreehandEditPolicy
 * @author markusb
 */
public class LineCreationTool extends PointlistCreationTool {

  @Override
  protected String getCommandName() {
    return XYLayoutWithFreehandEditPolicy.REQ_CREATE_LINE;
  }

  /** Creates a CreateLineRequest. Is called if "getTargetRequest" is called the first time */
  @Override
  protected Request createTargetRequest() {
    CreateLineRequest request = new CreateLineRequest();
    request.setFactory(getFactory());
    return request;
  }

  @Override
  protected CreatePointlistRequest getCreatePointlistRequest() {
    return (CreateLineRequest) getTargetRequest();
  }

  @Override
  protected String getDebugName() {
    return "Line Creation Tool";
  }

  /** Adds the current location to the point list of the CreatePointlistRequest. */
  @Override
  protected void updateTargetRequest() {
    CreatePointlistRequest req = getCreatePointlistRequest();
    if (isInState(STATE_DRAG)) {
      req.updateEndPoint(getLocation());
    } else {
      req.clear();
      req.setLocation(getLocation());
    }
  }
}

package de.fu_berlin.inf.dpp.whiteboard.gef.tools;

import de.fu_berlin.inf.dpp.whiteboard.gef.request.CreatePointlistRequest;

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
public class ArrowCreationTool extends LineCreationTool {

  /** Adds the current location to the point list of the CreatePointlistRequest. */
  @Override
  protected void updateTargetRequest() {
    CreatePointlistRequest req = getCreatePointlistRequest();
    if (isInState(STATE_DRAG)) {
      req.updateArrowPoint(getLocation());
    } else {
      req.clear();
      req.setLocation(getLocation());
    }
  }
}

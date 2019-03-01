package de.fu_berlin.inf.dpp.whiteboard.gef.request;

import de.fu_berlin.inf.dpp.whiteboard.gef.editpolicy.XYLayoutWithFreehandEditPolicy;

/**
 * class to track points of a line
 *
 * @author Markus
 */
public class CreateLineRequest extends CreatePointlistRequest {

  public CreateLineRequest() {
    setType(XYLayoutWithFreehandEditPolicy.REQ_CREATE_LINE);
  }
}

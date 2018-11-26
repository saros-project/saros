package de.fu_berlin.inf.dpp.whiteboard.gef.part;

import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGEllipseRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGPolylineRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGRectRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGRootRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGTextBoxRecord;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

/**
 * EditPartFactory to create edit parts respective the model
 *
 * @author jurke
 */
public class RecordPartFactory implements EditPartFactory {

  @Override
  public EditPart createEditPart(EditPart context, Object model) {
    AbstractGraphicalEditPart part = null;

    if (model instanceof SVGRootRecord) {
      part = new SVGRootPart();
    } else if (model instanceof SVGTextBoxRecord) {
      part = new SVGTextBoxPart();
    } else if (model instanceof SVGRectRecord) {
      part = new SVGRectPart();
    } else if (model instanceof SVGPolylineRecord) {
      part = new SVGPolylinePart();
    } else if (model instanceof SVGEllipseRecord) {
      part = new SVGEllipsePart();
    }
    if (part != null) part.setModel(model);

    return part;
  }
}

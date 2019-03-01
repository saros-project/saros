package saros.whiteboard.gef.part;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import saros.whiteboard.gef.model.SVGEllipseRecord;
import saros.whiteboard.gef.model.SVGPolylineRecord;
import saros.whiteboard.gef.model.SVGRectRecord;
import saros.whiteboard.gef.model.SVGRootRecord;
import saros.whiteboard.gef.model.SVGTextBoxRecord;

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

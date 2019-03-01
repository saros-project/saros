package saros.whiteboard.gef.tools;

import org.apache.log4j.Logger;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.tools.CreationTool;

/**
 * Same like GEF's CreationTool but after creating a new element it will be selected automatically.
 * Anyway this does not make sense in this whiteboard's context as the CreationFactory creates a
 * different object then will be created by the command.
 *
 * @author jurke
 */
public class CreationToolWithoutSelection extends CreationTool {

  Logger log = Logger.getLogger(CreationToolWithoutSelection.class);

  @Override
  protected Request createTargetRequest() {
    return super.createTargetRequest();
  }

  @Override
  protected void performCreation(int button) {
    executeCurrentCommand();
    if (getCurrentViewer() != null) getCurrentViewer().deselectAll();
  }

  @Override
  protected void showTargetFeedback() {
    super.showTargetFeedback();
  }

  @Override
  protected void setTargetEditPart(EditPart editpart) {
    super.setTargetEditPart(editpart);
  }

  @Override
  protected boolean updateTargetUnderMouse() {
    if (!isTargetLocked()) {
      EditPart editPart =
          getCurrentViewer()
              .findObjectAtExcluding(getLocation(), getExclusionSet(), getTargetingConditional());
      if (editPart != null) editPart = editPart.getTargetEditPart(getTargetRequest());
      boolean changed = getTargetEditPart() != editPart;
      setTargetEditPart(editPart);
      return changed;
    } else return false;
  }
}

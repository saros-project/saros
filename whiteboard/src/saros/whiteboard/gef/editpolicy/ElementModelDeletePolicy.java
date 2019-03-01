package saros.whiteboard.gef.editpolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import saros.whiteboard.gef.commands.DeleteRecordsCommand;
import saros.whiteboard.gef.model.SVGRootRecord;
import saros.whiteboard.sxe.records.ElementRecord;

/**
 * Simple edit policy to enable record deletion
 *
 * @author jurke
 */
public class ElementModelDeletePolicy extends ComponentEditPolicy {

  @Override
  protected Command createDeleteCommand(GroupRequest deleteRequest) {
    if (getHost().getModel() instanceof SVGRootRecord) return null;
    DeleteRecordsCommand command = new DeleteRecordsCommand();
    if (getHost().getModel() instanceof ElementRecord) {
      command.addRecordToDelete((ElementRecord) getHost().getModel());
      return command;
    }
    return null;
  }
}

package de.fu_berlin.inf.dpp.whiteboard.gef.editpolicy;

import de.fu_berlin.inf.dpp.whiteboard.gef.commands.DeleteRecordsCommand;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGRootRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ElementRecord;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

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

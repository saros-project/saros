package de.fu_berlin.inf.dpp.whiteboard.gef.actions;

import de.fu_berlin.inf.dpp.whiteboard.gef.commands.DeleteRecordsCommand;
import de.fu_berlin.inf.dpp.whiteboard.gef.part.ElementRecordPart;
import java.util.List;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.ui.IWorkbenchPart;

/**
 * For efficiency reasons we use a custom DeleteAction that uses a {@link
 * de.fu_berlin.inf.dpp.whiteboard.sxe.util.HierarchicalRecordSet} to create only <code>RemoveRecord
 * </code>s for the top-most <code>ElementRecord</code>s.
 *
 * @author jurke
 */
/*
 * Note, if convenient this may be reworked because it interrupts the GEF edit
 * policy structure - we don't request a command from the edit part.
 *
 * Minor issue because we only have one type of models anyway: records.
 */
public class SXEDeleteAction extends DeleteAction {

  public SXEDeleteAction(IWorkbenchPart part) {
    super(part);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Command createDeleteCommand(List objects) {
    if (objects.isEmpty()) return null;
    if (!(objects.get(0) instanceof EditPart)) return null;

    DeleteRecordsCommand cmd = new DeleteRecordsCommand();

    for (int i = 0; i < objects.size(); i++) {
      if (objects.get(i) instanceof ElementRecordPart) {
        cmd.addRecordToDelete(((ElementRecordPart) objects.get(i)).getElementRecord());
      }
    }

    return cmd;
  }
}

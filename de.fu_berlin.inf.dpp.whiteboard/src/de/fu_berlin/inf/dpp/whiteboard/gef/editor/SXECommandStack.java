package de.fu_berlin.inf.dpp.whiteboard.gef.editor;

import de.fu_berlin.inf.dpp.whiteboard.gef.commands.SXECommand;
import de.fu_berlin.inf.dpp.whiteboard.gef.commands.SXECompoundCommand;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;

/**
 * An adaption of the GEF CommandStack that will convert a {@link
 * org.eclipse.gef.commands.CompoundCommand} to the SXE correspondent to transmit only a single list
 * of <code>IRecord</code>s to the controller.
 *
 * @author jurke
 * @see de.fu_berlin.inf.dpp.whiteboard.gef.commands.SXECompoundCommand
 */
public class SXECommandStack extends CommandStack {

  @Override
  public void execute(Command command) {
    if (command instanceof CompoundCommand) {
      command = getSXECompoundCommand((CompoundCommand) command);
    }
    super.execute(command);
  }

  /**
   * Compounds all <code>SXECommand</code>s in an SXECompoundCommand and leaves other commands
   * untouched.
   *
   * @param cc
   * @return a new command that will execute all SXECommands as one list of records
   */
  protected CompoundCommand getSXECompoundCommand(CompoundCommand cc) {
    if (cc instanceof SXECompoundCommand) return cc;

    CompoundCommand r = new CompoundCommand();
    r.setLabel(cc.getLabel());

    SXECompoundCommand sxeCommand = new SXECompoundCommand();
    sxeCommand.setLabel(cc.getLabel());

    for (Object o : cc.getCommands()) {
      if (o instanceof SXECommand) sxeCommand.add((SXECommand) o);
      else r.add((Command) o);
    }

    if (r.getCommands().size() == 0) return sxeCommand;

    r.add(sxeCommand);

    return r;
  }

  /**
   * Unfortunately GEF does not allow to flush redo without flushing undo publicly. Thus we have to
   * check canExecute() here because remote operations may have caused to make it non-executable.
   */
  @Override
  public boolean canRedo() {
    Command c = getRedoCommand();
    if (c != null && !c.canExecute()) return false;
    return super.canRedo();
  }
}

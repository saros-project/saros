package saros.whiteboard.gef.commands;

import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import saros.whiteboard.sxe.records.IRecord;

/**
 * This compound of commands only accepts SXECommands and will execute them as one single list of
 * <code>IRecord</code>s.
 *
 * @author jurke
 */
public class SXECompoundCommand extends CompoundCommand {

  private static final Logger log = Logger.getLogger(SXECommand.class);

  @Override
  public final void execute() {
    try {

      List<IRecord> records = new LinkedList<IRecord>();

      for (Object o : getCommands()) {
        records.addAll(((SXECommand) o).getRecords());
      }

      ((SXECommand) getCommands().get(0))
          .getDocumentRecord()
          .getController()
          .executeAndCommit(records);

    } catch (Exception e) {
      log.error("Cannot execute operation: ", e);
    }
  }

  @Override
  public void add(Command command) {
    if (!(command instanceof SXECommand)) {
      log.error("Cannot add " + command + " to SXECompoundCommand");
      return;
    }
    super.add(command);
  }

  @Override
  public void redo() {
    try {

      List<IRecord> records = new LinkedList<IRecord>();

      for (Object o : getCommands()) {
        records.addAll(((SXECommand) o).getRedoRecords());
      }

      ((SXECommand) getCommands().get(0))
          .getDocumentRecord()
          .getController()
          .executeAndCommit(records);

    } catch (Exception e) {
      log.error("Cannot execute operation: ", e);
    }
  }

  @Override
  public void undo() {
    try {

      List<IRecord> records = new LinkedList<IRecord>();

      for (Object o : getCommands()) {
        records.addAll(((SXECommand) o).getUndoRecords());
      }

      ((SXECommand) getCommands().get(0))
          .getDocumentRecord()
          .getController()
          .executeAndCommit(records);

    } catch (Exception e) {
      log.error("Cannot execute operation: ", e);
    }
  }

  @Override
  public Command chain(Command c) {
    if (c instanceof SXECommand) {
      add(c);
      return this;
    } else return super.chain(c);
  }
}

package saros.whiteboard.gef.commands;

import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import saros.whiteboard.sxe.records.DocumentRecord;
import saros.whiteboard.sxe.records.IRecord;

/**
 * Base class for SXECommands that will transmit a list of <code>IRecord</code>s to the document
 * instead of manipulating any object by itself.
 *
 * @author jurke
 */
public abstract class SXECommand extends Command {

  private static final Logger log = Logger.getLogger(SXECommand.class);

  public abstract List<IRecord> getRecords();

  public abstract List<IRecord> getUndoRecords();

  public List<IRecord> getRedoRecords() {
    return getRecords();
  }

  public abstract DocumentRecord getDocumentRecord();

  @Override
  public final void execute() {
    try {

      List<IRecord> records = getRecords();
      getDocumentRecord().getController().executeAndCommit(records);

    } catch (Exception e) {
      log.error("Cannot execute operation: ", e);
    }
  }

  @Override
  public void undo() {
    try {

      List<IRecord> records = getUndoRecords();

      getDocumentRecord().getController().executeAndCommit(records);

    } catch (Exception e) {
      log.error("Cannot execute operation: ", e);
    }
  }

  @Override
  public void redo() {
    try {

      List<IRecord> records = getRedoRecords();

      getDocumentRecord().getController().executeAndCommit(records);

    } catch (Exception e) {
      log.error("Cannot execute operation: ", e);
    }
  }

  @Override
  public final boolean canExecute() {
    if (getDocumentRecord() == null || getDocumentRecord().getController() == null) return false;
    return canExecuteSXECommand();
  }

  protected abstract boolean canExecuteSXECommand();

  @Override
  public final boolean canUndo() {
    if (getDocumentRecord() == null || getDocumentRecord().getController() == null) {
      return false;
    }
    return canUndoSXECommand();
  }

  protected abstract boolean canUndoSXECommand();

  @Override
  public Command chain(Command command) {
    if (command == null) return this;
    if (command instanceof SXECommand) {
      CompoundCommand result = new SXECompoundCommand();
      result.setDebugLabel("chained SXECommands");
      result.add(this);
      result.add(command);
      return result;
    } else return super.chain(command);
  }
}

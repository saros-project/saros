package saros.ui.handlers.toolbar;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.di.annotations.Execute;

public class OpenPreferencesHandler {

  public static final String ID = OpenPreferencesHandler.class.getName();

  private static final Logger log = Logger.getLogger(OpenPreferencesHandler.class);

  @Execute
  public void run(ECommandService commandService) {
    Command openPreferencesCmd =
        commandService.getCommand("saros.ui.commands.OpenSarosPreferences");
    try {
      openPreferencesCmd.executeWithChecks(new ExecutionEvent());
    } catch (Exception e) {
      log.error("Could not execute command", e); // $NON-NLS-1$
    }
  }
}

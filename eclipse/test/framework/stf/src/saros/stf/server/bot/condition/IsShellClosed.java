package saros.stf.server.bot.condition;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

public class IsShellClosed extends DefaultCondition {

  private SWTBotShell shell;

  IsShellClosed(SWTBotShell shell) {

    this.shell = shell;
  }

  @Override
  public String getFailureMessage() {

    return "shell '" + shell.getText() + "' is still open";
  }

  @Override
  public boolean test() throws Exception {
    return !shell.isOpen();
  }
}

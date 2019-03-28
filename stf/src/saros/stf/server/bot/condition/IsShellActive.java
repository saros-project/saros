package saros.stf.server.bot.condition;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

public class IsShellActive extends DefaultCondition {

  private SWTBotShell shell;

  IsShellActive(SWTBotShell shell) {
    this.shell = shell;
  }

  @Override
  public String getFailureMessage() {
    return "STFBotShell  not found.";
  }

  @Override
  public boolean test() throws Exception {
    return shell.isActive();
  }
}

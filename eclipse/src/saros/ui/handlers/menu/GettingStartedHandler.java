package saros.ui.handlers.menu;

import org.eclipse.e4.core.di.annotations.Execute;
import saros.ui.util.SWTUtils;

public class GettingStartedHandler {

  @Execute
  public void execute() {

    SWTUtils.openInternalBrowser(
        "https://www.saros-project.org/documentation/getting-started.html", "Welcome to Saros");
  }
}

package saros.ui.commandHandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import saros.ui.util.SWTUtils;

public class GettingStartedHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {

    SWTUtils.openInternalBrowser("http://www.saros-project.org/GettingStarted", "Welcome to Saros");

    return null;
  }
}

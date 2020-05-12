package saros.ui.command_handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import saros.ui.util.WizardUtils;

public class StartSarosConfigurationHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    WizardUtils.openSarosConfigurationWizard();
    return null;
  }
}

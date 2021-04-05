package saros.ui.command_handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import saros.ui.util.WizardUtils;

public class CreateXMPPAccountHandler {

  @Execute
  public Object execute() {

    WizardUtils.openCreateXMPPAccountWizard(true);

    return null;
  }
}

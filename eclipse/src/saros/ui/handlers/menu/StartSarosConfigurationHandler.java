package saros.ui.handlers.menu;

import org.eclipse.e4.core.di.annotations.Execute;
import saros.ui.util.WizardUtils;

public class StartSarosConfigurationHandler {

  @Execute
  public Object execute() {
    WizardUtils.openSarosConfigurationWizard();
    return null;
  }
}

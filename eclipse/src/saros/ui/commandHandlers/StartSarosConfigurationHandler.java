package saros.ui.commandHandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.ui.ide_embedding.DialogManager;
import saros.ui.pages.ConfigurationPage;
import saros.ui.util.WizardUtils;

public class StartSarosConfigurationHandler extends AbstractHandler {

  @Inject private DialogManager dialogManager;

  @Inject private ConfigurationPage configurationPage;

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    if (Boolean.getBoolean("saros.swtbrowser")) {
      try {
        SarosPluginContext.initComponent(this);
        dialogManager.showDialogWindow(configurationPage);
      } catch (Exception e) {
        throw new ExecutionException(e.getMessage());
      }
    } else {
      WizardUtils.openSarosConfigurationWizard();
    }
    return null;
  }
}

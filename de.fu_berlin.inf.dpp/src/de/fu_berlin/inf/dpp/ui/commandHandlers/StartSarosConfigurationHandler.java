package de.fu_berlin.inf.dpp.ui.commandHandlers;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.pages.ConfigurationPage;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.picocontainer.annotations.Inject;

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

package saros.ui.commandHandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import saros.session.ISarosSession;
import saros.ui.util.WizardUtils;
import saros.ui.wizards.AddResourcesToSessionWizard;

/**
 * Handles the addition of {@link IResource}s that must explicitly be selected in the opening {@link
 * AddResourcesToSessionWizard} to the running {@link ISarosSession}.
 */
public class SessionAddProjectsHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    WizardUtils.openAddResourcesToSessionWizard(null);
    return null;
  }
}

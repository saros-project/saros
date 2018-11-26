package de.fu_berlin.inf.dpp.ui.commandHandlers;

import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.wizards.AddResourcesToSessionWizard;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;

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

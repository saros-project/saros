package de.fu_berlin.inf.dpp.ui.commandHandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.wizards.ShareProjectAddProjectsWizard;

/**
 * Handles the addition of {@link JID}s that must explicitly be selected in the
 * opening {@link ShareProjectAddProjectsWizard} to the running
 * {@link ISarosSession}.
 */
public class SharedProjectAddBuddiesHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        WizardUtils.openShareProjectAddBuddiesWizard();
        return null;
    }

}

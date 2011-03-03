package de.fu_berlin.inf.dpp.ui.commandHandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;

import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.wizards.ShareProjectAddProjectsWizard;

/**
 * Handles the addition of {@link IProject}s that must explicitly be selected in
 * the opening {@link ShareProjectAddProjectsWizard} to the running
 * {@link ISarosSession}.
 */
public class SharedProjectAddProjectsHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        WizardUtils.openShareProjectAddProjectsWizard();
        return null;
    }

}

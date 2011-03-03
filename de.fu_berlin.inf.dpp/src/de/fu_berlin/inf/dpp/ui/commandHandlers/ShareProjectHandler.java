package de.fu_berlin.inf.dpp.ui.commandHandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;

public class ShareProjectHandler extends AbstractHandler {

    @Inject
    SarosSessionManager sarosSessionManager;

    public Object execute(ExecutionEvent event) throws ExecutionException {
        WizardUtils.openShareProjectWizard();
        return null;
    }
}

package de.fu_berlin.inf.dpp.ui.commandHandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;

public class CreateXMPPAccountHandler extends AbstractHandler {

    @Inject
    protected Saros saros;

    @Inject
    protected SarosUI sarosUI;

    public CreateXMPPAccountHandler() {
        SarosPluginContext.initComponent(this);
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {

        WizardUtils.openCreateXMPPAccountWizard(true);

        return null;
    }

}

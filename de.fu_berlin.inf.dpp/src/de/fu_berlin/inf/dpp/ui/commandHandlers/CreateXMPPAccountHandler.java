package de.fu_berlin.inf.dpp.ui.commandHandlers;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.jivesoftware.smack.Roster;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.wizards.CreateXMPPAccountWizard;

public class CreateXMPPAccountHandler extends AbstractHandler {

    @Inject
    protected Saros saros;

    @Inject
    protected SarosUI sarosUI;

    @Inject
    protected PreferenceUtils preferenceUtils;

    public CreateXMPPAccountHandler() {
        SarosPluginContext.initComponent(this);
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        WizardDialog wd = new WizardDialog(null, new CreateXMPPAccountWizard(
            saros, preferenceUtils, true, true, false));
        wd.setHelpAvailable(false);
        // open() blocks until the user closed the dialog
        wd.open();
        /*
         * activate Roster and bring it to front (if not done already) to let
         * user start inviting buddies
         */
        sarosUI.activateRosterView();
        Roster sarosRoster = saros.getRoster();
        if (sarosRoster != null)
            sarosRoster.reload();

        return null;
    }

}

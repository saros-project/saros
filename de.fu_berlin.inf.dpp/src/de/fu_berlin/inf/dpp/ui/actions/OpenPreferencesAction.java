package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;

public class OpenPreferencesAction extends Action {
    private static final Logger log = Logger
        .getLogger(OpenPreferencesAction.class);

    public OpenPreferencesAction() {
        super(Messages.OpenPreferencesAction_title);
        this.setToolTipText(Messages.OpenPreferencesAction_tooltip);
        this.setImageDescriptor(ImageManager.ELCL_OPEN_PREFERENCES);
        this.setEnabled(true);
    }

    @Override
    public void run() {
        IHandlerService service = (IHandlerService) PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage().getActivePart()
            .getSite().getService(IHandlerService.class);
        try {
            service.executeCommand(
                "de.fu_berlin.inf.dpp.ui.commands.OpenSarosPreferences", //$NON-NLS-1$
                null);
        } catch (Exception e) {
            log.error("Could not execute command", e); //$NON-NLS-1$
        }
    }
}

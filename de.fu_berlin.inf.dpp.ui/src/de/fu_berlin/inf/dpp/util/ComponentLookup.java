package de.fu_berlin.inf.dpp.util;

import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;
import de.fu_berlin.inf.dpp.ui.view_parts.AddAccountWizard;
import de.fu_berlin.inf.dpp.ui.view_parts.SarosMainPage;
import org.picocontainer.annotations.Inject;

/**
 * This class is used to get pico container components for classes which are not
 * created by the pico container.
 * TODO I think over time a better solution has to be implemented but for now this
 * is sufficient.
 * In the IntelliJ package there is a similar solution in
 * de.fu_berlin.inf.dpp.core.context.SarosPluginContext which could be adapated
 * for this.
 */
public class ComponentLookup {

    @Inject
    private static IDialogManager dialogManager;

    @Inject
    private static XMPPConnectionService connectionService;

    @Inject
    private static UISynchronizer uiSynchronizer;

    @Inject
    private static XMPPAccountStore accountStore;

    @Inject
    private static AddAccountWizard addAccountWizard;

    @Inject
    private static SarosMainPage sarosMainPage;

    public static AddAccountWizard getAddAccountWizard() {
        return addAccountWizard;
    }

    public static SarosMainPage getSarosMainPage() {
        return sarosMainPage;
    }

    public static XMPPConnectionService getConnectionService() {
        return connectionService;
    }

    public static UISynchronizer getUISynchronizer() {
        return uiSynchronizer;
    }

    public static IDialogManager getDialogManager() {
        return dialogManager;
    }

    public static XMPPAccountStore getAccountStore() {
        return accountStore;
    }
}

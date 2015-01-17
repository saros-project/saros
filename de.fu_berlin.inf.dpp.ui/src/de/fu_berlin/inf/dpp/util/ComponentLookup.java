package de.fu_berlin.inf.dpp.util;

import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.ui.manager.HTMLUIManager;
import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;
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
    private static HTMLUIManager htmlUIManager;


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

    public static HTMLUIManager getHtmlUIManager() {
        return htmlUIManager;
    }
}

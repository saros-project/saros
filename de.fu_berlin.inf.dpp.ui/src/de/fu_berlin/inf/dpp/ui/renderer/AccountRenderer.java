package de.fu_berlin.inf.dpp.ui.renderer;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;

/**
 * This class is responsible for sending the account list to the HTML UI.
 * <p>
 * As changes to the state of the account store are currently not pushed via
 * listeners, this class actively queries the state on each render request from
 * the {@link XMPPAccountStore}.
 */
// Let AccountRenderer be a Listener on a core facade
public class AccountRenderer extends Renderer {

    private final XMPPAccountStore accountStore;

    /**
     * Created by PicoContainer
     * 
     * @param accountStore
     * @see HTMLUIContextFactory
     */
    public AccountRenderer(XMPPAccountStore accountStore) {
        this.accountStore = accountStore;
    }

    @Override
    public synchronized void render(IJQueryBrowser browser) {
        JavaScriptAPI.updateAccounts(browser, accountStore.getAllAccounts());
    }
}

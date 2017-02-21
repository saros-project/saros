package de.fu_berlin.inf.dpp.ui.browser_functions;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;
import de.fu_berlin.inf.dpp.ui.browser_functions.BrowserFunction.Policy;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;

/**
 * Save a given JID/Password combination.
 */
public class SaveAccount extends TypedJavascriptFunction {

    private static final Logger LOG = Logger.getLogger(SaveAccount.class);

    public static final String JS_NAME = "saveAccount";

    private XMPPAccountStore accountStore;
    private AccountRenderer accountRenderer;

    /**
     * Created by PicoContainer
     * 
     * @param accountStore
     *            the save action is delegated to
     * @param accountRenderer
     *            that should be re-render after the change
     * @see HTMLUIContextFactory
     */
    public SaveAccount(XMPPAccountStore accountStore,
        AccountRenderer accountRenderer) {
        super(JS_NAME);
        this.accountStore = accountStore;
        this.accountRenderer = accountRenderer;
    }

    /**
     * Saves a given {@link XMPPAccount}in the {@link XMPPAccountStore}.
     * <p>
     * No account is created and an error will be shown, if there is already an
     * account with the same values for all of <br>
     * {@link XMPPAccount#getUsername()}; {@link XMPPAccount#getDomain()};
     * {@link XMPPAccount#getServer()}; {@link XMPPAccount#getPort()},
     * <p>
     * Note that, on success, this action will trigger an re-rendering for the
     * {@link AccountRenderer} to reflect the changes immediately.
     * 
     * @param acc
     *            the JID of the new account
     */
    @BrowserFunction(Policy.ASYNC)
    public void saveAccount(XMPPAccount acc) {
        if (accountStore.exists(acc.getUsername(), acc.getDomain(),
            acc.getServer(), acc.getPort())) {
            LOG.debug("Account " + acc.getUsername() + " already present");
            JavaScriptAPI.showError(browser,
                HTMLUIStrings.SAVE_ACCOUNT_ALREADY_PRESENT);
        } else {
            accountStore.createAccount(acc.getUsername(), acc.getPassword(),
                acc.getDomain(), acc.getServer(), acc.getPort(), acc.useTLS(),
                acc.useSASL());
            accountRenderer.render();
        }
    }
}

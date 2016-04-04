package de.fu_berlin.inf.dpp.ui.browser_functions;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;
import de.fu_berlin.inf.dpp.ui.browser_functions.BrowserFunction.Policy;
import de.fu_berlin.inf.dpp.ui.core_facades.AccountStoreFacade;
import de.fu_berlin.inf.dpp.ui.model.Account;

/**
 * Save a given JID/Password combination.
 */
public class SaveAccount extends TypedJavascriptFunction {

    private static final Logger LOG = Logger.getLogger(SaveAccount.class);

    public static final String JS_NAME = "saveAccount";

    private AccountStoreFacade accountStoreFacade;

    /**
     * Created by PicoContainer
     * 
     * @param accountStoreFacade
     * @see HTMLUIContextFactory
     */
    public SaveAccount(AccountStoreFacade accountStoreFacade) {
        super(JS_NAME);
        this.accountStoreFacade = accountStoreFacade;
    }

    /**
     * Save a given JID/Password combination in the {@link XMPPAccountStore}.
     * <p>
     * Note that, on success, this action will trigger an re-rendering for the
     * {@link Account} model to reflect the changes immediately. If this
     * operation fails, an error is shown to the user.
     * 
     * @param jid
     *            the JID of the new account
     * @param password
     *            the password for that account
     */
    @BrowserFunction(Policy.ASYNC)
    public void saveAccount(String jid, String password) {
        try {
            accountStoreFacade.createAccount(jid, password);
        } catch (Exception e) {
            LOG.error("Could not create account", e);
            JavaScriptAPI.showError(browser, HTMLUIStrings.SAVE_ACCOUNT_FAILED);
        }
    }
}

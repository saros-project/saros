package de.fu_berlin.inf.dpp.ui.browser_functions;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;

/**
 * Edit an existing account in the account store.
 */
public class EditAccount extends TypedJavascriptFunction {

    private static final Logger LOG = Logger.getLogger(EditAccount.class);

    public static final String JS_NAME = "editAccount";

    private XMPPAccountStore accountStore;
    private AccountRenderer accountRenderer;

    /**
     * Created by PicoContainer
     * 
     * @param accountStore
     *            to manage the XMPPAccounts
     * @param accountRenderer
     *            the renderer that will trigger a re-rendering of the account
     *            information in the ui after the account data was changed
     * @see HTMLUIContextFactory
     */
    public EditAccount(XMPPAccountStore accountStore,
        AccountRenderer accountRenderer) {
        super(JS_NAME);
        this.accountStore = accountStore;
        this.accountRenderer = accountRenderer;
    }

    /**
     * Edit a given {@link XMPPAccount} in the {@link XMPPAccountStore}.
     * <p>
     * Note that, on success, this action will trigger a re-rendering of the
     * account data to reflect the changes immediately. If this operation fails,
     * an error is shown to the user.
     * 
     * @param oldAccount
     *            the account to be changed
     * @param newAccount
     *            the new data for the account
     */
    @BrowserFunction
    public void editAccount(XMPPAccount oldAccount, XMPPAccount newAccount) {
        try {
            accountStore
                .changeAccountData(oldAccount, newAccount.getUsername(),
                    newAccount.getPassword(), newAccount.getDomain(),
                    newAccount.getServer(), newAccount.getPort(),
                    newAccount.useTLS(), newAccount.useSASL());
            accountRenderer.render();
        } catch (IllegalArgumentException e) {
            LOG.error("Couldn't edit account " + e.getMessage(), e);
            JavaScriptAPI.showError(browser, HTMLUIStrings.EDIT_ACCOUNT_FAILED);
        }
    }

}
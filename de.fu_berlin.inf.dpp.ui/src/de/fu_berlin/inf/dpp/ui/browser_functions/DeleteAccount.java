package de.fu_berlin.inf.dpp.ui.browser_functions;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;

/**
 * Delete an existing account from the account store. The currently active
 * account cannot be deleted.
 */
public class DeleteAccount extends TypedJavascriptFunction {

    private static final Logger LOG = Logger.getLogger(DeleteAccount.class);

    public static final String JS_NAME = "deleteAccount";

    private XMPPAccountStore accountStore;
    private AccountRenderer accountRenderer;

    /**
     * Created by PicoContainer
     * 
     * @param accountStore
     *            to redirect the delete action
     * @param accountRenderer
     *            to trigger render action after deletion
     * @see HTMLUIContextFactory
     */
    public DeleteAccount(XMPPAccountStore accountStore,
        AccountRenderer accountRenderer) {
        super(JS_NAME);
        this.accountStore = accountStore;
        this.accountRenderer = accountRenderer;
    }

    /**
     * Delete a given {@link XMPPAccount} from the {@link XMPPAccountStore}. The
     * active account cannot be deleted.
     * <p>
     * Note that, on success, this action will trigger a re-rendering of the
     * account data to reflect the changes immediately. If this operation fails,
     * an error is shown to the user.
     * 
     * @param account
     *            to be deleted
     */
    @BrowserFunction
    public void deleteAccount(XMPPAccount account) {
        try {
            accountStore.deleteAccount(account);
            accountRenderer.render();
        } catch (IllegalStateException e) {
            LOG.error("Couldn't delete account: " + e.getMessage(), e);
            JavaScriptAPI.showError(browser,
                HTMLUIStrings.ERR_ACCOUNT_DELETE_ACTIVE);
        } catch (IllegalArgumentException e) {
            LOG.error("Couldn't delete account: " + e.getMessage(), e);
            JavaScriptAPI.showError(browser,
                HTMLUIStrings.ERR_ACCOUNT_DELETE_ACTIVE);
        }
    }

}

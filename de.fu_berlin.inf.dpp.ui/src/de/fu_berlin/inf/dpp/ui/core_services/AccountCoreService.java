package de.fu_berlin.inf.dpp.ui.core_services;

import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.model.Account;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;

import java.util.ArrayList;

/**
 * Bundles all backend calls for the account creation and retrieves accounts
 * for rendering.
 */
public class AccountCoreService {

    private final XMPPAccountStore accountStore;

    private AccountRenderer renderer;

    public AccountCoreService(XMPPAccountStore accountStore) {
        this.accountStore = accountStore;
    }

    /**
     * Create an account. This methods wraps the call to
     * {@link XMPPAccountStore#createAccount(String, String, String, String, int, boolean, boolean)}.
     * Should be called from non-UI thread.
     *
     * @param jid      a string in the form 'user@domain'
     * @param password the password for the account
     */
    public void createAccount(String jid, String password) {
        String[] pair = jid.split("@");
        //TODO result of call
        accountStore
            .createAccount(pair[0], password, pair[1], "", 0, true, true);
        renderAccounts();
    }

    /**
     * May be called from both UI and non-UI thread.
     */
    private synchronized void renderAccounts() {
        if (renderer != null) {
            ArrayList<Account> res = new ArrayList<Account>();
            for (XMPPAccount xmppAccount : accountStore.getAllAccounts()) {
                res.add(new Account(xmppAccount.getUsername(),
                    xmppAccount.getDomain()));
            }
            renderer.renderAccountList(res);
        }
    }

    /**
     * Sets a new renderer and initialially renders the current accounts.
     * @param renderer the new account renderer
     */
    public synchronized void setRenderer(AccountRenderer renderer) {
        this.renderer = renderer;
        renderAccounts();
    }

    /**
     * Removes the current renderer
     */
    public synchronized void removeRenderer() {
        renderer = null;
    }
}

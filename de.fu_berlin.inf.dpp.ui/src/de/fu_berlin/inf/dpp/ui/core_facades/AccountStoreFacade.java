package de.fu_berlin.inf.dpp.ui.core_facades;

import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;

/**
 * Bundles all backend calls for the account creation and retrieves accounts
 * for rendering.
 */
public class AccountStoreFacade {

    private final XMPPAccountStore accountStore;

    private final AccountRenderer renderer;

    public AccountStoreFacade(XMPPAccountStore accountStore,
        AccountRenderer renderer) {
        this.accountStore = accountStore;
        this.renderer = renderer;
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
        renderer.render();
    }

    /**
     * Tests if an account with the given JID already exists.
     *
     * @param jid a string in the form 'user@domain'
     * @return true if it exists, false otherwise
     */
    public boolean existsAccount(String jid) {
        String[] pair = jid.split("@");
        return accountStore.exists(pair[0], pair[1], "", 0);
    }
}

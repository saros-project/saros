package de.fu_berlin.inf.dpp.account;

import java.util.List;

/**
 * Listener interface to be notified on changes to the {@link XMPPAccountStore}.
 */
public interface IAccountStoreListener {
    /**
     * Will be called whenever an operation to add, delete, or alter an existing
     * account was successfully completed.
     * 
     * @param currentAccounts
     *            A list of all currently stored accounts as of after the
     *            previous change. Implementations must not alter this list
     *            (changes to this list would not affect the underlying
     *            {@link XMPPAccountStore} anyway).
     */
    public void accountsChanged(List<XMPPAccount> currentAccounts);

    /**
     * Will be called if the currently active account has been just changed.
     * 
     * @param activeAccount
     *            The currently active account. May be identical with the
     *            previously active account.
     */
    public void activeAccountChanged(XMPPAccount activeAccount);
}

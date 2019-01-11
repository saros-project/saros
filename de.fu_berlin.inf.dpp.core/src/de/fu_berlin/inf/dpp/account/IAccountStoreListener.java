package de.fu_berlin.inf.dpp.account;

import java.util.List;

/** Listener interface to be notified on changes to the {@link XMPPAccountStore}. */
public interface IAccountStoreListener {
  /**
   * Will be called whenever an operation to add, delete, or alter an existing account was
   * successfully completed. Will also be called after the store has been initialized.
   *
   * @param currentAccounts A list of all currently stored accounts as of after the previous change.
   *     Implementations must not alter this list (changes to this list would not affect the
   *     underlying {@link XMPPAccountStore} anyway).
   *     <p>Is never <code>null</code>; if the account store is empty or not yet initialized, the
   *     list is empty.
   */
  public default void accountsChanged(List<XMPPAccount> currentAccounts) {
    // NOP
  }

  /**
   * Will be called if the currently active account has been just changed. Will also be called after
   * the store has been initialized.
   *
   * @param activeAccount The currently active account. May be identical with the previously active
   *     account.
   *     <p>Will be <code>null</code> if the account store has no active account yet (e.g. during
   *     start up or for first-time Saros users).
   */
  public default void activeAccountChanged(XMPPAccount activeAccount) {
    // NOP
  }
}

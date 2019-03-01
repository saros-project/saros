package de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation.impl;

import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation.IAccountManipulator;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;

public class AccountManipulatorImpl extends StfRemoteObject implements IAccountManipulator {

  private static final Logger LOG = Logger.getLogger(AccountManipulatorImpl.class);

  private static final IAccountManipulator INSTANCE = new AccountManipulatorImpl();

  private AccountManipulatorImpl() {
    // NOP
  }

  public static IAccountManipulator getInstance() {
    return AccountManipulatorImpl.INSTANCE;
  }

  @Override
  public void restoreDefaultAccount(String username, String password, String domain)
      throws RemoteException {

    XMPPAccountStore accountStore = getXmppAccountStore();

    for (XMPPAccount account : accountStore.getAllAccounts()) {

      if (account.equals(accountStore.getActiveAccount())) continue;

      LOG.debug("deleting account: " + account);

      accountStore.deleteAccount(account);
    }

    if (accountStore.isEmpty()) {
      accountStore.createAccount(username, password, domain, "", 0, true, true);
      return;
    }

    XMPPAccount activeAccount = accountStore.getActiveAccount();

    if (accountStore.exists(username, domain, "", 0)) return;

    XMPPAccount defaultAccount =
        accountStore.createAccount(username, password, domain, "", 0, true, true);

    LOG.debug("activating account: " + defaultAccount);
    accountStore.setAccountActive(defaultAccount);

    LOG.debug("deleting account: " + activeAccount);
    accountStore.deleteAccount(activeAccount);
  }

  @Override
  public void addAccount(String username, String password, String domain) throws RemoteException {

    XMPPAccountStore accountStore = getXmppAccountStore();

    if (accountStore.exists(username, domain, "", 0)) {
      LOG.debug(
          "account with username '" + username + "' and domain '" + domain + "' already exists");
      return;
    }

    LOG.debug("creating account for username '" + username + "' and domain '" + domain + "'");
    accountStore.createAccount(username, password, domain, "", 0, true, true);
  }

  @Override
  public boolean activateAccount(String username, String domain) throws RemoteException {

    final XMPPAccountStore accountStore = getXmppAccountStore();

    XMPPAccount activeAccount = null;

    try {
      activeAccount = accountStore.getActiveAccount();
    } catch (IllegalStateException e) {
      // ignore
    }

    for (XMPPAccount account : accountStore.getAllAccounts()) {
      if (account.getUsername().equals(username) && account.getDomain().equals(domain)) {

        if (!account.equals(activeAccount)) {
          LOG.debug("activating account: " + account);
          accountStore.setAccountActive(account);
        } else {
          LOG.debug("account is already activated: " + account);
        }

        return !account.equals(activeAccount);
      }
    }

    throw new IllegalArgumentException(
        "an account with username '" + username + "' and domain '" + domain + "' does not exist");
  }
}

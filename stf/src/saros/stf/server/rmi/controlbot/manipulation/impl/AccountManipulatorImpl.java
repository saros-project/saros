package saros.stf.server.rmi.controlbot.manipulation.impl;

import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.controlbot.manipulation.IAccountManipulator;

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

      if (account.equals(accountStore.getDefaultAccount())) continue;

      LOG.debug("deleting account: " + account);

      accountStore.deleteAccount(account);
    }

    if (accountStore.isEmpty()) {
      accountStore.createAccount(username, password, domain, "", 0, true, true);
      return;
    }
  }

  @Override
  public void addAccount(String username, String password, String domain) throws RemoteException {

    XMPPAccountStore accountStore = getXmppAccountStore();

    if (accountStore.existsAccount(username, domain, "", 0)) {
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

    final XMPPAccount accountToSetAsDefault = accountStore.getAccount(username, domain);

    if (accountToSetAsDefault == null)
      throw new IllegalArgumentException(
          "an account with username '" + username + "' and domain '" + domain + "' does not exist");

    final XMPPAccount defaultAccount = accountStore.getDefaultAccount();

    accountStore.setDefaultAccount(accountToSetAsDefault);

    return !accountToSetAsDefault.equals(defaultAccount);
  }
}

package saros.stf.server.rmi.controlbot.manipulation;

import java.rmi.Remote;
import java.rmi.RemoteException;
import saros.account.XMPPAccountStore;

/**
 * This interface gives you control to manipulate the Saros account store.
 *
 * @author Stefan Rossbach
 */
public interface IAccountManipulator extends Remote {

  /**
   * Deletes all accounts except the default one. If the default account does not exist it will be
   * created and activated.
   *
   * @param username the username of the default account
   * @param password the password of the default account which should be used if the default account
   *     does not exists
   * @param domain the domain name of the account
   * @throws RemoteException
   */
  public void restoreDefaultAccount(String username, String password, String domain)
      throws RemoteException;

  /**
   * Adds an account to the Saros XMPP account store. If the account already exists this method will
   * do nothing.
   *
   * @param username the username of the new account
   * @param password the password of the new account
   * @param domain the domain of the new account
   * @throws RemoteException
   */
  public void addAccount(String username, String password, String domain) throws RemoteException;

  /**
   * Activates an account.
   *
   * @param username the username of the account to activate
   * @param domain the domain name of the account to active
   * @return <code>true</code> if the account was successfully activated, <code>false</code> if the
   *     account is already active
   * @throws RemoteException if the account does not exist in the current {@link XMPPAccountStore}
   */
  public boolean activateAccount(String username, String domain) throws RemoteException;
}

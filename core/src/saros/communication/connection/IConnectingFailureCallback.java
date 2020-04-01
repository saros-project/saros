package saros.communication.connection;

import saros.account.XMPPAccount;

/** Simple callback interface which is being notified in case of connecting error. */
public interface IConnectingFailureCallback {

  /**
   * Gets called when it was not possible to successfully connect the requested service.
   *
   * @param account the XMPP used for the connection attempt
   * @param exception the exception that occurred during the connection attempt
   */
  void connectingFailed(XMPPAccount account, Exception exception);
}

package de.fu_berlin.inf.dpp.communication.connection;

/** Simple callback interface which is being notified in case of connecting error. */
public interface IConnectingFailureCallback {

  /**
   * Gets called when it was not possible to successfully connect the requested service.
   *
   * @param exception the exception that occurred during the connection attempt or <code>null</code>
   *     if it could not be made because of missing data
   */
  public void connectingFailed(Exception exception);
}

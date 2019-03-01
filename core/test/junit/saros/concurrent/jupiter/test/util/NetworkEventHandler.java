package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.session.User;

/**
 * handler for simulated network events.
 *
 * @author troll
 */
public interface NetworkEventHandler {

  /**
   * receive a remote document request.
   *
   * @param req
   */
  public void receiveNetworkEvent(NetworkRequest req);

  /** User of appropriate client. */
  public User getUser();
}

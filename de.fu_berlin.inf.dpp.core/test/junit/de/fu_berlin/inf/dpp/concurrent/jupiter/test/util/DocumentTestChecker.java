package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.session.User;

/**
 * interface for testing document state and content.
 *
 * @author troll
 */
public interface DocumentTestChecker {

  public User getUser();

  public String getDocument();
}

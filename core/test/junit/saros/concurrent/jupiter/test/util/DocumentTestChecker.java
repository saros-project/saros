package saros.concurrent.jupiter.test.util;

import saros.session.User;

/**
 * interface for testing document state and content.
 *
 * @author troll
 */
public interface DocumentTestChecker {

  public User getUser();

  public String getDocument();
}

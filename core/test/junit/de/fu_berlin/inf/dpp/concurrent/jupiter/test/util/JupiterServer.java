package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.session.User;

public interface JupiterServer {

  public void addProxyClient(User user);

  public void removeProxyClient(User user);
}

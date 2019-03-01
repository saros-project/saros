package saros.concurrent.jupiter.test.util;

import saros.session.User;

public interface JupiterServer {

  public void addProxyClient(User user);

  public void removeProxyClient(User user);
}

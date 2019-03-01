package saros.concurrent.jupiter.test.util;

import saros.session.User;

public class TwoWayJupiterClientDocument extends ClientSynchronizedDocument {

  public static final User client = JupiterTestCase.createUser("Client");

  public TwoWayJupiterClientDocument(String content, NetworkSimulator con) {
    super(TwoWayJupiterServerDocument.server, content, con, client);
  }
}

package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.session.User;

public class TwoWayJupiterClientDocument extends ClientSynchronizedDocument {

  public static final User client = JupiterTestCase.createUser("Client");

  public TwoWayJupiterClientDocument(String content, NetworkSimulator con) {
    super(TwoWayJupiterServerDocument.server, content, con, client);
  }
}

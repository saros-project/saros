package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.User;

public class TwoWayJupiterClientDocument extends ClientSynchronizedDocument {

    public static final User client = JupiterTestCase.createUserMock("Client");

    public TwoWayJupiterClientDocument(String content, NetworkSimulator con) {
        super(TwoWayJupiterServerDocument.server.getJID(), content, con, client);
    }
}

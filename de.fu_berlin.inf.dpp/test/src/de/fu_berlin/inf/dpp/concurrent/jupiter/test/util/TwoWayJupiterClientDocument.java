package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.net.JID;

public class TwoWayJupiterClientDocument extends ClientSynchronizedDocument {

    public static final JID jidClient = new JID("Client");

    public TwoWayJupiterClientDocument(String content, SimulateNetzwork con) {
        super(TwoWayJupiterServerDocument.jidServer, content, con, jidClient);
    }

}

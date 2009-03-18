package de.fu_berlin.inf.dpp.test.jupiter.text;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.SimulateNetzwork;

public class TwoWayJupiterClientDocument extends ClientSynchronizedDocument {

    public TwoWayJupiterClientDocument(String content, SimulateNetzwork con) {
    	super(content, con, new JID("ori79@jabber.cc"));
    }

}

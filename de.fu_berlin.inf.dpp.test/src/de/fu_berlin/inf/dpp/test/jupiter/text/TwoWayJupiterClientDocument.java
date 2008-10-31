package de.fu_berlin.inf.dpp.test.jupiter.text;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.NetworkConnection;

public class TwoWayJupiterClientDocument extends ClientSynchronizedDocument {

    public TwoWayJupiterClientDocument(String content, NetworkConnection con) {
	super(content, con);
	jid = new JID("ori79@jabber.cc");
    }

}

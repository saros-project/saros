package de.fu_berlin.inf.dpp.test.jupiter.text;

import de.fu_berlin.inf.dpp.net.JID;

public interface JupiterServer {

    public void addProxyClient(JID jid);

    public void removeProxyClient(JID jid);
}

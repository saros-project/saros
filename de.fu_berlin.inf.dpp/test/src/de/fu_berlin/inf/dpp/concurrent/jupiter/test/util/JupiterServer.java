package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.net.JID;

public interface JupiterServer {

    public void addProxyClient(JID jid);

    public void removeProxyClient(JID jid);
}

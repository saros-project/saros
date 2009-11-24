package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;

public interface JupiterServer {

    public void addProxyClient(User user);

    public void removeProxyClient(JID jid);
}

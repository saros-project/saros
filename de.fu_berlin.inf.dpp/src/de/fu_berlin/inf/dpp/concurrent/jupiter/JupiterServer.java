package de.fu_berlin.inf.dpp.concurrent.jupiter;

import java.util.HashMap;

import de.fu_berlin.inf.dpp.net.JID;

public interface JupiterServer extends SynchronizedQueue, RequestForwarder{

	public void addProxyClient(JID jid);
	
	public void removeProxyClient(JID jid);
	
	/**
	 * get current document proxies.
	 * @return
	 * @throws InterruptedException
	 */
	public HashMap<JID, JupiterClient> getProxies() throws InterruptedException;
}

package de.fu_berlin.inf.dpp.concurrent.jupiter;

import java.util.HashMap;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.net.JID;

public interface JupiterServer extends SynchronizedQueue, RequestForwarder, JupiterEditor{

	public void addProxyClient(JID jid);
	
	public void removeProxyClient(JID jid);
	
	/**
	 * get current document proxies.
	 * @return
	 * @throws InterruptedException
	 */
	public HashMap<JID, JupiterClient> getProxies() throws InterruptedException;
	
	/**
	 * get exist state of proxy client for given jid.
	 * @param jid
	 * @return
	 */
	public boolean isExist(JID jid);
	
	
	public void updateVectorTime(JID source, JID dest);
	
	public void transformationErrorOccured();

}

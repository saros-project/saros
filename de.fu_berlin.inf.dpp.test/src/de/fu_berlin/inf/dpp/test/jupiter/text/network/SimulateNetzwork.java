package de.fu_berlin.inf.dpp.test.jupiter.text.network;

import java.util.HashMap;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.NetworkRequest;

/**
 * this class simulate a network.
 * 
 * @author troll
 * 
 */
public class SimulateNetzwork {

	private static Logger logger = Logger.getLogger(SimulateNetzwork.class);

	private HashMap<JID, NetworkEventHandler> clients;

	private RuntimeException error;

	public SimulateNetzwork() {
		clients = new HashMap<JID, NetworkEventHandler>();
	}
 
	private void sendOperation(NetworkRequest req) {
		if (clients.containsKey(req.getTo())) {
			logger.debug("send message to " + req.getTo());
			clients.get(req.getTo()).receiveNetworkEvent(req);
		}
	}

	public void sendOperation(final NetworkRequest req, final int delay) {
		new Thread(new Runnable() {
			public void run() {
				logger.debug("Delay in send operation "
						+ req.getRequest().getOperation().toString() + " of "
						+ delay + " millis");
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				sendOperation(req);
			}
		}).start();
	}

	public void addClient(NetworkEventHandler remote) {
		if (!clients.containsKey(remote.getJID())) {
			clients.put(remote.getJID(), remote);
		}
	}

	public void removeClient(NetworkEventHandler remote) {
		clients.remove(remote.getJID());
	}

	public RuntimeException getLastError() {
		return error;
	}

}

package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import java.util.HashMap;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * this class simulate a network.
 * 
 * @author troll
 * 
 */
public class SimulateNetzwork {

    private static Logger log = Logger.getLogger(SimulateNetzwork.class);

    private HashMap<JID, NetworkEventHandler> clients;

    private RuntimeException error;

    public SimulateNetzwork() {
        clients = new HashMap<JID, NetworkEventHandler>();
    }

    private void sendOperation(NetworkRequest req) {
        if (clients.containsKey(req.getTo())) {
            log.debug("send message to " + req.getTo());
            clients.get(req.getTo()).receiveNetworkEvent(req);
        }
    }

    public void sendOperation(final NetworkRequest req, final int delay) {
        new Thread(new Runnable() {
            public void run() {
                log.debug("Delay in send operation "
                    + req.getJupiterActivity().getOperation().toString()
                    + " of " + delay + " millis");
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    sendOperation(req);
                } catch (RuntimeException e) {
                    error = e;
                }
            }
        }).start();
    }

    public void addClient(NetworkEventHandler remote) {
        if (!clients.containsKey(remote.getUser().getJID())) {
            clients.put(remote.getUser().getJID(), remote);
        }
    }

    public void removeClient(NetworkEventHandler remote) {
        clients.remove(remote.getUser().getJID());
    }

    public RuntimeException getLastError() {
        return error;
    }

}

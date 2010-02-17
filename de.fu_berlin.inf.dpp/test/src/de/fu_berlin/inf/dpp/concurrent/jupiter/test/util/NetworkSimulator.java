package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.util.SarosTestUtils;

/**
 * This class simulates a network.
 * 
 * TODO The use of threads is unnecessary and causes non-deterministic behavior
 * when running the tests for example in debug mode. This should be replaced by
 * an implementation that does not use threads to "send" the messages.
 * 
 * @author troll
 * 
 */
public class NetworkSimulator {

    private static Logger log = Logger.getLogger(NetworkSimulator.class);

    private HashMap<JID, NetworkEventHandler> clients;

    private RuntimeException error;

    public IProject project = SarosTestUtils.replayFluid(EasyMock
        .createMock(IProject.class));

    public IPath path = new Path("dummy");

    public NetworkSimulator() {
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

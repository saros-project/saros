package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import static de.fu_berlin.inf.dpp.test.util.SarosTestUtils.replay;
import static org.easymock.EasyMock.createMock;

import java.util.HashMap;
import java.util.PriorityQueue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * This class simulates a network.
 * 
 * @author troll
 */
public class NetworkSimulator {

    private HashMap<JID, NetworkEventHandler> clients;

    private RuntimeException error;

    public IProject project = replay(createMock(IProject.class));

    public IPath path = new Path("dummy");

    protected PriorityQueue<NetworkRequest> sendQueue = new PriorityQueue<NetworkRequest>();

    protected int presentTime = -1;

    public NetworkSimulator() {
        clients = new HashMap<JID, NetworkEventHandler>();
    }

    public void sendOperation(final NetworkRequest req) {

        if (req.getDelay() == -1) {
            clients.get(req.getTo()).receiveNetworkEvent(req);
            return;
        }

        if (req.getDelay() <= presentTime)
            throw new IllegalArgumentException(
                "Request cannot have a time in the past");

        sendQueue.add(req);
    }

    public void execute() {
        while (sendQueue.size() > 0) {
            execute(sendQueue.peek().getDelay());
        }
    }

    public void execute(int newPresentTime) {

        if (newPresentTime <= presentTime)
            throw new IllegalArgumentException();

        presentTime = newPresentTime;
        int result = 0;
        while (sendQueue.size() > 0
            && sendQueue.peek().getDelay() <= presentTime) {
            NetworkRequest nextRequest = sendQueue.poll();
            clients.get(nextRequest.getTo()).receiveNetworkEvent(nextRequest);
            result++;
        }
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

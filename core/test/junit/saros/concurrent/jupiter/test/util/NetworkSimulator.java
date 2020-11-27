package saros.concurrent.jupiter.test.util;

import java.util.HashMap;
import java.util.PriorityQueue;
import org.easymock.EasyMock;
import saros.filesystem.IFile;
import saros.session.User;

/** This class simulates a network. */
public class NetworkSimulator {

  private HashMap<User, NetworkEventHandler> clients;

  public final IFile file;

  protected PriorityQueue<NetworkRequest> sendQueue = new PriorityQueue<NetworkRequest>();

  protected int presentTime = -1;

  public NetworkSimulator() {
    file = EasyMock.createNiceMock(IFile.class);

    clients = new HashMap<User, NetworkEventHandler>();
  }

  public void sendOperation(final NetworkRequest req) {

    if (req.getDelay() == -1) {
      clients.get(req.getTo()).receiveNetworkEvent(req);
      return;
    }

    if (req.getDelay() <= presentTime)
      throw new IllegalArgumentException("Request cannot have a time in the past");

    sendQueue.add(req);
  }

  public void execute() {
    while (sendQueue.size() > 0) {
      execute(sendQueue.peek().getDelay());
    }
  }

  public void execute(int newPresentTime) {

    if (newPresentTime <= presentTime) throw new IllegalArgumentException();

    presentTime = newPresentTime;

    while (sendQueue.size() > 0 && sendQueue.peek().getDelay() <= presentTime) {
      NetworkRequest nextRequest = sendQueue.poll();
      clients.get(nextRequest.getTo()).receiveNetworkEvent(nextRequest);
    }
  }

  public void addClient(NetworkEventHandler remote) {
    if (!clients.containsKey(remote.getUser())) {
      clients.put(remote.getUser(), remote);
    }
  }

  public void removeClient(NetworkEventHandler remote) {
    clients.remove(remote.getUser());
  }
}

package saros.concurrent.jupiter.test.util;

import java.util.HashMap;
import org.apache.log4j.Logger;
import saros.activities.JupiterActivity;
import saros.concurrent.jupiter.Algorithm;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.TransformationException;
import saros.session.User;

public class ServerSynchronizedDocument
    implements JupiterServer, NetworkEventHandler, DocumentTestChecker {

  private static Logger log = Logger.getLogger(ServerSynchronizedDocument.class);

  private Document doc;
  /* sync algorithm with ack-operation list. */
  private Algorithm algorithm;

  private User user;
  private NetworkSimulator connection;

  private boolean accessDenied = false;

  private HashMap<User, ProxySynchronizedQueue> proxyQueues;

  public ServerSynchronizedDocument(NetworkSimulator connection, User user) {
    this.user = user;
    this.connection = connection;
    this.proxyQueues = new HashMap<User, ProxySynchronizedQueue>();
  }

  @Override
  public User getUser() {
    return user;
  }

  private synchronized Operation receiveOperation(JupiterActivity jupiterActivity) {

    User user = jupiterActivity.getSource();

    while (accessDenied) {
      try {
        log.debug("wait for semaphore.");
        wait();
      } catch (InterruptedException e) {
        log.error(e.getMessage());
      }
    }

    /* get semaphore */
    accessDenied = true;

    /* transformed incoming operation of client jid. */
    Operation op = null;
    try {

      /* 1. transform client JupiterActivities in client proxy. */
      ProxySynchronizedQueue proxy = proxyQueues.get(user);
      if (proxy != null) {
        op = proxy.receiveOperation(jupiterActivity);
      } else throw new TransformationException("no proxy client queue for " + user);

      /* 2. submit transformed operation to other proxies. */
      for (User u : proxyQueues.keySet()) {
        proxy = proxyQueues.get(u);

        if (!u.equals(user)) {
          log.debug(
              u
                  + " : proxy timestamp "
                  + proxy.getAlgorithm().getTimestamp()
                  + " op before : "
                  + jupiterActivity.getOperation()
                  + " req timestamp: "
                  + jupiterActivity.getTimestamp());

          /*
           * 3. create submit op as local proxy operation and send to
           * client.
           */
          proxy.sendOperation(op);

          log.debug(
              u
                  + " : vector after receive "
                  + proxy.getAlgorithm().getTimestamp()
                  + " op after : "
                  + op);
        }
      }

    } catch (TransformationException e) {
      // TODO SZ Auto-generated catch block
      e.printStackTrace();

    } finally {
      log.debug("end of lock and clear semaphore.");
      accessDenied = false;
      notifyAll();
    }

    return op;
  }

  /* send to all proxy clients. */
  public void sendOperation(Operation op) {
    /* 1. execute locally */
    doc.execOperation(op);
    /* 2. transfer proxy queues. */
    for (User user : proxyQueues.keySet()) {
      proxyQueues.get(user).sendOperation(op);
    }
  }

  /**
   * send operation only for two-way protocol test.
   *
   * @param user
   * @param op
   * @param delay
   */
  public void sendOperation(User user, Operation op, int delay) {
    /* 1. execute locally */
    doc.execOperation(op);
    /* 2. transform operation. */
    JupiterActivity jupiterActivity = algorithm.generateJupiterActivity(op, this.user, null);
    /* sent to client */
    // connection.sendOperation(jid, req,delay);
    connection.sendOperation(new NetworkRequest(jupiterActivity, user, delay));
  }

  @Override
  public String getDocument() {
    return doc.getDocument();
  }

  @Override
  public void addProxyClient(User user) {
    ProxySynchronizedQueue queue = new ProxySynchronizedQueue(user, this.connection);
    proxyQueues.put(user, queue);
  }

  @Override
  public void removeProxyClient(User user) {
    proxyQueues.remove(user);
  }

  @Override
  public void receiveNetworkEvent(NetworkRequest req) {
    receiveOperation(req.getJupiterActivity());
  }

  public Algorithm getAlgorithm() {
    return algorithm;
  }
}

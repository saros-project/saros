package saros.concurrent.jupiter.test.util;

import org.apache.log4j.Logger;
import saros.activities.JupiterActivity;
import saros.concurrent.jupiter.Algorithm;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.TransformationException;
import saros.concurrent.jupiter.internal.Jupiter;
import saros.session.User;

/**
 * This proxy class on server represent the server side of the two-way jupiter protocol.
 *
 * @author troll
 */
public class ProxySynchronizedQueue {

  private static final Logger log = Logger.getLogger(ProxySynchronizedQueue.class);

  private Algorithm algorithm;
  private NetworkSimulator connection;
  private User user;

  public ProxySynchronizedQueue(User user, NetworkSimulator connection) {
    this.user = user;
    this.algorithm = new Jupiter(false);
    this.connection = connection;
  }

  public User getUser() {
    return user;
  }

  public Operation receiveOperation(JupiterActivity jupiterActivity) {
    Operation op = null;
    try {
      log.debug(user + ": Operation before OT:" + jupiterActivity.getOperation().toString());

      op = algorithm.receiveJupiterActivity(jupiterActivity);

      log.debug(user + ": Operation after OT: " + op.toString());
    } catch (TransformationException e) {
      throw new RuntimeException(e);
    }
    return op;
  }

  public void sendOperation(Operation op) {
    JupiterActivity jupiterActivity = algorithm.generateJupiterActivity(op, this.user, null);
    connection.sendOperation(new NetworkRequest(jupiterActivity, user, -1));
  }

  public Algorithm getAlgorithm() {
    return algorithm;
  }
}

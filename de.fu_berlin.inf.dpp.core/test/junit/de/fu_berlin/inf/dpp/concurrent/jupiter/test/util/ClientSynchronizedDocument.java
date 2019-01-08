package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.session.User;
import org.apache.log4j.Logger;

/**
 * test document to simulate the client site.
 *
 * @author orieger
 */
public class ClientSynchronizedDocument implements NetworkEventHandler, DocumentTestChecker {

  private static Logger log = Logger.getLogger(ClientSynchronizedDocument.class);

  private Document doc;
  private Algorithm algorithm;

  protected User user;
  private User server;
  private NetworkSimulator connection;

  public ClientSynchronizedDocument(
      User server, String content, NetworkSimulator connection, User user) {
    this.server = server;
    this.doc = new Document(content, connection.referencePoint, connection.path);
    this.algorithm = new Jupiter(true);
    this.connection = connection;
    this.user = user;
  }

  @Override
  public User getUser() {
    return this.user;
  }

  public Operation receiveOperation(JupiterActivity jupiterActivity) {
    Operation op = null;
    try {
      log.debug("Client: " + user + " receive " + jupiterActivity.getOperation().toString());
      /* 1. transform operation. */
      op = algorithm.receiveJupiterActivity(jupiterActivity);
      // op =
      // algorithm.receiveTransformedJupiterActivity(jupiterActivity);
      /* 2. execution on server document */
      log.info("" + user + " exec: " + op.toString());
      doc.execOperation(op);
    } catch (RuntimeException e) {
      log.error("" + user + " fail: ", e);
      throw e;
    } catch (Exception e) {
      log.error("" + user + " fail: ", e);
      throw new RuntimeException(e);
    }
    return op;
  }

  public void sendOperation(Operation op) {
    sendOperation(server, op, 0);
  }

  public void sendOperation(Operation op, int delay) {
    log.info(user + " send: " + op.toString());
    sendOperation(server, op, delay);
  }

  public void sendOperation(User remoteUser, Operation op, int delay) {

    /* 1. execute locally */
    doc.execOperation(op);

    /* 2. transform operation. */
    JupiterActivity jupiterActivity = algorithm.generateJupiterActivity(op, user, null);

    /* 3. send operation. */
    connection.sendOperation(new NetworkRequest(jupiterActivity, remoteUser, delay));
  }

  public void receiveNetworkEvent(JupiterActivity jupiterActivity) {
    log.info(this.user + " receive operation : " + jupiterActivity.getOperation().toString());
    receiveOperation(jupiterActivity);
  }

  @Override
  public String getDocument() {
    return doc.getDocument();
  }

  @Override
  public void receiveNetworkEvent(NetworkRequest req) {
    log.info(
        this.user
            + " recv: "
            + req.getJupiterActivity().getOperation().toString()
            + " timestamp : "
            + req.getJupiterActivity().getTimestamp());
    receiveOperation(req.getJupiterActivity());
  }

  public Algorithm getAlgorithm() {
    return algorithm;
  }

  public void updateVectorTime(Timestamp timestamp) {
    try {
      getAlgorithm().updateVectorTime(timestamp);
    } catch (TransformationException e) {
      throw new RuntimeException(e);
    }
  }
}

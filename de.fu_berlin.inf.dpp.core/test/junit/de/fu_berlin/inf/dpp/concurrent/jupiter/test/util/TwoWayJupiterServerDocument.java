package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.Document.JupiterDocumentListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.User;
import org.apache.log4j.Logger;

public class TwoWayJupiterServerDocument implements NetworkEventHandler, DocumentTestChecker {

  public static final User server = JupiterTestCase.createUser("server");

  private static Logger log = Logger.getLogger(TwoWayJupiterServerDocument.class);

  private Document doc;
  /* sync algorithm with ack-operation list. */
  private Algorithm algorithm;

  private NetworkSimulator connection;

  public TwoWayJupiterServerDocument(String content, NetworkSimulator con) {
    this.doc = new Document(content, con.referencePoint, con.path);
    this.algorithm = new Jupiter(false);
    this.connection = con;
  }

  @Override
  public User getUser() {
    return server;
  }

  public Operation receiveOperation(JupiterActivity jupiterActivity) {
    Operation op = null;
    try {
      log.debug("Operation before OT:" + jupiterActivity.getOperation().toString());
      /* 1. transform operation. */
      op = algorithm.receiveJupiterActivity(jupiterActivity);

      log.debug("Operation after OT: " + op.toString());
      /* 2. execution on server document */
      doc.execOperation(op);
    } catch (TransformationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return op;
  }

  /**
   * send operation to special user.
   *
   * @param user
   * @param op
   */
  public void sendOperation(User user, Operation op) {
    sendOperation(user, op, 0);
  }

  public void sendOperation(User user, Operation op, int delay) {
    /* 1. execute locally */
    doc.execOperation(op);
    /* 2. transform operation. */
    JupiterActivity jupiterActivity = algorithm.generateJupiterActivity(op, server, null);
    /* sent to client */
    connection.sendOperation(new NetworkRequest(jupiterActivity, user, delay));
  }

  public void sendOperation(Operation op, int delay) {
    sendOperation(TwoWayJupiterClientDocument.client, op, delay);
  }

  public void receiveNetworkEvent(JupiterActivity jupiterActivity) {
    log.info("receive operation : " + jupiterActivity.getOperation().toString());
    receiveOperation(jupiterActivity);
  }

  @Override
  public String getDocument() {
    return doc.getDocument();
  }

  public Algorithm getAlgorithm() {
    return this.algorithm;
  }

  public void sendTransformedOperation(Operation op, JID toJID) {
    // TODO Auto-generated method stub

  }

  /** receive network request. */
  @Override
  public void receiveNetworkEvent(NetworkRequest req) {
    receiveOperation(req.getJupiterActivity());
  }

  public void addJupiterDocumentListener(JupiterDocumentListener jdl) {
    // TODO Auto-generated method stub

  }

  public void removeJupiterDocumentListener(String id) {
    // TODO Auto-generated method stub

  }

  public void updateVectorTime(Timestamp timestamp) {
    // TODO Auto-generated method stub

  }
}

package saros.concurrent.jupiter.test.util;

import org.apache.log4j.Logger;
import saros.activities.JupiterActivity;
import saros.concurrent.jupiter.Algorithm;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.Timestamp;
import saros.concurrent.jupiter.TransformationException;
import saros.concurrent.jupiter.internal.Jupiter;
import saros.concurrent.jupiter.test.util.Document.JupiterDocumentListener;
import saros.net.xmpp.JID;
import saros.session.User;

public class TwoWayJupiterServerDocument implements NetworkEventHandler, DocumentTestChecker {

  public static final User server = JupiterTestCase.createUser("server");

  private static Logger log = Logger.getLogger(TwoWayJupiterServerDocument.class);

  private Document doc;
  /* sync algorithm with ack-operation list. */
  private Algorithm algorithm;

  private NetworkSimulator connection;

  public TwoWayJupiterServerDocument(String content, NetworkSimulator con) {
    this.doc = new Document(content, con.project, con.path);
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

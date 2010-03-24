package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.Document.JupiterDocumentListener;
import de.fu_berlin.inf.dpp.net.JID;

public class TwoWayJupiterServerDocument implements NetworkEventHandler,
    DocumentTestChecker {

    public static final User server = JupiterTestCase.createUserMock("server");

    private static Logger log = Logger
        .getLogger(TwoWayJupiterServerDocument.class);

    private Document doc;
    /* sync algorithm with ack-operation list. */
    private Algorithm algorithm;

    private NetworkSimulator connection;

    public TwoWayJupiterServerDocument(String content, NetworkSimulator con) {
        this.doc = new Document(content, con.project, con.path);
        this.algorithm = new Jupiter(false);
        this.connection = con;
    }

    public JID getJID() {
        return server.getJID();
    }

    public User getUser() {
        return server;
    }

    public Operation receiveOperation(JupiterActivity jupiterActivity) {
        Operation op = null;
        try {
            log.debug("Operation before OT:"
                + jupiterActivity.getOperation().toString());
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
     * send operation to special jid.
     * 
     * @param jid
     * @param op
     */
    public void sendOperation(JID jid, Operation op) {
        sendOperation(jid, op, 0);
    }

    public void sendOperation(JID jid, Operation op, int delay) {
        /* 1. execute locally */
        doc.execOperation(op);
        /* 2. transform operation. */
        JupiterActivity jupiterActivity = algorithm.generateJupiterActivity(op,
            server, null);
        /* sent to client */
        connection
            .sendOperation(new NetworkRequest(server,
                TwoWayJupiterClientDocument.client.getJID(), jupiterActivity,
                delay));
    }

    public void sendOperation(Operation op, int delay) {
        sendOperation(TwoWayJupiterClientDocument.client.getJID(), op, delay);
    }

    public void receiveNetworkEvent(JupiterActivity jupiterActivity) {
        log.info("receive operation : "
            + jupiterActivity.getOperation().toString());
        receiveOperation(jupiterActivity);

    }

    public String getDocument() {
        return doc.getDocument();
    }

    public Algorithm getAlgorithm() {
        return this.algorithm;
    }

    public void sendTransformedOperation(Operation op, JID toJID) {
        // TODO Auto-generated method stub

    }

    /**
     * receive network request.
     */
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

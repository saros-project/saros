package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import java.util.HashMap;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.Document.JupiterDocumentListener;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * test document to simulate the client site.
 * 
 * @author orieger
 * 
 */
public class ClientSynchronizedDocument implements NetworkEventHandler,
    DocumentTestChecker {

    private static Logger logger = Logger
        .getLogger(ClientSynchronizedDocument.class);

    private Document doc;
    private Algorithm algorithm;

    protected JID jid;
    private JID server_jid;
    private SimulateNetzwork connection;

    private HashMap<String, JupiterDocumentListener> documentListener = new HashMap<String, JupiterDocumentListener>();

    public ClientSynchronizedDocument(JID server, String content,
        SimulateNetzwork con, JID jid) {
        this.server_jid = server;
        this.doc = new Document(content);
        this.algorithm = new Jupiter(true);
        this.connection = con;
        this.jid = jid;
    }

    public JID getJID() {
        return this.jid;
    }

    public void setJID(JID jid) {
        this.jid = jid;
    }

    public Operation receiveOperation(Request req) {
        Operation op = null;
        try {
            logger.debug("Client: " + jid + " receive "
                + req.getOperation().toString());
            /* 1. transform operation. */
            op = algorithm.receiveRequest(req);
            // op = algorithm.receiveTransformedRequest(req);
            /* 2. execution on server document */
            logger.info("" + jid + " exec: " + op.toString());
            doc.execOperation(op);
        } catch (RuntimeException e) {
            logger.error("" + jid + " fail: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("" + jid + " fail: ", e);
            throw new RuntimeException(e);
        }
        return op;
    }

    public void sendOperation(Operation op) {
        sendOperation(server_jid, op, 0);
    }

    public void sendOperation(Operation op, int delay) {
        logger.info(jid + " send: " + op.toString());
        sendOperation(server_jid, op, delay);
    }

    public void sendOperation(JID remoteJid, Operation op, int delay) {

        /* 1. execute locally */
        doc.execOperation(op);

        /* 2. transform operation. */
        Request req = algorithm.generateRequest(op, jid, null);

        /* 3. send operation. */
        connection.sendOperation(new NetworkRequest(this.jid, remoteJid, req),
            delay);

        informListener();
    }

    public void receiveNetworkEvent(Request req) {
        logger.info(this.jid + " receive operation : "
            + req.getOperation().toString());
        receiveOperation(req);
        informListener();
    }

    public String getDocument() {
        return doc.getDocument();
    }

    public void receiveNetworkEvent(NetworkRequest req) {
        logger.info(this.jid + " recv: "
            + req.getRequest().getOperation().toString() + " timestamp : "
            + req.getRequest().getTimestamp());
        receiveOperation(req.getRequest());
        informListener();
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    private void informListener() {
        for (String key : documentListener.keySet()) {
            documentListener.get(key).documentAction(jid);
        }
    }

    public void updateVectorTime(Timestamp timestamp) {
        try {
            getAlgorithm().updateVectorTime(timestamp);
        } catch (TransformationException e) {
            throw new RuntimeException(e);
        }
    }

}

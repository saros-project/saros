package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import java.util.HashMap;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
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

    private static Logger log = Logger
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

    public Operation receiveOperation(JupiterActivity jupiterActivity) {
        Operation op = null;
        try {
            log.debug("Client: " + jid + " receive "
                + jupiterActivity.getOperation().toString());
            /* 1. transform operation. */
            op = algorithm.receiveJupiterActivity(jupiterActivity);
            // op =
            // algorithm.receiveTransformedJupiterActivity(jupiterActivity);
            /* 2. execution on server document */
            log.info("" + jid + " exec: " + op.toString());
            doc.execOperation(op);
        } catch (RuntimeException e) {
            log.error("" + jid + " fail: ", e);
            throw e;
        } catch (Exception e) {
            log.error("" + jid + " fail: ", e);
            throw new RuntimeException(e);
        }
        return op;
    }

    public void sendOperation(Operation op) {
        sendOperation(server_jid, op, 0);
    }

    public void sendOperation(Operation op, int delay) {
        log.info(jid + " send: " + op.toString());
        sendOperation(server_jid, op, delay);
    }

    public void sendOperation(JID remoteJid, Operation op, int delay) {

        /* 1. execute locally */
        doc.execOperation(op);

        /* 2. transform operation. */
        JupiterActivity jupiterActivity = algorithm.generateJupiterActivity(op,
            jid, null);

        /* 3. send operation. */
        connection.sendOperation(new NetworkRequest(this.jid, remoteJid,
            jupiterActivity), delay);

        informListener();
    }

    public void receiveNetworkEvent(JupiterActivity jupiterActivity) {
        log.info(this.jid + " receive operation : "
            + jupiterActivity.getOperation().toString());
        receiveOperation(jupiterActivity);
        informListener();
    }

    public String getDocument() {
        return doc.getDocument();
    }

    public void receiveNetworkEvent(NetworkRequest req) {
        log.info(this.jid + " recv: "
            + req.getJupiterActivity().getOperation().toString()
            + " timestamp : " + req.getJupiterActivity().getTimestamp());
        receiveOperation(req.getJupiterActivity());
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

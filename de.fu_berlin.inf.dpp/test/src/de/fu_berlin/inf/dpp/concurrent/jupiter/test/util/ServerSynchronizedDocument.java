package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import java.util.HashMap;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.Document.JupiterDocumentListener;
import de.fu_berlin.inf.dpp.net.JID;

public class ServerSynchronizedDocument implements JupiterServer,
    NetworkEventHandler, DocumentTestChecker {

    private static Logger logger = Logger
        .getLogger(ServerSynchronizedDocument.class);

    private Document doc;
    /* sync algorithm with ack-operation list. */
    private Algorithm algorithm;

    private JID jid;
    private SimulateNetzwork connection;

    private boolean accessDenied = false;

    private HashMap<JID, ProxySynchronizedQueue> proxyQueues;

    public ServerSynchronizedDocument(SimulateNetzwork con, JID jid) {
        this.jid = jid;
        /* init network connection. */
        init(con);
    }

    /**
     * init proxy queue and network connection.
     * 
     * @param con
     */
    private void init(SimulateNetzwork con) {
        this.connection = con;
        this.proxyQueues = new HashMap<JID, ProxySynchronizedQueue>();
    }

    public void setJID(JID jid) {
        this.jid = jid;
    }

    public JID getJID() {
        return jid;
    }

    /**
     * Receive operation between server and client as two-way protocol.
     */
    public Operation receiveOperation(JupiterActivity jupiterActivity) {
        Operation op = null;
        try {
            logger.debug("Operation before OT:"
                + jupiterActivity.getOperation().toString() + " "
                + algorithm.getTimestamp());
            /* 1. transform operation. */
            op = algorithm.receiveJupiterActivity(jupiterActivity);
            logger.debug("Operation after OT: " + op.toString() + " "
                + algorithm.getTimestamp());

            /* 2. execution on server document */
            doc.execOperation(op);
        } catch (TransformationException e) {
            // TODO Raise an error
            e.printStackTrace();
        }
        return op;
    }

    private synchronized Operation receiveOperation(
        JupiterActivity jupiterActivity, JID jid) {
        while (accessDenied) {
            try {
                logger.debug("wait for semaphore.");
                wait();
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }

        /* get semaphore */
        accessDenied = true;

        /* transformed incoming operation of client jid. */
        Operation op = null;
        try {

            /* 1. transform client JupiterActivities in client proxy. */
            ProxySynchronizedQueue proxy = proxyQueues.get(jid);
            if (proxy != null) {
                op = proxy.receiveOperation(jupiterActivity);
            } else
                throw new TransformationException("no proxy client queue for "
                    + jid);

            /* 2. submit transformed operation to other proxies. */
            for (JID j : proxyQueues.keySet()) {
                proxy = proxyQueues.get(j);

                if (!j.toString().equals(jid.toString())) {
                    logger.debug(j.toString() + " : proxy timestamp "
                        + proxy.getAlgorithm().getTimestamp() + " op before : "
                        + jupiterActivity.getOperation() + " req timestamp: "
                        + jupiterActivity.getTimestamp());

                    /*
                     * 3. create submit op as local proxy operation and send to
                     * client.
                     */
                    proxy.sendOperation(op);

                    logger.debug(j.toString() + " : vector after receive "
                        + proxy.getAlgorithm().getTimestamp() + " op after : "
                        + op);
                }

            }

        } catch (TransformationException e) {
            // TODO SZ Auto-generated catch block
            e.printStackTrace();

        } finally {
            logger.debug("end of lock and clear semaphore.");
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
        for (JID jid : proxyQueues.keySet()) {
            proxyQueues.get(jid).sendOperation(op);
        }
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

    /**
     * send operation only for two-way protocol test.
     * 
     * @param jid
     * @param op
     * @param delay
     */
    public void sendOperation(JID jid, Operation op, int delay) {
        /* 1. execute locally */
        doc.execOperation(op);
        /* 2. transform operation. */
        JupiterActivity jupiterActivity = algorithm.generateJupiterActivity(op,
            this.jid, null);
        /* sent to client */
        // connection.sendOperation(jid, req,delay);
        connection.sendOperation(new NetworkRequest(this.jid, jid,
            jupiterActivity), delay);

    }

    public void receiveNetworkEvent(JupiterActivity jupiterActivity) {
        logger.info("receive operation : "
            + jupiterActivity.getOperation().toString());
        receiveOperation(jupiterActivity);

    }

    public String getDocument() {
        return doc.getDocument();
    }

    public void addProxyClient(JID jid) {
        ProxySynchronizedQueue queue = new ProxySynchronizedQueue(jid,
            this.connection);
        proxyQueues.put(jid, queue);
    }

    public void removeProxyClient(JID jid) {
        proxyQueues.remove(jid);
    }

    public void sendTransformedOperation(Operation op, JID toJID) {
        // TODO Auto-generated method stub

    }

    public void receiveNetworkEvent(NetworkRequest req) {
        // logger.debug("receive network event with networtrequest from "+req.
        // getFrom());
        receiveOperation(req.getJupiterActivity(), req.getFrom());
    }

    public Algorithm getAlgorithm() {
        return algorithm;
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

package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import java.util.HashMap;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.JID;

public class ServerSynchronizedDocument implements JupiterServer,
    NetworkEventHandler, DocumentTestChecker {

    private static Logger log = Logger
        .getLogger(ServerSynchronizedDocument.class);

    private Document doc;
    /* sync algorithm with ack-operation list. */
    private Algorithm algorithm;

    private User user;
    private NetworkSimulator connection;

    private boolean accessDenied = false;

    private HashMap<JID, ProxySynchronizedQueue> proxyQueues;

    public ServerSynchronizedDocument(NetworkSimulator connection, User user) {
        this.user = user;
        this.connection = connection;
        this.proxyQueues = new HashMap<JID, ProxySynchronizedQueue>();
    }

    public JID getJID() {
        return user.getJID();
    }

    public User getUser() {
        return user;
    }

    /**
     * Receive operation between server and client as two-way protocol.
     */
    public Operation receiveOperation(JupiterActivity jupiterActivity) {
        Operation op = null;
        try {
            log.debug("Operation before OT:"
                + jupiterActivity.getOperation().toString() + " "
                + algorithm.getTimestamp());
            /* 1. transform operation. */
            op = algorithm.receiveJupiterActivity(jupiterActivity);
            log.debug("Operation after OT: " + op.toString() + " "
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
                    log.debug(j.toString() + " : proxy timestamp "
                        + proxy.getAlgorithm().getTimestamp() + " op before : "
                        + jupiterActivity.getOperation() + " req timestamp: "
                        + jupiterActivity.getTimestamp());

                    /*
                     * 3. create submit op as local proxy operation and send to
                     * client.
                     */
                    proxy.sendOperation(op);

                    log.debug(j.toString() + " : vector after receive "
                        + proxy.getAlgorithm().getTimestamp() + " op after : "
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
        for (JID jid : proxyQueues.keySet()) {
            proxyQueues.get(jid).sendOperation(op);
        }
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
            this.user, null);
        /* sent to client */
        // connection.sendOperation(jid, req,delay);
        connection.sendOperation(new NetworkRequest(this.user, jid,
            jupiterActivity, delay));

    }

    public void receiveNetworkEvent(JupiterActivity jupiterActivity) {
        log.info("receive operation : "
            + jupiterActivity.getOperation().toString());
        receiveOperation(jupiterActivity);

    }

    public String getDocument() {
        return doc.getDocument();
    }

    public void addProxyClient(User user) {
        ProxySynchronizedQueue queue = new ProxySynchronizedQueue(user,
            this.connection);
        proxyQueues.put(user.getJID(), queue);
    }

    public void removeProxyClient(JID jid) {
        proxyQueues.remove(jid);
    }

    public void receiveNetworkEvent(NetworkRequest req) {
        receiveOperation(req.getJupiterActivity(), req.getFrom().getJID());
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}

package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import java.util.HashMap;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.User;

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

    @Override
    public JID getJID() {
        return user.getJID();
    }

    @Override
    public User getUser() {
        return user;
    }

    private synchronized Operation receiveOperation(
        JupiterActivity jupiterActivity) {

        JID jid = jupiterActivity.getSource().getJID();

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
        connection
            .sendOperation(new NetworkRequest(jupiterActivity, jid, delay));

    }

    @Override
    public String getDocument() {
        return doc.getDocument();
    }

    @Override
    public void addProxyClient(User user) {
        ProxySynchronizedQueue queue = new ProxySynchronizedQueue(user,
            this.connection);
        proxyQueues.put(user.getJID(), queue);
    }

    @Override
    public void removeProxyClient(JID jid) {
        proxyQueues.remove(jid);
    }

    @Override
    public void receiveNetworkEvent(NetworkRequest req) {
        receiveOperation(req.getJupiterActivity());
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}

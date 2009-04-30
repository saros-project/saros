package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * This proxy class on server represent the server side of the two-way jupiter
 * protocol.
 * 
 * @author troll
 * 
 */
public class ProxySynchronizedQueue {

    private static Logger logger = Logger
        .getLogger(ProxySynchronizedQueue.class);

    private Algorithm algorithm;
    private SimulateNetzwork connection;
    private JID jid;

    public ProxySynchronizedQueue(JID jid, SimulateNetzwork con) {
        this.jid = jid;
        this.algorithm = new Jupiter(false);
        this.connection = con;
    }

    public JID getJID() {
        return jid;
    }

    public Operation receiveOperation(Request req) {
        Operation op = null;
        try {
            logger.debug(jid + ": Operation before OT:"
                + req.getOperation().toString());

            op = algorithm.receiveRequest(req);

            logger.debug(jid + ": Operation after OT: " + op.toString());
        } catch (TransformationException e) {
            throw new RuntimeException(e);
        }
        return op;
    }

    /**
     * send a transformed operation to client side.
     * 
     * @param op
     *            operation has transformed and only send to client side.
     */
    // TODO Is this method necessary?
    public void sendTransformedOperation(Operation op, JID jid) {
        Request send_req = new Request(algorithm.getTimestamp(), op, this.jid,
            null);
        connection
            .sendOperation(new NetworkRequest(this.jid, jid, send_req), 0);
    }

    public void sendOperation(Operation op) {
        Request req = algorithm.generateRequest(op, this.jid, null);
        connection.sendOperation(new NetworkRequest(this.jid, jid, req), 0);
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}

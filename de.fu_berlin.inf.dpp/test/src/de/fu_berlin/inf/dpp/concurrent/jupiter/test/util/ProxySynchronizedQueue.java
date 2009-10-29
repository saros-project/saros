package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
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

    private static Logger log = Logger.getLogger(ProxySynchronizedQueue.class);

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

    public Operation receiveOperation(JupiterActivity jupiterActivity) {
        Operation op = null;
        try {
            log.debug(jid + ": Operation before OT:"
                + jupiterActivity.getOperation().toString());

            op = algorithm.receiveJupiterActivity(jupiterActivity);

            log.debug(jid + ": Operation after OT: " + op.toString());
        } catch (TransformationException e) {
            throw new RuntimeException(e);
        }
        return op;
    }

    public void sendOperation(Operation op) {
        JupiterActivity jupiterActivity = algorithm.generateJupiterActivity(op,
            this.jid, null);
        connection.sendOperation(new NetworkRequest(this.jid, jid,
            jupiterActivity), 0);
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}

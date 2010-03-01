package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.User;
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
    private NetworkSimulator connection;
    private User user;

    public ProxySynchronizedQueue(User user, NetworkSimulator connection) {
        this.user = user;
        this.algorithm = new Jupiter(false);
        this.connection = connection;
    }

    public JID getJID() {
        return user.getJID();
    }

    public Operation receiveOperation(JupiterActivity jupiterActivity) {
        Operation op = null;
        try {
            log.debug(user + ": Operation before OT:"
                + jupiterActivity.getOperation().toString());

            op = algorithm.receiveJupiterActivity(jupiterActivity);

            log.debug(user + ": Operation after OT: " + op.toString());
        } catch (TransformationException e) {
            throw new RuntimeException(e);
        }
        return op;
    }

    public void sendOperation(Operation op) {
        JupiterActivity jupiterActivity = algorithm.generateJupiterActivity(op,
            this.user, null);
        connection.sendOperation(new NetworkRequest(this.user, user.getJID(),
            jupiterActivity, -1));
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}

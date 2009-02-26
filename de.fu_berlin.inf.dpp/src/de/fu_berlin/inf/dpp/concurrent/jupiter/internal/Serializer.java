package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * FIXME The Serializer is never stopped!
 */
public class Serializer {

    private static Logger log = Logger.getLogger(Serializer.class);

    protected JupiterServer server;

    public Serializer(JupiterServer server) {
        this.server = server;
    }

    protected void dispatch() throws InterruptedException,
        TransformationException {

        // get next request in queue.
        Request request = server.getNextRequestInSynchronizedQueue();

        HashMap<JID, JupiterClient> proxies = server.getProxies();

        // 1. execute receive action at appropriate proxy client.
        JupiterClient sourceProxy = proxies.get(request.getJID());
        Operation op = sourceProxy.receiveRequest(request);

        // 2. execute generate action at other proxy clients.
        for (Map.Entry<JID, JupiterClient> entry : proxies.entrySet()) {

            JID jid = entry.getKey();
            JupiterClient remoteProxy = entry.getValue();

            if (!jid.toString().equals(request.getJID().toString())) {
                // create submit op as local proxy operation and send to client.
                remoteProxy.generateRequest(op);
            }
        }
    }

    public void start() {
        Serializer.log.debug("Start Serializer");

        Util.runSafeAsync("Serializer-", log, new Runnable() {
            public void run() {

                while (true) {
                    try {
                        dispatch();
                    } catch (InterruptedException e) {
                        Serializer.log.warn("Interrupt Exception", e);
                        return;
                    } catch (TransformationException e) {
                        Serializer.log.error("Transformation Exception ", e);
                        server.transformationErrorOccured();
                    }
                }
            }
        });
    }
}

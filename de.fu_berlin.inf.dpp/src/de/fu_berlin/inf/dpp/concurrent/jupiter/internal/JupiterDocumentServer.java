package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.RequestForwarder;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * TODO [CO] Document and review this class
 * 
 * FIXME JupiterDocumentServer is never stopped
 */
public class JupiterDocumentServer implements JupiterServer {

    private static Logger logger = Logger
        .getLogger(JupiterDocumentServer.class);

    /**
     * List of proxy clients.
     */
    protected final HashMap<JID, JupiterClient> proxies;

    /**
     * Incoming Queue of Request to apply to the proxies.
     */
    protected final BlockingQueue<Request> incomingQueue;

    /**
     * outgoing queue to transfer request to appropriate clients.
     */
    protected final BlockingQueue<Request> outgoingQueue;

    protected final IPath editor;

    /**
     * forward outgoing request to activity sequencer;
     */
    protected final RequestTransmitter transmitter;

    protected final Serializer serializer;

    /**
     * this forwarder reads request form the local outgoing queue and transmit
     * the requests to the global outgoing queue.
     */
    class RequestTransmitter {

        private final RequestForwarder rf;

        public RequestTransmitter(RequestForwarder forw) {
            this.rf = forw;
        }

        public void start() {
            Util.runSafeAsync("JupiterDocumentServer-" + editor.lastSegment()
                + "-", logger, new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            rf.forwardOutgoingRequest(getNextOutgoingRequest());
                        } catch (InterruptedException e) {
                            return;
                        } catch (RuntimeException e) {
                            logger.error("Failed to forward request: ", e);
                        }

                    }
                }
            });
        }
    }

    /**
     * this constructor init an external request forwarder. The generate answer
     * request of the proxy clients forwarding to this forwarder.
     */
    public JupiterDocumentServer(RequestForwarder forwarder, IPath path) {
        this.editor = path;
        this.proxies = new HashMap<JID, JupiterClient>();
        this.incomingQueue = new LinkedBlockingQueue<Request>();
        this.outgoingQueue = new LinkedBlockingQueue<Request>();

        this.serializer = new Serializer(this);
        this.transmitter = new RequestTransmitter(forwarder);

        this.transmitter.start();
        this.serializer.start();
    }

    public void addProxyClient(JID jid) {
        this.proxies.put(jid, new ProxyJupiterDocument(jid, this, editor));
    }

    /**
     * TODO: sync with serializer.
     * 
     * 1. save current action count 2. stop serializer after this count and
     * remove client.
     */
    public void removeProxyClient(JID jid) {
        this.proxies.remove(jid);
    }

    /**
     * add request from transmitter to request queue.
     * 
     * TODO: Sync with serializer.
     */
    public void addRequest(Request request) {
        this.incomingQueue.add(request);
    }

    /**
     * next message in request queue.
     */
    public Request getNextRequestInSynchronizedQueue()
        throws InterruptedException {
        return incomingQueue.take();
    }

    /*
     * TODO Make sure that this is not a problem:
     * 
     * Was Passiert, wenn während der Bearbeitung ein neuer proxy eingefügt
     * wird?
     */
    public HashMap<JID, JupiterClient> getProxies() {
        return this.proxies;
    }

    /* start transfer section. */

    /**
     * Called from Proxies when they want to add generated request to outgoing queue.
     */
    public void forwardOutgoingRequest(Request req) {
        this.outgoingQueue.add(req);
    }

    /**
     * transmitter interface get next request for transfer.
     */
    public Request getNextOutgoingRequest() throws InterruptedException {
        return outgoingQueue.take();
    }

    public boolean isExist(JID jid) {
        if (this.proxies.containsKey(jid)) {
            return true;
        }
        return false;
    }

    public void updateVectorTime(JID source, JID dest) {
        JupiterClient proxy = this.proxies.get(source);
        if (proxy != null) {
            try {
                Timestamp ts = proxy.getTimestamp();
                getProxies().get(dest).updateVectorTime(
                    new JupiterVectorTime(ts.getComponents()[1], ts
                        .getComponents()[0]));
            } catch (TransformationException e) {
                JupiterDocumentServer.logger.error(
                    "Error during update vector time for " + dest, e);
            }
        } else {
            JupiterDocumentServer.logger
                .error("No proxy found for given source jid: " + source);
        }

    }

    public void transformationErrorOccured() {
        forwardOutgoingRequest(new RequestError(this.editor));
    }

}

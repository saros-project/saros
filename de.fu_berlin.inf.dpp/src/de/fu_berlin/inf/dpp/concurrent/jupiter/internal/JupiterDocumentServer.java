package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * The JupiterDocumentServer is the host side component managing all server
 * Jupiter instances.
 * 
 * TODO [CO] Document and review this class
 */
public class JupiterDocumentServer {

    private static Logger logger = Logger
        .getLogger(JupiterDocumentServer.class);

    /**
     * List of proxy clients.
     */
    protected final HashMap<JID, Jupiter> proxies = new HashMap<JID, Jupiter>();

    protected final IPath editor;

    /**
     * this constructor init an external request forwarder. The generate answer
     * request of the proxy clients forwarding to this forwarder.
     */
    public JupiterDocumentServer(IPath path) {
        this.editor = path;
    }

    public void addProxyClient(JID jid) {
        this.proxies.put(jid, new Jupiter(false));
    }

    /**
     * TODO SZ Removing a proxy client needs to be synced probably
     */
    public void removeProxyClient(JID jid) {
        this.proxies.remove(jid);
    }

    public Map<JID, Request> transformRequest(Request request)
        throws TransformationException {

        Map<JID, Request> result = new HashMap<JID, Request>();

        JID source = request.getJID();

        // 1. Use JupiterClient of sender to transform request
        Jupiter sourceProxy = proxies.get(source);
        Operation op = sourceProxy.receiveRequest(request);

        // 2. Generate outgoing requests for all other clients and the host
        for (Map.Entry<JID, Jupiter> entry : proxies.entrySet()) {

            JID jid = entry.getKey();

            // Skip sender
            if (jid.equals(source))
                continue;

            Jupiter remoteProxy = entry.getValue();

            Request transformed = remoteProxy.generateRequest(op, source,
                editor);

            result.put(jid, transformed);
        }

        return result;
    }

    /*
     * TODO Make sure that this is not a problem:
     * 
     * Was Passiert, wenn während der Bearbeitung ein neuer proxy eingefügt
     * wird?
     */
    public HashMap<JID, Jupiter> getProxies() {
        return this.proxies;
    }

    public boolean isExist(JID jid) {
        if (this.proxies.containsKey(jid)) {
            return true;
        }
        return false;
    }

    public void updateVectorTime(JID source, JID dest) {
        Jupiter proxy = this.proxies.get(source);
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
}

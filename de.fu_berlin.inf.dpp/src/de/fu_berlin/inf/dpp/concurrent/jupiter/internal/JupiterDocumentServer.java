package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
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

    private static Logger log = Logger.getLogger(JupiterDocumentServer.class);

    /**
     * List of proxy clients.
     */
    protected final HashMap<JID, Jupiter> proxies = new HashMap<JID, Jupiter>();

    protected final SPath editor;

    /**
     * Create a new JupiterDocument (server-side) representing the document
     * identified by the given SPath
     */
    public JupiterDocumentServer(SPath path) {
        this.editor = path;
    }

    public synchronized void addProxyClient(JID jid) {
        if (!this.proxies.containsKey(jid))
            this.proxies.put(jid, new Jupiter(false));
    }

    public synchronized boolean removeProxyClient(JID jid) {
        return this.proxies.remove(jid) != null;
    }

    public Map<JID, JupiterActivity> transformJupiterActivity(
        JupiterActivity jupiterActivity) throws TransformationException {

        Map<JID, JupiterActivity> result = new HashMap<JID, JupiterActivity>();

        User source = jupiterActivity.getSource();

        // 1. Use JupiterClient of sender to transform JupiterActivity
        Jupiter sourceProxy = proxies.get(source.getJID());
        Operation op = sourceProxy.receiveJupiterActivity(jupiterActivity);

        // 2. Generate outgoing JupiterActivities for all other clients and the
        // host
        for (Map.Entry<JID, Jupiter> entry : proxies.entrySet()) {

            JID jid = entry.getKey();

            // Skip sender
            if (jid.equals(source.getJID()))
                continue;

            Jupiter remoteProxy = entry.getValue();

            JupiterActivity transformed = remoteProxy.generateJupiterActivity(
                op, source, editor);

            result.put(jid, transformed);
        }

        return result;
    }

    public boolean isExist(JID jid) {
        if (this.proxies.containsKey(jid)) {
            return true;
        }
        return false;
    }

    public synchronized void updateVectorTime(JID source, JID dest) {
        Jupiter proxy = this.proxies.get(source);
        if (proxy != null) {
            try {
                Timestamp ts = proxy.getTimestamp();
                this.proxies.get(dest).updateVectorTime(
                    new JupiterVectorTime(ts.getComponents()[1], ts
                        .getComponents()[0]));
            } catch (TransformationException e) {
                JupiterDocumentServer.log.error(
                    "Error during update vector time for " + dest, e);
            }
        } else {
            JupiterDocumentServer.log
                .error("No proxy found for given source jid: " + source);
        }

    }

    public synchronized void reset(JID jid) {
        if (removeProxyClient(jid))
            addProxyClient(jid);
    }

    public Map<JID, ChecksumActivity> withTimestamp(
        ChecksumActivity checksumActivity) throws TransformationException {

        Map<JID, ChecksumActivity> result = new HashMap<JID, ChecksumActivity>();

        User source = checksumActivity.getSource();

        // 1. Verify that this checksum can still be sent to others...
        Jupiter sourceProxy = proxies.get(source.getJID());

        boolean isCurrent = sourceProxy.isCurrent(checksumActivity
            .getTimestamp());

        if (!isCurrent)
            return result; // Checksum is no longer valid => discard

        // 2. Put timestamp into all resulting checksums
        for (Map.Entry<JID, Jupiter> entry : proxies.entrySet()) {

            JID jid = entry.getKey();

            // Skip sender
            if (jid.equals(source.getJID()))
                continue;

            Jupiter remoteProxy = entry.getValue();

            ChecksumActivity timestamped = checksumActivity
                .withTimestamp(remoteProxy.getTimestamp());
            result.put(jid, timestamped);
        }

        return result;
    }
}

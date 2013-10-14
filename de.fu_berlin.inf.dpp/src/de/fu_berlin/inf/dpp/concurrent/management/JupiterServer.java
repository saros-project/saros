package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * A JupiterServer manages Jupiter server instances for a number of users AND
 * number of paths.
 * 
 * (in contrast to a JupiterDocumentServer which only handles a single path)
 */
public class JupiterServer {

    /**
     * Jupiter server instance documents
     * 
     * @host
     */
    private HashMap<SPath, JupiterDocumentServer> concurrentDocuments = new HashMap<SPath, JupiterDocumentServer>();

    private Set<JID> currentClients = new HashSet<JID>();

    private ISarosSession sarosSession;

    public JupiterServer(ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
    }

    public synchronized void removePath(SPath path) {
        concurrentDocuments.remove(path);
    }

    public synchronized void addUser(User user) {
        JID jid = user.getJID();
        currentClients.add(jid);
        for (JupiterDocumentServer server : concurrentDocuments.values()) {
            server.addProxyClient(jid);
        }
    }

    public synchronized void removeUser(User user) {
        JID jid = user.getJID();
        currentClients.remove(jid);
        for (JupiterDocumentServer server : concurrentDocuments.values()) {
            server.removeProxyClient(jid);
        }
    }

    /**
     * @host
     */
    /*
     * FIXME this does currently only work the first time a project is added ...
     * to really fix the issue we have to track the project ID / resources that
     * are added to the project sharing for each user !
     */
    protected synchronized JupiterDocumentServer getServer(SPath path) {

        JupiterDocumentServer docServer = concurrentDocuments.get(path);

        if (docServer == null) {
            Set<JID> clients = new HashSet<JID>(currentClients);

            clients.add(sarosSession.getHost().getJID());

            docServer = new JupiterDocumentServer(path);

            for (JID client : clients)
                docServer.addProxyClient(client);

            concurrentDocuments.put(path, docServer);
        }
        return docServer;
    }

    public synchronized void reset(SPath path, JID jid) {
        getServer(path).reset(jid);
    }

    public synchronized Map<JID, JupiterActivity> transform(
        JupiterActivity jupiterActivity) throws TransformationException {

        JupiterDocumentServer docServer = getServer(jupiterActivity.getPath());

        return docServer.transformJupiterActivity(jupiterActivity);
    }

    public synchronized Map<JID, ChecksumActivity> withTimestamp(
        ChecksumActivity checksumActivity) throws TransformationException {

        JupiterDocumentServer docServer = getServer(checksumActivity.getPath());

        return docServer.withTimestamp(checksumActivity);
    }

}

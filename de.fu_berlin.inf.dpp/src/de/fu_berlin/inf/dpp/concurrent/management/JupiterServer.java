package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.HashMap;
import java.util.Map;

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
    protected HashMap<SPath, JupiterDocumentServer> concurrentDocuments = new HashMap<SPath, JupiterDocumentServer>();

    private ISarosSession sarosSession;

    public JupiterServer(ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
    }

    public synchronized void removePath(SPath path) {
        concurrentDocuments.remove(path);
    }

    public synchronized void addUser(User user) {
        JID jid = user.getJID();
        for (JupiterDocumentServer server : concurrentDocuments.values()) {
            server.addProxyClient(jid);
        }
    }

    public synchronized void removeUser(User user) {
        JID jid = user.getJID();
        for (JupiterDocumentServer server : concurrentDocuments.values()) {
            server.removeProxyClient(jid);
        }
    }

    /**
     * @host
     */
    protected synchronized JupiterDocumentServer getServer(SPath path) {

        JupiterDocumentServer docServer = this.concurrentDocuments.get(path);

        if (docServer == null) {
            // Create new document server
            docServer = new JupiterDocumentServer(path);

            // Create new local host document client
            docServer.addProxyClient(sarosSession.getHost().getJID());

            /** Add all users with {@link User.Permission#WRITE_ACCESS} */
            for (User userWithWriteAccess : sarosSession.getUsersWithWriteAccess()) {
                docServer.addProxyClient(userWithWriteAccess.getJID());
            }

            this.concurrentDocuments.put(path, docServer);
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
        ChecksumActivity checksumActivityDataObject)
        throws TransformationException {

        JupiterDocumentServer docServer = getServer(checksumActivityDataObject
            .getPath());

        return docServer.withTimestamp(checksumActivityDataObject);
    }

}

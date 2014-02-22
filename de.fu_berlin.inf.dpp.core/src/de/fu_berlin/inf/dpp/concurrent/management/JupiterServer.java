package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

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

    private Set<User> currentClients = new HashSet<User>();

    private ISarosSession sarosSession;

    public JupiterServer(ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
    }

    public synchronized void removePath(SPath path) {
        concurrentDocuments.remove(path);
    }

    public synchronized void addUser(User user) {
        JID jid = user.getJID();
        currentClients.add(user);
        for (JupiterDocumentServer server : concurrentDocuments.values()) {
            server.addProxyClient(jid);
        }
    }

    public synchronized void removeUser(User user) {
        JID jid = user.getJID();
        currentClients.remove(user);
        for (JupiterDocumentServer server : concurrentDocuments.values()) {
            server.removeProxyClient(jid);
        }
    }

    /**
     * Retrieves the JupiterDocumentServer for a given path. If no
     * JupiterDocumentServer exists for this path, a new one is created and
     * returned afterwards.
     * 
     * @host
     */
    /*
     * TODO This solution currently just works for partial sharing by
     * coincidence. To really fix the issue we have to expand the
     * SarosSessionMapper to also track the resources and not just the projects
     * that are already shared for every user individually.
     */
    protected synchronized JupiterDocumentServer getServer(SPath path) {

        JupiterDocumentServer docServer = concurrentDocuments.get(path);

        if (docServer == null) {

            docServer = new JupiterDocumentServer(path);

            for (User client : currentClients) {
                /*
                 * Make sure that we only add clients that already have the
                 * resources in question. Other clients that haven't accepted
                 * the Project yet will be added later.
                 */
                if (sarosSession.userHasProject(client, path.getProject())) {
                    docServer.addProxyClient(client.getJID());
                }
            }
            docServer.addProxyClient(sarosSession.getHost().getJID());

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

package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProject;

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
    protected HashMap<IPath, JupiterDocumentServer> concurrentDocuments = new HashMap<IPath, JupiterDocumentServer>();

    private ISharedProject project;

    public JupiterServer(ISharedProject project) {
        this.project = project;
    }

    public synchronized void removePath(IPath path) {
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
    protected synchronized JupiterDocumentServer getServer(IPath path) {

        JupiterDocumentServer docServer = this.concurrentDocuments.get(path);

        if (docServer == null) {
            // Create new document server
            docServer = new JupiterDocumentServer(path);

            // Create new local host document client
            docServer.addProxyClient(project.getHost().getJID());

            // Add all drivers
            for (User driver : project.getDrivers()) {
                docServer.addProxyClient(driver.getJID());
            }

            this.concurrentDocuments.put(path, docServer);
        }
        return docServer;
    }

    public synchronized void reset(IPath path, JID jid) {
        getServer(path).reset(jid);
    }

    public synchronized Map<JID, JupiterActivity> transform(
        JupiterActivity jupiterActivity) throws TransformationException {

        JupiterDocumentServer docServer = getServer(jupiterActivity
            .getEditorPath());

        return docServer.transformJupiterActivity(jupiterActivity);
    }

    public synchronized Map<JID, ChecksumActivityDataObject> withTimestamp(
        ChecksumActivityDataObject checksumActivityDataObject)
        throws TransformationException {

        JupiterDocumentServer docServer = getServer(checksumActivityDataObject
            .getPath());

        return docServer.withTimestamp(checksumActivityDataObject);
    }

}

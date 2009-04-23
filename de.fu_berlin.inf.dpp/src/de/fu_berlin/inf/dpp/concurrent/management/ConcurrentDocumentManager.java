package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.util.Util;

public class ConcurrentDocumentManager implements Disposable {

    public static enum Side {
        CLIENT_SIDE, HOST_SIDE
    }

    private static Logger logger = Logger
        .getLogger(ConcurrentDocumentManager.class);

    /**
     * Jupiter server instance documents
     * 
     * @host
     */
    private HashMap<IPath, JupiterDocumentServer> concurrentDocuments;

    /**
     * Jupiter instances for each local editor.
     * 
     * @host and @client
     */
    private final HashMap<IPath, Jupiter> clientDocs = new HashMap<IPath, Jupiter>();

    private final JID host;

    private final JID myJID;

    private final Side side;

    private final ActivitySequencer sequencer;

    private final ISharedProject sharedProject;

    private final ISharedProjectListener projectListener;

    private final IActivityReceiver activityReceiver = new AbstractActivityReceiver() {
        @Override
        public boolean receive(EditorActivity editor) {

            /*
             * TODO Host: start and stop jupiter server process depending on
             * editor activities of remote clients. Client: start and stop local
             * jupiter clients depending on editor activities.
             */

            // We did not handle it!
            return false;
        }

        @Override
        public boolean receive(TextEditActivity textEdit) {

            Jupiter document = getClientDoc(textEdit.getEditor());
            final Request request = document.generateRequest(textEdit
                .toOperation(), myJID, textEdit.getEditor());

            if (isHostSide()) {

                // TODO ConcurrentDocumentManager should not depend on
                // Transmitter.

                // Skip network and apply directly but make sure that we use
                // the same thread as the messages that really arrive via
                // the network.
                sharedProject.getTransmitter().executeAsDispatch(
                    new Runnable() {
                        public void run() {
                            receiveRequestHostSide(request);
                        }
                    });

                /*
                 * This activity still needs to be sent to all observers,
                 * because they are not notified by receiveRequestHostSide(...).
                 */
                return false;
            } else {

                // TODO ConcurrentDocumentManager should not depend on
                // ActivitySequencer.

                // Send to host
                Collection<JID> hostJID = new ArrayList<JID>(1);
                hostJID.add(host);
                List<IActivity> requests = new ArrayList<IActivity>(1);
                requests.add(request);
                sequencer.sendActivities(hostJID, requests);
                return true;
            }
        }
    };

    public ConcurrentDocumentManager(final Side side, User host, JID myJID,
        final ISharedProject sharedProject, ActivitySequencer sequencer) {

        this.side = side;
        this.host = host.getJID();
        this.myJID = myJID;
        this.sharedProject = sharedProject;
        this.sequencer = sequencer;

        if (isHostSide()) {
            this.concurrentDocuments = new HashMap<IPath, JupiterDocumentServer>();

            projectListener = new HostSideProjectListener();
        } else {
            projectListener = new ClientSideProjectListener();
        }
        sharedProject.addListener(projectListener);
    }

    public void dispose() {
        sharedProject.removeListener(projectListener);
    }

    /**
     * ISharedProjectListener for updating Jupiter documents on the host.
     * 
     * @host
     */
    public class HostSideProjectListener extends AbstractSharedProjectListener {

        @Override
        public void roleChanged(User user, boolean replicated) {
            JID jid = user.getJID();

            if (isHost(jid))
                return;

            /* if driver changed to observer */
            if (user.isObserver()) {
                /*
                 * The following code only removes drivers (and does not add
                 * them), because new drivers are added lazily, once they edit a
                 * file
                 */
                userLeft(user);
            }
        }

        @Override
        public void userLeft(User user) {
            /* remove user proxies from jupiter server. */
            JID jid = user.getJID();
            for (JupiterDocumentServer server : concurrentDocuments.values()) {
                if (server.isExist(jid)) {
                    server.removeProxyClient(jid);
                }
            }
        }
    }

    /**
     * ISharedProjectListener used to reset Jupiter on the client side, when the
     * user is no longer a driver.
     * 
     * @client
     */
    public class ClientSideProjectListener extends
        AbstractSharedProjectListener {

        @Override
        public void roleChanged(User user, boolean replicated) {
            // Clear clientdocs
            if (user.getJID().equals(myJID)) {
                clientDocs.clear();
            }
        }
    }

    /**
     * This is called (only) from the ActivitySequencer when a local activity
     * has been caused by User activity.
     * 
     * If the event is handled by Jupiter then true should be returned, false
     * otherwise.
     * 
     * @host and @client
     */
    public boolean activityCreated(IActivity activity) {
        return activity.dispatch(activityReceiver);
    }

    /**
     * This is called (only) from the JupiterHandler (the network layer) when a
     * remote activity has been received.
     * 
     * Synchronizes the given request with the jupiter server document (if host)
     * and local clients (if host or client) and applies the request locally.
     * 
     * @host and @client
     */
    public void receiveRequest(Request request) {
        if (isHostSide()) {
            receiveRequestHostSide(request);
        } else {
            receiveRequestClientSide(request);
        }
    }

    public boolean shouldBeManagedByJupiter(JID jid) {
        return sharedProject.getHost().getJID().equals(jid)
            || sharedProject.getParticipant(jid).isDriver();
    }

    public boolean isHostSide() {
        return this.side == Side.HOST_SIDE;
    }

    public boolean isHost(JID jid) {
        return jid != null && jid.equals(this.host);
    }

    public void execFileActivity(IActivity activity) {
        if (activity instanceof FileActivity) {

            FileActivity file = (FileActivity) activity;
            if (file.getType() == FileActivity.Type.Created) {
                // Do nothing
            }
            if (file.getType() == FileActivity.Type.Removed) {
                if (isHostSide()) {
                    /* remove jupiter document server */
                    this.concurrentDocuments.remove(file.getPath());
                }
                this.clientDocs.remove(file.getPath());
            }
        }
    }

    /**
     * This method is called when a given Request should be executed locally.
     * 
     * It will be transformed and executed in the SWT thread to ensure that no
     * user activity occurs in between.
     * 
     * @host and @client
     */
    private void execTextEditActivity(final Request request) {

        final Jupiter jupiterClient = getClientDoc(request.getEditorPath());

        Util.runSafeSWTSync(logger, new Runnable() {
            public void run() {
                Operation op;
                try {
                    op = jupiterClient.receiveRequest(request);
                } catch (TransformationException e) {
                    ConcurrentDocumentManager.logger.error(
                        "Error during transformation: ", e);

                    /*
                     * TODO If this happens we should start an consistency check
                     * automatically
                     */
                    return;
                }

                /* execute activity in activity sequencer. */
                for (TextEditActivity textEdit : op.toTextEdit(request
                    .getEditorPath(), request.getSource())) {

                    sequencer.execTransformedActivity(textEdit);
                }
            }
        });
    }

    /**
     * Create or remove proxies on the JupiterDocumentServer depending on the
     * activity.
     * 
     * @host
     */
    public void execEditorActivity(IActivity activity) {

        if (!isHostSide())
            return;

        if (!(activity instanceof EditorActivity))
            return;

        EditorActivity editorActivity = (EditorActivity) activity;

        JID sourceJID = new JID(editorActivity.getSource());

        if (!shouldBeManagedByJupiter(sourceJID))
            return;

        if (!(editorActivity.getType() == Type.Activated || editorActivity
            .getType() == Type.Closed))
            return;

        // Now: We are on the host, and a driver closed or activated an editor

        JupiterDocumentServer server = getJupiterServer(editorActivity
            .getPath());

        if (!server.isExist(sourceJID)) {
            // add proxy for this combination of editor and client
            if (editorActivity.getType() == Type.Activated) {
                server.addProxyClient(sourceJID);
            }
        } else {
            // remove proxy for this combination of editor and client
            if (editorActivity.getType() == Type.Closed) {
                /*
                 * TODO Currently we still keep this ProxyClient, because
                 * creating ProxyClients is asynchronous to the edit operations
                 */
                // server.removeProxyClient(sourceJID);
            }
        }
    }

    public Jupiter getClientDoc(IPath path) {

        Jupiter clientDoc = this.clientDocs.get(path);

        if (clientDoc == null) {
            clientDoc = new Jupiter(true);
            this.clientDocs.put(path, clientDoc);
        }
        return clientDoc;
    }

    private JupiterDocumentServer getJupiterServer(IPath path) {

        JupiterDocumentServer docServer = this.concurrentDocuments.get(path);

        if (docServer == null) {
            /* create new document server. */
            docServer = new JupiterDocumentServer(path);

            /* create new local host document client. */
            docServer.addProxyClient(this.host);

            this.concurrentDocuments.put(path, docServer);
        }
        return docServer;
    }

    /**
     * 
     * @host
     * 
     * @notSWT This method may not be called from SWT, otherwise a deadlock
     *         might occur!!
     */
    protected synchronized void receiveRequestHostSide(Request request) {

        assert isHostSide() : "receiveRequestHostSide called on the Client";

        assert !Util.isSWT() : "receiveRequestHostSide called from SWT";

        // Get JupiterServer
        JupiterDocumentServer docServer = getJupiterServer(request
            .getEditorPath());

        // Check if sender exists in proxy list
        JID sender = new JID(request.getSource());
        if (!docServer.isExist(sender)) {
            docServer.addProxyClient(sender);
        }

        /* sync request with jupiter document server. */
        Map<JID, Request> outgoing;
        try {
            outgoing = docServer.transformRequest(request);
        } catch (TransformationException e) {
            // TODO this should trigger a consistency check
            logger.error("Transformation error: ", e);
            return;
        }

        for (Entry<JID, Request> entry : outgoing.entrySet()) {

            JID to = entry.getKey();
            Request transformed = entry.getValue();

            if (to.equals(host)) {
                execTextEditActivity(transformed);
            } else {
                Collection<JID> recipient = new ArrayList<JID>(1);
                recipient.add(to);
                List<IActivity> requests = new ArrayList<IActivity>(1);
                requests.add(transformed);
                sequencer.sendActivities(recipient, requests);
            }
        }
    }

    /**
     * @client
     */
    protected void receiveRequestClientSide(Request request) {

        assert !isHostSide() : "receiveRequestClientSide called on the Host";

        if (request.getOperation() instanceof TimestampOperation) {

            // TODO Use timestamps correctly or discard this code
            logger.warn("Timestamp operations are not tested at the moment");

            Jupiter jupClient = getClientDoc(request.getEditorPath());

            try {
                jupClient.updateVectorTime(request.getTimestamp());
            } catch (TransformationException e) {
                logger.error(
                    "Jupiter [Client] - Error during vector time update for "
                        + request.getEditorPath(), e);
            }
        } else {
            /*
             * This is an answer we received from the Host upon our
             * JupiterRequest
             * 
             * Transform it locally and execute it.
             */
            execTextEditActivity(request);
        }
    }

    /**
     * Resets the JupiterServer for the given combination and path and user.
     * 
     * When this is called on the host, a call to resetJupiterClient should be
     * executed at the same time on the side of the given user.
     * 
     * @host
     */
    public void resetJupiterServer(JID jid, IPath path) {

        assert isHostSide();

        if (this.concurrentDocuments.containsKey(path)) {
            JupiterDocumentServer server = this.concurrentDocuments.get(path);

            if (server.isExist(jid)) {
                logger.info("Resetting jupiter server for [" + jid.getBase()
                    + "]: " + path.toOSString());
                server.removeProxyClient(jid);
                server.addProxyClient(jid);
            } else {
                logger.warn("No Jupiter server for user [" + jid.getBase()
                    + "]: " + path.toOSString());
            }
        } else {
            logger.warn("No Jupiter server for path: " + path.toOSString());
        }
    }

    /**
     * Resets the JupiterClient for the given path.
     * 
     * When this is called on the client (or on the host for one of his
     * JupiterClient), a call to resetJupiterServer should be executed at the
     * same time on the side of the host.
     * 
     * @client and @host This can be called on the host as well, if the host
     *         wants to reset his client document (which at the moment never
     *         happens, because the version of the host is the authoritative one
     *         and thus does not need to be reset).
     */
    public void resetJupiterClient(IPath path) {
        if (this.clientDocs.containsKey(path)) {
            logger.debug("Resetting jupiter client: " + path.toOSString());
            this.clientDocs.remove(path);
            this.clientDocs.put(path, new Jupiter(true));
        } else {
            logger.warn("No Jupiter document exists for path: "
                + path.toOSString());
        }
    }

    /**
     * Returns true if a JupiterServer exists for the given path and a proxy
     * document for the given user exists.
     */
    public boolean isManagedByJupiterServer(JID jid, IPath path) {
        JupiterDocumentServer server = this.concurrentDocuments.get(path);
        if (server == null)
            return false;
        else
            return server.isExist(jid);
    }

    /**
     * Returns true if a JupiterClient exists for the given path.
     */
    public boolean isManagedByJupiter(IPath path) {
        return this.clientDocs.containsKey(path);
    }

    /**
     * Returns true, if there is a clientDoc for the path referenced by the
     * given TextEditActivity.
     * 
     * If the given activity is not a TextEditActivity or if there is no
     * JupiterClient for the path, then false is returned.
     * 
     * @sugar This method is syntactic sugar for checking whether this is a
     *        TextEditActivity and calling isManagedByJupiter(Path) using the
     *        path of the TextEditActivity.
     */
    public boolean isManagedByJupiter(IActivity activity) {

        if (activity instanceof TextEditActivity) {
            TextEditActivity textEdit = (TextEditActivity) activity;

            return clientDocs.containsKey(textEdit.getEditor());
        }
        return false;
    }
}

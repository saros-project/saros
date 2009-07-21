package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.util.Util;

public class ConcurrentDocumentManager implements Disposable {

    public static enum Side {
        CLIENT_SIDE, HOST_SIDE
    }

    private static Logger log = Logger
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

    private final User host;

    private final JID myJID;

    // TODO [MR] Remove.
    private final Side side;

    private final ActivitySequencer sequencer;

    private final ISharedProject sharedProject;

    private final ISharedProjectListener projectListener;

    protected final IActivityReceiver activityReceiver = new AbstractActivityReceiver() {
        @Override
        public boolean receive(EditorActivity editorActivity) {
            execEditorActivity(editorActivity);
            return false;
        }

        @Override
        public boolean receive(FileActivity fileActivity) {
            if (fileActivity.getType() == FileActivity.Type.Removed) {
                IPath path = fileActivity.getPath();
                if (isHostSide()) {
                    /* remove jupiter document server */
                    concurrentDocuments.remove(path);
                }
                clientDocs.remove(path);
            }
            return false;
        }

        @Override
        public boolean receive(FolderActivity folderActivity) {
            /*
             * TODO Folder activities like deleting a folder are not handled
             * yet. Then all affected documents managed by the
             * ConcurrentDocumentManager have to be deleted too.
             */
            return false;
        }

        /**
         * This is called (only) from the JupiterHandler (the network layer)
         * when a remote activity has been received.
         * 
         * Synchronizes the given JupiterActivity with the jupiter server
         * document (if host) and local clients (if host or client) and applies
         * the JupiterActivity locally.
         * 
         * @host and @client
         */
        @Override
        public boolean receive(JupiterActivity jupiterActivity) {
            if (isHostSide()) {
                receiveJupiterActivityHostSide(jupiterActivity);
            } else {
                receiveJupiterActivityClientSide(jupiterActivity);
            }
            return true;
        }

        @Override
        public boolean receive(TextEditActivity textEditActivity) {
            return isHostSide() || isManagedByJupiter(textEditActivity);
        }

        /**
         * Create or remove proxies on the JupiterDocumentServer depending on
         * the activity.
         * 
         * @host
         */
        protected void execEditorActivity(EditorActivity editorActivity) {

            if (!isHostSide())
                return;

            JID sourceJID = new JID(editorActivity.getSource());

            if (!shouldBeManagedByJupiter(sourceJID))
                return;

            EditorActivity.Type type = editorActivity.getType();
            if (!(type == EditorActivity.Type.Activated || type == EditorActivity.Type.Closed))
                return;

            // Now: We are on the host, and a driver closed or activated an
            // editor

            JupiterDocumentServer server = getJupiterServer(editorActivity
                .getPath());

            if (!server.isExist(sourceJID)) {
                // add proxy for this combination of editor and client
                if (type == EditorActivity.Type.Activated) {
                    server.addProxyClient(sourceJID);
                }
            } else {
                // remove proxy for this combination of editor and client
                if (type == EditorActivity.Type.Closed) {
                    /*
                     * TODO Currently we still keep this ProxyClient, because
                     * creating ProxyClients is asynchronous to the edit
                     * operations
                     */
                    // server.removeProxyClient(sourceJID);
                }
            }
        }
    };

    /**
     * Queue containing the JupiterActivity to be executed locally strictly in
     * this order.
     */
    protected Queue<JupiterActivity> executionQueue = new LinkedBlockingQueue<JupiterActivity>();

    private final IActivityReceiver activityCreatedReceiver = new AbstractActivityReceiver() {
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
            final JupiterActivity jupiterActivity = document
                .generateJupiterActivity(textEdit.toOperation(), myJID,
                    textEdit.getEditor());

            if (isHostSide()) {

                // TODO ConcurrentDocumentManager should not depend on
                // Transmitter.

                // Skip network and apply directly but make sure that we use
                // the same thread as the messages that really arrive via
                // the network.
                sharedProject.getTransmitter().executeAsDispatch(
                    new Runnable() {
                        public void run() {
                            receiveJupiterActivityHostSide(jupiterActivity);
                        }
                    });

                /*
                 * This activity still needs to be sent to all observers,
                 * because they are not notified by
                 * receiveJupiterActivityHostSide(...).
                 */
                return false;
            } else {
                /*
                 * TODO ConcurrentDocumentManager should not depend on
                 * ActivitySequencer.
                 */
                sequencer.sendActivity(host, jupiterActivity);
                return true;
            }
        }
    };

    public ConcurrentDocumentManager(final Side side, User host, JID myJID,
        final ISharedProject sharedProject, ActivitySequencer sequencer) {

        this.side = side;
        this.host = host;
        this.myJID = myJID;
        this.sharedProject = sharedProject;
        this.sequencer = sequencer;

        if (isHostSide()) {
            this.concurrentDocuments = new HashMap<IPath, JupiterDocumentServer>();
            projectListener = new HostSideProjectListener();
        } else {
            this.concurrentDocuments = null;
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
        public void roleChanged(User user) {

            if (user.isHost()) {
                return;
            }
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
        public void roleChanged(User user) {
            // Clear clientdocs
            if (user.isLocal()) {
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
        return activity.dispatch(activityCreatedReceiver);
    }

    public boolean shouldBeManagedByJupiter(JID jid) {
        User user = sharedProject.getParticipant(jid);
        return user.isHost() || user.isDriver();
    }

    /*
     * TODO Is this ever different from #sharedProject.isHost()!?
     */
    public boolean isHostSide() {
        return this.side == Side.HOST_SIDE;
    }

    /**
     * Executes the given activities and returns a list of activities that still
     * must be executed by other {@link IActivityProvider}s.
     * 
     * Must be executed in the Saros thread.
     */
    public List<IActivity> exec(List<IActivity> activities) {
        List<IActivity> result = new ArrayList<IActivity>();
        for (IActivity activity : activities) {
            try {
                boolean consumed = activity.dispatch(activityReceiver);
                if (!consumed) {
                    result.add(activity);
                }
            } catch (Exception e) {
                log.error("Error while executing activity.", e);
            }
        }
        return result;
    }

    /**
     * This method is called when a given JupiterActivity should be executed
     * locally.
     * 
     * It will be transformed and executed in the SWT thread to ensure that no
     * user activity occurs in between.
     * 
     * @host and @client
     */
    protected void execTextEditActivity(final JupiterActivity jupiterActivity) {

        executionQueue.add(jupiterActivity);

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {

                while (executionQueue.size() > 0) {
                    JupiterActivity jupiterActivity = executionQueue.poll();
                    if (jupiterActivity == null)
                        return;
                    exec(jupiterActivity);
                }
            }

            private void exec(JupiterActivity jupiterActivity) {

                Jupiter jupiterClient = getClientDoc(jupiterActivity
                    .getEditorPath());

                Operation op;
                try {
                    op = jupiterClient.receiveJupiterActivity(jupiterActivity);
                    // log.trace("\n  " + "Transforming: "
                    // + jupiterActivity.getOperation() + " ("
                    // + jupiterActivity.getTimestamp() + ")\n" +
                    // "  into        : "
                    // + op);
                } catch (TransformationException e) {
                    ConcurrentDocumentManager.log.error(
                        "Error during transformation: ", e);

                    /*
                     * TODO If this happens we should start an consistency check
                     * automatically
                     */
                    return;
                }

                /* execute activity in activity sequencer. */
                for (TextEditActivity textEdit : op.toTextEdit(jupiterActivity
                    .getEditorPath(), jupiterActivity.getSource())) {

                    sharedProject.execTransformedActivity(textEdit);
                }
            }
        });
    }

    protected synchronized Jupiter getClientDoc(IPath path) {

        Jupiter clientDoc = this.clientDocs.get(path);

        if (clientDoc == null) {
            clientDoc = new Jupiter(true);
            this.clientDocs.put(path, clientDoc);
        }
        return clientDoc;
    }

    protected synchronized JupiterDocumentServer getJupiterServer(IPath path) {

        JupiterDocumentServer docServer = this.concurrentDocuments.get(path);

        if (docServer == null) {
            /* create new document server. */
            docServer = new JupiterDocumentServer(path);

            /* create new local host document client. */
            docServer.addProxyClient(this.host.getJID());

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
    protected void receiveJupiterActivityHostSide(
        JupiterActivity jupiterActivity) {

        assert isHostSide() : "receiveJupiterActivityHostSide called on the Client";

        assert !Util.isSWT() : "receiveJupiterActivityHostSide called from SWT";

        // Get JupiterServer
        JupiterDocumentServer docServer = getJupiterServer(jupiterActivity
            .getEditorPath());

        // Check if sender exists in proxy list
        JID sender = new JID(jupiterActivity.getSource());
        if (!docServer.isExist(sender)) {
            docServer.addProxyClient(sender);
        }

        /* sync jupiterActivity with jupiter document server. */
        Map<JID, JupiterActivity> outgoing;
        try {
            outgoing = docServer.transformJupiterActivity(jupiterActivity);
        } catch (TransformationException e) {
            // TODO this should trigger a consistency check
            log.error("Transformation error: ", e);
            return;
        }

        for (Entry<JID, JupiterActivity> entry : outgoing.entrySet()) {

            User to = sharedProject.getUser(entry.getKey());
            JupiterActivity transformed = entry.getValue();

            if (to.equals(host)) {
                execTextEditActivity(transformed);
            } else {
                sequencer.sendActivity(to, transformed);
            }
        }
    }

    /**
     * @client
     */
    protected void receiveJupiterActivityClientSide(
        JupiterActivity jupiterActivity) {

        assert !isHostSide() : "receiveJupiterActivityClientSide called on the Host";

        if (jupiterActivity.getOperation() instanceof TimestampOperation) {

            // TODO Use timestamps correctly or discard this code
            log.warn("Timestamp operations are not tested at the moment");

            Jupiter jupClient = getClientDoc(jupiterActivity.getEditorPath());

            try {
                jupClient.updateVectorTime(jupiterActivity.getTimestamp());
            } catch (TransformationException e) {
                log.error(
                    "Jupiter [Client] - Error during vector time update for "
                        + jupiterActivity.getEditorPath(), e);
            }
        } else {
            /*
             * This is an answer we received from the Host upon our
             * JupiterActivity
             * 
             * Transform it locally and execute it.
             */
            execTextEditActivity(jupiterActivity);
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
    public synchronized void resetJupiterServer(JID jid, IPath path) {

        assert isHostSide();

        if (this.concurrentDocuments.containsKey(path)) {
            JupiterDocumentServer server = this.concurrentDocuments.get(path);

            if (server.isExist(jid)) {
                log.info("Resetting jupiter server for [" + jid.getBase()
                    + "]: " + path.toOSString());
                server.removeProxyClient(jid);
                server.addProxyClient(jid);
            } else {
                log.warn("No Jupiter server for user [" + jid.getBase() + "]: "
                    + path.toOSString());
            }
        } else {
            log.warn("No Jupiter server for path: " + path.toOSString());
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
            log.debug("Resetting jupiter client: " + path.toOSString());
            this.clientDocs.remove(path);
            this.clientDocs.put(path, new Jupiter(true));
        } else {
            log.warn("No Jupiter document exists for path: "
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
            return isManagedByJupiter(textEdit.getEditor());
        }
        return false;
    }
}

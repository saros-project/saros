package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.ObservableValue;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * TODO Make ConsistencyWatchDog configurable => Timeout, Whether run or not,
 * etc.
 */
public class ConcurrentDocumentManager {

    public static enum Side {
        CLIENT_SIDE, HOST_SIDE
    }

    private static Logger logger = Logger
        .getLogger(ConcurrentDocumentManager.class);

    /** Jupiter server instance documents */
    private HashMap<IPath, JupiterDocumentServer> concurrentDocuments;

    /** current open editor at client side. */
    private final HashMap<IPath, Jupiter> clientDocs = new HashMap<IPath, Jupiter>();

    private final JID host;

    private final JID myJID;

    private final Side side;

    private final ActivitySequencer sequencer;

    private final ConsistencyWatchdog consistencyWatchdog = new ConsistencyWatchdog(
        "ConsistencyWatchdog");

    private final ObservableValue<Boolean> inconsistencyToResolve = new ObservableValue<Boolean>(
        false);

    private final Set<IPath> pathsWithWrongChecksums = new CopyOnWriteArraySet<IPath>();

    private final ISharedProject sharedProject;

    private final ISharedProjectListener projectListener;

    /**
     * Returns the TextFileBuffer associated with this project relative path OR
     * null if the path could not be traced to a Buffer.
     */
    public ITextFileBuffer getTextFileBuffer(IPath docPath) {

        if (sharedProject == null)
            return null;

        IResource resource = sharedProject.getProject().findMember(docPath);
        if (resource == null)
            return null;

        IPath fullPath = resource.getFullPath();

        ITextFileBufferManager tfbm = FileBuffers.getTextFileBufferManager();

        ITextFileBuffer fileBuff = tfbm.getTextFileBuffer(fullPath,
            LocationKind.IFILE);
        if (fileBuff != null)
            return fileBuff;
        else {
            try {
                tfbm.connect(fullPath, LocationKind.IFILE,
                    new NullProgressMonitor());
            } catch (CoreException e) {
                return null;
            }
            return tfbm.getTextFileBuffer(fullPath, LocationKind.IFILE);
        }
    }

    /**
     * This class is an eclipse job run on the host side ONLY.
     * 
     * The job computes checksums for all files currently managed by Jupiter
     * (the ConcurrentDocumentManager) and sends them to all guests.
     * 
     * These will call their ConcurrentDocumentManager.check(...) method, to
     * verify that their version is correct.
     * 
     * Once started with schedule() the job is scheduled to rerun every 5
     * seconds.
     * 
     * @author chjacob
     */
    public class ConsistencyWatchdog extends Job {

        public ConsistencyWatchdog(String name) {
            super(name);
        }

        // this map holds for all jupiter controlled documents the checksums
        private final HashMap<IPath, DocumentChecksum> docsChecksums = new HashMap<IPath, DocumentChecksum>();

        @Override
        protected IStatus run(IProgressMonitor monitor) {

            assert isHostSide() : "This job is intended to be run on host side!";

            // If connection is closed, checking does not make sense...
            if (Saros.getDefault().getConnectionState() != ConnectionState.CONNECTED) {
                // Reschedule the next run in 30 seconds
                schedule(30000);
                return Status.OK_STATUS;
            }

            Set<IDocument> missingDocuments = new HashSet<IDocument>(
                registeredDocuments);

            // Update Checksums for all documents controlled by jupiter
            for (IPath docPath : clientDocs.keySet()) {

                // Get document
                ITextFileBuffer fileBuff = getTextFileBuffer(docPath);

                // TODO CO Handle missing files correctly
                if (fileBuff == null) {
                    logger.error("Can't get File Buffer");
                    docsChecksums.remove(docPath);
                    continue;
                }
                IDocument doc = fileBuff.getDocument();

                // Update listener management
                missingDocuments.remove(doc);
                if (!registeredDocuments.contains(doc)) {
                    registeredDocuments.add(doc);
                    doc.addDocumentListener(dirtyListener);
                    dirtyDocument.add(doc);
                }

                // If document not changed, skip
                if (!dirtyDocument.contains(doc))
                    continue;

                // If no entry for this document exists create a new one
                if (docsChecksums.get(docPath) == null) {
                    DocumentChecksum c = new DocumentChecksum(docPath, doc
                        .getLength(), doc.get().hashCode());
                    docsChecksums.put(docPath, c);
                } else {
                    // else set new length and hash
                    DocumentChecksum c = docsChecksums.get(docPath);
                    c.setLength(doc.getLength());
                    c.setHash(doc.get().hashCode());
                }
            }

            // Reset dirty states
            dirtyDocument.clear();

            // Unregister all documents that are no longer there
            for (IDocument missing : missingDocuments) {
                registeredDocuments.remove(missing);
                missing.removeDocumentListener(dirtyListener);
            }

            // Send to all Clients
            if (docsChecksums.values().size() > 0) {
                // TODO Connection of Transmitter might be closed at the moment
                Saros.getDefault().getSessionManager().getTransmitter()
                    .sendDocChecksumsToClients(docsChecksums.values());
            }

            // Reschedule the next run in 10 seconds
            schedule(10000);
            return Status.OK_STATUS;
        }

        Set<IDocument> registeredDocuments = new HashSet<IDocument>();

        Set<IDocument> dirtyDocument = new HashSet<IDocument>();

        public IDocumentListener dirtyListener = new IDocumentListener() {

            public void documentAboutToBeChanged(DocumentEvent event) {
                // we are only interested in events after the change
            }

            public void documentChanged(DocumentEvent event) {
                dirtyDocument.add(event.getDocument());
            }
        };

        public void stop() {

            // Cancel Job
            cancel();

            // Unregister from all documents
            for (IDocument document : registeredDocuments) {
                document.removeDocumentListener(dirtyListener);
            }
            registeredDocuments.clear();

            // Reset all dirty states
            dirtyDocument.clear();
        }
    }

    public ConcurrentDocumentManager(final Side side, User host, JID myJID,
        ISharedProject sharedProject, ActivitySequencer sequencer) {

        this.side = side;
        this.host = host.getJID();
        this.myJID = myJID;
        this.sharedProject = sharedProject;
        this.sequencer = sequencer;

        if (isHostSide()) {
            this.concurrentDocuments = new HashMap<IPath, JupiterDocumentServer>();
            logger.debug("Starting consistency watchdog");
            consistencyWatchdog.setSystem(true);
            consistencyWatchdog.setPriority(Job.SHORT);
            consistencyWatchdog.schedule();

            projectListener = new HostSideProjectListener();
        } else {
            projectListener = new ClientSideProjectListener();
        }

        sharedProject.addListener(projectListener);

        Saros.getDefault().getSessionManager().addSessionListener(
            new AbstractSessionListener() {

                @Override
                public void sessionEnded(ISharedProject endedProject) {

                    assert endedProject == ConcurrentDocumentManager.this.sharedProject;

                    Saros.getDefault().getSessionManager()
                        .removeSessionListener(this);

                    endedProject.removeListener(projectListener);

                    if (isHostSide()) {
                        consistencyWatchdog.stop();
                    }

                    // TODO we should not need this
                    pathsWithWrongChecksums.clear();
                }
            });
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
                userLeft(jid);
            }

        }

        @Override
        public void userLeft(JID user) {
            /* remove user proxies from jupiter server. */
            for (JupiterDocumentServer server : concurrentDocuments.values()) {
                if (server.isExist(user)) {
                    server.removeProxyClient(user);
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
     * Called from the ActivitySequencer when a local activity has occurred.
     * 
     * If the event is handled by Jupiter then true should be returned, false
     * otherwise.
     */
    public boolean activityCreated(IActivity activity) {

        return activity.dispatch(new AbstractActivityReceiver() {
            @Override
            public boolean receive(EditorActivity editor) {

                /*
                 * Host: start and stop jupiter server process depending on
                 * editor activities of remote clients. Client: start and stop
                 * local jupiter clients depending on editor activities.
                 */

                // TODO Consistency Check?
                if (editor.getType() == Type.Saved) {
                    // // calculate checksum for saved file
                    // long checksum = FileUtil.checksum(this.sharedProject
                    // .getProject().getFile(editor.getPath()));
                    // editor.setChecksum(checksum);
                    // ConcurrentDocumentManager.logger
                    //.debug("Add checksumme to created editor save activity : "
                    // + checksum
                    // + " for path : "
                    // + editor.getPath().toOSString());
                }

                // We did not handle it!
                return false;
            }

            @Override
            public boolean receive(TextEditActivity textEdit) {

                Jupiter document = getClientDoc(textEdit.getEditor());
                final Request request = document.generateRequest(textEdit
                    .toOperation());
                request.setJID(myJID);
                request.setEditorPath(textEdit.getEditor());

                if (isHostSide()) {

                    // Skip network and apply directly but make sure that we use
                    // the same thread as the messages that really arrive via
                    // the network.
                    Saros.getDefault().getSessionManager().getTransmitter()
                        .executeAsDispatch(new Runnable() {
                            public void run() {
                                receiveRequestHostSide(request);
                            }
                        });

                    /*
                     * This activity still needs to be sent to all observers,
                     * because they are not notified by
                     * receiveRequestHostSide(...).
                     */
                    return false;
                } else {
                    // Send to host
                    sequencer.forwardOutgoingRequest(host, request);
                    return true;
                }
            }
        });
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

                // Client Side
                this.clientDocs.remove(file.getPath());
            }
        }
    }

    // TODO CJ: review
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
                    .getEditorPath(), request.getJID().toString())) {

                    sequencer.execTransformedActivity(textEdit);
                }
            }
        });
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

    /**
     * Create or remove proxies on the JupiterDocumentServer depending on the
     * activity.
     * 
     * 
     * 
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
                // server.removeProxyClient(sourceJID);
            }
        }

        // TODO We don't understand
        // /* update vector time for new proxy. */
        // // TODO: stop serializer and after this update
        // // vector time.
        // server.updateVectorTime(this.myJID, sourceJID);
        // // TODO: forward vector time method.
        //
        // /* get vector time of host for this editor path. */
        // try {
        //
        // Jupiter jupC = this.clientDocs.get(editorActivity
        // .getPath());
        // if (jupC != null) {
        // Timestamp ts = jupC.getTimestamp();
        //
        // /* create update vector time request. */
        // Request updateRequest = new RequestImpl(0,
        // new JupiterVectorTime(ts.getComponents()[1], ts
        // .getComponents()[0]),
        // new TimestampOperation());
        // updateRequest.setEditorPath(editorActivity
        // .getPath());
        // updateRequest.setJID(sourceJID);
        //
        // this.sequencer.forwardOutgoingRequest(sourceJID,
        // updateRequest);
        // }
        // } catch (Exception e) {
        //
        // ConcurrentDocumentManager.logger.error(
        // "Error during get timestamp of host proxy for "
        // + editorActivity.getPath(), e);
        // }
        // }

        // else {
        // /* create new jupiter proxy client. */
        // if (editorActivity.getType() == Type.Activated) {
        // Request createRequest = new RequestImpl(0,
        // new JupiterVectorTime(0, 0), new TimestampOperation());
        // createRequest.setEditorPath(editorActivity.getPath());
        // createRequest.setJID(sourceJID);
        //
        // }
        // }

    }

    private JupiterDocumentServer initDocumentServer(IPath path) {

        /* create new document server. */
        JupiterDocumentServer docServer = new JupiterDocumentServer(path);

        /* create new local host document client. */
        docServer.addProxyClient(this.host);
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

        // Get JupiterServer
        JupiterDocumentServer docServer = getJupiterServer(request
            .getEditorPath());

        // Check if sender exists in proxy list
        JID sender = request.getJID();
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
                sequencer.forwardOutgoingRequest(to, transformed);
            }
        }
    }

    private JupiterDocumentServer getJupiterServer(IPath path) {
        JupiterDocumentServer docServer;
        /**
         * if no jupiter document server exists.
         */
        if (!this.concurrentDocuments.containsKey(path)) {
            docServer = initDocumentServer(path);
            this.concurrentDocuments.put(path, docServer);
        }
        docServer = this.concurrentDocuments.get(path);
        return docServer;
    }

    /**
     * Synchronizes the given request with the jupiter server document (if host)
     * and local clients (if host or client).
     * 
     * This is called only from the JupiterHandler (the network layer).
     */
    public void receiveRequest(Request request) {
        if (isHostSide()) {
            receiveRequestHostSide(request);
        } else {
            receiveRequestClientSide(request);
        }
    }

    public Jupiter getClientDoc(IPath path) {

        if (this.clientDocs.containsKey(path)) {
            return this.clientDocs.get(path);
        } else {
            Jupiter client = new Jupiter(true);
            this.clientDocs.put(path, client);
            return client;
        }
    }

    protected void receiveRequestClientSide(Request request) {

        if (request.getOperation() instanceof TimestampOperation) {

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
            logger.warn("Resetting jupiter server for [" + jid.getBase()
                + "]: " + path.toOSString());

            JupiterDocumentServer server = this.concurrentDocuments.get(path);

            if (server.isExist(jid)) {
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

    ExecutorService executor = new ThreadPoolExecutor(1, 1, 0,
        TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1),
        new NamedThreadFactory("ChecksumCruncher-"));

    /**
     * Starts a new Consistency Check.
     * 
     * If a check is already in progress, nothing happens (but a warning)
     * 
     * @nonBlocking This method returns immediately.
     */
    public void checkConsistency() {

        try {
            executor.submit(new Runnable() {
                public void run() {
                    try {
                        performCheck(currentChecksums);
                    } catch (RuntimeException e) {
                        logger.error("Failed to check consistency", e);
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            /*
             * Ignore Checksums that arrive before we are done processing the
             * last set of Checksums.
             */
            logger
                .warn("Received Checksums before processing of previous checksums finished");
        }

    }

    List<DocumentChecksum> currentChecksums;

    public void setChecksums(List<DocumentChecksum> checksums) {
        this.currentChecksums = checksums;
    }

    /**
     * Checks the local documents against the given checksums.
     * 
     * Use the VariableProxy getConsistenciesToResolve() to be notified if
     * inconsistencies are found or resolved.
     * 
     * @param checksums
     *            the checksums to check the documents against
     * 
     * @nonReentrant This method cannot be called twice at the same time.
     */
    public void performCheck(List<DocumentChecksum> checksums) {

        if (checksums == null) {
            logger
                .warn("Consistency Check triggered with out preceeding call to setChecksums()");
            return;
        }

        logger.debug(String.format(
            "Received %d checksums for %d inconsistencies", checksums.size(),
            pathsWithWrongChecksums.size()));

        for (DocumentChecksum checksum : checksums) {
            if (isInconsistent(checksum)) {

                ConcurrentDocumentManager.this.pathsWithWrongChecksums
                    .add(checksum.getPath());

                if (!inconsistencyToResolve.getValue()) {
                    inconsistencyToResolve.setValue(true);
                }
                return;
            }
        }

        ConcurrentDocumentManager.this.pathsWithWrongChecksums.clear();

        if (inconsistencyToResolve.getValue()) {
            logger.debug("All Inconsistencies are resolved");
            inconsistencyToResolve.setValue(false);
        }
    }

    private boolean isInconsistent(DocumentChecksum checksum) {
        IPath path = checksum.getPath();

        IFile file = sharedProject.getProject().getFile(path);
        if (!file.exists()) {
            return true;
        }

        IDocument doc = EditorManager.getDefault().getDocument(path);

        // if doc == null there is no editor with this resource open
        if (doc == null) {
            // get Document from FileBuffer
            doc = getTextFileBuffer(path).getDocument();
        }

        // if doc is still null give up
        if (doc == null) {
            logger
                .warn("Could not check checksum of file " + path.toOSString());
            return false;
        }

        if ((doc.getLength() != checksum.getLength())
            || (doc.get().hashCode() != checksum.getHash())) {

            long lastEdited = (EditorManager.getDefault().getLastEditTime(path));

            long lastRemoteEdited = (EditorManager.getDefault()
                .getLastRemoteEditTime(path));

            if ((System.currentTimeMillis() - lastEdited) > 4000
                && (System.currentTimeMillis() - lastRemoteEdited > 4000)) {

                logger.debug(String.format(
                    "Inconsistency detected: %s L(%d %s %d) H(%x %s %x)", path
                        .toString(), doc.getLength(),
                    doc.getLength() == checksum.getLength() ? "==" : "!=",
                    checksum.getLength(), doc.get().hashCode(), doc.get()
                        .hashCode() == checksum.getHash() ? "==" : "!=",
                    checksum.getHash()));

                return true;
            }
        }
        return false;
    }

    /**
     * Returns the variable proxy which stores the current inconsistency state
     * 
     */
    public ObservableValue<Boolean> getConsistencyToResolve() {
        return this.inconsistencyToResolve;
    }

    public Set<IPath> getPathsWithWrongChecksums() {
        return this.pathsWithWrongChecksums;
    }

    /**
     * Returns true, if there is a clientDoc for the path referenced by the
     * given TextEditActivity.
     * 
     * If the given activity is not a TextEditActivity or if there is no
     * JupiterClient for the path, then false is returned.
     */
    public boolean isManagedByJupiter(IActivity activity) {

        if (activity instanceof TextEditActivity) {
            TextEditActivity textEdit = (TextEditActivity) activity;

            return clientDocs.containsKey(textEdit.getEditor());
        }
        return false;
    }
}

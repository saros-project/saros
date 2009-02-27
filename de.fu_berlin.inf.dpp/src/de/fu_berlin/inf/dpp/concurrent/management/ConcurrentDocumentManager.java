package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.concurrent.IConcurrentManager;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.RequestForwarder;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.RequestImpl;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.IActivitySequencer;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.ObservableValue;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * TODO Make ConsistencyWatchDog configurable => Timeout, Whether run or not,
 * etc.
 */
public class ConcurrentDocumentManager implements IConcurrentManager {

    private static Logger logger = Logger
        .getLogger(ConcurrentDocumentManager.class);

    /** Jupiter server instance documents */
    private HashMap<IPath, JupiterDocumentServer> concurrentDocuments;

    /** current open editor at client side. */
    private final HashMap<IPath, JupiterDocumentClient> clientDocs = new HashMap<IPath, JupiterDocumentClient>();

    private JID host;

    private final JID myJID;

    private final Side side;

    private RequestForwarder forwarder;

    private IActivitySequencer sequencer;

    private final ConsistencyWatchdog consistencyWatchdog = new ConsistencyWatchdog(
        "ConsistencyWatchdog");

    private ObservableValue<Boolean> inconsistencyToResolve = new ObservableValue<Boolean>(
        false);

    private Set<IPath> pathsWithWrongChecksums = new CopyOnWriteArraySet<IPath>();

    private ISharedProject sharedProject;

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
    private class ConsistencyWatchdog extends Job {

        public ConsistencyWatchdog(String name) {
            super(name);
        }

        // this map holds for all jupiter controlled documents the checksums
        private final HashMap<IPath, DocumentChecksum> docsChecksums = new HashMap<IPath, DocumentChecksum>();

        @Override
        protected IStatus run(IProgressMonitor monitor) {

            assert isHostSide() : "This job is intended to be run on host side!";

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
        ISharedProject sharedProject) {

        this.side = side;
        this.host = host.getJID();
        this.myJID = myJID;
        this.sharedProject = sharedProject;

        if (isHostSide()) {
            this.concurrentDocuments = new HashMap<IPath, JupiterDocumentServer>();
            logger.debug("Starting consistency watchdog");
            consistencyWatchdog.setSystem(true);
            consistencyWatchdog.setPriority(Job.SHORT);
            consistencyWatchdog.schedule();
        }

        Saros.getDefault().getSessionManager().addSessionListener(
            new AbstractSessionListener() {

                @Override
                public void sessionEnded(ISharedProject endedProject) {

                    assert endedProject == ConcurrentDocumentManager.this.sharedProject;

                    Saros.getDefault().getSessionManager()
                        .removeSessionListener(this);

                    if (isHostSide()) {
                        consistencyWatchdog.stop();
                    }

                    // TODO we should not need this
                    pathsWithWrongChecksums.clear();
                }
            });

    }

    public void setActivitySequencer(IActivitySequencer sequencer) {
        this.sequencer = sequencer;
    }

    public void setRequestForwarder(RequestForwarder f) {
        this.forwarder = f;
    }

    public RequestForwarder getRequestForwarder() {
        return this.forwarder;
    }

    public IActivity activityCreated(IActivity activity) {

        if (activity instanceof EditorActivity) {
            EditorActivity editor = (EditorActivity) activity;

            // TODO Consistency Check?
            if (editor.getType() == Type.Saved) {
                // // calculate checksum for saved file
                // long checksum = FileUtil.checksum(this.sharedProject
                // .getProject().getFile(editor.getPath()));
                // editor.setChecksum(checksum);
                // ConcurrentDocumentManager.logger
                // .debug("Add checksumme to created editor save activity : "
                // + checksum
                // + " for path : "
                // + editor.getPath().toOSString());
            }

            // We did not handle it!
            return activity;
        }

        if (activity instanceof TextEditActivity) {

            TextEditActivity textEdit = (TextEditActivity) activity;

            JupiterDocumentClient document = getClientDoc(textEdit.getEditor());
            document.generateRequest(textEdit.toOperation());

            // We did consume this activity
            return null;
        }
        return activity;
    }

    private void fileActivity(IActivity activity) {
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

        final JupiterDocumentClient jupiterClient = getClientDoc(request
            .getEditorPath());

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

    public IActivity exec(IActivity activity) {

        if (activity instanceof EditorActivity) {
            EditorActivity editorActivity = (EditorActivity) activity;

            if (isHostSide()) {
                JID sourceJID = new JID(editorActivity.getSource());

                /* if one driver activated a new editor. */
                if (shouldBeManagedByJupiter(sourceJID)
                    && ((editorActivity.getType() == Type.Activated) || (editorActivity
                        .getType() == Type.Closed))) {

                    /* start jupiter proxy for this driver. */
                    if (this.concurrentDocuments.containsKey(editorActivity
                        .getPath())) {
                        JupiterServer server = this.concurrentDocuments
                            .get(editorActivity.getPath());

                        /* client has no proxy for this editor. */
                        if (!server.isExist(sourceJID)) {
                            if (editorActivity.getType() == Type.Activated) {
                                server.addProxyClient(sourceJID);
                                /* update vector time for new proxy. */
                                // TODO: stop serializer and after this update
                                // vector time.
                                server.updateVectorTime(this.myJID, sourceJID);
                                // TODO: forward vector time method.

                                /* get vector time of host for this editor path. */
                                try {

                                    JupiterClient jupC = this.clientDocs
                                        .get(editorActivity.getPath());
                                    if (jupC != null) {
                                        Timestamp ts = jupC.getTimestamp();

                                        /* create update vector time request. */
                                        Request updateRequest = new RequestImpl(
                                            0, new JupiterVectorTime(ts
                                                .getComponents()[1], ts
                                                .getComponents()[0]),
                                            new TimestampOperation());
                                        updateRequest
                                            .setEditorPath(editorActivity
                                                .getPath());
                                        updateRequest.setJID(sourceJID);

                                        this.forwarder
                                            .forwardOutgoingRequest(updateRequest);
                                    }
                                } catch (Exception e) {

                                    ConcurrentDocumentManager.logger.error(
                                        "Error during get timestamp of host proxy for "
                                            + editorActivity.getPath(), e);
                                }
                            }
                        } else {
                            /* remove proxy for this jid. */
                            if (editorActivity.getType() == Type.Closed) {
                                server.removeProxyClient(sourceJID);
                            }
                        }
                    } else {
                        /* create new jupiter proxy client. */
                        if (editorActivity.getType() == Type.Activated) {
                            Request createRequest = new RequestImpl(0,
                                new JupiterVectorTime(0, 0),
                                new TimestampOperation());
                            createRequest.setEditorPath(editorActivity
                                .getPath());
                            createRequest.setJID(sourceJID);

                        }
                    }
                }
            }
        }

        if (activity instanceof TextEditActivity) {
            // check for jupiter client documents
            TextEditActivity text = (TextEditActivity) activity;
            if (this.clientDocs.containsKey(text.getEditor())) {
                /* activity have to be transformed with jupiter on this client. */
                return null;
            }
        }

        /* handles file activities. e.g. renamed files etc. */
        fileActivity(activity);

        return activity;
    }

    public boolean isHostSide() {
        return this.side == Side.HOST_SIDE;
    }

    public boolean isHost(JID jid) {
        return jid != null && jid.equals(this.host);
    }

    private JupiterDocumentServer initDocumentServer(IPath path) {

        /* create new document server. */
        JupiterDocumentServer docServer = new JupiterDocumentServer(
            this.forwarder, path);

        /* create new local host document client. */
        docServer.addProxyClient(this.host);
        return docServer;
    }

    /**
     * 
     * @host
     */
    protected void receiveRequestHostSide(Request request) {

        assert isHostSide() : "receiveRequestHostSide called on the Client";

        JID sender = request.getJID();
        IPath path = request.getEditorPath();

        /* if host side and server jupiter side of request */
        if (isHost(sender) && (request.getSiteId() == 0)) {
            /* Request already has been transformed and has to be executed. */
            execTextEditActivity(request);
            return;
        }

        JupiterDocumentServer docServer = null;
        /**
         * if no jupiter document server exists.
         */
        if (!this.concurrentDocuments.containsKey(path)) {
            docServer = initDocumentServer(path);
            this.concurrentDocuments.put(path, docServer);
        }
        docServer = this.concurrentDocuments.get(path);

        /* check if sender id exists in proxy list. */
        if (!docServer.getProxies().containsKey(sender)) {
            docServer.addProxyClient(sender);
        }

        /* sync request with jupiter document server. */
        docServer.addRequest(request);
    }

    /**
     * Synchronizes the given request with the jupiter server document (if host)
     * and local clients (if host or client).
     */
    public void receiveRequest(Request request) {
        if (isHostSide()) {
            receiveRequestHostSide(request);
        } else {
            receiveRequestClientSide(request);
        }
    }

    public JupiterDocumentClient getClientDoc(IPath path) {

        if (this.clientDocs.containsKey(path)) {
            return this.clientDocs.get(path);
        } else {
            JupiterDocumentClient client = new JupiterDocumentClient(
                this.myJID, this.forwarder, path);
            this.clientDocs.put(path, client);
            return client;
        }
    }

    protected void receiveRequestClientSide(Request request) {

        if (request.getOperation() instanceof TimestampOperation) {
            /*
             * Update timestamp
             */

            JupiterClient jupClient = getClientDoc(request.getEditorPath());

            try {
                jupClient.updateVectorTime(request.getTimestamp());
            } catch (TransformationException e) {
                logger.error(
                    "Jupiter[Client] - Error during vector time update for "
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

    public void roleChanged(User user, boolean replicated) {
        JID jid = user.getJID();

        if (isHost(jid))
            return;

        /*
         * The following code only removes drivers, because new drivers are
         * added lazyly
         */
        if (isHostSide()) {
            /* if driver changed to observer */
            if (user.isObserver()) {
                userLeft(jid);
            }
        } else {
            // ClientSide
            if (user.equals(this.myJID)) {
                this.clientDocs.clear();
            }
        }
    }

    public void userJoined(JID user) {
        // do nothing
    }

    public void userLeft(JID user) {
        if (isHostSide()) {
            /* remove user proxies from jupiter server. */
            for (JupiterServer server : this.concurrentDocuments.values()) {
                if (server.isExist(user)) {
                    server.removeProxyClient(user);
                }
            }
        }
    }

    /**
     * reset jupiter document server component.
     */
    public void resetJupiterDocument(IPath path) {

        // host side
        if (!isHostSide()) {
            return;
        }

        if (this.concurrentDocuments.containsKey(path)) {
            logger.debug("Resetting jupiter server...");

            JupiterDocumentServer oldServer = this.concurrentDocuments
                .remove(path);

            JupiterDocumentServer newServer = initDocumentServer(path);

            for (JID jid : oldServer.getProxies().keySet()) {
                newServer.addProxyClient(jid);
            }

            this.concurrentDocuments.put(path, newServer);

        } else {
            ConcurrentDocumentManager.logger
                .error("No jupter document exists for " + path.toOSString());
        }

        // reset client documents
        if (this.clientDocs.containsKey(path)) {
            this.clientDocs.remove(path);
            this.clientDocs.put(path, new JupiterDocumentClient(this.myJID,
                this.forwarder, path));
            ConcurrentDocumentManager.logger.debug("Reset jupiter client doc: "
                + this.myJID);
        } else {
            ConcurrentDocumentManager.logger
                .error("No Jupiter document exists for " + path.toOSString());
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
}

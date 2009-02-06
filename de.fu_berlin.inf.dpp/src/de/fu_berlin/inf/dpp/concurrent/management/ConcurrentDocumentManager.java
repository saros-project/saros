package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.concurrent.IConcurrentManager;
import de.fu_berlin.inf.dpp.concurrent.IDriverDocumentManager;
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
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.IActivitySequencer;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.VariableProxy;

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
    private final HashMap<IPath, JupiterClient> clientDocs = new HashMap<IPath, JupiterClient>();

    // private List<JID> drivers;

    private JID host;

    private final JID myJID;

    private final Side side;

    private RequestForwarder forwarder;

    private IActivitySequencer sequencer;

    private final IDriverDocumentManager driverManager;

    private final ConsistencyWatchdog consistencyWatchdog = new ConsistencyWatchdog(
            "ConsistencyWatchdog");

    private VariableProxy<Boolean> inconsistencyToResolve = new VariableProxy<Boolean>(
            false);

    private Set<IPath> pathesWithWrongChecksums = new CopyOnWriteArraySet<IPath>();

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

        ITextFileBuffer fileBuff = FileBuffers.getTextFileBufferManager()
                .getTextFileBuffer(fullPath, LocationKind.IFILE);
        return fileBuff;
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

        private boolean executingChecksumErrorHandling;

        public ConsistencyWatchdog(String name) {
            super(name);
        }

        // this map holds for all jupiter controlled documents the checksums
        private final HashMap<IPath, DocumentChecksum> docsChecksums = new HashMap<IPath, DocumentChecksum>();

        @Override
        protected IStatus run(IProgressMonitor monitor) {

            assert isHostSide() : "This job is intended to be run on host side!";

            // Update Checksums for all documents controlled by jupiter
            for (IPath docPath : clientDocs.keySet()) {

                // get document
                ITextFileBuffer fileBuff = getTextFileBuffer(docPath);

                // TODO CO Handle missing files correctly
                if (fileBuff == null) {
                    logger.error("Can't get File Buffer");
                    docsChecksums.remove(docPath);
                    continue;
                }
                IDocument doc = fileBuff.getDocument();

                // if no entry for this document exists create a new one
                if (docsChecksums.get(docPath) == null) {
                    DocumentChecksum c = new DocumentChecksum(docPath, doc
                            .getLength(), doc.get().hashCode());
                    docsChecksums.put(docPath, c);
                } else {
                    DocumentChecksum c = docsChecksums.get(docPath);
                    if (c.getLength() != doc.getLength()) {
                        // length has changed, compute the hash new
                        c.setLength(doc.getLength());
                        c.setHash(doc.get().hashCode());
                    }
                }
            }

            // Send to all Clients
            Saros.getDefault().getSessionManager().getTransmitter()
                    .sendDocChecksumsToClients(docsChecksums.values());

            // Reschedule the next run in 10 seconds
            schedule(10000);
            return Status.OK_STATUS;
        }
    }

    public ConcurrentDocumentManager(final Side side, User host, JID myJID,
            ISharedProject sharedProject) {

        this.driverManager = DriverDocumentManager.getInstance();
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

                        if (side == Side.HOST_SIDE) {
                            consistencyWatchdog.cancel();
                        }

                        // TODO we should not need this
                        pathesWithWrongChecksums.clear();
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

    /**
	 *
	 */
    public IActivity activityCreated(IActivity activity) {

        editorActivitiy(activity);

        if (createdTextEditActivity(activity)) {
            /* handled by jupiter and is sended by request transmitting. */
            return null;
        }
        return activity;
    }

    /**
     * handled closed editor activity to remove the local jupiter clients.
     *
     * @param activity
     */
    private void editorActivitiy(IActivity activity) {
        if (activity instanceof EditorActivity) {
            EditorActivity editor = (EditorActivity) activity;

            // if (isHostSide()) {
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
        }
    }

    private void fileActivity(IActivity activity) {
        if (activity instanceof FileActivity) {

            FileActivity file = (FileActivity) activity;
            if (file.getType() == FileActivity.Type.Created) {
                resetJupiterDocument(file.getPath());
            }
            if (file.getType() == FileActivity.Type.Removed) {
                if (isHostSide()) {
                    /* remove jupiter document server */
                    if (this.concurrentDocuments.containsKey(file.getPath())) {
                        this.concurrentDocuments.remove(file.getPath());
                    }
                }

                // Client Side
                if (this.clientDocs.containsKey(file.getPath())) {
                    this.clientDocs.remove(file.getPath());
                }
            }
        }
    }

    /**
     * handles text edit activities with jupiter.
     *
     * @param activity
     * @return true if activity is transformed with jupiter.
     */
    private boolean createdTextEditActivity(IActivity activity) {

        if (activity instanceof TextEditActivity) {
            TextEditActivity textEdit = (TextEditActivity) activity;
            // if (!isHostSide()) {
            JupiterClient jupClient = null;
            /* no jupiter client already exists for this editor text edit */
            if (!this.clientDocs.containsKey(textEdit.getEditor())) {
                jupClient = new JupiterDocumentClient(this.myJID,
                        this.forwarder, textEdit.getEditor());
                // jupClient.setEditor(textEdit.getEditor());
                this.clientDocs.put(textEdit.getEditor(), jupClient);
            }

            /* generate request. */
            jupClient = this.clientDocs.get(textEdit.getEditor());
            if (jupClient != null) {
                Operation op = getOperation(textEdit);
                jupClient.generateRequest(op);

                /* already set and forward inside of jup client. */
                // /* add appropriate Editor path. */
                // req.setEditorPath(textEdit.getEditor());
                // /* transmit request */
                // forwarder.forwardOutgoingRequest(req);
                return true;
            }
            // }
        }
        return false;
    }

    // TODO CJ: review
    private void execTextEditActivity(final Request request) {

        // if (!isHostSide()) {
        JupiterClient jupClient = null;
        /* no jupiter client already exists for this editor text edit */
        if (!this.clientDocs.containsKey(request.getEditorPath())) {
            jupClient = new JupiterDocumentClient(this.myJID, this.forwarder,
                    request.getEditorPath());
            // jupClient.setEditor(request.getEditorPath());
            this.clientDocs.put(request.getEditorPath(), jupClient);
        }

        /* generate request. */
        jupClient = this.clientDocs.get(request.getEditorPath());
        if (jupClient != null) {

            /* operational transformation. */
            final JupiterClient jupiterClient = jupClient;
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    Operation op;
                    try {
                        op = jupiterClient.receiveRequest(request);
                    } catch (TransformationException e) {
                        ConcurrentDocumentManager.logger.error(
                                "Error during transformation: ", e);

                        /* create save activity. */
                        IActivity activity = new EditorActivity(Type.Saved,
                                request.getEditorPath());
                        /* execute save activity and start consistency check. */
                        ConcurrentDocumentManager.this.sequencer.exec(activity);
                        return;
                    }

                    for (TextEditActivity textEdit : getTextEditActivity(op)) {
                        textEdit.setEditor(request.getEditorPath());
                        textEdit.setSource(request.getJID().toString());
                        /* execute activity in activity sequencer. */
                        ConcurrentDocumentManager.this.sequencer
                                .execTransformedActivity(textEdit);
                    }
                }
            });
        }
    }

    public IActivity exec(IActivity activity) {

        if (activity instanceof EditorActivity) {
            EditorActivity editorAc = (EditorActivity) activity;

            if (isHostSide()) {
                JID sourceJID = new JID(editorAc.getSource());

                /* inform driver document manager */
                this.driverManager.receiveActivity(activity);

                /* if one driver activate a new editor. */
                // if (drivers.contains(sourceJID)
                if (this.driverManager.isDriver(sourceJID)
                        && ((editorAc.getType() == Type.Activated) || (editorAc
                                .getType() == Type.Closed))) {
                    /* start jupiter proxy for this driver. */
                    if (this.concurrentDocuments
                            .containsKey(editorAc.getPath())) {
                        JupiterServer server = this.concurrentDocuments
                                .get(editorAc.getPath());

                        /* client has no proxy for this editor. */
                        if (!server.isExist(sourceJID)) {
                            if (editorAc.getType() == Type.Activated) {
                                server.addProxyClient(sourceJID);
                                /* update vector time for new proxy. */
                                // TODO: stop serializer and after this update
                                // vector time.
                                server.updateVectorTime(this.myJID, sourceJID);
                                // TODO: forward vector time method.

                                /* get vector time of host for this editor path. */
                                try {

                                    JupiterClient jupC = this.clientDocs
                                            .get(editorAc.getPath());
                                    if (jupC != null) {
                                        Timestamp ts = jupC.getTimestamp();

                                        /* create update vector time request. */
                                        Request updateRequest = new RequestImpl(
                                                0, new JupiterVectorTime(ts
                                                        .getComponents()[1], ts
                                                        .getComponents()[0]),
                                                new TimestampOperation());
                                        updateRequest.setEditorPath(editorAc
                                                .getPath());
                                        updateRequest.setJID(sourceJID);

                                        this.forwarder
                                                .forwardOutgoingRequest(updateRequest);
                                    }
                                } catch (Exception e) {

                                    ConcurrentDocumentManager.logger.error(
                                            "Error during get timestamp of host proxy for "
                                                    + editorAc.getPath(), e);
                                }
                            }
                        } else {
                            /* remove proxy for this jid. */
                            if (editorAc.getType() == Type.Closed) {
                                server.removeProxyClient(sourceJID);
                            }
                        }
                    } else {
                        /* create new jupiter proxy client. */
                        if (editorAc.getType() == Type.Activated) {
                            Request createRequest = new RequestImpl(0,
                                    new JupiterVectorTime(0, 0),
                                    new TimestampOperation());
                            createRequest.setEditorPath(editorAc.getPath());
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

    public void setHost(JID host) {
        this.host = host;
    }

    /**
     * convert TextEditActivity to Operation op
     *
     * @param text
     * @return
     */
    public Operation getOperation(TextEditActivity text) {

        Operation op = null;
        // delete activity
        if ((text.length > 0) && (text.text.length() == 0)) {
            /* string placeholder in length of delete area. */
            String placeholder = "";
            for (int i = 0; i < text.length; i++) {
                placeholder += 1;
            }
            op = new DeleteOperation(text.offset, placeholder);
        }
        // insert activity
        if ((text.length == 0) && (text.text.length() > 0)) {
            op = new InsertOperation(text.offset, text.text);
        }
        // replace operation has to split into delete and insert operation
        if ((text.length > 0) && (text.text.length() > 0)) {
            /* string placeholder in length of delete area. */
            String placeholder = "";
            for (int i = 0; i < text.length; i++) {
                placeholder += 1;
            }
            op = new SplitOperation(new DeleteOperation(text.offset,
                    placeholder), new InsertOperation(text.offset, text.text));
        }
        return op;
    }

    /**
     * Convert Operation to text edit activity. NoOperation will be ignored.
     *
     * @param op
     *            incoming transformed operation.
     * @return List with executable text edit activities.
     */
    public List<TextEditActivity> getTextEditActivity(Operation op) {
        List<TextEditActivity> result = new Vector<TextEditActivity>();
        TextEditActivity textEdit = null;
        if (op instanceof DeleteOperation) {
            DeleteOperation del = (DeleteOperation) op;
            textEdit = new TextEditActivity(del.getPosition(), "", del
                    .getTextLength());
            result.add(textEdit);
        }
        if (op instanceof InsertOperation) {
            InsertOperation ins = (InsertOperation) op;
            textEdit = new TextEditActivity(ins.getPosition(), ins.getText(), 0);
            result.add(textEdit);
        }
        if (op instanceof SplitOperation) {
            SplitOperation split = (SplitOperation) op;
            TextEditActivity op1 = getTextEditActivity(split.getFirst()).get(0);
            TextEditActivity op2 = getTextEditActivity(split.getSecond())
                    .get(0);

            /*
             * if operation one is delete operation the offset of second
             * operation has to modified.
             */
            if ((op1.length > 0) && (op1.text.length() == 0)
                    && (op2.length > 0) && (op2.text.length() == 0)) {
                op2 = new TextEditActivity(op2.offset - op1.length, "",
                        op2.length);
            }
            result.add(op1);
            result.add(op2);
        }

        return result;
    }

    private JupiterDocumentServer initDocumentServer(IPath path) {
        JupiterDocumentServer docServer = null;
        /* create new document server. */
        docServer = new JupiterDocumentServer(this.forwarder);
        // docServer = new JupiterDocumentServer();
        docServer.setEditor(path);
        /* create new local host document client. */
        docServer.addProxyClient(this.host);
        return docServer;
    }

    /**
     * sync received request with right jupiter server document and local
     * client.
     *
     */
    public void receiveRequest(Request request) {

        /* 1. Sync with jupiter server component. */
        if (isHostSide()) {

            /* if host side and server jupiter side of request */
            if (isHost(request.getJID()) && (request.getSiteId() == 0)) {
                /* Request already has been transformed and has to be executed. */
                execTextEditActivity(request);
                return;
            }

            JupiterDocumentServer docServer = null;
            /**
             * if no jupiter document server exists.
             */
            if (!this.concurrentDocuments.containsKey(request.getEditorPath())) {
                docServer = initDocumentServer(request.getEditorPath());

                if (!isHost(request.getJID())) {
                    this.driverManager.addDriverToDocument(request
                            .getEditorPath(), request.getJID());
                    docServer.addProxyClient(request.getJID());
                }
                this.concurrentDocuments
                        .put(request.getEditorPath(), docServer);
            }
            docServer = this.concurrentDocuments.get(request.getEditorPath());
            try {
                /* check if sender id exists in proxy list. */
                if (!docServer.getProxies().containsKey(request.getJID())) {
                    docServer.addProxyClient(request.getJID());
                }
            } catch (InterruptedException ie) {
                ConcurrentDocumentManager.logger.error(
                        "Error during get proxy list of jupiter server.", ie);
            }

            /* sync request with jupiter document server. */
            docServer.addRequest(request);

            return;
        } else {
            /* update timestamp of local jupiter client. */
            if (request.getOperation() instanceof TimestampOperation) {
                if (this.clientDocs.containsKey(request.getEditorPath())) {
                    ConcurrentDocumentManager.logger
                            .info("update vector time : "
                                    + request.getEditorPath());
                    JupiterClient jupClient = this.clientDocs.get(request
                            .getEditorPath());
                    try {
                        jupClient.updateVectorTime(request.getTimestamp());
                    } catch (TransformationException e) {
                        ConcurrentDocumentManager.logger.error(
                                "Error during update jupiter client for "
                                        + request.getEditorPath(), e);
                    }
                } else {
                    /* if no jupiter client exists. */
                    JupiterClient client = new JupiterDocumentClient(
                            this.myJID, this.forwarder, request.getEditorPath());
                    // client.setEditor(request.getEditorPath());
                    try {
                        client.updateVectorTime(request.getTimestamp());
                        this.clientDocs.put(request.getEditorPath(), client);
                    } catch (TransformationException e) {
                        ConcurrentDocumentManager.logger.error(
                                "Error during update jupiter client for "
                                        + request.getEditorPath(), e);
                    }

                }
            } else {
                /*
                 * 2. receive request in local client component and execute the
                 * transformed operation as IActivity.
                 */
                execTextEditActivity(request);
            }
        }
    }

    public void driverChanged(JID driver, boolean replicated) {

        if (isHost(driver))
            return;
        /*
         * 1. check if driver exists. 2. add new driver or remove driver. 3.
         */
        if (isHostSide()) {
            /* if driver changed to observer */
            if (this.driverManager.isDriver(driver)) {
                userLeft(driver);
            }
            // /* new driver added to project. */
            // else {
            // drivers.add(driver);
            // //TODO: add driver to current open document proxy ?
            // }
        } else {
            // ClientSide
            if (driver.equals(this.myJID)) {
                this.clientDocs.clear();
            }
        }

    }

    public void userJoined(JID user) {
        // do nothing

    }

    public void userLeft(JID user) {
        if (isHostSide()) {
            /* remove user from driver list */
            // drivers.remove(user);
            /* remove user proxies from jupiter server. */
            for (JupiterServer server : this.concurrentDocuments.values()) {
                if (server.isExist(user)) {
                    server.removeProxyClient(user);

                    /* if only host has an proxy */

                }
            }
        }
    }

    /**
     * reset jupiter document server component.
     */
    protected void resetJupiterDocument(IPath path) {
        // host side
        if (isHostSide()) {
            if (this.concurrentDocuments.containsKey(path)) {
                /* remove document server. */
                this.concurrentDocuments.remove(path);
                /* init new server. */
                JupiterDocumentServer doc = initDocumentServer(path);
                ConcurrentDocumentManager.logger
                        .debug("Reset jupiter server : ");
                /* add proxy documents for active driver. */
                for (JID jid : this.driverManager.getDriversForDocument(path)) {
                    doc.addProxyClient(jid);
                    ConcurrentDocumentManager.logger
                            .debug("add driver proxy : " + jid);
                }

                this.concurrentDocuments.put(path, doc);

            } else {
                ConcurrentDocumentManager.logger
                        .error("No jupter document exists for "
                                + path.toOSString());
            }
        }

        // reset client documents
        if (this.clientDocs.containsKey(path)) {
            this.clientDocs.remove(path);
            this.clientDocs.put(path, new JupiterDocumentClient(this.myJID,
                    this.forwarder, path));
            ConcurrentDocumentManager.logger
                    .debug("Reset jupiter client doc : " + this.myJID);
        } else {
            ConcurrentDocumentManager.logger
                    .error("No jupter document exists for " + path.toOSString());
        }

    }

    /**
     * Checks the local documents against the given checksums.
     *
     * Use the VariableProxy getConsistenciesToResolve() to be notified if
     * inconsistencies are found or resolved.
     *
     * @param checksums
     *            the checksums to check the documents against
     */
    public void checkConsistency(DocumentChecksum[] checksums) {

        for (DocumentChecksum checksum : checksums) {

            IPath path = checksum.getPath();

            IDocument doc = EditorManager.getDefault().getDocument(path);

            // if doc == null there is no editor with this resource open
            if (doc == null)
                continue;

            if ((doc.getLength() != checksum.getLength())
                    || (doc.get().hashCode() != checksum.getHash())) {

                long lastEdited = (EditorManager.getDefault()
                        .getLastEditTime(path));

                long lastRemoteEdited = (EditorManager.getDefault()
                        .getLastRemoteEditTime(path));

                if ((System.currentTimeMillis() - lastEdited) > 2000
                        && (System.currentTimeMillis() - lastRemoteEdited > 2000)) {
                    logger
                            .debug(String
                                    .format(
                                            "Inconsistency detected: %s L(%d %s %d) H(%x %s %x)",
                                            path.toString(), doc.getLength(),
                                            doc.getLength() == checksum
                                                    .getLength() ? "==" : "!=",
                                            checksum.getLength(), doc.get()
                                                    .hashCode(), doc.get()
                                                    .hashCode() == checksum
                                                    .getHash() ? "==" : "!=",
                                            checksum.getHash()));

                    ConcurrentDocumentManager.this.pathesWithWrongChecksums
                            .add(path);
                    if (!inconsistencyToResolve.getVariable()) {
                        inconsistencyToResolve.setVariable(true);
                    }
                }
                return;
            }
            logger.debug("All Inconsistencies are resolved");

            ConcurrentDocumentManager.this.pathesWithWrongChecksums.clear();

            if (inconsistencyToResolve.getVariable() == true) {
                inconsistencyToResolve.setVariable(false);
            }
        }
    }

    /**
     * Returns the variable proxy which stores the current inconsistency state
     *
     */
    public VariableProxy<Boolean> getConsistencyToResolve() {
        return this.inconsistencyToResolve;
    }

    /**
     * TODO CJ: write javadoc
     *
     */
    public boolean getExecutingChecksumErrorHandling() {
        return consistencyWatchdog.executingChecksumErrorHandling;
    }

    public Set<IPath> getPathesWithWrongChecksums() {
        return this.pathesWithWrongChecksums;
    }
}

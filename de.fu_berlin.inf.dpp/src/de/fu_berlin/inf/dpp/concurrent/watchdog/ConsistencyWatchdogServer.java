package de.fu_berlin.inf.dpp.concurrent.watchdog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * This class is an eclipse job run on the host side ONLY.
 * 
 * The job computes checksums for all files currently managed by Jupiter (the
 * ConcurrentDocumentManager) and sends them to all guests.
 * 
 * These will call their ConcurrentDocumentManager.check(...) method, to verify
 * that their version is correct.
 * 
 * Once started with schedule() the job is scheduled to rerun every INTERVAL ms.
 * 
 * @author chjacob
 * 
 *         TODO Make ConsistencyWatchDog configurable => Timeout, Whether run or
 *         not, etc.
 * 
 */
@Component(module = "consistency")
public class ConsistencyWatchdogServer extends Job {

    private static Logger log = Logger
        .getLogger(ConsistencyWatchdogServer.class);

    protected static final long INTERVAL = 10000;

    // this map holds for all open editors of all participants the checksums
    protected final HashMap<IPath, DocumentChecksum> docsChecksums = new HashMap<IPath, DocumentChecksum>();

    @Inject
    protected Saros saros;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected XMPPChatTransmitter transmitter;

    protected SessionManager sessionManager;

    protected ISharedProject sharedProject;

    public ConsistencyWatchdogServer(SessionManager sessionManager) {
        super("ConsistencyWatchdog");

        this.sessionManager = sessionManager;

        this.sessionManager.addSessionListener(new AbstractSessionListener() {
            @Override
            public void sessionStarted(ISharedProject newSharedProject) {

                if (newSharedProject.isHost()) {
                    sharedProject = newSharedProject;
                    log.debug("Starting consistency watchdog");
                    setSystem(true);
                    setPriority(Job.SHORT);
                    schedule(10000);
                }
            }

            @Override
            public void sessionEnded(ISharedProject session) {

                if (sharedProject != null) {
                    // Cancel Job
                    cancel();
                    sharedProject = null;

                    // Unregister from all documents
                    for (DocumentChecksum document : docsChecksums.values()) {
                        document.dispose();
                    }
                    docsChecksums.clear();
                }
            }
        });
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        try {
            return runUnsafe(monitor);
        } catch (RuntimeException e) {
            log.error("Internal Error: ", e);
            schedule(10000);
            return Status.OK_STATUS;
        }
    }

    protected IStatus runUnsafe(IProgressMonitor monitor) {
        if (sessionManager.getSharedProject() == null)
            return Status.OK_STATUS;

        assert sharedProject.isHost() : "This job is intended to be run on host side!";

        // If connection is closed, checking does not make sense...
        if (saros.getConnectionState() != ConnectionState.CONNECTED) {
            // Reschedule the next run in 30 seconds
            schedule(30000);
            return Status.OK_STATUS;
        }

        Set<IPath> missingDocuments = new HashSet<IPath>(docsChecksums.keySet());

        Set<IPath> localEditors = editorManager.getLocallyOpenEditors();
        Set<IPath> remoteEditors = editorManager.getRemoteOpenEditors();
        Set<IPath> allEditors = new HashSet<IPath>();
        allEditors.addAll(localEditors);
        allEditors.addAll(remoteEditors);

        // Update Checksums for all open documents
        for (IPath docPath : allEditors) {

            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            IDocument doc = editorManager.getDocument(docPath);

            // Null means that the document does not exist locally
            if (doc == null) {
                if (localEditors.contains(docPath)) {
                    log.error("EditorManager is in an inconsistent state. "
                        + "It is reporting a locally open editor but no"
                        + " document could be found on disk: " + docPath);
                }
                if (!remoteEditors.contains(docPath)) {
                    /*
                     * Since remote users do not report this document as open,
                     * they are right (and our EditorPool might be confused)
                     */
                    continue;
                }
            }

            // Update listener management
            missingDocuments.remove(docPath);

            DocumentChecksum checksum = docsChecksums.get(docPath);
            if (checksum == null) {
                checksum = new DocumentChecksum(docPath);
                docsChecksums.put(docPath, checksum);
            }

            /*
             * Potentially bind to null doc, which will set the Checksum to
             * represent a missing file (existsFile() == false)
             */
            checksum.bind(doc);
            checksum.update();
        }

        // Unregister all documents that are no longer there
        for (IPath missing : missingDocuments) {
            docsChecksums.remove(missing).dispose();
        }

        // Send to all Clients
        // TODO Since this is done asynchronously a race condition might occur
        if (saros.isConnected()) {
            // We will send a message, even if no file is open, because
            // otherwise clients do not know that the consistency has been
            // resolved.
            transmitter.sendDocChecksumsToClients(getOthers(), docsChecksums
                .values());
        }

        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;

        // Reschedule the next run in INTERVAL ms
        schedule(INTERVAL);
        return Status.OK_STATUS;
    }

    public List<JID> getOthers() {
        ArrayList<JID> result = new ArrayList<JID>();
        for (User user : sharedProject.getParticipants()) {
            if (user.isRemote()) {
                result.add(user.getJID());
            }
        }
        return result;
    }
}

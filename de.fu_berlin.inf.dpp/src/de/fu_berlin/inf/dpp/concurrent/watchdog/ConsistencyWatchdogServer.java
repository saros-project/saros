package de.fu_berlin.inf.dpp.concurrent.watchdog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.util.Util;

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
    protected XMPPTransmitter transmitter;

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

        IProject project = sharedProject.getProject();

        // Update Checksums for all open documents
        for (IPath docPath : allEditors) {

            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            updateChecksum(missingDocuments, localEditors, remoteEditors,
                project, docPath);
        }

        // Unregister all documents that are no longer there
        for (IPath missing : missingDocuments) {
            docsChecksums.remove(missing).dispose();
        }

        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;

        // Reschedule the next run in INTERVAL ms
        schedule(INTERVAL);
        return Status.OK_STATUS;
    }

    protected void updateChecksum(final Set<IPath> missingDocuments,
        final Set<IPath> localEditors, final Set<IPath> remoteEditors,
        final IProject project, final IPath docPath) {

        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {

                IFile file = project.getFile(docPath);

                IDocument doc;
                IDocumentProvider provider = null;
                FileEditorInput input = null;
                if (!file.exists()) {
                    doc = null;
                } else {
                    input = new FileEditorInput(file);
                    provider = EditorManager.getDocumentProvider(input);
                    try {
                        provider.connect(input);
                        doc = provider.getDocument(input);
                    } catch (CoreException e) {
                        log.warn("Could not check checksum of file "
                            + docPath.toOSString());
                        provider = null;
                        doc = null;
                    }
                }

                try {
                    // Null means that the document does not exist locally
                    if (doc == null) {
                        if (localEditors.contains(docPath)) {
                            log
                                .error("EditorManager is in an inconsistent state. "
                                    + "It is reporting a locally open editor but no"
                                    + " document could be found on disk: "
                                    + docPath);
                        }
                        if (!remoteEditors.contains(docPath)) {
                            /*
                             * Since remote users do not report this document as
                             * open, they are right (and our EditorPool might be
                             * confused)
                             */
                            return;
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
                     * Potentially bind to null doc, which will set the Checksum
                     * to represent a missing file (existsFile() == false)
                     */
                    checksum.bind(doc);
                    checksum.update();

                    // Sent an checksum to everybody
                    ChecksumActivity checksumActivity = new ChecksumActivity(
                        sharedProject.getLocalUser(), new SPath(checksum
                            .getPath()), checksum.getHash(), checksum
                            .getLength());

                    sharedProject.activityCreated(checksumActivity);

                } finally {
                    if (provider != null) {
                        provider.disconnect(input);
                    }
                }

            }
        });
    }
}

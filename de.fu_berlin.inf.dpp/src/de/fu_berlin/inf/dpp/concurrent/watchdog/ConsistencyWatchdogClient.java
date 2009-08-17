package de.fu_berlin.inf.dpp.concurrent.watchdog;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SessionView;
import de.fu_berlin.inf.dpp.ui.actions.ConsistencyAction;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.ObservableValue;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This class is responsible to process checksums sent to us from the server by
 * checking our locally existing files against them.
 * 
 * If an inconsistency is detected the inconsistency state is set via the
 * {@link IsInconsistentObservable}. This enables the {@link ConsistencyAction}
 * (a.k.a. the yellow triangle) in the {@link SessionView}.
 * 
 */
@Component(module = "consistency")
public class ConsistencyWatchdogClient {

    private static Logger log = Logger
        .getLogger(ConsistencyWatchdogClient.class);

    protected Set<IPath> pathsWithWrongChecksums = new CopyOnWriteArraySet<IPath>();

    protected ISharedProject sharedProject;

    @Inject
    protected IsInconsistentObservable inconsistencyToResolve;

    @Inject
    protected EditorManager editorManager;

    /**
     * @Inject Injected via Constructor Injection
     */
    protected SessionManager sessionManager;

    @Inject
    protected ITransmitter transmitter;

    @Inject
    protected DataTransferManager dataTransferManager;

    protected ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0,
        TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2),
        new NamedThreadFactory("ChecksumCruncher-"));

    public ConsistencyWatchdogClient(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        sessionManager.addSessionListener(new AbstractSessionListener() {
            @Override
            public void sessionStarted(ISharedProject session) {
                setSharedProject(session);
            }

            @Override
            public void sessionEnded(ISharedProject session) {
                setSharedProject(null);
            }
        });

        setSharedProject(sessionManager.getSharedProject());
    }

    /**
     * Method for queuing another consistency check, which does not print a
     * warning if there are already checks queued.
     */
    public void queueConsistencyCheck() {
        // If there is already a consistency recovery waiting to be executed,
        // we do not need to append another one
        if (executor.getQueue().size() > 1)
            return;

        checkConsistency();
    }

    /**
     * Starts a new Consistency Check.
     * 
     * If a check is already in progress, nothing happens (but a warning)
     * 
     * @nonBlocking This method returns immediately.
     */
    public void checkConsistency() {

        try {
            executor.submit(Util.wrapSafe(log, new Runnable() {
                public void run() {
                    performCheck(currentChecksums);
                }
            }));
        } catch (RejectedExecutionException e) {
            /*
             * Ignore Checksums that arrive before we are done processing the
             * last set of Checksums.
             */
            log.warn("Received Checksums before processing"
                + " of previous checksums finished");
        }

    }

    protected List<DocumentChecksum> currentChecksums;

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
            log.warn("Consistency Check triggered with out"
                + " preceeding call to setChecksums()");
            return;
        }

        log.trace(String.format(
            "Received %d checksums for %d existing inconsistencies", checksums
                .size(), pathsWithWrongChecksums.size()));

        Set<IPath> newInconsistencies = new CopyOnWriteArraySet<IPath>();

        for (DocumentChecksum checksum : checksums) {
            if (isInconsistent(checksum)) {
                newInconsistencies.add(checksum.getPath());
            }
        }

        pathsWithWrongChecksums = newInconsistencies;
        if (pathsWithWrongChecksums.isEmpty()) {
            if (inconsistencyToResolve.getValue()) {
                log.info("All Inconsistencies are resolved");
                inconsistencyToResolve.setValue(false);
            }
        } else {
            if (!inconsistencyToResolve.getValue()) {
                log.info("Inconsistencies have been detected");
                inconsistencyToResolve.setValue(true);
            }
        }

    }

    private boolean isInconsistent(DocumentChecksum checksum) {
        IPath path = checksum.getPath();

        ISharedProject sharedProject = sessionManager.getSharedProject();
        IFile file = sharedProject.getProject().getFile(path);

        if (!checksum.existsFile()) {
            /*
             * If the checksum tells us that the file does not exist at the
             * host, check whether we still have it. If it exists, we do have an
             * inconsistency
             */
            return file.exists();
        }

        /*
         * If the checksum tells us, that the file exists, but we do not have
         * it, it is an inconsistency as well
         */
        if (!file.exists()) {
            return true;
        }

        IDocument doc = editorManager.getDocument(path);

        // if doc is still null give up
        if (doc == null) {
            log.warn("Could not check checksum of file " + path.toOSString());
            return false;
        }

        if ((doc.getLength() != checksum.getLength())
            || (doc.get().hashCode() != checksum.getHash())) {

            long lastEdited = editorManager.getLastEditTime(path);

            long lastRemoteEdited = editorManager.getLastRemoteEditTime(path);

            if ((System.currentTimeMillis() - lastEdited) > 4000
                && (System.currentTimeMillis() - lastRemoteEdited > 4000)) {

                log.debug(String.format(
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

    /**
     * Returns the set of files for which the ConsistencyWatchdog has identified
     * an inconsistency
     */
    public Set<IPath> getPathsWithWrongChecksums() {
        return this.pathsWithWrongChecksums;
    }

    protected void setSharedProject(ISharedProject newSharedProject) {
        sharedProject = newSharedProject;
    }

    /**
     * boolean condition variable used to interrupt another thread from
     * performing a recovery in {@link #runRecovery()}
     */
    private AtomicBoolean cancelRecovery = new AtomicBoolean();

    /**
     * Lock used exclusively in {@link #runRecovery()} to prevent two recovery
     * operations running concurrently.
     */
    private Lock lock = new ReentrantLock();

    /**
     * Start a consistency recovery by sending a checksum error to the host and
     * waiting for his reply.
     * 
     * @blocking This method returns after the recovery has finished
     */
    public void runRecovery() {

        if (!lock.tryLock()) {
            log.error("Restarting Checksum Error Handling"
                + " while another operation is running");
            try {
                // Try to cancel currently running recovery
                do {
                    cancelRecovery.set(true);
                } while (!lock.tryLock(100, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                log.error("Not designed to be interruptable");
                return;
            }
        }

        // Lock has been acquired
        try {
            cancelRecovery.set(false);

            Set<IPath> pathsOfHandledFiles = new HashSet<IPath>(
                pathsWithWrongChecksums);

            for (final IPath path : pathsOfHandledFiles) {

                if (cancelRecovery.get())
                    return;

                // Save document before asking host to resend
                try {
                    editorManager.saveLazy(path);
                } catch (FileNotFoundException e) {
                    // Sending the checksum error message should recreate
                    // this file
                }
            }

            if (cancelRecovery.get())
                return;

            // Send checksumErrorMessage to host
            transmitter.sendFileChecksumErrorMessage(Collections
                .singletonList(sharedProject.getHost().getJID()),
                pathsOfHandledFiles, false);

            // block until all inconsistencies are resolved
            Set<IPath> remainingFiles = new HashSet<IPath>(pathsOfHandledFiles);
            while (remainingFiles.size() > 0) {

                if (cancelRecovery.get())
                    return;

                remainingFiles.retainAll(pathsWithWrongChecksums);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
        } finally {
            lock.unlock();
        }
    }
}

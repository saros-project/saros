package de.fu_berlin.inf.dpp.concurrent.watchdog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.fu_berlin.inf.dpp.activities.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.ChecksumErrorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.filesystem.EclipseFileImpl;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.remote.RemoteProgressManager;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.AbstractSessionListener;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.session.NullSessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.actions.ConsistencyAction;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;

/**
 * This class is responsible for two things:
 * <ol>
 * <li>Process checksums sent to us from the server by checking our locally
 * existing files against them. See {@link #performCheck(ChecksumActivity)} If
 * an inconsistency is detected the inconsistency state is set via the
 * {@link IsInconsistentObservable}. This enables the {@link ConsistencyAction}
 * in the {@link SarosView}.</li>
 * <li>Send a ChecksumError to the host, if the user wants to recover from an
 * inconsistency. See {@link #runRecovery}</li>
 * </ol>
 * This class both produces and consumes activities.
 */
@Component(module = "consistency")
public class ConsistencyWatchdogClient extends AbstractActivityProducer {

    private static Logger LOG = Logger
        .getLogger(ConsistencyWatchdogClient.class);

    private static final Random RANDOM = new Random();

    /**
     * boolean condition variable used to interrupt another thread from
     * performing a recovery in {@link #runRecovery}
     */
    private AtomicBoolean cancelRecovery = new AtomicBoolean();

    /**
     * The number of files remaining in the current recovery session.
     */
    private AtomicInteger filesRemaining = new AtomicInteger();

    /**
     * The id of the currently running recovery
     */
    private volatile String recoveryID;

    /**
     * Lock used exclusively in {@link #runRecovery} to prevent two recovery
     * operations running concurrently.
     */
    private Lock lock = new ReentrantLock();

    private final IsInconsistentObservable inconsistencyToResolve;

    private final EditorManager editorManager;

    private final IEditorAPI editorAPI;

    private final Set<SPath> pathsWithWrongChecksums = new CopyOnWriteArraySet<SPath>();

    /*
     * TODO make sure latestChecksums is accessed in the SWT thread when
     * invoking sessionXYZ listener methods so the synchronized wrapper is not
     * needed
     */
    private final Map<SPath, ChecksumActivity> latestChecksums = Collections
        .synchronizedMap(new HashMap<SPath, ChecksumActivity>());

    private final RemoteProgressManager remoteProgressManager;

    private final ISarosSessionManager sessionManager;

    private volatile ISarosSession session;

    public ConsistencyWatchdogClient(final ISarosSessionManager sessionManager,
        final IsInconsistentObservable inconsistencyToResolve,
        final EditorManager editorManager, final IEditorAPI editorAPI,
        final RemoteProgressManager remoteProgressManager) {
        this.sessionManager = sessionManager;
        this.inconsistencyToResolve = inconsistencyToResolve;
        this.editorManager = editorManager;
        this.editorAPI = editorAPI;
        this.remoteProgressManager = remoteProgressManager;

        this.sessionManager
            .addSessionLifecycleListener(sessionLifecycleListener);
    }

    public void dispose() {
        sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
    }

    private final ISessionListener sessionListener = new AbstractSessionListener() {
        @Override
        public void permissionChanged(User user) {

            if (user.isRemote())
                return;

            // Clear our checksums
            latestChecksums.clear();
        }
    };

    private final ISessionLifecycleListener sessionLifecycleListener = new NullSessionLifecycleListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            session = newSarosSession;

            pathsWithWrongChecksums.clear();
            inconsistencyToResolve.setValue(false);

            newSarosSession.addActivityConsumer(consumer);
            newSarosSession.addActivityProducer(ConsistencyWatchdogClient.this);
            newSarosSession.addListener(sessionListener);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            oldSarosSession.removeActivityConsumer(consumer);
            oldSarosSession
                .removeActivityProducer(ConsistencyWatchdogClient.this);
            oldSarosSession.removeListener(sessionListener);

            latestChecksums.clear();
            pathsWithWrongChecksums.clear();

            session = null;

            // abort running recoveries
            cancelRecovery.set(true);
        }
    };

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void receive(ChecksumActivity checksumActivity) {
            latestChecksums.put(checksumActivity.getPath(), checksumActivity);

            performCheck(checksumActivity);
        }

        @Override
        public void receive(TextEditActivity text) {
            latestChecksums.remove(text.getPath());
        }

        @Override
        public void receive(ChecksumErrorActivity error) {
            if (error.getSource().isHost()) {
                String myRecoveryID = recoveryID;
                if (myRecoveryID != null
                    && myRecoveryID.equals(error.getRecoveryID())) {
                    filesRemaining.set(0); // Host tell us he is done
                }
            }
        }

        @Override
        public void receive(FileActivity fileActivity) {
            if (fileActivity.isRecovery()) {
                int currentValue;
                while ((currentValue = filesRemaining.get()) > 0) {
                    if (filesRemaining.compareAndSet(currentValue,
                        currentValue - 1)) {
                        break;
                    }
                }
                // Recoveries do not invalidate checksums :-)
                return;
            }

            /*
             * (we do not need to handle FolderActivities because all files are
             * created/deleted via FileActivity)
             */

            switch (fileActivity.getType()) {
            case CREATED:
            case REMOVED:
                latestChecksums.remove(fileActivity.getPath());
                break;
            case MOVED:
                latestChecksums.remove(fileActivity.getPath());
                latestChecksums.remove(fileActivity.getOldPath());
                break;
            default:
                LOG.error("Unhandled FileActivity.Type: " + fileActivity);
            }
        }
    };

    /**
     * Returns the set of files for which the ConsistencyWatchdog has identified
     * an inconsistency
     */
    public Set<SPath> getPathsWithWrongChecksums() {
        return new HashSet<SPath>(pathsWithWrongChecksums);
    }

    /**
     * Start a consistency recovery by sending a checksum error to the host and
     * waiting for his reply. <br>
     * The <strong>cancellation</strong> of this method is <strong>not
     * implemented</strong>, so canceling the given monitor does not have any
     * effect.
     *
     * @noSWT This method should not be called from SWT
     * @blocking This method returns after the recovery has finished
     * @client Can only be called on the client!
     */
    public void runRecovery(IProgressMonitor monitor) {

        ISarosSession currentSession = session;

        if (currentSession == null)
            return;

        if (currentSession.isHost())
            throw new IllegalStateException("Can only be called on the client");

        /*
         * FIXME this is to lazy, make sure every recovery is terminated when
         * the session ends
         */
        if (!lock.tryLock()) {
            LOG.error("Restarting Checksum Error Handling"
                + " while another operation is running");
            try {
                // Try to cancel currently running recovery
                do {
                    cancelRecovery.set(true);
                } while (!lock.tryLock(100, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                LOG.error("Not designed to be interruptible");
                return;
            }

            currentSession = session;

            if (currentSession == null) {
                lock.unlock();
                return;
            }
        }

        // Lock has been acquired
        try {
            cancelRecovery.set(false);

            final ArrayList<SPath> pathsOfHandledFiles = new ArrayList<SPath>(
                pathsWithWrongChecksums);

            for (SPath path : pathsOfHandledFiles) {

                if (cancelRecovery.get() || monitor.isCanceled())
                    return;

                editorManager.saveLazy(path);
            }

            if (cancelRecovery.get())
                return;

            monitor.beginTask("Consistency recovery",
                pathsOfHandledFiles.size());

            final IProgressMonitor remoteProgress = remoteProgressManager
                .createRemoteProgressMonitor(currentSession.getRemoteUsers(),
                    new NullProgressMonitor());

            recoveryID = getNextRecoveryID();

            filesRemaining.set(pathsOfHandledFiles.size());

            remoteProgress.beginTask(
                "Consistency recovery for user "
                    + ModelFormatUtils.getDisplayName(currentSession
                        .getLocalUser()), filesRemaining.get());

            fireActivity(new ChecksumErrorActivity(
                currentSession.getLocalUser(), currentSession.getHost(),
                pathsOfHandledFiles, recoveryID));

            try {
                // block until all inconsistencies are resolved
                int filesRemainingBefore = filesRemaining.get();
                int filesRemainingCurrently;
                while ((filesRemainingCurrently = filesRemaining.get()) > 0) {

                    if (cancelRecovery.get() || monitor.isCanceled())
                        return;

                    if (filesRemainingCurrently < filesRemainingBefore) {
                        int worked = filesRemainingBefore
                            - filesRemainingCurrently;

                        // Inform others for progress...
                        monitor.worked(worked);
                        remoteProgress.worked(worked);

                        filesRemainingBefore = filesRemainingCurrently;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            } finally {
                // Inform others for progress...
                remoteProgress.done();
            }

        } finally {
            monitor.done();
            lock.unlock();
        }
    }

    private String getNextRecoveryID() {
        return Long.toHexString(RANDOM.nextLong());
    }

    private boolean isInconsistent(ChecksumActivity checksum) {

        SPath path = checksum.getPath();
        IFile file = ((EclipseFileImpl) path.getFile()).getDelegate();

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

        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = editorAPI.getDocumentProvider(input);

        try {
            provider.connect(input);
        } catch (CoreException e) {
            LOG.warn("Could not check checksum of file " + path.toString());
            return false;
        }

        try {
            IDocument doc = provider.getDocument(input);

            // if doc is still null give up
            if (doc == null) {
                LOG.warn("Could not check checksum of file " + path.toString());
                return false;
            }

            if ((doc.getLength() != checksum.getLength())
                || (doc.get().hashCode() != checksum.getHash())) {

                LOG.debug(String.format(
                    "Inconsistency detected: %s L(%d %s %d) H(%x %s %x)", path
                        .toString(), doc.getLength(),
                    doc.getLength() == checksum.getLength() ? "==" : "!=",
                    checksum.getLength(), doc.get().hashCode(), doc.get()
                        .hashCode() == checksum.getHash() ? "==" : "!=",
                    checksum.getHash()));

                return true;
            }
        } finally {
            provider.disconnect(input);
        }
        return false;
    }

    private void performCheck(ChecksumActivity checksumActivity) {

        final ISarosSession currentSession = session;

        if (currentSession == null)
            return;

        if (currentSession.hasWriteAccess()
            && !currentSession.getConcurrentDocumentClient().isCurrent(
                checksumActivity))
            return;

        boolean changed;

        if (isInconsistent(checksumActivity)) {
            changed = pathsWithWrongChecksums.add(checksumActivity.getPath());
        } else {
            changed = pathsWithWrongChecksums
                .remove(checksumActivity.getPath());
        }

        if (!changed)
            return;

        // Update InconsistencyToResolve observable
        if (pathsWithWrongChecksums.isEmpty()) {
            if (inconsistencyToResolve.getValue()) {
                LOG.info("All Inconsistencies are resolved");
            }
            inconsistencyToResolve.setValue(false);
        } else {
            if (!inconsistencyToResolve.getValue()) {
                LOG.info("Inconsistencies have been detected");
            }
            inconsistencyToResolve.setValue(true);
        }
    }
}

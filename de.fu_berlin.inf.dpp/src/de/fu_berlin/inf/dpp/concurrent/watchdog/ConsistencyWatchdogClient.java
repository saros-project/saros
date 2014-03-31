package de.fu_berlin.inf.dpp.concurrent.watchdog;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.business.ChecksumErrorActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.TextEditActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.filesystem.EclipseFileImpl;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.RemoteProgressManager;
import de.fu_berlin.inf.dpp.ui.actions.ConsistencyAction;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * This class is responsible for two things: 1.) Process checksums sent to us
 * from the server by checking our locally existing files against them. See
 * {@link #performCheck(ChecksumActivity)} If an inconsistency is detected the
 * inconsistency state is set via the {@link IsInconsistentObservable}. This
 * enables the {@link ConsistencyAction} (a.k.a. the yellow triangle) in the
 * {@link SarosView}. 2.) Send a ChecksumError to the host, if the user wants to
 * recover from an inconsistency. See {@link #runRecovery(SubMonitor)}
 */
@Component(module = "consistency")
public class ConsistencyWatchdogClient extends AbstractActivityProvider {

    private static Logger log = Logger
        .getLogger(ConsistencyWatchdogClient.class);

    private static final Random RANDOM = new Random();

    @Inject
    protected IsInconsistentObservable inconsistencyToResolve;

    @Inject
    protected EditorManager editorManager;

    /**
     * @Inject Injected via Constructor Injection
     */
    protected ISarosSessionManager sessionManager;

    @Inject
    protected RemoteProgressManager remoteProgressManager;

    protected ISarosSession sarosSession;

    protected Set<SPath> pathsWithWrongChecksums = new CopyOnWriteArraySet<SPath>();

    protected Map<SPath, ChecksumActivity> latestChecksums = new HashMap<SPath, ChecksumActivity>();

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        private ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {

            @Override
            public void permissionChanged(User user) {

                if (user.isRemote())
                    return;

                // Clear our checksums
                latestChecksums.clear();
            }
        };

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            synchronized (this) {
                sarosSession = newSarosSession;
            }

            pathsWithWrongChecksums.clear();
            inconsistencyToResolve.setValue(false);

            newSarosSession.addActivityProvider(ConsistencyWatchdogClient.this);
            newSarosSession.addListener(sharedProjectListener);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {

            oldSarosSession
                .removeActivityProvider(ConsistencyWatchdogClient.this);
            oldSarosSession.removeListener(sharedProjectListener);

            latestChecksums.clear();
            pathsWithWrongChecksums.clear();

            synchronized (this) {
                sarosSession = null;
            }
        }
    };

    public ConsistencyWatchdogClient(ISarosSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.sessionManager.addSarosSessionListener(sessionListener);
    }

    public void dispose() {
        this.sessionManager.removeSarosSessionListener(sessionListener);
    }

    protected IActivityReceiver activityReceiver = new AbstractActivityReceiver() {

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
                log.error("Unhandled FileActivity.Type: " + fileActivity);
            }
        }
    };

    /**
     * Returns the set of files for which the ConsistencyWatchdog has identified
     * an inconsistency
     */
    public Set<SPath> getPathsWithWrongChecksums() {
        return this.pathsWithWrongChecksums;
    }

    /**
     * boolean condition variable used to interrupt another thread from
     * performing a recovery in {@link #runRecovery(SubMonitor)}
     */
    private AtomicBoolean cancelRecovery = new AtomicBoolean();

    /**
     * The number of files remaining in the current recovery session.
     */
    protected AtomicInteger filesRemaining = new AtomicInteger();

    /**
     * The id of the currently running recovery
     */
    protected volatile String recoveryID;

    /**
     * Lock used exclusively in {@link #runRecovery(SubMonitor)} to prevent two
     * recovery operations running concurrently.
     */
    private Lock lock = new ReentrantLock();

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
    public void runRecovery(SubMonitor monitor) {

        ISarosSession session;
        synchronized (this) {
            // Keep a local copy, since the session might end while we're doing
            // this.
            session = sarosSession;
        }

        if (session.isHost())
            throw new IllegalStateException("Can only be called on the client");

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

            final ArrayList<SPath> pathsOfHandledFiles = new ArrayList<SPath>(
                pathsWithWrongChecksums);

            for (SPath path : pathsOfHandledFiles) {

                if (cancelRecovery.get() || monitor.isCanceled())
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

            monitor.beginTask("Consistency recovery",
                pathsOfHandledFiles.size());
            final IProgressMonitor remoteProgress = remoteProgressManager
                .createRemoteProgress(session, session.getRemoteUsers());
            recoveryID = getNextRecoveryID();

            filesRemaining.set(pathsOfHandledFiles.size());

            remoteProgress.beginTask("Consistency recovery for user "
                + session.getLocalUser().getJID().getBase(),
                filesRemaining.get());

            fireActivity(new ChecksumErrorActivity(session.getLocalUser(),
                session.getHost(), pathsOfHandledFiles, recoveryID));

            try {
                // block until all inconsistencies are resolved
                int filesRemainingBefore = filesRemaining.get();
                int filesRemainingCurrently;
                while ((filesRemainingCurrently = filesRemaining.get()) > 0) {

                    if (cancelRecovery.get() || monitor.isCanceled()
                        || sarosSession == null)
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

    protected SimpleDateFormat format = new SimpleDateFormat("HHmmssSS");

    protected String getNextRecoveryID() {
        return format.format(new Date()) + RANDOM.nextLong();
    }

    @Override
    public void exec(IActivity activity) {
        activity.dispatch(activityReceiver);
    }

    protected boolean isInconsistent(ChecksumActivity checksum) {

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
        IDocumentProvider provider = EditorManager.getDocumentProvider(input);

        try {
            provider.connect(input);
        } catch (CoreException e) {
            log.warn("Could not check checksum of file " + path.toString());
            return false;
        }

        try {
            IDocument doc = provider.getDocument(input);

            // if doc is still null give up
            if (doc == null) {
                log.warn("Could not check checksum of file " + path.toString());
                return false;
            }

            if ((doc.getLength() != checksum.getLength())
                || (doc.get().hashCode() != checksum.getHash())) {

                log.debug(String.format(
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

    /**
     * Will run a consistency check.
     * 
     * @return whether a consistency check could be performed or not (for
     *         instance because no current checksum is available)
     * @swt This must be called from SWT
     * @client This can only be called on the client
     */
    public boolean performCheck(SPath path) {

        if (sarosSession == null) {
            log.warn("Session already ended. Cannot perform consistency check",
                new StackTrace());
            return false;
        }

        ChecksumActivity checksumActivity = latestChecksums.get(path);
        if (checksumActivity != null) {
            performCheck(checksumActivity);
            return true;
        } else {
            return false;
        }
    }

    protected synchronized void performCheck(ChecksumActivity checksumActivity) {

        if (sarosSession.hasWriteAccess()
            && !sarosSession.getConcurrentDocumentClient().isCurrent(
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
                log.info("All Inconsistencies are resolved");
            }
            inconsistencyToResolve.setValue(false);
        } else {
            if (!inconsistencyToResolve.getValue()) {
                log.info("Inconsistencies have been detected");
            }
            inconsistencyToResolve.setValue(true);
        }
    }
}

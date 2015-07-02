/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.core.concurrent;

import de.fu_berlin.inf.dpp.activities.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.ChecksumErrorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.core.editor.adapter.DocumentFactory;
import de.fu_berlin.inf.dpp.core.editor.adapter.IDocument;
import de.fu_berlin.inf.dpp.core.monitoring.remote.RemoteProgressManager;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorHandler;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionListener;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.NullSarosSessionListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
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

//import de.fu_berlin.inf.dpp.intellij.ui.actions.ConsistencyAction;

/**
 * This class is responsible for two things: 1.) Process checksums sent to us
 * from the server by checking our locally existing files against them. See
 * {@link #performCheck(ChecksumActivity)} If an inconsistency is detected the
 * inconsistency state is set via the {@link IsInconsistentObservable}. This
 * enables the {@link ConsistencyAction} in the SarosToolWindow.
 * 2.) Send a ChecksumError to the host, if the user wants to
 * recover from an inconsistency. See {@link #runRecovery(IProgressMonitor)}
 */
public class ConsistencyWatchdogClient extends AbstractActivityProducer {

    private static final Random RANDOM = new Random();
    private static final Logger LOG = Logger
        .getLogger(ConsistencyWatchdogClient.class);

    private IsInconsistentObservable inconsistencyToResolve;

    private LocalEditorHandler localEditorHandler;

    private final ISarosSessionManager sessionManager;

    private RemoteProgressManager remoteProgressManager;

    private volatile ISarosSession session;

    private final Set<SPath> pathsWithWrongChecksums = new CopyOnWriteArraySet<SPath>();

    private final Map<SPath, ChecksumActivity> latestChecksums = new HashMap<SPath, ChecksumActivity>();
    /**
     * The number of files remaining in the current recovery session.
     */
    private final AtomicInteger filesRemaining = new AtomicInteger();
    /**
     * The id of the currently running recovery
     */
    private volatile String recoveryID;

    /**
     * boolean condition variable used to interrupt another thread from
     * performing a recovery in {@link #runRecovery(IProgressMonitor)}
     */
    private AtomicBoolean cancelRecovery = new AtomicBoolean();
    /**
     * 7
     * Lock used exclusively in {@link #runRecovery(IProgressMonitor)} to prevent two
     * recovery operations running concurrently.
     */
    private final Lock lock = new ReentrantLock();

    public ConsistencyWatchdogClient(final ISarosSessionManager sessionManager,
        final IsInconsistentObservable inconsistencyToResolve,
        final LocalEditorHandler localEditorHandler,
        final RemoteProgressManager remoteProgressManager) {
        this.sessionManager = sessionManager;
        this.inconsistencyToResolve = inconsistencyToResolve;
        this.localEditorHandler = localEditorHandler;
        this.remoteProgressManager = remoteProgressManager;

        this.sessionManager.addSarosSessionListener(sessionListener);
    }

    public void dispose() {
        sessionManager.removeSarosSessionListener(sessionListener);
    }

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
                if (myRecoveryID != null && myRecoveryID
                    .equals(error.getRecoveryID()))
                    filesRemaining.set(0); // Host tell us he is done
            }
        }

        @Override
        public void receive(FileActivity fileActivity) {
            if (fileActivity.isRecovery()) {
                int currentValue;
                while ((currentValue = filesRemaining.get()) > 0) {
                    if (filesRemaining
                        .compareAndSet(currentValue, currentValue - 1))
                        break;
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

    private final ISarosSessionListener sessionListener = new NullSarosSessionListener() {
        private final ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {

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
            session = newSarosSession;

            pathsWithWrongChecksums.clear();
            inconsistencyToResolve.setValue(false);

            newSarosSession.addActivityConsumer(consumer);
            newSarosSession.addActivityProducer(ConsistencyWatchdogClient.this);
            newSarosSession.addListener(sharedProjectListener);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            oldSarosSession.removeActivityConsumer(consumer);
            oldSarosSession
                .removeActivityProducer(ConsistencyWatchdogClient.this);

            oldSarosSession.removeListener(sharedProjectListener);

            latestChecksums.clear();
            pathsWithWrongChecksums.clear();

            session = null;

            // abort running recoveries
            cancelRecovery.set(true);
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
                LOG.error("Not designed to be interruptable");
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

                localEditorHandler.saveFile(path);

            }

            if (cancelRecovery.get())
                return;

            monitor
                .beginTask("Consistency recovery", pathsOfHandledFiles.size());
            final IProgressMonitor remoteProgress = remoteProgressManager
                .createRemoteProgress(session.getRemoteUsers(), null);
            recoveryID = getNextRecoveryID();

            filesRemaining.set(pathsOfHandledFiles.size());

            remoteProgress.beginTask(
                "Consistency recovery for user " + ModelFormatUtils.getDisplayName(
                    currentSession.getLocalUser()), filesRemaining.get()
            );

            fireActivity(
                new ChecksumErrorActivity(currentSession.getLocalUser(),
                    currentSession.getHost(), pathsOfHandledFiles, recoveryID)
            );

            try {
                // block until all inconsistencies are resolved
                int filesRemainingBefore = filesRemaining.get();
                int filesRemainingCurrently;
                while ((filesRemainingCurrently = filesRemaining.get()) > 0) {
                    if (cancelRecovery.get() || monitor.isCanceled()
                        || session == null)
                        return;

                    if (filesRemainingCurrently < filesRemainingBefore) {
                        int worked =
                            filesRemainingBefore - filesRemainingCurrently;

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
        IFile file = path.getFile();

        if (!checksum.existsFile())
            /*
             * If the checksum tells us that the file does not exist at the
             * host, check whether we still have it. If it exists, we do have an
             * inconsistency
             */
            return file.exists();


        /*
         * If the checksum tells us, that the file exists, but we do not have
         * it, it is an inconsistency as well
         */
        if (!file.exists())
            return true;

        IDocument doc = DocumentFactory.getDocument(file);

        // if doc is still null give up
        if (doc == null) {
            LOG.warn("Could not check checksum of file " + path);
            return false;
        }

        if ((doc.getLength() != checksum.getLength()) || (doc.get().hashCode()
            != checksum.getHash())) {

            LOG.debug(String
                .format("Inconsistency detected: %s L(%d %s %d) H(%x %s %x)",
                    path.toString(), doc.getLength(),
                    doc.getLength() == checksum.getLength() ? "==" : "!=",
                    checksum.getLength(), doc.get().hashCode(),
                    doc.get().hashCode() == checksum.getHash() ? "==" : "!=",
                    checksum.getHash()));

            return true;
        }

        return false;
    }

    private void performCheck(ChecksumActivity checksumActivity) {

        final ISarosSession currentSession = session;

        if (currentSession == null)
            return;

        if (currentSession.hasWriteAccess() && !currentSession
            .getConcurrentDocumentClient().isCurrent(checksumActivity))
            return;

        boolean changed;

        if (isInconsistent(checksumActivity))
            changed = pathsWithWrongChecksums.add(checksumActivity.getPath());
        else
            changed = pathsWithWrongChecksums
                .remove(checksumActivity.getPath());

        if (!changed)
            return;

        // Update InconsistencyToResolve observable
        if (pathsWithWrongChecksums.isEmpty()) {
            if (inconsistencyToResolve.getValue())
                LOG.info("All Inconsistencies are resolved");

            inconsistencyToResolve.setValue(false);
        } else {
            if (!inconsistencyToResolve.getValue())
                LOG.info("Inconsistencies have been detected");

            inconsistencyToResolve.setValue(true);
        }
    }
}

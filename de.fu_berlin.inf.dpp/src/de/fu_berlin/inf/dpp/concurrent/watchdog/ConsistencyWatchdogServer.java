package de.fu_berlin.inf.dpp.concurrent.watchdog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * The server side of the <i>consistency watchdog</i> infrastructure. It
 * periodically checksums the files associated with all locally and remotely
 * open {@link IEditorManager editors} in the current session. It then sends
 * these checksums to all watchdog clients, which can compare them with their
 * own checksum calculations to detect inconsistencies and request file recovery
 * if needed.
 * <p>
 * This component is only run on the session's host.
 */
@Component(module = "consistency")
public class ConsistencyWatchdogServer extends AbstractActivityProducer
    implements Startable, Blockable {

    private static final Logger LOG = Logger
        .getLogger(ConsistencyWatchdogServer.class);

    private static final long CHECKSUM_CALCULATION_INTERVAL = 10000;
    private static final long TERMINATION_TIMEOUT = 10000;

    private final ISarosSession session;
    private final IEditorManager editorManager;
    private final StopManager stopManager;
    private final UISynchronizer synchronizer;

    private final Map<SPath, DocumentChecksum> documentChecksums = new HashMap<SPath, DocumentChecksum>();
    private ScheduledThreadPoolExecutor checksumCalculationExecutor;
    private Future<?> checksumCalculationFuture;
    private boolean blocked;

    private final Runnable checksumCalculation = new Runnable() {
        /**
         * Called periodically to calculate new checksums for all editors and
         * send them to clients.
         */
        @Override
        public void run() {
            /*
             * Run on the UI thread to guarantee that the editor contents won't
             * be changed while we calculate the checksums. We also do this to
             * synchronize with block().
             */
            synchronizer.syncExec(ThreadUtils.wrapSafe(LOG, new Runnable() {
                @Override
                public void run() {
                    if (blocked)
                        return;
                    calculateChecksums();
                }
            }));
        }
    };

    private ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {
        /**
         * Marks checksums as dirty as soon as their associated documents are
         * modified. With this information, checksum calculation can be avoided
         * when the document hasn't changed between checksum iterations.
         */
        @Override
        public void textEdited(User user, SPath filePath, int offset,
            String deletedText, String insertedText) {

            DocumentChecksum checksum = documentChecksums.get(filePath);
            if (checksum != null)
                checksum.markDirty();
        }
    };

    /**
     * Creates a ConsistencyWatchdogServer.
     * 
     * @param session
     *            the currently running session
     * @param editorManager
     *            {@link IEditorManager} to get the document contents from
     * @param stopManager
     *            {@link StopManager} to listen to for (un)block requests
     * @param synchronizer
     *            {@link UISynchronizer} to use
     */
    public ConsistencyWatchdogServer(ISarosSession session,
        IEditorManager editorManager, StopManager stopManager,
        UISynchronizer synchronizer) {
        this.session = session;
        this.editorManager = editorManager;
        this.stopManager = stopManager;
        this.synchronizer = synchronizer;
    }

    @Override
    public void start() {
        if (!session.isHost())
            throw new IllegalStateException(
                "Component can only be run on the session's host");

        session.addActivityProducer(this);
        stopManager.addBlockable(this);
        editorManager.addSharedEditorListener(sharedEditorListener);

        checksumCalculationExecutor = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("Consistency-Watchdog-Server", false));

        checksumCalculationExecutor
            .setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

        checksumCalculationFuture = checksumCalculationExecutor
            .scheduleWithFixedDelay(checksumCalculation, 0,
                CHECKSUM_CALCULATION_INTERVAL, TimeUnit.MILLISECONDS);

    }

    @Override
    public void stop() {
        session.removeActivityProducer(this);
        stopManager.removeBlockable(this);
        editorManager.removeSharedEditorListener(sharedEditorListener);

        checksumCalculationFuture.cancel(false);
        checksumCalculationExecutor.shutdown();

        boolean isTerminated = false;
        boolean terminationWasInterrupted = false;

        try {
            isTerminated = checksumCalculationExecutor.awaitTermination(
                TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for consistency watchdog to terminate");
            terminationWasInterrupted = true;
        }

        if (!isTerminated)
            LOG.error("Consistency watchdog server is still running");

        /*
         * Make sure we only clear the checksum map after the last checksum
         * calculation cycle that might still be running.
         */
        synchronizer.asyncExec(new Runnable() {
            @Override
            public void run() {
                documentChecksums.clear();
            }
        });

        if (terminationWasInterrupted)
            Thread.currentThread().interrupt();
    }

    @Override
    public void block() {
        /*
         * Set the blocking flag synchronously on the UI thread to guarantee
         * that no checksum calculation is in progress or being started after
         * this method returns.
         */
        synchronizer.syncExec(new Runnable() {
            @Override
            public void run() {
                blocked = true;
            }
        });
    }

    @Override
    public void unblock() {
        /*
         * We don't need to run on the UI thread here - the checksum calculation
         * runnable will pick up the unset blocking flag eventually. A missed
         * calculation cycle has no negative impact other than slightly delaying
         * possibly needed recovery operations.
         */
        blocked = false;
    }

    private void calculateChecksums() {
        Set<SPath> localEditors = editorManager.getLocallyOpenEditors();
        Set<SPath> remoteEditors = editorManager.getRemotelyOpenEditors();

        Set<SPath> allEditors = new HashSet<SPath>();
        allEditors.addAll(localEditors);
        allEditors.addAll(remoteEditors);

        /*
         * Purge checksums from documents which have been closed since the last
         * cheksum calculation cycle.
         */

        Iterator<Entry<SPath, DocumentChecksum>> it = documentChecksums
            .entrySet().iterator();

        while (it.hasNext()) {
            Entry<SPath, DocumentChecksum> entry = it.next();

            if (!allEditors.contains(entry.getKey())) {
                it.remove();
            }
        }

        /*
         * Update or create checksums for all currently open documents.
         */

        for (SPath docPath : allEditors) {
            updateChecksum(docPath, localEditors, remoteEditors);
            broadcastChecksum(docPath);
        }
    }

    private void updateChecksum(SPath docPath, Set<SPath> localEditors,
        Set<SPath> remoteEditors) {

        DocumentChecksum checksum = documentChecksums.get(docPath);
        if (checksum == null) {
            checksum = new DocumentChecksum(docPath);
            documentChecksums.put(docPath, checksum);
        }

        if (!checksum.isDirty())
            return;

        String content = editorManager.getContent(checksum.getPath());

        if (content == null) {
            if (localEditors.contains(checksum)) {
                LOG.error("EditorManager is in an inconsistent state. "
                    + "It is reporting a locally open editor but no"
                    + " document could be found in the underlying file system: "
                    + checksum);
            }
            if (!remoteEditors.contains(checksum)) {
                /*
                 * Since session participants do not report this document as
                 * open, they are right (and our EditorPool might be confused)
                 */
                documentChecksums.remove(checksum.getPath());
                return;
            }
        }

        checksum.update(content);
    }

    private void broadcastChecksum(SPath docPath) {

        DocumentChecksum checksum = documentChecksums.get(docPath);
        if (checksum == null)
            return;

        ChecksumActivity checksumActivity = new ChecksumActivity(
            session.getLocalUser(), checksum.getPath(), checksum.getHash(),
            checksum.getLength(), null);

        fireActivity(checksumActivity);
    }
}

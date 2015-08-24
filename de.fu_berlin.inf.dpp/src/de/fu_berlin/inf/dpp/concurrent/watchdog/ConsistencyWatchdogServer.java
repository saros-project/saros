package de.fu_berlin.inf.dpp.concurrent.watchdog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
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
public class ConsistencyWatchdogServer extends AbstractActivityProducer
    implements Startable, Blockable {

    private static final Logger LOG = Logger
        .getLogger(ConsistencyWatchdogServer.class);

    private static final long INTERVAL = 10000;

    private ScheduledThreadPoolExecutor executor;

    private ScheduledFuture<?> triggerChecksumFuture;

    private final HashMap<SPath, DocumentChecksum> docsChecksums = new HashMap<SPath, DocumentChecksum>();

    private final IEditorManager editorManager;

    private final ISarosSession session;

    private final StopManager stopManager;

    private final UISynchronizer synchronizer;

    private boolean locked;

    private final Runnable checksumCalculationTrigger = new Runnable() {

        @Override
        public void run() {
            synchronizer.syncExec(ThreadUtils.wrapSafe(LOG, new Runnable() {
                @Override
                public void run() {
                    if (locked)
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

            DocumentChecksum checksum = docsChecksums.get(filePath);
            if (checksum != null)
                checksum.markDirty();
        }
    };

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
                "component can only be run on host side");

        session.addActivityProducer(this);
        stopManager.addBlockable(this);
        editorManager.addSharedEditorListener(sharedEditorListener);

        executor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(
            "Consistency-Watchdog-Server", false));

        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

        triggerChecksumFuture = executor.scheduleWithFixedDelay(
            checksumCalculationTrigger, 0, INTERVAL, TimeUnit.MILLISECONDS);

    }

    @Override
    public void stop() {
        session.removeActivityProducer(this);
        stopManager.removeBlockable(this);
        editorManager.removeSharedEditorListener(sharedEditorListener);

        triggerChecksumFuture.cancel(false);
        executor.shutdown();

        boolean isTerminated = false;
        boolean isInterrupted = false;

        try {
            isTerminated = executor.awaitTermination(10000,
                TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.warn("interrupted while waiting for consistency watchdog to terminate");
            isInterrupted = true;
        }

        if (!isTerminated)
            LOG.error("consistency watchdog is still running");

        /*
         * Make sure we only clear the checksum map after the last checksum
         * calculation cycle that might still be running.
         */
        synchronizer.asyncExec(new Runnable() {
            @Override
            public void run() {
                docsChecksums.clear();
            }
        });

        if (isInterrupted)
            Thread.currentThread().interrupt();
    }

    @Override
    public void block() {
        // sync here to ensure we do not send anything after we return
        synchronizer.syncExec(new Runnable() {
            @Override
            public void run() {
                locked = true;
            }
        });
    }

    @Override
    public void unblock() {
        // unlock lazy is sufficient as it does not matter if we miss one update
        // cycle
        locked = false;
    }

    // UI thread access only !
    private void calculateChecksums() {

        Set<SPath> localEditors = editorManager.getLocallyOpenEditors();
        Set<SPath> remoteEditors = editorManager.getRemotelyOpenEditors();

        Set<SPath> allEditors = new HashSet<SPath>();

        allEditors.addAll(localEditors);
        allEditors.addAll(remoteEditors);

        Iterator<Entry<SPath, DocumentChecksum>> it = docsChecksums.entrySet()
            .iterator();

        while (it.hasNext()) {
            Entry<SPath, DocumentChecksum> entry = it.next();

            if (!allEditors.contains(entry.getKey())) {
                it.remove();
            }
        }

        for (SPath docPath : allEditors) {
            updateChecksum(docPath, localEditors, remoteEditors);
            broadcastChecksum(docPath);
        }
    }

    // UI thread access only !
    private void updateChecksum(SPath docPath, Set<SPath> localEditors,
        Set<SPath> remoteEditors) {

        DocumentChecksum checksum = docsChecksums.get(docPath);
        if (checksum == null) {
            checksum = new DocumentChecksum(docPath);
            docsChecksums.put(docPath, checksum);
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
                docsChecksums.remove(checksum.getPath());
                return;
            }
        }

        checksum.update(content);
    }

    private void broadcastChecksum(SPath docPath) {

        DocumentChecksum checksum = docsChecksums.get(docPath);
        if (checksum == null)
            return;

        ChecksumActivity checksumActivity = new ChecksumActivity(
            session.getLocalUser(), checksum.getPath(), checksum.getHash(),
            checksum.getLength(), null);

        fireActivity(checksumActivity);
    }
}

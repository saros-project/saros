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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.filesystem.EclipseFileImpl;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
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

    private final EditorManager editorManager;

    private final IEditorAPI editorAPI;

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

    public ConsistencyWatchdogServer(ISarosSession session,
        EditorManager editorManager, IEditorAPI editorAPI,
        StopManager stopManager, UISynchronizer synchronizer) {
        this.session = session;
        this.editorManager = editorManager;
        this.editorAPI = editorAPI;
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

        synchronizer.asyncExec(new Runnable() {
            @Override
            public void run() {
                for (DocumentChecksum document : docsChecksums.values())
                    document.dispose();

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
                entry.getValue().dispose();
                it.remove();
            }
        }

        for (SPath docPath : allEditors) {
            updateChecksum(localEditors, remoteEditors, docPath);
        }
    }

    // UI thread access only !
    private void updateChecksum(final Set<SPath> localEditors,
        final Set<SPath> remoteEditors, final SPath docPath) {

        IFile file = ((EclipseFileImpl) docPath.getFile()).getDelegate();

        IDocument doc = null;
        IDocumentProvider provider = null;
        FileEditorInput input = null;

        try {

            if (file.exists()) {
                input = new FileEditorInput(file);
                provider = editorAPI.getDocumentProvider(input);
                try {
                    provider.connect(input);
                    doc = provider.getDocument(input);
                } catch (CoreException e) {
                    LOG.warn("could not check checksum of file " + docPath);
                    provider = null;
                }
            }

            // Null means that the document does not exist locally
            if (doc == null) {

                if (localEditors.contains(docPath)) {
                    LOG.error("EditorManager is in an inconsistent state. "
                        + "It is reporting a locally open editor but no"
                        + " document could be found in the underlying file system: "
                        + docPath);
                }

                if (!remoteEditors.contains(docPath)) {
                    /*
                     * Since session participants do not report this document as
                     * open, they are right (and our EditorPool might be
                     * confused)
                     */
                    return;
                }
            }

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

            ChecksumActivity checksumActivity = new ChecksumActivity(
                session.getLocalUser(), checksum.getPath(), checksum.getHash(),
                checksum.getLength(), null);

            fireActivity(checksumActivity);

        } finally {
            if (provider != null && input != null) {
                provider.disconnect(input);
            }
        }
    }
}

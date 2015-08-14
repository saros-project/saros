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
 *
 */

package de.fu_berlin.inf.dpp.core.concurrent;

import com.intellij.openapi.editor.Document;
import de.fu_berlin.inf.dpp.activities.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.ResourceConverter;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class is a job run on the host side ONLY.
 * <p/>
 * The job computes checksums for all files currently managed by Jupiter (the
 * ConcurrentDocumentManager) and sends them to all guests.
 * <p/>
 * These will call their ConcurrentDocumentManager.check(...) method, to verify
 * that their version is correct.
 * <p/>
 * Once started with schedule() the job is scheduled to rerun every INTERVAL ms.
 * <p/>
 * TODO Make ConsistencyWatchDog configurable => Timeout, Whether run or
 * not, etc.
 */
public class ConsistencyWatchdogServer extends AbstractActivityProducer
    implements Startable, Blockable {

    private static final Logger LOG = Logger
        .getLogger(ConsistencyWatchdogServer.class);

    private static final long INTERVAL = 10000;
    private final Map<SPath, DocumentChecksum> docsChecksums = new HashMap<SPath, DocumentChecksum>();
    private final EditorManager editorManager;
    private final ISarosSession session;
    private final StopManager stopManager;
    private final UISynchronizer synchronizer;
    private ScheduledThreadPoolExecutor executor;
    private ScheduledFuture<?> triggerChecksumFuture;
    private boolean locked;

    private final Runnable checksumCalculationTrigger = new Runnable() {

        @Override
        public void run() {
            synchronizer.syncExec(ThreadUtils.wrapSafe(LOG, new Runnable() {
                @Override
                public void run() {
                    if (locked) {
                        return;
                    }

                    calculateChecksums();
                }
            }));
        }
    };

    public ConsistencyWatchdogServer(ISarosSession session,
        EditorManager editorManager, StopManager stopManager,
        UISynchronizer synchronizer) {
        this.session = session;
        this.editorManager = editorManager;
        this.stopManager = stopManager;
        this.synchronizer = synchronizer;
    }

    @Override
    public void start() {
        if (!session.isHost()) {
            throw new IllegalStateException(
                "component can only be run on host side");
        }

        session.addActivityProducer(this);
        stopManager.addBlockable(this);

        executor = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("Consistency-Watchdog-Server", false));

        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

        triggerChecksumFuture = executor
            .scheduleWithFixedDelay(checksumCalculationTrigger, 0, INTERVAL,
                TimeUnit.MILLISECONDS);

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
            isTerminated = executor
                .awaitTermination(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.warn(
                "interrupted while waiting for consistency watchdog to terminate");
            isInterrupted = true;
        }

        if (!isTerminated) {
            LOG.error("consistency watchdog is still running");
        }

        synchronizer.asyncExec(new Runnable() {
            @Override
            public void run() {
                for (DocumentChecksum document : docsChecksums.values()) {
                    document.dispose();
                }

                docsChecksums.clear();
            }
        });

        if (isInterrupted) {
            Thread.currentThread().interrupt();
        }
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

        IFile file = docPath.getFile();

        Document doc = null;

        if (file.exists()) {
            doc = ResourceConverter.getDocument(file.getFullPath().toFile());
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

    }
}
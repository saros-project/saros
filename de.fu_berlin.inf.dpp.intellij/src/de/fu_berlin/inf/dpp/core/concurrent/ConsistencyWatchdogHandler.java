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

import com.intellij.openapi.vfs.encoding.EncodingProjectManager;
import de.fu_berlin.inf.dpp.activities.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.ChecksumErrorActivity;
import de.fu_berlin.inf.dpp.activities.RecoveryFileActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.core.monitoring.IStatus;
import de.fu_berlin.inf.dpp.core.monitoring.Status;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorHandler;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.ResourceConverter;
import de.fu_berlin.inf.dpp.intellij.runtime.UIMonitoredJob;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * This class is responsible for handling Consistency Errors on the host.
 */
public class ConsistencyWatchdogHandler extends AbstractActivityProducer
    implements Startable {

    private static final Logger LOG = Logger
        .getLogger(ConsistencyWatchdogHandler.class);

    private final LocalEditorHandler localEditorHandler;

    private final ISarosSession session;

    private final UISynchronizer synchronizer;

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void receive(ChecksumErrorActivity checksumError) {
            if (session.isHost())
                startRecovery(checksumError);
        }
    };

    public ConsistencyWatchdogHandler(ISarosSession session,
        LocalEditorHandler localEditorHandler, UISynchronizer synchronizer) {
        this.session = session;
        this.localEditorHandler = localEditorHandler;
        this.synchronizer = synchronizer;
    }

    @Override
    public void start() {
        session.addActivityConsumer(consumer);
        session.addActivityProducer(this);
    }

    @Override
    public void stop() {
        session.removeActivityConsumer(consumer);
        session.removeActivityProducer(this);
    }

    /**
     * This method starts the recovery as a {@link UIMonitoredJob}.
     */
    protected void startRecovery(final ChecksumErrorActivity checksumError) {

        LOG.debug("Received Checksum Error: " + checksumError);

        UIMonitoredJob recoveryJob = new UIMonitoredJob("File recovery") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                runRecovery(checksumError, monitor);

                return new Status(IStatus.OK);
            }
        };

        recoveryJob.schedule();

    }

    void runRecovery(ChecksumErrorActivity checksumError,
        IProgressMonitor progress) throws CancellationException {

        List<StartHandle> startHandles = null;

        progress.beginTask("Performing recovery", IProgressMonitor.UNKNOWN);
        try {

            startHandles = session.getStopManager()
                .stop(session.getUsers(), "Consistency recovery");

            progress.subTask("Sending files to client...");
            recoverFiles(checksumError, progress);

            /*
             * We have to start the StartHandle of the inconsistent user first
             * (blocking!) because otherwise the other participants can be
             * started before the inconsistent user completely processed the
             * consistency recovery.
             */
            progress.subTask("Wait for peers...");

            // find the StartHandle of the inconsistent user
            StartHandle inconsistentStartHandle = null;
            for (StartHandle startHandle : startHandles) {
                if (checksumError.getSource().equals(startHandle.getUser())) {
                    inconsistentStartHandle = startHandle;
                    break;
                }
            }
            if (inconsistentStartHandle == null) {
                LOG.error("could not find start handle"
                    + " of the inconsistent user");
            } else {
                // FIXME evaluate the return value
                inconsistentStartHandle.startAndAwait();
                startHandles.remove(inconsistentStartHandle);
            }
        } finally {
            if (startHandles != null)
                for (StartHandle startHandle : startHandles)
                    startHandle.start();
            progress.done();
        }
    }

    /**
     * This method is only called on the host.
     * <p/>
     * It should not be called from the UI Thread!
     */
    void recoverFiles(final ChecksumErrorActivity checksumError,
        final IProgressMonitor progress) {

        try {
            for (final SPath path : checksumError.getPaths()) {
                progress.subTask(
                    "Recovering file: " + path.getProjectRelativePath());
                synchronizer.syncExec(new Runnable() {

                    @Override public void run() {
                        recoverFile(checksumError.getSource(), session, path);
                    }
                });
            }

            // Tell the user that we sent all files
            fireActivity(new ChecksumErrorActivity(session.getLocalUser(),
                checksumError.getSource(), null,
                checksumError.getRecoveryID()));
        } finally {
            progress.done();
        }
    }

    /**
     * Recover a single file for the given user (that is either send the file or
     * tell the user to removeAll it).
     */
    void recoverFile(User from, final ISarosSession sarosSession,
        final SPath path) {

        IFile file = path.getFile();

        // Reset jupiter
        sarosSession.getConcurrentDocumentServer().reset(from, path);

        final User user = sarosSession.getLocalUser();

        if (!file.exists()) {
            // TODO Warn the user...
            // Tell the client to delete the file
            fireActivity(RecoveryFileActivity.removed(user, path, from, null));
            fireActivity(ChecksumActivity.missing(user, path));
            return;
        } else {
            localEditorHandler.saveFile(path);
        }

        String charset = null;
        try {
            charset = file.getCharset();
        } catch (IOException e) {
            LOG.warn("could not determine encoding for file: " + file, e);
        }

        if (charset == null) {
            charset = EncodingProjectManager.getInstance().getDefaultCharset()
                .name();
        }

        byte[] content = FileUtils.getLocalFileContent(file);

        if (content == null) {
            LOG.error("could not read file: " + file);
            return;
        }

        fireActivity(
            RecoveryFileActivity.created(user, path, content, from, charset));
        /*
         * immediately follow up with a new checksum to the remote side can
         * verify the recovered file
         */
        final DocumentChecksum checksum = new DocumentChecksum(path);
        checksum
            .bind(ResourceConverter.getDocument(path.getFullPath().toFile()));
        checksum.update();

        fireActivity(new ChecksumActivity(user, path, checksum.hashCode(),
                checksum.getLength(), null)
        );
        checksum.dispose();
    }
}

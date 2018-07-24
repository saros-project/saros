package de.fu_berlin.inf.dpp.negotiation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingResponse;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.SubProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.util.CoreUtils;

/**
 * Implementation of {@link AbstractIncomingProjectNegotiation} utilizing
 * a transferred zip archive to exchange differences in the project files.
 */
public class ArchiveIncomingProjectNegotiation extends
    AbstractIncomingProjectNegotiation {

    private static final Logger LOG = Logger
        .getLogger(ArchiveIncomingProjectNegotiation.class);
    private ArchiveTransferListener archiveTransferListener = null;

    public ArchiveIncomingProjectNegotiation(
        final JID peer, //
        final String negotiationID, //
        final List<ProjectNegotiationData> projectNegotiationData, //

        final ISarosSessionManager sessionManager, //
        final ISarosSession session, //

        final FileReplacementInProgressObservable fileReplacementInProgressObservable, //
        final IWorkspace workspace, //
        final IChecksumCache checksumCache, //

        final XMPPConnectionService connectionService, //
        final ITransmitter transmitter, //
        final IReceiver receiver //
    ) {
        super(peer, TransferType.ARCHIVE, negotiationID, projectNegotiationData,
            sessionManager, session, fileReplacementInProgressObservable,
            workspace, checksumCache, connectionService, transmitter, receiver);
    }

    @Override
    protected void setup(IProgressMonitor monitor)
        throws IOException {
        archiveTransferListener = new ArchiveTransferListener(
            ARCHIVE_TRANSFER_ID + getID());

        if (fileTransferManager == null)
            // FIXME: the logic will try to send this to the remote contact
            throw new IOException("not connected to a XMPP server");

        fileTransferManager.addFileTransferListener(archiveTransferListener);
    }

    @Override
    protected void transfer(IProgressMonitor monitor,
        Map<String, IProject> projectMapping, List<FileList> missingFiles)
        throws IOException, SarosCancellationException {

        awaitActivityQueueingActivation(monitor);
        monitor.subTask("");

        /*
         * the user who sends this ProjectNegotiation is now responsible for the
         * resources of the contained projects
         */
        for (Entry<String, IProject> entry : projectMapping.entrySet()) {

            final String projectID = entry.getKey();
            final IProject project = entry.getValue();
            /*
             * TODO Move enable (and disable) queuing responsibility to
             * SarosSession, since the second call relies on the first one, and
             * the first one is never done without the second. (see also TODO in
             * {@link cleanup}).
             */
            session.addProjectMapping(projectID, project);
            session.enableQueuing(project);
        }

        transmitter.send(ISarosSession.SESSION_CONNECTION_ID, getPeer(),
            StartActivityQueuingResponse.PROVIDER
                .create(new StartActivityQueuingResponse(getSessionID(),
                    getID())));

        checkCancellation(CancelOption.NOTIFY_PEER);

        boolean filesMissing = false;

        for (FileList list : missingFiles)
            filesMissing |= list.getPaths().size() > 0;

        // the host do not send an archive if we do not need any files
        if (filesMissing) {
            receiveAndUnpackArchive(projectMapping, archiveTransferListener,
                monitor);
        }
    }

    @Override
    protected void cleanup(IProgressMonitor monitor,
        Map<String, IProject> projectMapping) {
        /*
         * TODO Move disable queuing responsibility to SarosSession (see todo
         * above in {@link transfer}).
         */
        for (IProject project : projectMapping.values())
            session.disableQueuing(project);

        if (fileTransferManager != null)
            fileTransferManager
                .removeFileTransferListener(archiveTransferListener);

        super.cleanup(monitor, projectMapping);
    }

    /**
     * Receives the archive with all missing files and unpacks it.
     */
    private void receiveAndUnpackArchive(
        final Map<String, IProject> localProjectMapping,
        final ArchiveTransferListener archiveTransferListener,
        final IProgressMonitor monitor) throws IOException,
        SarosCancellationException {

        // waiting for the big archive to come in

        monitor.beginTask(null, 100);

        File archiveFile = receiveArchive(archiveTransferListener,
            new SubProgressMonitor(monitor, 50));

        /*
         * FIXME at this point it makes no sense to report the cancellation to
         * the remote side, because his negotiation is already finished !
         */

        try {
            unpackArchive(localProjectMapping, archiveFile,
                new SubProgressMonitor(monitor, 50));
            monitor.done();
        } finally {
            if (archiveFile != null && !archiveFile.delete()) {
                LOG.warn("Could not clean up archive file "+archiveFile.getAbsolutePath());
            }
        }
    }

    private void unpackArchive(final Map<String, IProject> localProjectMapping,
        final File archiveFile, final IProgressMonitor monitor)
        throws LocalCancellationException, IOException {

        final Map<String, IProject> projectMapping = new HashMap<String, IProject>();

        for (Entry<String, IProject> entry : localProjectMapping.entrySet())
            projectMapping.put(entry.getKey(), entry.getValue());

        final DecompressArchiveTask decompressTask = new DecompressArchiveTask(
            archiveFile, projectMapping, PATH_DELIMITER, monitor);

        long startTime = System.currentTimeMillis();

        LOG.debug(this + " : unpacking archive file...");

        /*
         * TODO: calculate the ADLER32 checksums during decompression and add
         * them into the ChecksumCache. The insertion must be done after the
         * WorkspaceRunnable has run or all checksums will be invalidated during
         * the IResourceChangeListener updates inside the WorkspaceRunnable or
         * after it finished!
         */

        try {
            workspace.run(decompressTask,
                projectMapping.values().toArray(new IResource[0]));
        } catch (de.fu_berlin.inf.dpp.exceptions.OperationCanceledException e) {
            LocalCancellationException canceled = new LocalCancellationException(
                null, CancelOption.DO_NOT_NOTIFY_PEER);
            canceled.initCause(e);
            throw canceled;
        }

        LOG.debug(String.format("unpacked archive in %d s",
            (System.currentTimeMillis() - startTime) / 1000));

        // TODO: now add the checksums into the cache
    }

    private File receiveArchive(
        ArchiveTransferListener archiveTransferListener,
        IProgressMonitor monitor) throws IOException,
        SarosCancellationException {

        monitor.beginTask("Receiving archive file...", 100);
        LOG.debug("waiting for incoming archive stream request");

        monitor
            .subTask("Host is compressing project files. Waiting for the archive file...");

        try {
            while (!archiveTransferListener.hasReceived()) {
                checkCancellation(CancelOption.NOTIFY_PEER);
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LocalCancellationException();
        }

        monitor.subTask("Receiving archive file...");

        LOG.debug(this + " : receiving archive");

        IncomingFileTransfer transfer = archiveTransferListener.getRequest()
            .accept();

        File archiveFile = File.createTempFile(
            "saros_archive_" + System.currentTimeMillis(), null);

        boolean transferFailed = true;

        try {
            transfer.recieveFile(archiveFile);

            monitorFileTransfer(transfer, monitor);
            transferFailed = false;
        } catch (XMPPException e) {
            throw new IOException(e.getMessage(), e.getCause());
        } finally {
            if (transferFailed && !archiveFile.delete()) {
                LOG.warn("Could not clean up archive file "+archiveFile.getAbsolutePath());
            }
        }

        monitor.done();

        LOG.debug(this + " : stored archive in file "
            + archiveFile.getAbsolutePath() + ", size: "
            + CoreUtils.formatByte(archiveFile.length()));

        return archiveFile;
    }

    private static class ArchiveTransferListener implements
        FileTransferListener {
        private String description;
        private volatile FileTransferRequest request;

        public ArchiveTransferListener(String description) {
            this.description = description;
        }

        @Override
        public void fileTransferRequest(FileTransferRequest request) {
            if (request.getDescription().equals(description)) {
                this.request = request;
            }
        }

        public boolean hasReceived() {
            return this.request != null;
        }

        public FileTransferRequest getRequest() {
            return this.request;
        }
    }
}

package de.fu_berlin.inf.dpp.negotiation;

import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingResponse;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
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
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

/**
 * Implementation of {@link AbstractIncomingProjectNegotiation} utilizing a transferred zip archive
 * to exchange differences in the project files.
 */
public class ArchiveIncomingProjectNegotiation extends AbstractIncomingProjectNegotiation {

  private static final Logger LOG = Logger.getLogger(ArchiveIncomingProjectNegotiation.class);

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
    super(
        peer,
        TransferType.ARCHIVE,
        negotiationID,
        projectNegotiationData,
        sessionManager,
        session,
        fileReplacementInProgressObservable,
        workspace,
        checksumCache,
        connectionService,
        transmitter,
        receiver);
  }

  @Override
  protected void transfer(
      IProgressMonitor monitor,
      Map<String, IReferencePoint> referencePointMapping,
      List<FileList> missingFiles)
      throws IOException, SarosCancellationException {

    awaitActivityQueueingActivation(monitor);
    monitor.subTask("");

    /*
     * the user who sends this ProjectNegotiation is now responsible for the
     * resources of the contained projects
     */
    for (Entry<String, IReferencePoint> entry : referencePointMapping.entrySet()) {

      final String referencePointID = entry.getKey();
      final IReferencePoint referencePoint = entry.getValue();
      /*
       * TODO Queuing responsibility should be moved to Project
       * Negotiation, since its the only consumer of queuing
       * functionality. This will enable a specific Queuing mechanism per
       * TransferType (see github issue #137).
       */

      session.addReferencePointMapping(referencePointID, referencePoint);
      session.enableQueuing(referencePoint);
    }

    transmitter.send(
        ISarosSession.SESSION_CONNECTION_ID,
        getPeer(),
        StartActivityQueuingResponse.PROVIDER.create(
            new StartActivityQueuingResponse(getSessionID(), getID())));

    checkCancellation(CancelOption.NOTIFY_PEER);

    boolean filesMissing = false;

    for (FileList list : missingFiles) filesMissing |= list.getPaths().size() > 0;

    // the host do not send an archive if we do not need any files
    if (filesMissing) {
      receiveAndUnpackArchive(referencePointMapping, transferListener, monitor);
    }
  }

  /** Receives the archive with all missing files and unpacks it. */
  private void receiveAndUnpackArchive(
      final Map<String, IReferencePoint> localReferencePointMapping,
      final TransferListener archiveTransferListener,
      final IProgressMonitor monitor)
      throws IOException, SarosCancellationException {

    // waiting for the big archive to come in

    monitor.beginTask(null, 100);

    File archiveFile = receiveArchive(archiveTransferListener, new SubProgressMonitor(monitor, 50));

    /*
     * FIXME at this point it makes no sense to report the cancellation to
     * the remote side, because his negotiation is already finished !
     */

    try {
      unpackArchive(localReferencePointMapping, archiveFile, new SubProgressMonitor(monitor, 50));
      monitor.done();
    } finally {
      if (archiveFile != null && !archiveFile.delete()) {
        LOG.warn("Could not clean up archive file " + archiveFile.getAbsolutePath());
      }
    }
  }

  private void unpackArchive(
      final Map<String, IReferencePoint> localReferencePointMapping,
      final File archiveFile,
      final IProgressMonitor monitor)
      throws LocalCancellationException, IOException {

    final DecompressArchiveTask decompressTask =
        new DecompressArchiveTask(
            archiveFile,
            localReferencePointMapping,
            PATH_DELIMITER,
            monitor,
            referencePointManager);

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
      workspace.run(
          decompressTask,
          localReferencePointMapping.values().toArray(new IReferencePoint[0]),
          referencePointManager);
    } catch (de.fu_berlin.inf.dpp.exceptions.OperationCanceledException e) {
      LocalCancellationException canceled =
          new LocalCancellationException(null, CancelOption.DO_NOT_NOTIFY_PEER);
      canceled.initCause(e);
      throw canceled;
    }

    LOG.debug(
        String.format("unpacked archive in %d s", (System.currentTimeMillis() - startTime) / 1000));

    // TODO: now add the checksums into the cache
  }

  private File receiveArchive(TransferListener archiveTransferListener, IProgressMonitor monitor)
      throws IOException, SarosCancellationException {

    monitor.beginTask("Receiving archive file...", 100);
    LOG.debug("waiting for incoming archive stream request");

    monitor.subTask("Host is compressing project files. Waiting for the archive file...");

    awaitTransferRequest();

    monitor.subTask("Receiving archive file...");

    LOG.debug(this + " : receiving archive");

    IncomingFileTransfer transfer = archiveTransferListener.getRequest().accept();

    File archiveFile = File.createTempFile("saros_archive_" + System.currentTimeMillis(), null);

    boolean transferFailed = true;

    try {
      transfer.recieveFile(archiveFile);

      monitorFileTransfer(transfer, monitor);
      transferFailed = false;
    } catch (XMPPException e) {
      throw new IOException(e.getMessage(), e.getCause());
    } finally {
      if (transferFailed && !archiveFile.delete()) {
        LOG.warn("Could not clean up archive file " + archiveFile.getAbsolutePath());
      }
    }

    monitor.done();

    LOG.debug(
        this
            + " : stored archive in file "
            + archiveFile.getAbsolutePath()
            + ", size: "
            + CoreUtils.formatByte(archiveFile.length()));

    return archiveFile;
  }
}

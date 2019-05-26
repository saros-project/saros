package saros.negotiation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import saros.exceptions.LocalCancellationException;
import saros.exceptions.SarosCancellationException;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IProject;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IWorkspace;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.SubProgressMonitor;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.observables.FileReplacementInProgressObservable;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.util.CoreUtils;

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

    final Map<String, IProject> projectMapping = new HashMap<String, IProject>();

    for (Entry<String, IReferencePoint> entry : localReferencePointMapping.entrySet())
      projectMapping.put(entry.getKey(), referencePointManager.get(entry.getValue()));

    final DecompressArchiveTask decompressTask =
        new DecompressArchiveTask(archiveFile, projectMapping, PATH_DELIMITER, monitor);

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
    } catch (saros.exceptions.OperationCanceledException e) {
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

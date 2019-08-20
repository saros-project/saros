package saros.negotiation;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import saros.SarosPluginContext;
import saros.exceptions.LocalCancellationException;
import saros.exceptions.SarosCancellationException;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.SubProgressMonitor;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.net.IConnectionManager;
import saros.net.IReceiver;
import saros.net.IStreamConnection;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.observables.FileReplacementInProgressObservable;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.util.CoreUtils;

/**
 * Implementation of {@link AbstractIncomingProjectNegotiation} utilizing a transferred zip archive
 * to exchange differences in the project files.
 */
public class ArchiveIncomingProjectNegotiation extends AbstractIncomingProjectNegotiation {

  private static final Logger LOG = Logger.getLogger(ArchiveIncomingProjectNegotiation.class);

  // TODO move to factory
  @Inject private IConnectionManager connectionManager;

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

    // FIXME remove
    SarosPluginContext.initComponent(this);
  }

  @Override
  protected void transfer(
      IProgressMonitor monitor, Map<String, IProject> projectMapping, List<FileList> missingFiles)
      throws IOException, SarosCancellationException {

    boolean filesMissing = false;

    for (FileList list : missingFiles) filesMissing |= list.getPaths().size() > 0;

    // the host do not send an archive if we do not need any files
    if (filesMissing) {
      receiveAndUnpackArchive(projectMapping, monitor);
    }
  }

  /** Receives the archive with all missing files and unpacks it. */
  private void receiveAndUnpackArchive(
      final Map<String, IProject> localProjectMapping, final IProgressMonitor monitor)
      throws IOException, SarosCancellationException {

    // waiting for the big archive to come in

    monitor.beginTask(null, 100);

    File archiveFile = receiveArchive(new SubProgressMonitor(monitor, 50));

    /*
     * FIXME at this point it makes no sense to report the cancellation to
     * the remote side, because his negotiation is already finished !
     */

    try {
      unpackArchive(localProjectMapping, archiveFile, new SubProgressMonitor(monitor, 50));
      monitor.done();
    } finally {
      if (archiveFile != null && !archiveFile.delete()) {
        LOG.warn("Could not clean up archive file " + archiveFile.getAbsolutePath());
      }
    }
  }

  private void unpackArchive(
      final Map<String, IProject> localProjectMapping,
      final File archiveFile,
      final IProgressMonitor monitor)
      throws LocalCancellationException, IOException {

    final Map<String, IProject> projectMapping = new HashMap<String, IProject>();

    for (Entry<String, IProject> entry : localProjectMapping.entrySet())
      projectMapping.put(entry.getKey(), entry.getValue());

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
      workspace.run(decompressTask, projectMapping.values().toArray(new IResource[0]));
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

  private File receiveArchive(IProgressMonitor monitor)
      throws IOException, SarosCancellationException {

    monitor.beginTask("Receiving archive file...", 100);
    LOG.debug("connecting to " + getPeer() + " to receive archive file");

    monitor.subTask("Connecting to " + getPeer().getName() + "...");

    IStreamConnection connection =
        connectionManager.connectStream(TRANSFER_ID_PREFIX + getID(), getPeer());

    File archiveFile = File.createTempFile("saros_archive_" + System.currentTimeMillis(), null);

    OutputStream out = null;

    boolean transferFailed = true;

    try {

      out = new FileOutputStream(archiveFile);

      connection.setReadTimeout(60 * 60 * 1000);
      monitor.subTask("Host is compressing project files. Waiting for the archive file...");

      DataInputStream dis = new DataInputStream(connection.getInputStream());

      long remainingDataSize = dis.readLong();

      monitor.subTask("Receiving archive file...");

      LOG.debug(this + " : receiving archive");

      final byte buffer[] = new byte[BUFFER_SIZE];

      while (remainingDataSize > 0) {
        int read = dis.read(buffer);

        if (read == -1) break;

        out.write(buffer, 0, read);
        remainingDataSize -= read;

        checkCancellation(CancelOption.NOTIFY_PEER);
      }

      if (remainingDataSize > 0)
        localCancel(
            "The receiving of the archive file was not successful.", CancelOption.NOTIFY_PEER);

      transferFailed = false;
    } finally {
      if (transferFailed && !archiveFile.delete()) {
        LOG.warn("Could not clean up archive file " + archiveFile.getAbsolutePath());
      }

      IOUtils.closeQuietly(out);
      connection.close();
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

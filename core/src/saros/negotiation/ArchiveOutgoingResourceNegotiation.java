package saros.negotiation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import saros.editor.IEditorManager;
import saros.exceptions.LocalCancellationException;
import saros.exceptions.OperationCanceledException;
import saros.exceptions.SarosCancellationException;
import saros.filesystem.IFile;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.filesystem.checksum.IChecksumCache;
import saros.monitoring.IProgressMonitor;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.filetransfer.XMPPFileTransfer;
import saros.net.xmpp.filetransfer.XMPPFileTransferManager;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.User;
import saros.synchronize.StartHandle;

/**
 * Implementation of {@link AbstractOutgoingResourceNegotiation} utilizing a transferred zip archive
 * to exchange differences in the reference point files.
 */
public class ArchiveOutgoingResourceNegotiation extends AbstractOutgoingResourceNegotiation {

  private static final Logger log = Logger.getLogger(ArchiveOutgoingResourceNegotiation.class);
  private File zipArchive = null;

  public ArchiveOutgoingResourceNegotiation( //
      final JID peer, //
      final ProjectSharingData resourceSharingData, //
      final ISarosSessionManager sessionManager, //
      final ISarosSession session, //
      final IEditorManager editorManager, //
      final IWorkspace workspace, //
      final IChecksumCache checksumCache, //
      final XMPPFileTransferManager fileTransferManager, //
      final ITransmitter transmitter, //
      final IReceiver receiver, //
      final AdditionalResourceDataFactory additionalResourceDataFactory //
      ) {
    super(
        peer,
        resourceSharingData,
        sessionManager,
        session,
        editorManager,
        workspace,
        checksumCache,
        fileTransferManager,
        transmitter,
        receiver,
        additionalResourceDataFactory);
  }

  @Override
  protected void setup(IProgressMonitor monitor) throws IOException {
    // NOP
  }

  @Override
  protected void prepareTransfer(IProgressMonitor monitor, List<FileList> fileLists)
      throws IOException, SarosCancellationException {

    List<StartHandle> stoppedUsers = null;
    try {
      stoppedUsers = stopUsers(monitor);
      monitor.subTask("");

      sendAndAwaitActivityQueueingActivation(monitor);
      monitor.subTask("");

      User user = session.getUser(getPeer());

      if (user == null) throw new LocalCancellationException(null, CancelOption.DO_NOT_NOTIFY_PEER);

      /*
       * inform all listeners that the peer has started queuing and can
       * therefore process IResourceActivities now
       *
       * TODO this needs a review as this is called inside the "blocked"
       * section and so it is not allowed to send resource activities at
       * this time. Maybe change the description of the listener interface
       * ?
       */
      session.userStartedQueuing(user);

      zipArchive = createResourceArchive(fileLists, monitor);
      monitor.subTask("");
    } finally {
      if (stoppedUsers != null) startUsers(stoppedUsers);
    }
  }

  @Override
  protected void transfer(IProgressMonitor monitor, List<FileList> fileLists)
      throws SarosCancellationException, IOException {
    if (zipArchive != null)
      sendArchive(zipArchive, getPeer(), TRANSFER_ID_PREFIX + getID(), monitor);
  }

  @Override
  protected void cleanup(IProgressMonitor monitor) {
    if (zipArchive != null && !zipArchive.delete())
      log.warn("could not delete archive file: " + zipArchive.getAbsolutePath());
    super.cleanup(monitor);
  }

  /**
   * @param fileLists a list of file lists containing the files to archive
   * @return zip file containing all files denoted by the file lists or <code>null</code> if the
   *     file lists do not contain any files
   */
  private File createResourceArchive(final List<FileList> fileLists, final IProgressMonitor monitor)
      throws IOException, SarosCancellationException {

    boolean skip = true;

    int fileCount = 0;

    for (final FileList list : fileLists) {
      skip &= list.getPaths().isEmpty();
      fileCount += list.getPaths().size();
    }

    if (skip) return null;

    checkCancellation(CancelOption.NOTIFY_PEER);

    final List<Pair<IFile, String>> filesToCompress = new ArrayList<>(fileCount);

    final List<IResource> resourcesToLock = new ArrayList<IResource>();

    for (final FileList list : fileLists) {
      final String referencePointID = list.getProjectID();

      final IReferencePoint referencePoint = resourceSharingData.getProject(referencePointID);

      if (referencePoint == null)
        throw new LocalCancellationException(
            "reference point with id " + referencePointID + " was unshared during synchronization",
            CancelOption.NOTIFY_PEER);

      resourcesToLock.add(referencePoint);

      /*
       * force editor buffer flush because we read the files from the
       * underlying storage
       */
      if (editorManager != null) editorManager.saveEditors(referencePoint);

      final StringBuilder aliasBuilder = new StringBuilder();

      aliasBuilder.append(referencePointID).append(PATH_DELIMITER);

      final int prefixLength = aliasBuilder.length();

      for (final String path : list.getPaths()) {
        // assert path is relative !
        aliasBuilder.append(path);

        IFile file = referencePoint.getFile(path);
        String qualifiedPath = aliasBuilder.toString();

        filesToCompress.add(new ImmutablePair<>(file, qualifiedPath));

        aliasBuilder.setLength(prefixLength);
      }
    }

    log.debug(this + " : creating archive");

    File tempArchive = null;

    try {
      tempArchive = File.createTempFile("saros_" + getID(), ".zip");
      workspace.run(
          new CreateArchiveTask(tempArchive, filesToCompress, monitor),
          resourcesToLock.toArray(new IResource[0]));
    } catch (OperationCanceledException e) {
      LocalCancellationException canceled = new LocalCancellationException();
      canceled.initCause(e);
      throw canceled;
    }

    monitor.done();

    return tempArchive;
  }

  private void sendArchive(
      File archive, JID remoteContact, String transferID, IProgressMonitor monitor)
      throws SarosCancellationException, IOException {

    log.debug(this + " : sending archive");
    monitor.beginTask("Sending archive file...", 100);

    XMPPFileTransfer transfer =
        fileTransferManager.fileSendStart(remoteContact, archive, transferID);
    monitorFileTransfer(transfer, monitor);

    monitor.done();

    log.debug(this + " : archive send");
  }
}

package de.fu_berlin.inf.dpp.negotiation;

import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

/**
 * Implementation of {@link AbstractOutgoingProjectNegotiation} utilizing a transferred zip archive
 * to exchange differences in the project files.
 */
public class ArchiveOutgoingProjectNegotiation extends AbstractOutgoingProjectNegotiation {

  private static final Logger LOG = Logger.getLogger(ArchiveOutgoingProjectNegotiation.class);
  private File zipArchive = null;

  public ArchiveOutgoingProjectNegotiation( //
      final JID peer, //
      final ProjectSharingData projects, //
      final ISarosSessionManager sessionManager, //
      final ISarosSession session, //
      final IEditorManager editorManager, //
      final IWorkspace workspace, //
      final IChecksumCache checksumCache, //
      final XMPPConnectionService connectionService, //
      final ITransmitter transmitter, //
      final IReceiver receiver //
      ) {
    super(
        peer,
        projects,
        sessionManager,
        session,
        editorManager,
        workspace,
        checksumCache,
        connectionService,
        transmitter,
        receiver);
  }

  @Override
  protected void setup(IProgressMonitor monitor) throws IOException {
    if (fileTransferManager == null)
      // FIXME: the logic will try to send this to the remote contact
      throw new IOException("not connected to a XMPP server");
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

      zipArchive = createProjectArchive(fileLists, monitor);
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
      LOG.warn("could not delete archive file: " + zipArchive.getAbsolutePath());
    super.cleanup(monitor);
  }

  /**
   * @param fileLists a list of file lists containing the files to archive
   * @return zip file containing all files denoted by the file lists or <code>null</code> if the
   *     file lists do not contain any files
   */
  private File createProjectArchive(final List<FileList> fileLists, final IProgressMonitor monitor)
      throws IOException, SarosCancellationException {

    boolean skip = true;

    int fileCount = 0;

    for (final FileList list : fileLists) {
      skip &= list.getPaths().isEmpty();
      fileCount += list.getPaths().size();
    }

    if (skip) return null;

    checkCancellation(CancelOption.NOTIFY_PEER);

    final List<IFile> filesToCompress = new ArrayList<IFile>(fileCount);
    final List<String> fileAlias = new ArrayList<String>(fileCount);

    final List<IResource> projectsToLock = new ArrayList<IResource>();

    for (final FileList list : fileLists) {
      final String projectID = list.getProjectID();

      final IProject project = projects.getProject(projectID);

      if (project == null)
        throw new LocalCancellationException(
            "project with id " + projectID + " was unshared during synchronization",
            CancelOption.NOTIFY_PEER);

      projectsToLock.add(project);

      /*
       * force editor buffer flush because we read the files from the
       * underlying storage
       */
      if (editorManager != null) editorManager.saveEditors(project);

      final StringBuilder aliasBuilder = new StringBuilder();

      aliasBuilder.append(projectID).append(PATH_DELIMITER);

      final int prefixLength = aliasBuilder.length();

      for (final String path : list.getPaths()) {

        // assert path is relative !
        filesToCompress.add(project.getFile(path));
        aliasBuilder.append(path);
        fileAlias.add(aliasBuilder.toString());
        aliasBuilder.setLength(prefixLength);
      }
    }

    LOG.debug(this + " : creating archive");

    File tempArchive = null;

    try {
      tempArchive = File.createTempFile("saros_" + getID(), ".zip");
      workspace.run(
          new CreateArchiveTask(tempArchive, filesToCompress, fileAlias, monitor),
          projectsToLock.toArray(new IResource[0]));
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

    LOG.debug(this + " : sending archive");
    monitor.beginTask("Sending archive file...", 100);

    assert fileTransferManager != null;

    try {
      OutgoingFileTransfer transfer =
          fileTransferManager.createOutgoingFileTransfer(remoteContact.toString());

      transfer.sendFile(archive, transferID);
      monitorFileTransfer(transfer, monitor);
    } catch (XMPPException e) {
      throw new IOException(e.getMessage(), e);
    }

    monitor.done();

    LOG.debug(this + " : archive send");
  }
}

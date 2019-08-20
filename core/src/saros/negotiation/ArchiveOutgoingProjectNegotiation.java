package saros.negotiation;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import saros.SarosPluginContext;
import saros.editor.IEditorManager;
import saros.exceptions.LocalCancellationException;
import saros.exceptions.OperationCanceledException;
import saros.exceptions.SarosCancellationException;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IFile;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.monitoring.IProgressMonitor;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.net.IConnectionManager;
import saros.net.IReceiver;
import saros.net.IStreamConnection;
import saros.net.IStreamConnectionListener;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.User;
import saros.synchronize.StartHandle;

/**
 * Implementation of {@link AbstractOutgoingProjectNegotiation} utilizing a transferred zip archive
 * to exchange differences in the project files.
 */
public class ArchiveOutgoingProjectNegotiation extends AbstractOutgoingProjectNegotiation {

  private static final Logger LOG = Logger.getLogger(ArchiveOutgoingProjectNegotiation.class);
  private File zipArchive = null;

  // TODO move to factory
  @Inject private IConnectionManager connectionManager;

  private IStreamConnection connection;

  private boolean awaitConnection = true;

  private final IStreamConnectionListener streamConnectionListener =
      new IStreamConnectionListener() {

        @Override
        public boolean streamConnectionEstablished(String id, IStreamConnection connection) {

          synchronized (ArchiveOutgoingProjectNegotiation.this) {
            if (!awaitConnection) return false;

            if (!(TRANSFER_ID_PREFIX + getID()).equals(id)) return false;

            ArchiveOutgoingProjectNegotiation.this.connection = connection;

            ArchiveOutgoingProjectNegotiation.this.notifyAll();
          }

          return true;
        }
      };

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

    // FIXME remove
    SarosPluginContext.initComponent(this);
  }

  @Override
  protected void setup(IProgressMonitor monitor) throws IOException {
    if (fileTransferManager == null)
      // FIXME: the logic will try to send this to the remote contact
      throw new IOException("not connected to a XMPP server");

    connectionManager.addStreamConnectionListener(streamConnectionListener);
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
    connectionManager.addStreamConnectionListener(streamConnectionListener);
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

    LOG.debug(this + " : waiting for remote connection");
    monitor.beginTask("Sending archive file...", 100);

    final long timeout = 60 * 1000;

    long currentTime = System.currentTimeMillis();

    synchronized (this) {
      while (System.currentTimeMillis() - currentTime < timeout) {
        if (connection != null) break;

        if (monitor.isCanceled()) {
          awaitConnection = false;
          checkCancellation(CancelOption.NOTIFY_PEER);
        }

        try {
          wait(1000);
        } catch (InterruptedException e) {
          awaitConnection = false;
          Thread.currentThread().interrupt();
          this.localCancel("Negotiation got internally interrupted.", CancelOption.NOTIFY_PEER);
          break;
        }
      }

      awaitConnection = false;
    }

    assert connection != null;

    DataOutputStream out = null;
    InputStream in = null;

    try {

      in = new FileInputStream(archive);

      long fileSize = archive.length();

      out = new DataOutputStream(connection.getOutputStream());

      out.writeLong(fileSize);
      final byte buffer[] = new byte[BUFFER_SIZE];

      int read = 0;

      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
        checkCancellation(CancelOption.NOTIFY_PEER);
      }
    } finally {
      connection.close();
      IOUtils.closeQuietly(in);
    }

    monitor.done();

    LOG.debug(this + " : archive send");
  }
}

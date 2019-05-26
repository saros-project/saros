package saros.negotiation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import saros.communication.extensions.ProjectNegotiationMissingFilesExtension;
import saros.communication.extensions.StartActivityQueuingRequest;
import saros.communication.extensions.StartActivityQueuingResponse;
import saros.exceptions.LocalCancellationException;
import saros.exceptions.SarosCancellationException;
import saros.filesystem.FileSystem;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IFolder;
import saros.filesystem.IProject;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.SubProgressMonitor;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.PacketCollector;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.observables.FileReplacementInProgressObservable;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.SessionEndReason;

// MAJOR TODO refactor this class !!!

/**
 * Handles incoming ProjectNegotiations except for the actual file transfer.
 *
 * <p>Concrete implementations need to provide an implementation to exchange the calculated
 * differences. This class only provides the initial setup and calculation.
 */
public abstract class AbstractIncomingProjectNegotiation extends ProjectNegotiation {

  private static final Logger LOG = Logger.getLogger(AbstractIncomingProjectNegotiation.class);

  private static int MONITOR_WORK_SCALE = 1000;

  private final Map<String, ProjectNegotiationData> projectNegotiationData;

  protected final FileReplacementInProgressObservable fileReplacementInProgressObservable;

  protected boolean running;

  private PacketCollector startActivityQueuingRequestCollector;

  /** used to handle file transmissions * */
  protected TransferListener transferListener = null;

  public AbstractIncomingProjectNegotiation(
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
        negotiationID,
        peer,
        sessionManager,
        session,
        workspace,
        checksumCache,
        connectionService,
        transmitter,
        receiver);

    this.projectNegotiationData = new HashMap<String, ProjectNegotiationData>();

    for (final ProjectNegotiationData data : projectNegotiationData)
      this.projectNegotiationData.put(data.getProjectID(), data);

    this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;
  }

  /**
   * Starts the negotiation. The negotiation can be aborted by canceling the given monitor. The
   * execution of this method perform changes to the file system! It is the responsibility of the
   * caller to ensure that appropriate actions are performed to avoid unintended data loss, i.e this
   * method will do a best effort to backup altered data but no guarantee can be made in doing so!
   *
   * @param projectMapping mapping from remote project ids to the target local projects
   * @throws IllegalArgumentException if either a project id is not valid or the referenced project
   *     for that id does not exist
   */
  public Status run(Map<String, IProject> projectMapping, final IProgressMonitor monitor) {

    checkProjectMapping(projectMapping);

    synchronized (this) {
      running = true;
    }

    observeMonitor(monitor);

    fileReplacementInProgressObservable.startReplacement();

    Exception exception = null;

    createCollectors();

    try {
      checkCancellation(CancelOption.NOTIFY_PEER);
      setup(monitor);

      List<FileList> missingFiles =
          synchronizeProjectStructures(
              projectMapping, computeLocalVsRemoteDiff(projectMapping, monitor));

      monitor.subTask("");

      transmitter.send(
          ISarosSession.SESSION_CONNECTION_ID,
          getPeer(),
          ProjectNegotiationMissingFilesExtension.PROVIDER.create(
              new ProjectNegotiationMissingFilesExtension(getSessionID(), getID(), missingFiles)));

      awaitActivityQueueingActivation(monitor);

      /*
       * the user who sends this ProjectNegotiation is now responsible for the
       * resources of the contained projects
       */
      for (Entry<String, IProject> entry : projectMapping.entrySet()) {
        final String referencePointID = entry.getKey();
        final IReferencePoint referencePoint = entry.getValue().getReferencePoint();
        /*
         * TODO Queuing responsibility should be moved to Project
         * Negotiation, since its the only consumer of queuing
         * functionality. This will enable a specific Queuing mechanism per
         * TransferType (see github issue #137).
         */
        session.addReferencePointMapping(referencePointID, referencePoint);
        /* TODO change queuing to resource based queuing */
        session.enableQueuing(referencePoint);
      }

      /*
       * If we are the session's host and are receiving projects from a
       * non-host user, we need to notify all components that the user
       * already has these projects and can process (and send) activities
       * targeting them. Otherwise, activities generated while we are
       * still receiving the project archive will get lost.
       *
       * FIXME: userStartedQueuing() needs a better name which is less
       * bound to its use in OutgoingProjectNegotiation.
       */
      if (session.isHost()) {
        session.userStartedQueuing(session.getUser(getPeer()));
      }

      transmitter.send(
          ISarosSession.SESSION_CONNECTION_ID,
          getPeer(),
          StartActivityQueuingResponse.PROVIDER.create(
              new StartActivityQueuingResponse(getSessionID(), getID())));

      checkCancellation(CancelOption.NOTIFY_PEER);

      transfer(monitor, projectMapping, missingFiles);

      checkCancellation(CancelOption.NOTIFY_PEER);

      /*
       * We are finished with the negotiation. Add all projects resources
       * to the session.
       */
      for (Entry<String, IProject> entry : projectMapping.entrySet()) {

        final String referencePointID = entry.getKey();
        final IReferencePoint referencePoint = entry.getValue().getReferencePoint();

        final boolean isPartialRemoteProject =
            getProjectNegotiationData(referencePointID).isPartial();

        final FileList remoteFileList = getProjectNegotiationData(referencePointID).getFileList();

        List<IResource> resources = null;

        if (isPartialRemoteProject) {

          final List<String> paths = remoteFileList.getPaths();

          resources = new ArrayList<IResource>(paths.size());

          for (final String path : paths)
            resources.add(getResource(referencePointManager.get(referencePoint), path));
        }

        session.addSharedResources(referencePoint, referencePointID, resources);
      }
    } catch (Exception e) {
      exception = e;
    } finally {
      cleanup(monitor, projectMapping);
    }

    return terminate(exception);
  }

  /**
   * In preparation of the Project Negotiation, this setups a File Transfer Handler, used to receive
   * the incoming negotiation data.
   *
   * @param monitor monitor to show progress to the user
   * @throws SarosCancellationException
   */
  protected void setup(IProgressMonitor monitor) throws SarosCancellationException {
    if (fileTransferManager == null)
      throw new LocalCancellationException(
          "not connected to a XMPP server", CancelOption.DO_NOT_NOTIFY_PEER);

    transferListener = new TransferListener(TRANSFER_ID_PREFIX + getID());
    fileTransferManager.addFileTransferListener(transferListener);
  }

  /**
   * Handle the actual transfer. The negotiation can be aborted by canceling the given monitor.
   *
   * @param monitor monitor to show progress to the user
   * @param projectMapping mapping from remote project ids to the target local projects
   * @param missingFiles files missing, that should be transferred and synchronized by this method
   *     call
   * @throws IOException, SarosCancellationException
   */
  protected abstract void transfer(
      IProgressMonitor monitor, Map<String, IProject> projectMapping, List<FileList> missingFiles)
      throws IOException, SarosCancellationException;

  /**
   * Cleanup ends the negotiation process, by disabling the project based queue and removes acquired
   * handlers during {@link #setup} and {@link #transfer}.
   *
   * @param monitor mapping from remote project ids to the target local projects
   * @param projectMapping mapping of projects
   */
  protected void cleanup(IProgressMonitor monitor, Map<String, IProject> projectMapping) {
    fileReplacementInProgressObservable.replacementDone();

    /*
     * TODO Queuing responsibility should be moved to Project
     * Negotiation, since its the only consumer of queuing
     * functionality. This will enable a specific Queuing mechanism per
     * TransferType (see github issue #137).
     */
    for (IProject project : projectMapping.values())
      session.disableQueuing(project.getReferencePoint());

    if (fileTransferManager != null)
      fileTransferManager.removeFileTransferListener(transferListener);

    deleteCollectors();
    monitor.done();
  }

  /**
   * Returns the {@link ProjectNegotiationData negotiation data} for all projects which are part of
   * this negotiation.
   *
   * @return negotiation data for all projects which are part of this negotiation.
   */
  public List<ProjectNegotiationData> getProjectNegotiationData() {
    return new ArrayList<ProjectNegotiationData>(projectNegotiationData.values());
  }

  /**
   * Returns the {@link ProjectNegotiationData negotiation data} for the given project id.
   *
   * @return negotiation data for the given project id or <code>null</code> if no negotiation data
   *     exists for the given project id.
   */
  public ProjectNegotiationData getProjectNegotiationData(final String id) {
    return projectNegotiationData.get(id);
  }

  @Override
  protected void executeCancellation() {

    /*
     * Remove the entries from the mapping in the SarosSession.
     *
     * Stefan Rossbach 28.12.2012: This will not gain you anything because
     * the project is marked as shared on the remote side and so will never
     * be able to be shared again to us. Again the whole architecture does
     * currently NOT support cancellation of the project negotiation
     * properly !
     */
    // for (Entry<String, IProject> entry : localProjectMapping.entrySet())
    // {
    // session.removeProjectMapping(entry.getKey(), entry.getValue());
    // }

    // // The session might have been stopped already, if not we will stop
    // it.
    // if (session.getProjectResourcesMapping().keySet().isEmpty()
    // || session.getRemoteUsers().isEmpty())
    // sessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);

    if (!session.isHost() || session.getRemoteUsers().isEmpty())
      sessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
  }

  @Override
  public synchronized boolean remoteCancel(String errorMsg) {
    if (!super.remoteCancel(errorMsg)) return false;

    if (!running) terminate(null);

    return true;
  }

  @Override
  public synchronized boolean localCancel(String errorMsg, CancelOption cancelOption) {
    if (!super.localCancel(errorMsg, cancelOption)) return false;

    if (!running) terminate(null);

    return true;
  }

  /**
   * Computes the differences (files and folders) between the local and the remote side for the
   * given project mapping.
   *
   * @param localProjectMapping the local project mapping to use
   * @param monitor
   * @return list of differences (one for each project) between the local and the remote side.
   * @throws SarosCancellationException
   * @throws IOException
   */
  protected Map<String, FileListDiff> computeLocalVsRemoteDiff(
      final Map<String, IProject> localProjectMapping, final IProgressMonitor monitor)
      throws SarosCancellationException, IOException {

    LOG.debug(this + " : computing file and folder differences");

    monitor.beginTask(
        "Computing project(s) difference(s)...", localProjectMapping.size() * MONITOR_WORK_SCALE);

    final Map<String, FileListDiff> result = new HashMap<String, FileListDiff>();

    for (final Entry<String, IProject> entry : localProjectMapping.entrySet()) {

      final String id = entry.getKey();
      final IProject project = entry.getValue();

      // TODO optimize for partial shared projects

      final FileList localProjectFileList =
          FileListFactory.createFileList(
              project,
              null,
              checksumCache,
              new SubProgressMonitor(
                  monitor, 1 * MONITOR_WORK_SCALE, SubProgressMonitor.SUPPRESS_BEGINTASK));

      final ProjectNegotiationData data = getProjectNegotiationData(id);

      final FileListDiff diff =
          FileListDiff.diff(localProjectFileList, data.getFileList(), data.isPartial());

      checkCancellation(CancelOption.NOTIFY_PEER);

      if (data.isPartial()
          && (!diff.getRemovedFiles().isEmpty() || !diff.getRemovedFolders().isEmpty()))
        throw new IllegalStateException("partial sharing cannot delete existing resources");

      result.put(id, diff);
    }

    monitor.done();

    return result;
  }

  /**
   * Synchronize the project structures, deleting files and folders that are not present on the
   * remote side and creating empty folders that do not exists and the local side.
   *
   * @param localProjectMapping
   * @param diffs
   * @return list of file lists (each for every project) containing the missing files that are not
   *     present on the local side.
   * @throws IOException
   */
  protected List<FileList> synchronizeProjectStructures(
      final Map<String, IProject> localProjectMapping, final Map<String, FileListDiff> diffs)
      throws IOException {

    LOG.debug(this + " : deleting files and folders, creating empty folders");

    final List<FileList> result = new ArrayList<FileList>();

    for (final Entry<String, IProject> entry : localProjectMapping.entrySet()) {

      final String id = entry.getKey();
      final IProject project = entry.getValue();

      final FileListDiff diff = diffs.get(id);

      final List<String> resourcesToDelete =
          new ArrayList<String>(diff.getRemovedFiles().size() + diff.getRemovedFolders().size());

      resourcesToDelete.addAll(diff.getRemovedFiles());
      resourcesToDelete.addAll(diff.getRemovedFolders());

      Collections.sort(resourcesToDelete, Collections.reverseOrder());

      for (final String path : resourcesToDelete) {
        final IResource resource = getResource(project, path);

        if (resource.exists()) {

          if (LOG.isTraceEnabled()) LOG.trace("deleting resource: " + resource);

          resource.delete(IResource.KEEP_HISTORY);
        }
      }

      for (final String path : diff.getAddedFolders()) {
        final IFolder folder = project.getFolder(path);

        if (!folder.exists()) {

          if (LOG.isTraceEnabled()) LOG.trace("creating folder(s): " + folder);

          FileSystem.createFolder(folder);
        }
      }

      final List<String> missingFiles = new ArrayList<String>();

      missingFiles.addAll(diff.getAddedFiles());
      missingFiles.addAll(diff.getAlteredFiles());

      LOG.debug(this + " : " + missingFiles.size() + " file(s) must be synchronized");

      /*
       * We send an empty file list to the host as a notification that we
       * do not need any files for the given project.
       */
      final FileList fileList =
          missingFiles.isEmpty()
              ? FileListFactory.createEmptyFileList()
              : FileListFactory.createFileList(missingFiles);

      fileList.setProjectID(id);

      result.add(fileList);
    }

    return result;
  }

  /**
   * Waits for the activity queuing request from the remote side.
   *
   * @param monitor
   */
  protected void awaitActivityQueueingActivation(IProgressMonitor monitor)
      throws SarosCancellationException {

    monitor.beginTask(
        "Waiting for " + getPeer().getName() + " to continue the project negotiation...",
        IProgressMonitor.UNKNOWN);

    Packet packet = collectPacket(startActivityQueuingRequestCollector, PACKET_TIMEOUT);

    if (packet == null)
      throw new LocalCancellationException(
          "received no response from "
              + getPeer()
              + " while waiting to continue the project negotiation",
          CancelOption.DO_NOT_NOTIFY_PEER);

    monitor.done();
  }

  protected void createCollectors() {
    startActivityQueuingRequestCollector =
        receiver.createCollector(
            StartActivityQueuingRequest.PROVIDER.getPacketFilter(getSessionID(), getID()));
  }

  protected void deleteCollectors() {
    startActivityQueuingRequestCollector.cancel();
  }

  protected void checkProjectMapping(final Map<String, IProject> mapping) {

    for (final Entry<String, IProject> entry : mapping.entrySet()) {

      final String id = entry.getKey();
      final IProject project = entry.getValue();

      final ProjectNegotiationData data = getProjectNegotiationData(id);

      if (data == null) throw new IllegalArgumentException("invalid project id: " + id);

      if (!project.exists())
        throw new IllegalArgumentException("project does not exist: " + project);
    }
  }

  protected IResource getResource(IProject project, String path) {
    if (path.endsWith(FileList.DIR_SEPARATOR)) return project.getFolder(path);
    else return project.getFile(path);
  }

  @Override
  public String toString() {
    return "IPN [remote side: " + getPeer() + "]";
  }

  /**
   * Checks continuously, if the host started a FileTransferRequest. Returns when a request was
   * received.
   *
   * @throws SarosCancellationException on user cancellation
   */
  protected void awaitTransferRequest() throws SarosCancellationException {
    LOG.debug(this + ": waiting for incoming transfer request");
    try {
      while (!transferListener.hasReceived()) {
        checkCancellation(CancelOption.NOTIFY_PEER);
        Thread.sleep(200);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new LocalCancellationException();
    }
  }

  /** Listens to FileTransferRequests and checks if they meet the provided description. */
  protected static class TransferListener implements FileTransferListener {
    private String description;
    private volatile FileTransferRequest request;

    public TransferListener(String description) {
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

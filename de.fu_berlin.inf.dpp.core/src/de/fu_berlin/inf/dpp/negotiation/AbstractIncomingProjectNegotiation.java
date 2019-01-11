package de.fu_berlin.inf.dpp.negotiation;

import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationMissingFilesExtension;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingRequest;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.filesystem.FileSystem;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IFolder_V2;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.SubProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
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
  protected final FileReplacementInProgressObservable fileReplacementInProgressObservable;
  private final Map<String, ProjectNegotiationData> projectNegotiationData;
  protected boolean running;
  /** used to handle file transmissions * */
  protected TransferListener transferListener = null;

  private PacketCollector startActivityQueuingRequestCollector;

  public AbstractIncomingProjectNegotiation(
      final JID peer, //
      final TransferType transferType, //
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
        transferType,
        sessionManager,
        session,
        workspace,
        checksumCache,
        connectionService,
        transmitter,
        receiver);

    this.projectNegotiationData = new HashMap<String, ProjectNegotiationData>();

    for (final ProjectNegotiationData data : projectNegotiationData)
      this.projectNegotiationData.put(data.getReferencePointID(), data);

    this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;
  }

  /**
   * Starts the negotiation. The negotiation can be aborted by canceling the given monitor. The
   * execution of this method perform changes to the file system! It is the responsibility of the
   * caller to ensure that appropriate actions are performed to avoid unintended data loss, i.e this
   * method will do a best effort to backup altered data but no guarantee can be made in doing so!
   *
   * @param referencePointMapping mapping from remote reference point ids to the target local
   *     reference point
   * @throws IllegalArgumentException if either a project id is not valid or the referenced project
   *     for that id does not exist
   */
  public Status run(
      Map<String, IReferencePoint> referencePointMapping, final IProgressMonitor monitor) {

    checkProjectMapping(referencePointMapping);

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
              referencePointMapping, computeLocalVsRemoteDiff(referencePointMapping, monitor));

      monitor.subTask("");

      transmitter.send(
          ISarosSession.SESSION_CONNECTION_ID,
          getPeer(),
          ProjectNegotiationMissingFilesExtension.PROVIDER.create(
              new ProjectNegotiationMissingFilesExtension(getSessionID(), getID(), missingFiles)));

      transfer(monitor, referencePointMapping, missingFiles);

      checkCancellation(CancelOption.NOTIFY_PEER);

      /*
       * We are finished with the negotiation. Add all projects resources
       * to the session.
       */
      for (Entry<String, IReferencePoint> entry : referencePointMapping.entrySet()) {

        final String referencePointID = entry.getKey();
        final IReferencePoint referencePoint = entry.getValue();

        final boolean isPartialRemoteProject =
            getProjectNegotiationData(referencePointID).isPartial();

        final FileList remoteFileList = getProjectNegotiationData(referencePointID).getFileList();

        List<IResource> resources = null;

        if (isPartialRemoteProject) {

          final List<String> paths = remoteFileList.getPaths();

          resources = new ArrayList<IResource>(paths.size());

          for (final String path : paths) resources.add(getResource(referencePoint, path));
        }

        session.addSharedResources(referencePoint, referencePointID, resources);
      }
    } catch (Exception e) {
      exception = e;
    } finally {
      cleanup(monitor, referencePointMapping);
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
   * @param referencePointMapping mapping from remote reference points ids to the target local
   *     reference points
   * @param missingFiles files missing, that should be transferred and synchronized by this method
   *     call
   * @throws IOException, SarosCancellationException
   */
  protected abstract void transfer(
      IProgressMonitor monitor,
      Map<String, IReferencePoint> referencePointMapping,
      List<FileList> missingFiles)
      throws IOException, SarosCancellationException;

  /**
   * Cleanup ends the negotiation process, by disabling the reference point based queue and removes
   * acquired handlers during {@link #setup} and {@link #transfer}.
   *
   * @param monitor mapping from remote project ids to the target local reference point
   * @param referencePointMapping mapping of reference points
   */
  protected void cleanup(
      IProgressMonitor monitor, Map<String, IReferencePoint> referencePointMapping) {
    fileReplacementInProgressObservable.replacementDone();

    /*
     * TODO Queuing responsibility should be moved to Project
     * Negotiation, since its the only consumer of queuing
     * functionality. This will enable a specific Queuing mechanism per
     * TransferType (see github issue #137).
     */
    for (IReferencePoint referencePoint : referencePointMapping.values())
      session.disableQueuing(referencePoint);

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
   * @param localReferencePointMapping the local project mapping to use
   * @param monitor
   * @return list of differences (one for each project) between the local and the remote side.
   * @throws SarosCancellationException
   * @throws IOException
   */
  protected Map<String, FileListDiff> computeLocalVsRemoteDiff(
      final Map<String, IReferencePoint> localReferencePointMapping, final IProgressMonitor monitor)
      throws SarosCancellationException, IOException {

    LOG.debug(this + " : computing file and folder differences");

    monitor.beginTask(
        "Computing project(s) difference(s)...",
        localReferencePointMapping.size() * MONITOR_WORK_SCALE);

    final Map<String, FileListDiff> result = new HashMap<String, FileListDiff>();

    for (final Entry<String, IReferencePoint> entry : localReferencePointMapping.entrySet()) {

      final String id = entry.getKey();
      final IReferencePoint referencePoint = entry.getValue();

      // TODO optimize for partial shared projects

      final FileList localProjectFileList =
          FileListFactory.createFileList(
              referencePointManager,
              referencePoint,
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
   * @param localReferencePointMapping
   * @param diffs
   * @return list of file lists (each for every project) containing the missing files that are not
   *     present on the local side.
   * @throws IOException
   */
  protected List<FileList> synchronizeProjectStructures(
      final Map<String, IReferencePoint> localReferencePointMapping,
      final Map<String, FileListDiff> diffs)
      throws IOException {

    LOG.debug(this + " : deleting files and folders, creating empty folders");

    final List<FileList> result = new ArrayList<FileList>();

    for (final Entry<String, IReferencePoint> entry : localReferencePointMapping.entrySet()) {

      final String id = entry.getKey();
      final IReferencePoint referencePoint = entry.getValue();

      final FileListDiff diff = diffs.get(id);

      final List<String> resourcesToDelete =
          new ArrayList<String>(diff.getRemovedFiles().size() + diff.getRemovedFolders().size());

      resourcesToDelete.addAll(diff.getRemovedFiles());
      resourcesToDelete.addAll(diff.getRemovedFolders());

      Collections.sort(resourcesToDelete, Collections.reverseOrder());

      for (final String path : resourcesToDelete) {
        final IResource resource = getResource(referencePoint, path);

        if (resource.exists()) {

          if (LOG.isTraceEnabled()) LOG.trace("deleting resource: " + resource);

          resource.delete(IResource.KEEP_HISTORY);
        }
      }

      for (final String path : diff.getAddedFolders()) {
        final IFolder_V2 folder = referencePointManager.get(referencePoint).getFolder(path);

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

  protected void checkProjectMapping(final Map<String, IReferencePoint> mapping) {

    for (final Entry<String, IReferencePoint> entry : mapping.entrySet()) {

      final String id = entry.getKey();
      final IReferencePoint referencePoint = entry.getValue();

      final ProjectNegotiationData data = getProjectNegotiationData(id);

      if (data == null) throw new IllegalArgumentException("invalid project id: " + id);

      if (!referencePointManager.get(referencePoint).exists())
        throw new IllegalArgumentException("project does not exist: " + referencePoint);
    }
  }

  protected IResource getResource(IReferencePoint referencePoint, String path) {
    if (path.endsWith(FileList.DIR_SEPARATOR))
      return referencePointManager.get(referencePoint).getFolder(path);
    else return referencePointManager.get(referencePoint).getFile(path);
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

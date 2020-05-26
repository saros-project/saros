package saros.negotiation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CancellationException;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Packet;
import saros.communication.extensions.ProjectNegotiationMissingFilesExtension;
import saros.communication.extensions.ProjectNegotiationOfferingExtension;
import saros.communication.extensions.StartActivityQueuingRequest;
import saros.communication.extensions.StartActivityQueuingResponse;
import saros.editor.IEditorManager;
import saros.exceptions.LocalCancellationException;
import saros.exceptions.SarosCancellationException;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IWorkspace;
import saros.filesystem.checksum.IChecksumCache;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.SubProgressMonitor;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.PacketCollector;
import saros.net.xmpp.JID;
import saros.net.xmpp.filetransfer.XMPPFileTransferManager;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.synchronize.StartHandle;

/**
 * Handles outgoing ResourceNegotiations except for the actual file transfer.
 *
 * <p>Concrete implementations need to provide an implementation to exchange the calculated
 * differences. This class only provides the initial setup and calculation.
 */
public abstract class AbstractOutgoingResourceNegotiation extends ResourceNegotiation {

  private static final Logger log = Logger.getLogger(AbstractOutgoingResourceNegotiation.class);

  protected ProjectSharingData resourceSharingData;

  private static final Random NEGOTIATION_ID_GENERATOR = new Random();

  protected final IEditorManager editorManager;

  private PacketCollector remoteFileListResponseCollector;

  private PacketCollector startActivityQueuingResponseCollector;

  private final AdditionalResourceDataFactory additionalResourceDataFactory;

  protected AbstractOutgoingResourceNegotiation( //
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
        String.valueOf(NEGOTIATION_ID_GENERATOR.nextLong()),
        peer,
        sessionManager,
        session,
        workspace,
        checksumCache,
        fileTransferManager,
        transmitter,
        receiver);

    this.resourceSharingData = resourceSharingData;

    this.editorManager = editorManager;
    this.additionalResourceDataFactory = additionalResourceDataFactory;
  }

  public Status run(IProgressMonitor monitor) {

    createCollectors();

    observeMonitor(monitor);

    Exception exception = null;

    try {
      setup(monitor);

      sendFileList(createResourceNegotiationDataList(resourceSharingData, monitor), monitor);

      monitor.subTask("");

      List<FileList> fileLists = getRemoteFileList(monitor);
      monitor.subTask("");

      /*
       * If we are a non-host sharing resources with the host, now is the
       * time where we know that the host has accepted our resources. We
       * can thus safely assume these resources to be shared now.
       */
      if (!session.isHost()) {
        for (IReferencePoint referencePoint : resourceSharingData) {
          String referencePointID = resourceSharingData.getProjectID(referencePoint);
          session.addSharedProject(referencePoint, referencePointID);
        }
      }

      prepareTransfer(monitor, fileLists);

      checkCancellation(CancelOption.NOTIFY_PEER);

      transfer(monitor, fileLists);

      User user = session.getUser(getPeer());
      if (user == null) throw new LocalCancellationException(null, CancelOption.DO_NOT_NOTIFY_PEER);

      session.userFinishedProjectNegotiation(user);
    } catch (Exception e) {
      exception = e;
    } finally {
      cleanup(monitor);
    }

    return terminate(exception);
  }

  /**
   * Preparation for the resource negotiation. The negotiation can be aborted by canceling the given
   * monitor.
   *
   * @param monitor monitor to show progress to the user
   * @throws IOException , SarosCancellationException
   */
  protected abstract void setup(IProgressMonitor monitor)
      throws IOException, SarosCancellationException;

  /**
   * Prepare the file transfer. Can be used to process files (e.g. compress them) before the actual
   * transfer.
   *
   * @param monitor monitor to show progress to the user
   * @param fileLists list of files to be send
   * @throws IOException , SarosCancellationException
   */
  protected abstract void prepareTransfer(IProgressMonitor monitor, List<FileList> fileLists)
      throws IOException, SarosCancellationException;

  /**
   * Transfer the differing files.
   *
   * @param monitor monitor to show progress to the user
   * @param fileLists list of files to be send
   * @throws IOException , SarosCancellationException
   */
  protected abstract void transfer(IProgressMonitor monitor, List<FileList> fileLists)
      throws IOException, SarosCancellationException;

  /**
   * Cleanup acquired resources during {@link #setup}, {@link #prepareTransfer} and {@link
   * #transfer}.
   *
   * @param monitor the progress monitor
   */
  protected void cleanup(IProgressMonitor monitor) {
    deleteCollectors();
    monitor.done();
  }

  protected void sendFileList(
      List<ResourceNegotiationData> resourceNegotiationData, IProgressMonitor monitor)
      throws IOException, SarosCancellationException {

    /*
     * FIXME display the remote side something that will it receive
     * something in the near future
     */

    checkCancellation(CancelOption.NOTIFY_PEER);

    log.debug(this + " : sending file list");

    /*
     * file lists are normally very small so we "accept" the circumstance
     * that this step cannot be canceled.
     */

    monitor.setTaskName("Sending file list...");

    /*
     * The Remote receives this message at the InvitationHandler which calls
     * the SarosSessionManager which creates a IncomingResourceNegotiation
     * instance and pass it to the installed callback handler (which in the
     * current implementation opens a wizard on the remote side)
     */
    ProjectNegotiationOfferingExtension offering =
        new ProjectNegotiationOfferingExtension(getSessionID(), getID(), resourceNegotiationData);

    transmitter.send(
        ISarosSession.SESSION_CONNECTION_ID,
        getPeer(),
        ProjectNegotiationOfferingExtension.PROVIDER.create(offering));
  }

  /**
   * Retrieve the peer's partial file list and remember which files need to be sent to that user
   *
   * @param monitor
   * @throws IOException
   * @throws SarosCancellationException
   */
  protected List<FileList> getRemoteFileList(IProgressMonitor monitor)
      throws IOException, SarosCancellationException {

    log.debug(this + " : waiting for remote file list");

    monitor.beginTask(
        "Waiting for " + getPeer().getName() + " to choose reference point(s) location",
        IProgressMonitor.UNKNOWN);

    checkCancellation(CancelOption.NOTIFY_PEER);

    Packet packet = collectPacket(remoteFileListResponseCollector, 60 * 60 * 1000);

    if (packet == null)
      throw new LocalCancellationException(
          "received no response from " + getPeer() + " while waiting for the file list",
          CancelOption.DO_NOT_NOTIFY_PEER);

    List<FileList> remoteFileLists =
        ProjectNegotiationMissingFilesExtension.PROVIDER.getPayload(packet).getFileLists();

    log.debug(this + " : remote file list has been received");

    checkCancellation(CancelOption.NOTIFY_PEER);

    monitor.done();

    return remoteFileLists;
  }

  @Override
  protected void executeCancellation() {
    if (session.isHost() && session.getRemoteUsers().isEmpty())
      sessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
  }

  protected List<StartHandle> stopUsers(IProgressMonitor monitor) {

    /*
     * TODO: Make sure that all users are fully registered when stopping
     * them, otherwise failures might occur while a user is currently
     * joining and has not fully initialized yet.
     *
     * See also OutgoingSessionNegotiation#completeInvitation
     *
     * srossbach: This may already be the case ... just review this
     */

    final List<User> usersToStop = new ArrayList<User>(session.getUsers());

    log.debug(this + " : stopping users " + usersToStop);

    monitor.beginTask("Locking the session...", IProgressMonitor.UNKNOWN);

    // FIXME better handling of users that do not reply !!!
    try {
      return session
          .getStopManager()
          .stop(usersToStop, "archive creation for OPN [id=" + getID() + "]");
    } catch (CancellationException e) {
      log.warn("failed to stop users", e);
      return null;
    } finally {
      monitor.done();
    }
  }

  protected void startUsers(List<StartHandle> startHandles) {
    for (StartHandle startHandle : startHandles) {
      log.debug(this + " : restarting user " + startHandle.getUser());
      startHandle.start();
    }
  }

  protected void createCollectors() {
    remoteFileListResponseCollector =
        receiver.createCollector(
            ProjectNegotiationMissingFilesExtension.PROVIDER.getPacketFilter(
                getSessionID(), getID()));

    startActivityQueuingResponseCollector =
        receiver.createCollector(
            StartActivityQueuingResponse.PROVIDER.getPacketFilter(getSessionID(), getID()));
  }

  protected void deleteCollectors() {
    remoteFileListResponseCollector.cancel();
    startActivityQueuingResponseCollector.cancel();
  }

  protected List<ResourceNegotiationData> createResourceNegotiationDataList(
      final ProjectSharingData resourceSharingData, final IProgressMonitor monitor)
      throws IOException, LocalCancellationException {

    // *stretch* progress bar so it will increment smoothly
    final int scale = 1000;

    monitor.beginTask(
        "Creating file list and calculating file checksums. This may take a while...",
        resourceSharingData.size() * scale);

    List<ResourceNegotiationData> negData =
        new ArrayList<ResourceNegotiationData>(resourceSharingData.size());

    for (IReferencePoint referencePoint : resourceSharingData) {

      if (monitor.isCanceled())
        throw new LocalCancellationException(null, CancelOption.DO_NOT_NOTIFY_PEER);
      try {
        String referencePointID = resourceSharingData.getProjectID(referencePoint);

        /*
         * force editor buffer flush because we read the files from the
         * underlying storage
         */
        if (editorManager != null) editorManager.saveEditors(referencePoint);

        FileList referencePointFileList =
            FileListFactory.createFileList(
                referencePoint,
                checksumCache,
                new SubProgressMonitor(
                    monitor,
                    1 * scale,
                    SubProgressMonitor.SUPPRESS_BEGINTASK
                        | SubProgressMonitor.SUPPRESS_SETTASKNAME));

        referencePointFileList.setReferencePointID(referencePointID);

        Map<String, String> additionalResourceData =
            additionalResourceDataFactory.build(referencePoint);

        ResourceNegotiationData data =
            new ResourceNegotiationData(
                referencePointID,
                referencePoint.getName(),
                referencePointFileList,
                additionalResourceData);

        negData.add(data);

      } catch (IOException e) {
        /*
         * avoid that the error is send to remote side (which is default
         * for IOExceptions) at this point because the remote side has
         * no existing resource negotiation yet
         */
        localCancel(e.getMessage(), CancelOption.DO_NOT_NOTIFY_PEER);
        // throw to log this error in the Negotiation class
        throw new IOException(e.getMessage(), e);
      }
    }

    monitor.done();

    return negData;
  }

  /**
   * Sends an activity queuing request to the remote side and awaits the confirmation of the
   * request.
   *
   * @param monitor
   */
  protected void sendAndAwaitActivityQueueingActivation(IProgressMonitor monitor)
      throws IOException, SarosCancellationException {

    monitor.beginTask(
        "Waiting for " + getPeer().getName() + " to perform additional initialization...",
        IProgressMonitor.UNKNOWN);

    transmitter.send(
        ISarosSession.SESSION_CONNECTION_ID,
        getPeer(),
        StartActivityQueuingRequest.PROVIDER.create(
            new StartActivityQueuingRequest(getSessionID(), getID())));

    Packet packet = collectPacket(startActivityQueuingResponseCollector, PACKET_TIMEOUT);

    if (packet == null)
      throw new LocalCancellationException(
          "received no response from "
              + getPeer()
              + " while waiting to finish additional initialization",
          CancelOption.DO_NOT_NOTIFY_PEER);

    monitor.done();
  }

  @Override
  public String toString() {
    return "OPN [remote side: " + getPeer() + "]";
  }
}

package de.fu_berlin.inf.dpp.negotiation;

import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationMissingFilesExtension;
import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationOfferingExtension;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingRequest;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingResponse;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
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
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CancellationException;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Packet;

/**
 * Handles outgoing ProjectNegotiations except for the actual file transfer.
 *
 * <p>Concrete implementations need to provide an implementation to exchange the calculated
 * differences. This class only provides the initial setup and calculation.
 */
public abstract class AbstractOutgoingProjectNegotiation extends ProjectNegotiation {

  private static final Logger LOG = Logger.getLogger(AbstractOutgoingProjectNegotiation.class);

  protected ProjectSharingData projects;

  private static final Random NEGOTIATION_ID_GENERATOR = new Random();

  protected final IEditorManager editorManager;

  private PacketCollector remoteFileListResponseCollector;

  private PacketCollector startActivityQueuingResponseCollector;

  protected AbstractOutgoingProjectNegotiation( //
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
        String.valueOf(NEGOTIATION_ID_GENERATOR.nextLong()),
        peer,
        sessionManager,
        session,
        workspace,
        checksumCache,
        connectionService,
        transmitter,
        receiver);

    this.projects = projects;

    this.editorManager = editorManager;
  }

  public Status run(IProgressMonitor monitor) {

    createCollectors();

    observeMonitor(monitor);

    Exception exception = null;

    try {
      setup(monitor);

      sendFileList(createProjectNegotiationDataList(projects, monitor), monitor);

      monitor.subTask("");

      List<FileList> fileLists = getRemoteFileList(monitor);
      monitor.subTask("");

      /*
       * If we are a non-host sharing projects with the host, now is the
       * time where we know that the host has accepted our projects. We
       * can thus safely assume these projects to be shared now.
       */
      if (!session.isHost()) {
        for (IProject project : projects) {
          String projectID = projects.getProjectID(project);
          List<IResource> resources = projects.getResourcesToShare(project);
          session.addSharedResources(project, projectID, resources);
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
   * Preparation for the Project Negotiation. The negotiation can be aborted by canceling the given
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
   * @param monitor mapping from remote project ids to the target local projects
   */
  protected void cleanup(IProgressMonitor monitor) {
    deleteCollectors();
    monitor.done();
  }

  protected void sendFileList(List<ProjectNegotiationData> projectInfos, IProgressMonitor monitor)
      throws IOException, SarosCancellationException {

    /*
     * FIXME display the remote side something that will it receive
     * something in the near future
     */

    checkCancellation(CancelOption.NOTIFY_PEER);

    LOG.debug(this + " : sending file list");

    /*
     * file lists are normally very small so we "accept" the circumstance
     * that this step cannot be canceled.
     */

    monitor.setTaskName("Sending file list...");

    /*
     * The Remote receives this message at the InvitationHandler which calls
     * the SarosSessionManager which creates a IncomingProjectNegotiation
     * instance and pass it to the installed callback handler (which in the
     * current implementation opens a wizard on the remote side)
     */
    ProjectNegotiationOfferingExtension offering =
        new ProjectNegotiationOfferingExtension(getSessionID(), getID(), projectInfos);

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

    LOG.debug(this + " : waiting for remote file list");

    monitor.beginTask(
        "Waiting for " + getPeer().getName() + " to choose project(s) location",
        IProgressMonitor.UNKNOWN);

    checkCancellation(CancelOption.NOTIFY_PEER);

    Packet packet = collectPacket(remoteFileListResponseCollector, 60 * 60 * 1000);

    if (packet == null)
      throw new LocalCancellationException(
          "received no response from " + getPeer() + " while waiting for the file list",
          CancelOption.DO_NOT_NOTIFY_PEER);

    List<FileList> remoteFileLists =
        ProjectNegotiationMissingFilesExtension.PROVIDER.getPayload(packet).getFileLists();

    LOG.debug(this + " : remote file list has been received");

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

    LOG.debug(this + " : stopping users " + usersToStop);

    monitor.beginTask("Locking the session...", IProgressMonitor.UNKNOWN);

    // FIXME better handling of users that do not reply !!!
    try {
      return session
          .getStopManager()
          .stop(usersToStop, "archive creation for OPN [id=" + getID() + "]");
    } catch (CancellationException e) {
      LOG.warn("failed to stop users", e);
      return null;
    } finally {
      monitor.done();
    }
  }

  protected void startUsers(List<StartHandle> startHandles) {
    for (StartHandle startHandle : startHandles) {
      LOG.debug(this + " : restarting user " + startHandle.getUser());
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

  protected List<ProjectNegotiationData> createProjectNegotiationDataList(
      final ProjectSharingData projectSharingData, final IProgressMonitor monitor)
      throws IOException, LocalCancellationException {

    // *stretch* progress bar so it will increment smoothly
    final int scale = 1000;

    monitor.beginTask(
        "Creating file list and calculating file checksums. This may take a while...",
        projectSharingData.size() * scale);

    List<ProjectNegotiationData> negData =
        new ArrayList<ProjectNegotiationData>(projectSharingData.size());

    for (IProject project : projectSharingData) {

      if (monitor.isCanceled())
        throw new LocalCancellationException(null, CancelOption.DO_NOT_NOTIFY_PEER);
      try {
        String projectID = projectSharingData.getProjectID(project);
        List<IResource> resources = projectSharingData.getResourcesToShare(project);

        /*
         * force editor buffer flush because we read the files from the
         * underlying storage
         */
        if (editorManager != null) editorManager.saveEditors(project);

        FileList projectFileList =
            FileListFactory.createFileList(
                project,
                resources,
                checksumCache,
                new SubProgressMonitor(
                    monitor,
                    1 * scale,
                    SubProgressMonitor.SUPPRESS_BEGINTASK
                        | SubProgressMonitor.SUPPRESS_SETTASKNAME));

        boolean partial = projectSharingData.shouldBeSharedPartially(project);

        projectFileList.setProjectID(projectID);

        ProjectNegotiationData data =
            new ProjectNegotiationData(projectID, project.getName(), partial, projectFileList);

        negData.add(data);

      } catch (IOException e) {
        /*
         * avoid that the error is send to remote side (which is default
         * for IOExceptions) at this point because the remote side has
         * no existing project negotiation yet
         */
        localCancel(e.getMessage(), CancelOption.DO_NOT_NOTIFY_PEER);
        // throw to LOG this error in the Negotiation class
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

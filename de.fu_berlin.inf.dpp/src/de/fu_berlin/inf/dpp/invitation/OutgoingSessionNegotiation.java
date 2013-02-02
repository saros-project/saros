package de.fu_berlin.inf.dpp.invitation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.FileListFactory;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.discoverymanager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcceptedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationCompletedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationOfferingExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationParameterExchangeExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.IChecksumCache;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.Messages;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.Compatibility;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/*
 * IMPORTANT: All messages in the cancellation exception are SHOWN to the end user !
 */
public final class OutgoingSessionNegotiation extends InvitationProcess {

    private static final Logger log = Logger
        .getLogger(OutgoingSessionNegotiation.class);

    private static final Random INVITATION_ID_GENERATOR = new Random();

    private static final Object SESSION_JOIN_LOCK = new Object();

    private ISarosSession sarosSession;
    private SubMonitor monitor;

    private VersionInfo versionInfo;

    private SarosPacketCollector invitationAcceptedCollector;
    private SarosPacketCollector invitationAcknowledgedCollector;
    private SarosPacketCollector invitationDataExchangeCollector;
    private SarosPacketCollector invitationCompletedCollector;

    @Inject
    private VersionManager versionManager;

    @Inject
    private DiscoveryManager discoveryManager;

    @Inject
    private SessionIDObservable sessionID;

    @Inject
    private EditorManager editorManager;

    @Inject
    private DataTransferManager dataTransferManager;

    @Inject
    private ISarosSessionManager sessionManager;

    @Inject
    private IChecksumCache checksumCache;

    private int colorID = -1;

    public OutgoingSessionNegotiation(JID peer, ISarosSession sarosSession,
        String description, SarosContext sarosContext) {
        super(String.valueOf(INVITATION_ID_GENERATOR.nextLong()), peer,
            description, sarosContext);

        this.sarosSession = sarosSession;
    }

    /**
     * @JTourBusStop 5, Invitation Process:
     * 
     *               The details of the invitation process are implemented in
     *               the invitation package. OutgoingSessionNegotiation is an
     *               example of a class that participates in this process.
     * 
     *               The host of a session needs negotiations for:
     * 
     *               - Sending invitation to a session
     *               (OutgoingSessionNegotiation)
     * 
     *               - Sending project resources included in a session
     *               (OutgoingProjectNegotiation)
     * 
     *               All other participants need negotiations for:
     * 
     *               - Dealing with a received invitation to a session
     *               (IncomingSessionNegotiation)
     * 
     *               - Handling incoming shared project resources
     *               (IncomingProjectNegotiation)
     */

    public Status start(IProgressMonitor progressMonitor) {
        log.debug(this + " : starting invitation");

        monitor = SubMonitor.convert(progressMonitor, "Inviting "
            + peerNickname + "...", 100);

        observeMonitor(monitor);

        createCollectors();

        Exception exception = null;

        try {
            checkAvailability(monitor.newChild(0));

            checkVersion(monitor.newChild(0));

            sendInvitation(monitor.newChild(0));

            awaitAcceptation(monitor.newChild(0));

            modifiyAndSendSessionNegotiationData(awaitRemoteSessionNegotiationData(monitor
                .newChild(0)));
            /*
             * HACK Ensure byte stream connection to peer so the project wizard
             * always show the currently used connection (IBB, Socks5(D/M)
             */

            // FIMXE: MUST BE CALLED HERE, or the Network Layer will crash
            // on the first activity that is send

            awaitCompletion(monitor.newChild(0));

            monitor.setTaskName("Negotiating data connection...");

            dataTransferManager.connect(peer);

            User newUser = addUserToSession(monitor.newChild(0));

            /*
             * User accepted the invitation, so NOW we can create the file
             * lists... not before the invitation was accepted!
             */

            // FIXME lock the projects on the workspace !

            editorManager.setAllLocalOpenedEditorsLocked(true);

            /*
             * FIXME this should not be calculated here but in the
             * OutgoingProjectNegotiation !
             * 
             * FIXME the file list may contain dirty checksums after this call
             * because dirty editors are NOT saved !
             */
            List<ProjectExchangeInfo> projectExchangeInfos = createProjectExchangeInfoList(
                new ArrayList<IProject>(sarosSession.getProjects()),
                monitor.newChild(100, SubMonitor.SUPPRESS_NONE));

            monitor.subTask("");

            completeInvitation(projectExchangeInfos, monitor);

            // Whiteboard is using this listener
            sessionManager.postOutgoingInvitationCompleted(monitor, newUser);

            sarosSessionManager
                .startSharingProjects(peer, projectExchangeInfos);

        } catch (Exception e) {
            exception = e;
        } finally {
            deleteCollectors();
            monitor.done();
            invitationProcesses.removeInvitationProcess(this);
        }

        return terminateProcess(exception);
    }

    /**
     * Performs a discovery request on the remote side and checks for Saros
     * support. When this method returns the remote JID has been properly
     * updated to a full resource qualified JID.
     */
    private void checkAvailability(IProgressMonitor monitor)
        throws LocalCancellationException {

        log.debug(this + " : checking Saros support");
        monitor.setTaskName("Checking Saros support...");

        JID resourceQualifiedJID = discoveryManager.getSupportingPresence(peer,
            Saros.NAMESPACE);

        if (resourceQualifiedJID == null)
            throw new LocalCancellationException(
                peerNickname
                    + " does not support Saros or the request timed out. Please try again.",
                CancelOption.DO_NOT_NOTIFY_PEER);

        log.debug(this + " :  remote contact offers Saros support");

        peer = resourceQualifiedJID;
    }

    /**
     * Checks the compatibility of the local Saros version with the remote side.
     * If the versions are compatible, the invitation continues, otherwise a
     * confirmation of the user is required (a {@link MessageDialog} pops up).
     */
    private void checkVersion(IProgressMonitor monitor)
        throws SarosCancellationException {

        log.debug(this + " : checking version compatibility");
        monitor.setTaskName("Checking version compatibility...");

        VersionInfo versionInfo = versionManager.determineCompatibility(peer);

        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);

        Compatibility comp = null;

        if (versionInfo != null) {
            comp = versionInfo.compatibility;

            if (comp == VersionManager.Compatibility.OK) {
                log.debug(this + " : Saros versions are compatible");
                this.versionInfo = versionInfo;
            } else {
                log.debug(this + " : Saros versions are not compatible");
                if (DialogUtils.confirmVersionConflict(versionInfo, peer,
                    versionManager.getVersion()))
                    this.versionInfo = versionInfo;
                else {
                    throw new LocalCancellationException(null,
                        CancelOption.DO_NOT_NOTIFY_PEER);
                }
            }
        } else {
            log.debug(this + " : could not obtain remote version information");
            if (!DialogUtils.confirmUnknownVersion(peer,
                versionManager.getVersion()))
                throw new LocalCancellationException(null,
                    CancelOption.DO_NOT_NOTIFY_PEER);
        }
    }

    /**
     * Sends an invitation and waits for acknowledgment. The acknowledgment is
     * auto generated on the remote side and generates a packet reply that is
     * different as if the remote user manually accepts the invitation.
     * 
     */
    private void sendInvitation(IProgressMonitor monitor)
        throws SarosCancellationException {
        monitor.setTaskName("Sending invitation...");

        log.debug(this + " : sending invitation");
        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);

        /*
         * TODO: this method should get a complete VersionInfo object from the
         * checkVersion() method.
         */
        VersionInfo hostVersionInfo = versionInfo;
        if (hostVersionInfo == null) {
            hostVersionInfo = new VersionInfo();
            hostVersionInfo.compatibility = null;
        }

        hostVersionInfo.version = versionManager.getVersion().toString();

        InvitationOfferingExtension invitationOffering = new InvitationOfferingExtension(
            invitationID, sessionID.getValue(), sarosSession.getSessionStart(),
            versionInfo, description);

        transmitter.sendMessageToUser(peer,
            InvitationOfferingExtension.PROVIDER.create(invitationOffering));

        log.debug(this + " : waiting for invitation acknowledgement");

        monitor.setTaskName("Waiting for " + peerNickname
            + " to acknowledge the invitation...");

        if (collectPacket(invitationAcknowledgedCollector, PACKET_TIMEOUT) == null) {
            throw new LocalCancellationException(
                "Received no invitation acknowledgement from " + peerNickname
                    + ".", CancelOption.DO_NOT_NOTIFY_PEER);
        }
    }

    /**
     * Waits until the remote side accepted manually the invitation.
     */
    private void awaitAcceptation(IProgressMonitor monitor)
        throws SarosCancellationException {

        log.debug(this + " : waiting for peer to accept the invitation");

        monitor.setTaskName("Waiting for " + peerNickname
            + " to accept invitation...");

        if (collectPacket(invitationAcceptedCollector,
            INVITATION_ACCEPTED_TIMEOUT) == null) {
            throw new LocalCancellationException(
                "Invitation was not accepted.", CancelOption.NOTIFY_PEER);
        }

        log.debug(this + " : invitation accepted");
    }

    /**
     * Waits until the remote side has completed the invitation which is the
     * case after the remote side has started its {@link SarosSession}.
     */
    private void awaitCompletion(IProgressMonitor monitor)
        throws SarosCancellationException {

        log.debug(this
            + " : waiting for remote side to start its Saros session");

        monitor.setTaskName("Waiting for " + peerNickname
            + " to perform final initialization...");

        if (collectPacket(invitationCompletedCollector, PACKET_TIMEOUT) == null) {
            throw new LocalCancellationException(
                "Invitation was not accepted.", CancelOption.NOTIFY_PEER);
        }

        log.debug(this + " : remote side started its Saros session");
    }

    /**
     * Waits for the remote parameters which may contain some desired default
     * values that should be used on session start.
     */
    private InvitationParameterExchangeExtension awaitRemoteSessionNegotiationData(
        IProgressMonitor monitor) throws SarosCancellationException {

        log.debug(this + " : waiting for remote session negotiation data");

        monitor.setTaskName("Waiting for remote session configuration...");

        InvitationParameterExchangeExtension parameters;

        Packet packet = collectPacket(invitationDataExchangeCollector,
            PACKET_TIMEOUT);

        if (packet == null)
            throw new LocalCancellationException(peerNickname
                + " does not respond. (Timeout)",
                CancelOption.DO_NOT_NOTIFY_PEER);

        parameters = InvitationParameterExchangeExtension.PROVIDER
            .getPayload(packet);

        if (parameters == null)
            throw new LocalCancellationException(peerNickname
                + " sent malformed data", CancelOption.DO_NOT_NOTIFY_PEER);

        log.debug(this + " : received remote session negotiation data");

        return parameters;
    }

    /**
     * Checks and modifies the received remote parameters. The changes will be
     * send back and must then be used on the remote side to configure the
     * session environment.
     */
    private InvitationParameterExchangeExtension modifiyAndSendSessionNegotiationData(
        InvitationParameterExchangeExtension remoteParameters) {

        log.debug(this + " : sending updated session negotiation data");

        monitor.setTaskName("Sending local session configuration...");

        InvitationParameterExchangeExtension modifiedParameters = new InvitationParameterExchangeExtension(
            invitationID);

        modifiedParameters.setRemoteColorID(sarosSession.getLocalUser()
            .getColorID());

        modifiedParameters
            .setMUCPreferences(sarosSession.isHost() ? mucNegotiatingManager
                .getOwnPreferences() : mucNegotiatingManager
                .getSessionPreferences());

        modifiedParameters.setSessionHost(sarosSession.getHost().getJID());

        // side effect !
        colorID = sarosSession.getColor(remoteParameters.getLocalColorID());

        modifiedParameters.setLocalColorID(colorID);

        transmitter.sendMessageToUser(peer,
            InvitationParameterExchangeExtension.PROVIDER
                .create(modifiedParameters));

        log.debug(this + " : sent updated session negotiation data");

        return modifiedParameters;
    }

    /**
     * Adds the invited user to the current SarosSession. After the user is
     * added to the session an acknowledgment is send to the remote side that
     * the remote user can now start working in this session.
     */
    // TODO move to SarosSession.
    private User addUserToSession(IProgressMonitor monitor)
        throws SarosCancellationException {

        synchronized (SESSION_JOIN_LOCK) {

            User newUser = new User(sarosSession, peer, colorID);

            sarosSession.addUser(newUser);
            log.debug(this + " : added " + Utils.prefix(peer)
                + " to the current session, colorID: " + colorID);

            transmitter.sendMessageToUser(peer,
                InvitationAcknowledgedExtension.PROVIDER
                    .create(new InvitationAcknowledgedExtension(invitationID)));

            checkCancellation(CancelOption.NOTIFY_PEER);
            sarosSession.synchronizeUserList(transmitter, peer, monitor);
            return newUser;
        }
    }

    /**
     * Completes the invitation by setting the appropriate flag in the user
     * object and synchronize the user lists with all session users afterwards.
     * 
     */
    private void completeInvitation(
        List<ProjectExchangeInfo> projectExchangeInfos, IProgressMonitor monitor)
        throws SarosCancellationException {

        log.debug(this + " : synchronizing user list");

        monitor.setTaskName("Synchronizing user list...");

        synchronized (SESSION_JOIN_LOCK) {
            sarosSession.userInvitationCompleted(sarosSession.getUser(peer));
            checkCancellation(CancelOption.NOTIFY_PEER);
            sarosSession.synchronizeUserList(transmitter, peer, monitor); // SUPPRESSALL
        }

        log.debug(this + " : session negotiation finished");
    }

    @Override
    protected void executeCancellation() {
        if (colorID >= 0)
            sarosSession.returnColor(colorID);

        if (sarosSession.getRemoteUsers().isEmpty())
            sarosSessionManager.stopSarosSession();

        if (invitationProcesses.getProcesses().contains(this))
            invitationProcesses.removeInvitationProcess(this);
    }

    private void deleteCollectors() {
        invitationAcceptedCollector.cancel();
        invitationAcknowledgedCollector.cancel();
        invitationDataExchangeCollector.cancel();
        invitationCompletedCollector.cancel();
    }

    private void createCollectors() {
        invitationAcceptedCollector = receiver
            .createCollector(InvitationAcceptedExtension.PROVIDER
                .getPacketFilter(invitationID));

        invitationAcknowledgedCollector = receiver
            .createCollector(InvitationAcknowledgedExtension.PROVIDER
                .getPacketFilter(invitationID));

        invitationDataExchangeCollector = receiver
            .createCollector(InvitationParameterExchangeExtension.PROVIDER
                .getPacketFilter(invitationID));

        invitationCompletedCollector = receiver
            .createCollector(InvitationCompletedExtension.PROVIDER
                .getPacketFilter(invitationID));
    }

    /**
     * Method to create list of ProjectExchangeInfo.
     * 
     * @param projectsToShare
     *            List of projects initially to share
     * @param monitor
     *            Show progress
     * @return
     * @throws LocalCancellationException
     */
    private List<ProjectExchangeInfo> createProjectExchangeInfoList(
        List<IProject> projectsToShare, SubMonitor monitor)
        throws LocalCancellationException {

        monitor.beginTask(Messages.SarosSessionManager_creating_file_list,
            projectsToShare.size());

        List<ProjectExchangeInfo> pInfos = new ArrayList<ProjectExchangeInfo>(
            projectsToShare.size());

        for (IProject iProject : projectsToShare) {
            if (monitor.isCanceled())
                throw new LocalCancellationException(null,
                    CancelOption.DO_NOT_NOTIFY_PEER);
            try {
                String projectID = sarosSession.getProjectID(iProject);
                String projectName = iProject.getName();

                FileList projectFileList = FileListFactory.createFileList(
                    iProject, sarosSession.getSharedResources(iProject),
                    checksumCache, sarosSession.useVersionControl(),
                    monitor.newChild(100 / projectsToShare.size()));

                projectFileList.setProjectID(projectID);
                boolean partial = !sarosSession.isCompletelyShared(iProject);

                ProjectExchangeInfo pInfo = new ProjectExchangeInfo(projectID,
                    "", projectName, partial, projectFileList);

                pInfos.add(pInfo);

            } catch (CoreException e) {
                throw new LocalCancellationException(e.getMessage(),
                    CancelOption.DO_NOT_NOTIFY_PEER);
            }
        }
        return pInfos;
    }

    @Override
    public String toString() {
        return "OSN [remote side: " + peer + "]";
    }
}

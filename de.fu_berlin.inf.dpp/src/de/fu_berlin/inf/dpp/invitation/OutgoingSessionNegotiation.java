package de.fu_berlin.inf.dpp.invitation;

import java.io.IOException;
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
import de.fu_berlin.inf.dpp.communication.chat.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.communication.chat.muc.negotiation.MUCSessionPreferencesNegotiatingManager;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.discoverymanager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcceptedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationParametersExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.IChecksumCache;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.Messages;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.Compatibility;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

public final class OutgoingSessionNegotiation extends InvitationProcess {

    private final static Logger log = Logger
        .getLogger(OutgoingSessionNegotiation.class);

    private static final long INVITATION_ACKNOWLEDGEMENT_TIMEOUT = Long
        .getLong(
            "de.fu_berlin.inf.dpp.invitation.session.INVITATION_ACKNOWLEDGEMENT_TIMEOUT",
            30000L);

    private static final long INVITATION_ACCEPTED_TIMEOUT = Long.getLong(
        "de.fu_berlin.inf.dpp.invitation.session.INVITATION_ACCEPTED_TIMEOUT",
        600000L);

    private final static Random INVITATION_ID_GENERATOR = new Random();

    private ISarosSession sarosSession;
    private SubMonitor monitor;

    private boolean peerAdvertisesSarosSupport = true;

    private VersionInfo versionInfo;

    private SarosPacketCollector invitationAcceptedCollector;
    private SarosPacketCollector invitationAcknowledgedCollector;

    @Inject
    private VersionManager versionManager;

    @Inject
    private DiscoveryManager discoveryManager;

    @Inject
    private MUCSessionPreferencesNegotiatingManager comNegotiatingManager;

    @Inject
    private SessionIDObservable sessionID;

    @Inject
    private IReceiver xmppReceiver;

    @Inject
    private EditorManager editorManager;

    @Inject
    private DataTransferManager dataTransferManager;

    @Inject
    private ISarosSessionManager sessionManager;

    @Inject
    private IChecksumCache checksumCache;

    public OutgoingSessionNegotiation(JID peer, int colorID,
        ISarosSession sarosSession, String description,
        SarosContext sarosContext) {
        super(String.valueOf(INVITATION_ID_GENERATOR.nextLong()), peer,
            description, colorID, sarosContext);

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
        log.debug(this + " : Invitation has started");

        monitor = SubMonitor.convert(progressMonitor, "Starting invitation...",
            100);

        observeMonitor(monitor);

        createCollectors();

        Exception exception = null;

        try {
            checkAvailability(monitor.newChild(0));

            checkVersion(monitor.newChild(0));

            sendInvitation(monitor.newChild(0));

            /*
             * HACK Ensure byte stream connection to peer so the project wizard
             * always show the currently used connection (IBB, Socks5(D/M)
             */

            // FIMXE: MUST BE CALLED HERE, or the Network Layer will crash
            // on the first activity that is send

            monitor.setTaskName("Negotiating data connection...");

            dataTransferManager.getConnection(peer);

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

    private void checkAvailability(IProgressMonitor monitor)
        throws LocalCancellationException {

        log.debug(this + " : Checking Saros support");
        monitor.setTaskName("Checking Saros support...");

        JID rqPeer = discoveryManager.getSupportingPresence(peer,
            Saros.NAMESPACE);

        if (rqPeer == null)
            throw new LocalCancellationException(
                peer
                    + " does not support Saros or the request timed out. Please try again.",
                CancelOption.DO_NOT_NOTIFY_PEER);

        log.debug(this + " :  remote contact offers Saros support");

        peer = rqPeer;
    }

    /**
     * Checks the compatibility of the local Saros version with the peer's one.
     * If the versions are compatible, the invitation continues, otherwise a
     * confirmation of the user is required (a {@link MessageDialog} pops up).
     */
    private void checkVersion(IProgressMonitor monitor)
        throws SarosCancellationException {

        log.debug(this + " : Checking peer's version");
        monitor.setTaskName("Checking version compatibility...");

        VersionInfo versionInfo = versionManager.determineCompatibility(peer);

        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);

        Compatibility comp = null;

        if (versionInfo != null) {
            comp = versionInfo.compatibility;

            if (comp == VersionManager.Compatibility.OK) {
                log.debug(this + " : Saros versions are compatible, proceeding");
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
            log.debug(this + " : Unable to obtain peer's version information");
            if (!DialogUtils.confirmUnknownVersion(peer,
                versionManager.getVersion()))
                throw new LocalCancellationException(null,
                    CancelOption.DO_NOT_NOTIFY_PEER);
        }
    }

    /**
     * Send an invitation, then wait for the peer to accept and request the file
     * list.
     * 
     * @param monitor
     * @throws SarosCancellationException
     * @throws IOException
     */
    private void sendInvitation(IProgressMonitor monitor)
        throws SarosCancellationException, IOException {
        monitor.setTaskName("Sending invitation...");

        log.debug(this + " : Sending invitation");
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

        MUCSessionPreferences comPrefs;
        if (sarosSession.isHost()) {
            comPrefs = comNegotiatingManager.getOwnPreferences();
        } else {
            comPrefs = comNegotiatingManager.getSessionPreferences();
        }

        InvitationParametersExtension invInfo = new InvitationParametersExtension(
            sessionID.getValue(), invitationID, colorID, description,
            versionInfo, sarosSession.getSessionStart(), comPrefs, sarosSession
                .getHost().getJID(), sarosSession.getLocalUser().getColorID());

        transmitter.sendMessageToUser(peer,
            InvitationParametersExtension.PROVIDER.create(invInfo));

        monitor.setTaskName("Invitation sent. Waiting for acknowledgement...");

        if (collectPacket(invitationAcknowledgedCollector,
            INVITATION_ACKNOWLEDGEMENT_TIMEOUT, monitor) == null) {
            throw new LocalCancellationException(
                peerAdvertisesSarosSupport ? "No invitation acknowledgement received."
                    : "Missing Saros support.", CancelOption.DO_NOT_NOTIFY_PEER);
        }

        monitor.setTaskName("Waiting for user to accept invitation");

        if (collectPacket(invitationAcceptedCollector,
            INVITATION_ACCEPTED_TIMEOUT, monitor) == null) {
            throw new LocalCancellationException(
                peerAdvertisesSarosSupport ? "no invitation complete response received"
                    : "Missing Saros support.", CancelOption.DO_NOT_NOTIFY_PEER);
        }

        // Reply is send in addUserToSession !

        log.debug(this + " : peer accepted the invitation");
    }

    /**
     * Adds the invited user to the current SarosSession.<br>
     */
    // TODO move to SarosSession.
    private User addUserToSession(IProgressMonitor monitor)
        throws SarosCancellationException {
        synchronized (sarosSession) {
            User newUser = new User(sarosSession, peer, colorID);
            this.sarosSession.addUser(newUser);
            log.debug(Utils.prefix(peer) + " added to project, colorID: "
                + colorID);

            checkCancellation(CancelOption.NOTIFY_PEER);
            sarosSession.synchronizeUserList(transmitter, peer, monitor);
            return newUser;
        }
    }

    /**
     * Receives an invitation complete message and finalizes the Invitation
     * negotiation job, then starts the OutgoingProjectNegotiation
     * 
     * @param monitor
     * @param projectExchangeInfos
     * @throws SarosCancellationException
     */
    private void completeInvitation(
        List<ProjectExchangeInfo> projectExchangeInfos, IProgressMonitor monitor)
        throws SarosCancellationException {

        log.debug(this + " : synchronizing user list");

        monitor.setTaskName("Synchronizing user list ...");
        synchronized (sarosSession) {
            sarosSession.userInvitationCompleted(sarosSession.getUser(peer));
            checkCancellation(CancelOption.NOTIFY_PEER);
            sarosSession.synchronizeUserList(transmitter, peer, monitor); // SUPPRESSALL
        }

        log.debug(this + " : Invitation has completed successfully");
    }

    @Override
    protected void executeCancellation() {
        sarosSession.returnColor(this.colorID);

        if (sarosSession.getRemoteUsers().isEmpty())
            sarosSessionManager.stopSarosSession();

        if (invitationProcesses.getProcesses().contains(this))
            invitationProcesses.removeInvitationProcess(this);
    }

    private Packet collectPacket(SarosPacketCollector collector, long timeout,
        IProgressMonitor monitor) throws SarosCancellationException {

        Packet packet = null;

        while (timeout > 0) {
            checkCancellation(CancelOption.NOTIFY_PEER);

            packet = collector.nextResult(1000);

            if (packet != null)
                break;

            timeout -= 1000;
        }

        return packet;
    }

    private void deleteCollectors() {
        invitationAcceptedCollector.cancel();
        invitationAcknowledgedCollector.cancel();
    }

    private void createCollectors() {
        invitationAcceptedCollector = xmppReceiver
            .createCollector(InvitationAcceptedExtension.PROVIDER
                .getPacketFilter(invitationID));

        invitationAcknowledgedCollector = xmppReceiver
            .createCollector(InvitationAcknowledgedExtension.PROVIDER
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

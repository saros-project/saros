package de.fu_berlin.inf.dpp.ui.eventhandler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.ui.progress.IProgressConstants;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
import de.fu_berlin.inf.dpp.invitation.OutgoingProjectNegotiation;
import de.fu_berlin.inf.dpp.invitation.OutgoingSessionNegotiation;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.project.INegotiationHandler;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.Messages;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.ui.wizards.AddProjectToSessionWizard;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.ui.wizards.dialogs.WizardDialogAccessable;

/**
 * This handler is responsible for presenting and running the session and
 * project negotiations that are received by the Saros Session Manager
 * component.
 * 
 * @author srossbach
 */
public class NegotiationHandler implements INegotiationHandler {

    private static final Logger LOG = Logger
        .getLogger(NegotiationHandler.class);

    /**
     * 
     * OutgoingInvitationJob wraps the instance of
     * {@link OutgoingSessionNegotiation} and cares about handling the
     * exceptions like local or remote cancellation.
     * 
     * It notifies the user about the progress using the Eclipse Jobs API and
     * interrupts the process if the session closes.
     * 
     */
    private class OutgoingInvitationJob extends Job {

        private OutgoingSessionNegotiation process;
        private String peer;

        public OutgoingInvitationJob(OutgoingSessionNegotiation process) {
            super(MessageFormat.format(
                Messages.SarosSessionManager_inviting_user,
                User.getHumanReadableName(network, process.getPeer())));
            this.process = process;
            this.peer = process.getPeer().getBase();

            setUser(true);
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ICON_PROPERTY,
                ImageManager
                    .getImageDescriptor("/icons/elcl16/project_share_tsk.png"));
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                InvitationProcess.Status status = process.start(monitor);

                switch (status) {
                case CANCEL:
                    return Status.CANCEL_STATUS;
                case ERROR:
                    return new Status(IStatus.ERROR, Saros.SAROS,
                        process.getErrorMessage());
                case OK:
                    break;
                case REMOTE_CANCEL:
                    SarosView
                        .showNotification(
                            Messages.SarosSessionManager_canceled_invitation,
                            MessageFormat
                                .format(
                                    Messages.SarosSessionManager_canceled_invitation_text,
                                    peer));

                    return new Status(
                        IStatus.CANCEL,
                        Saros.SAROS,
                        MessageFormat
                            .format(
                                Messages.SarosSessionManager_canceled_invitation_text,
                                peer));

                case REMOTE_ERROR:
                    SarosView
                        .showNotification(
                            Messages.SarosSessionManager_error_during_invitation,
                            MessageFormat
                                .format(
                                    Messages.SarosSessionManager_error_during_invitation_text,
                                    peer, process.getErrorMessage()));

                    return new Status(
                        IStatus.ERROR,
                        Saros.SAROS,
                        MessageFormat
                            .format(
                                Messages.SarosSessionManager_error_during_invitation_text,
                                peer, process.getErrorMessage()));
                }
            } catch (Exception e) {
                LOG.error("This exception is not expected here: ", e);
                return new Status(IStatus.ERROR, Saros.SAROS, e.getMessage(), e);

            }

            sessionManager.startSharingProjects(process.getPeer());

            return Status.OK_STATUS;
        }
    }

    private class OutgoingProjectJob extends Job {

        private OutgoingProjectNegotiation process;
        private String peer;

        public OutgoingProjectJob(
            OutgoingProjectNegotiation outgoingProjectNegotiation) {
            super(Messages.SarosSessionManager_sharing_project);
            process = outgoingProjectNegotiation;
            peer = process.getPeer().getBase();

            setUser(true);
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ICON_PROPERTY,
                ImageManager.getImageDescriptor("/icons/invites.png"));
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                ProjectNegotiation.Status status = process.start(monitor);
                String peerName = User.getHumanReadableName(network, new JID(
                    peer));

                final String message;

                switch (status) {
                case CANCEL:
                    return Status.CANCEL_STATUS;
                case ERROR:
                    return new Status(IStatus.ERROR, Saros.SAROS,
                        process.getErrorMessage());
                case OK:
                    break;
                case REMOTE_CANCEL:
                    message = MessageFormat
                        .format(
                            Messages.SarosSessionManager_project_sharing_cancelled_text,
                            peerName);

                    SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.openInformationMessageDialog(
                                EditorAPI.getShell(), message, message);
                        }
                    });

                    return new Status(IStatus.CANCEL, Saros.SAROS, message);

                case REMOTE_ERROR:
                    message = MessageFormat
                        .format(
                            Messages.SarosSessionManager_sharing_project_cancelled_remotely,
                            peerName, process.getErrorMessage());
                    SarosView
                        .showNotification(
                            Messages.SarosSessionManager_sharing_project_cancelled_remotely_text,
                            message);

                    return new Status(IStatus.ERROR, Saros.SAROS, message);
                }
            } catch (Exception e) {
                LOG.error("This exception is not expected here: ", e);
                return new Status(IStatus.ERROR, Saros.SAROS, e.getMessage(), e);

            }

            return Status.OK_STATUS;
        }
    }

    private final SarosUI sarosUI;

    private final ISarosSessionManager sessionManager;

    private final SarosNet network;

    public NegotiationHandler(ISarosSessionManager sessionManager,
        SarosNet network, SarosUI sarosUI) {
        this.sarosUI = sarosUI;
        this.network = network;
        this.sessionManager = sessionManager;
        this.sessionManager.setNegotiationHandler(this);
    }

    @Override
    public void handleOutgoingSessionNegotiation(
        OutgoingSessionNegotiation negotiation) {

        OutgoingInvitationJob outgoingInvitationJob = new OutgoingInvitationJob(
            negotiation);

        outgoingInvitationJob.setPriority(Job.SHORT);
        outgoingInvitationJob.schedule();
    }

    @Override
    public void handleIncomingSessionNegotiation(
        IncomingSessionNegotiation negotiation) {
        showIncomingInvitationUI(negotiation);
    }

    @Override
    public void handleOutgoingProjectNegotiation(
        OutgoingProjectNegotiation negotiation) {

        OutgoingProjectJob job = new OutgoingProjectJob(negotiation);
        job.setPriority(Job.SHORT);
        job.schedule();
    }

    @Override
    public void handleIncomingProjectNegotiation(
        IncomingProjectNegotiation negotiation) {
        showIncomingProjectUI(negotiation);
    }

    private void showIncomingInvitationUI(
        final IncomingSessionNegotiation process) {

        SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
            @Override
            public void run() {
                sarosUI.openSarosView();
            }
        });

        // Fixes #2727848: InvitationDialog is opened in the
        // background
        SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
            @Override
            public void run() {
                /**
                 * @JTourBusStop 8, Invitation Process:
                 * 
                 *               (4a) The SessionManager then hands over the
                 *               control to the NegotiationHandler (this class)
                 *               which works on a newly started
                 *               IncomingSessionNegotiation. This handler opens
                 *               the JoinSessionWizard, a dialog for the user to
                 *               decide whether to accept the invitation.
                 */
                JoinSessionWizard sessionWizard = new JoinSessionWizard(process);

                final WizardDialogAccessable wizardDialog = new WizardDialogAccessable(
                    EditorAPI.getShell(), sessionWizard);

                // TODO Provide help :-)
                wizardDialog.setHelpAvailable(false);

                // as we are not interested in the result
                wizardDialog.setBlockOnOpen(false);

                DialogUtils.openWindow(wizardDialog);
            }
        });
    }

    private void showIncomingProjectUI(final IncomingProjectNegotiation process) {
        List<ProjectExchangeInfo> pInfos = process.getProjectInfos();
        final List<FileList> fileLists = new ArrayList<FileList>(pInfos.size());

        for (ProjectExchangeInfo pInfo : pInfos)
            fileLists.add(pInfo.getFileList());

        SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

            @Override
            public void run() {
                AddProjectToSessionWizard projectWizard = new AddProjectToSessionWizard(
                    process, process.getPeer(), fileLists, process
                        .getProjectNames());

                final WizardDialogAccessable wizardDialog = new WizardDialogAccessable(
                    EditorAPI.getShell(), projectWizard, SWT.MIN | SWT.MAX,
                    SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL
                        | SWT.PRIMARY_MODAL);

                /*
                 * IMPORTANT: as the dialog is non modal it MUST NOT block on
                 * open or there is a good chance to crash the whole GUI
                 * 
                 * Scenario: A modal dialog is currently open with
                 * setBlockOnOpen(true) (as most input dialogs are).
                 * 
                 * When we now open this wizard with setBlockOnOpen(true) this
                 * wizard will become the main dispatcher for the SWT Thread. As
                 * this wizard is non modal you cannot close it because you
                 * could not access it. Therefore the modal dialog cannot be
                 * closed as well because it is stuck on the non modal dialog
                 * which currently serves as main dispatcher !
                 */

                wizardDialog.setBlockOnOpen(false);

                wizardDialog.setHelpAvailable(false);
                projectWizard.setWizardDlg(wizardDialog);

                DialogUtils.openWindow(wizardDialog);
            }
        });
    }
}

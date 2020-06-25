package saros.ui.eventhandler;

import java.text.MessageFormat;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.ui.progress.IProgressConstants;
import saros.Saros;
import saros.monitoring.ProgressMonitorAdapterFactory;
import saros.negotiation.AbstractIncomingResourceNegotiation;
import saros.negotiation.AbstractOutgoingResourceNegotiation;
import saros.negotiation.IncomingSessionNegotiation;
import saros.negotiation.OutgoingSessionNegotiation;
import saros.negotiation.ResourceNegotiation;
import saros.negotiation.SessionNegotiation;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.JID;
import saros.session.INegotiationHandler;
import saros.session.ISarosSessionManager;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.DialogUtils;
import saros.ui.util.SWTUtils;
import saros.ui.util.ViewUtils;
import saros.ui.views.SarosView;
import saros.ui.wizards.AddReferencePointsToSessionWizard;
import saros.ui.wizards.JoinSessionWizard;
import saros.ui.wizards.dialogs.WizardDialogAccessable;

/**
 * This handler is responsible for presenting and running the session and resource negotiations that
 * are received by the Saros Session Manager component.
 */
public class NegotiationHandler implements INegotiationHandler {

  private static final Logger log = Logger.getLogger(NegotiationHandler.class);

  /**
   * OutgoingInvitationJob wraps the instance of {@link OutgoingSessionNegotiation} and cares about
   * handling the exceptions like local or remote cancellation.
   *
   * <p>It notifies the user about the progress using the Eclipse Jobs API and interrupts the
   * negotiation if the session closes.
   */
  private class OutgoingInvitationJob extends Job {

    private OutgoingSessionNegotiation negotiation;
    private String peer;

    public OutgoingInvitationJob(OutgoingSessionNegotiation negotiation) {

      super(
          MessageFormat.format(
              Messages.NegotiationHandler_inviting_user,
              getNickname(negotiation.getPeer()),
              negotiation.getPeer().getRAW()));

      this.negotiation = negotiation;
      this.peer = negotiation.getPeer().getBase();

      setUser(true);
      setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
      setProperty(
          IProgressConstants.ICON_PROPERTY,
          ImageManager.getImageDescriptor("/icons/elcl16/session_tsk.png"));
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      try {
        SessionNegotiation.Status status =
            negotiation.start(ProgressMonitorAdapterFactory.convert(monitor));

        switch (status) {
          case CANCEL:
            return Status.CANCEL_STATUS;
          case ERROR:
            return new Status(IStatus.ERROR, Saros.PLUGIN_ID, negotiation.getErrorMessage());
          case OK:
            break;
          case REMOTE_CANCEL:
            SarosView.showNotification(
                Messages.NegotiationHandler_canceled_invitation,
                MessageFormat.format(Messages.NegotiationHandler_canceled_invitation_text, peer));

            return new Status(
                IStatus.CANCEL,
                Saros.PLUGIN_ID,
                MessageFormat.format(Messages.NegotiationHandler_canceled_invitation_text, peer));

          case REMOTE_ERROR:
            SarosView.showNotification(
                Messages.NegotiationHandler_error_during_invitation,
                MessageFormat.format(
                    Messages.NegotiationHandler_error_during_invitation_text,
                    peer,
                    negotiation.getErrorMessage()));

            return new Status(
                IStatus.ERROR,
                Saros.PLUGIN_ID,
                MessageFormat.format(
                    Messages.NegotiationHandler_error_during_invitation_text,
                    peer,
                    negotiation.getErrorMessage()));
        }
      } catch (Exception e) {
        log.error("This exception is not expected here: ", e);
        return new Status(IStatus.ERROR, Saros.PLUGIN_ID, e.getMessage(), e);
      }

      sessionManager.startSharingReferencePoints(negotiation.getPeer());

      return Status.OK_STATUS;
    }
  }

  private class OutgoingResourceJob extends Job {

    private AbstractOutgoingResourceNegotiation negotiation;
    private String peer;

    public OutgoingResourceJob(AbstractOutgoingResourceNegotiation outgoingResourceNegotiation) {
      super(Messages.NegotiationHandler_sharing_resources);
      negotiation = outgoingResourceNegotiation;
      peer = negotiation.getPeer().getBase();

      setUser(true);
      setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
      setProperty(
          IProgressConstants.ICON_PROPERTY, ImageManager.getImageDescriptor("/icons/invites.png"));
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      try {
        ResourceNegotiation.Status status =
            negotiation.run(ProgressMonitorAdapterFactory.convert(monitor));

        String peerName = getNickname(new JID(peer));

        final String message;

        switch (status) {
          case CANCEL:
            return Status.CANCEL_STATUS;
          case ERROR:
            return new Status(IStatus.ERROR, Saros.PLUGIN_ID, negotiation.getErrorMessage());
          case OK:
            break;
          case REMOTE_CANCEL:
            message =
                MessageFormat.format(
                    Messages.NegotiationHandler_resource_sharing_canceled_text, peerName);

            SWTUtils.runSafeSWTAsync(
                log,
                new Runnable() {
                  @Override
                  public void run() {
                    DialogUtils.openInformationMessageDialog(SWTUtils.getShell(), message, message);
                  }
                });

            return new Status(IStatus.CANCEL, Saros.PLUGIN_ID, message);

          case REMOTE_ERROR:
            message =
                MessageFormat.format(
                    Messages.NegotiationHandler_sharing_resources_canceled_remotely,
                    peerName,
                    negotiation.getErrorMessage());
            SarosView.showNotification(
                Messages.NegotiationHandler_sharing_resources_canceled_remotely_text, message);

            return new Status(IStatus.ERROR, Saros.PLUGIN_ID, message);
        }
      } catch (Exception e) {
        log.error("This exception is not expected here: ", e);
        return new Status(IStatus.ERROR, Saros.PLUGIN_ID, e.getMessage(), e);
      }

      return Status.OK_STATUS;
    }
  }

  private final ISarosSessionManager sessionManager;

  public NegotiationHandler(ISarosSessionManager sessionManager) {
    sessionManager.setNegotiationHandler(this);
    this.sessionManager = sessionManager;
  }

  @Override
  public void handleOutgoingSessionNegotiation(OutgoingSessionNegotiation negotiation) {

    OutgoingInvitationJob outgoingInvitationJob = new OutgoingInvitationJob(negotiation);

    outgoingInvitationJob.setPriority(Job.SHORT);
    outgoingInvitationJob.schedule();
  }

  @Override
  public void handleIncomingSessionNegotiation(IncomingSessionNegotiation negotiation) {
    showIncomingInvitationUI(negotiation);
  }

  @Override
  public void handleOutgoingResourceNegotiation(AbstractOutgoingResourceNegotiation negotiation) {

    OutgoingResourceJob job = new OutgoingResourceJob(negotiation);
    job.setPriority(Job.SHORT);
    job.schedule();
  }

  @Override
  public void handleIncomingResourceNegotiation(AbstractIncomingResourceNegotiation negotiation) {
    showIncomingResourceNegotiationUI(negotiation);
  }

  private void showIncomingInvitationUI(final IncomingSessionNegotiation negotiation) {

    SWTUtils.runSafeSWTAsync(
        log,
        new Runnable() {
          @Override
          public void run() {
            ViewUtils.openSarosView();
          }
        });

    // Fixes #2727848: InvitationDialog is opened in the
    // background
    SWTUtils.runSafeSWTAsync(
        log,
        new Runnable() {
          @Override
          public void run() {
            /**
             * @JTourBusStop 7, Invitation Process:
             *
             * <p>(4a) The SessionManager then hands over the control to the NegotiationHandler
             * (this class) which works on a newly started IncomingSessionNegotiation. This handler
             * opens the JoinSessionWizard, a dialog for the user to decide whether to accept the
             * invitation.
             */
            JoinSessionWizard sessionWizard = new JoinSessionWizard(negotiation);

            final WizardDialogAccessable wizardDialog =
                new WizardDialogAccessable(SWTUtils.getShell(), sessionWizard);

            // TODO Provide help :-)
            wizardDialog.setHelpAvailable(false);

            // as we are not interested in the result
            wizardDialog.setBlockOnOpen(false);

            DialogUtils.openWindow(wizardDialog);
          }
        });
  }

  private void showIncomingResourceNegotiationUI(
      final AbstractIncomingResourceNegotiation negotiation) {

    SWTUtils.runSafeSWTAsync(
        log,
        new Runnable() {

          @Override
          public void run() {
            AddReferencePointsToSessionWizard referencePointWizard =
                new AddReferencePointsToSessionWizard(negotiation);

            final WizardDialogAccessable wizardDialog =
                new WizardDialogAccessable(
                    SWTUtils.getShell(),
                    referencePointWizard,
                    SWT.MIN | SWT.MAX,
                    SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.PRIMARY_MODAL);

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
            referencePointWizard.setWizardDlg(wizardDialog);

            DialogUtils.openWindow(wizardDialog);
          }
        });
  }

  private static String getNickname(JID jid) {
    return XMPPUtils.getNickname(null, jid, jid.getBase());
  }
}

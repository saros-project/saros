package saros.core.ui.eventhandler;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import java.awt.Window;
import java.text.MessageFormat;
import org.apache.log4j.Logger;
import saros.core.monitoring.IStatus;
import saros.core.monitoring.Status;
import saros.intellij.SarosComponent;
import saros.intellij.runtime.EDTExecutor;
import saros.intellij.runtime.UIMonitoredJob;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.NotificationPanel;
import saros.intellij.ui.util.UIProjectUtils;
import saros.intellij.ui.wizards.AddReferencePointToSessionWizard;
import saros.intellij.ui.wizards.JoinSessionWizard;
import saros.monitoring.IProgressMonitor;
import saros.negotiation.AbstractIncomingProjectNegotiation;
import saros.negotiation.AbstractOutgoingProjectNegotiation;
import saros.negotiation.IncomingSessionNegotiation;
import saros.negotiation.OutgoingSessionNegotiation;
import saros.negotiation.ProjectNegotiation;
import saros.negotiation.SessionNegotiation;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.JID;
import saros.session.INegotiationHandler;
import saros.session.ISarosSessionManager;

/**
 * This handler is responsible for presenting and running the session and reference point
 * negotiations that are received by the Saros Session Manager component.
 */
public class NegotiationHandler implements INegotiationHandler {

  private static final Logger log = Logger.getLogger(NegotiationHandler.class);

  private final ISarosSessionManager sessionManager;
  private final UIProjectUtils projectUtils;

  public NegotiationHandler(ISarosSessionManager sessionManager, UIProjectUtils projectUtils) {
    sessionManager.setNegotiationHandler(this);

    this.sessionManager = sessionManager;
    this.projectUtils = projectUtils;
  }

  private static String getNickname(JID jid) {
    return XMPPUtils.getNickname(null, jid, jid.getBase());
  }

  @Override
  public void handleOutgoingSessionNegotiation(OutgoingSessionNegotiation negotiation) {
    OutgoingInvitationJob outgoingInvitationJob = new OutgoingInvitationJob(negotiation);

    outgoingInvitationJob.schedule();
  }

  @Override
  public void handleIncomingSessionNegotiation(IncomingSessionNegotiation negotiation) {
    projectUtils.runWithProject(project -> showIncomingInvitationUI(project, negotiation));
  }

  @Override
  public void handleOutgoingProjectNegotiation(AbstractOutgoingProjectNegotiation negotiation) {
    OutgoingProjectJob job = new OutgoingProjectJob(negotiation);
    job.schedule();
  }

  @Override
  public void handleIncomingProjectNegotiation(AbstractIncomingProjectNegotiation negotiation) {
    projectUtils.runWithProject(
        project -> showIncomingReferencePointNegotiationUI(project, negotiation));
  }

  private void showIncomingInvitationUI(
      Project project, final IncomingSessionNegotiation negotiation) {

    EDTExecutor.invokeLater(
        () -> {
          JoinSessionWizard wizard =
              new JoinSessionWizard(project, getWindow(project), negotiation);

          wizard.open();
        },
        ModalityState.defaultModalityState());
  }

  private void showIncomingReferencePointNegotiationUI(
      Project project, final AbstractIncomingProjectNegotiation negotiation) {

    EDTExecutor.invokeLater(
        () -> {
          AddReferencePointToSessionWizard wizard =
              new AddReferencePointToSessionWizard(project, getWindow(project), negotiation);

          wizard.open();
        },
        ModalityState.defaultModalityState());
  }

  private Window getWindow(Project project) {
    return WindowManager.getInstance().getFrame(project);
  }
  /**
   * OutgoingInvitationJob wraps the instance of {@link OutgoingSessionNegotiation} and cares about
   * handling the exceptions like local or remote cancellation.
   *
   * <p>It notifies the user about the progress using the Eclipse Jobs API and interrupts the
   * negotiation if the session closes.
   */
  private class OutgoingInvitationJob extends UIMonitoredJob {

    private final OutgoingSessionNegotiation negotiation;
    private final String peer;

    OutgoingInvitationJob(OutgoingSessionNegotiation negotiation) {
      super(
          MessageFormat.format(
              Messages.NegotiationHandler_session_processing, getNickname(negotiation.getPeer())));
      this.negotiation = negotiation;
      peer = negotiation.getPeer().getBase();
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      try {
        SessionNegotiation.Status status = negotiation.start(monitor);

        String message;

        switch (status) {
          case CANCEL:
            return Status.CANCEL_STATUS;
          case ERROR:
            message =
                MessageFormat.format(
                    Messages.NegotiationHandler_session_local_error_message,
                    negotiation.getErrorMessage());

            NotificationPanel.showError(
                message, Messages.NegotiationHandler_session_local_error_title);

            return new Status(IStatus.ERROR, SarosComponent.PLUGIN_ID, message);
          case OK:
            break;
          case REMOTE_CANCEL:
            message =
                MessageFormat.format(
                    Messages.NegotiationHandler_session_remote_cancel_message, peer);

            NotificationPanel.showInformation(
                message, Messages.NegotiationHandler_session_remote_cancel_title);

            return new Status(IStatus.CANCEL, SarosComponent.PLUGIN_ID, message);

          case REMOTE_ERROR:
            message =
                MessageFormat.format(
                    Messages.NegotiationHandler_session_remote_error_message,
                    peer,
                    negotiation.getErrorMessage());

            NotificationPanel.showError(
                message, Messages.NegotiationHandler_session_remote_error_title);

            return new Status(IStatus.ERROR, SarosComponent.PLUGIN_ID, message);
        }

      } catch (Exception e) {
        log.error("This exception is not expected here: ", e);
        return new Status(IStatus.ERROR, SarosComponent.PLUGIN_ID, e.getMessage(), e);
      }

      sessionManager.startSharingProjects(negotiation.getPeer());

      return Status.OK_STATUS;
    }
  }

  private class OutgoingProjectJob extends UIMonitoredJob {

    private final AbstractOutgoingProjectNegotiation negotiation;
    private final String peer;

    OutgoingProjectJob(AbstractOutgoingProjectNegotiation outgoingProjectNegotiation) {
      super(Messages.NegotiationHandler_sharing_reference_point);
      negotiation = outgoingProjectNegotiation;
      peer = negotiation.getPeer().getBase();
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      try {
        ProjectNegotiation.Status status = negotiation.run(monitor);
        String peerName = getNickname(new JID(peer));

        final String message;

        switch (status) {
          case CANCEL:
            return Status.CANCEL_STATUS;
          case ERROR:
            return new Status(
                IStatus.ERROR, SarosComponent.PLUGIN_ID, negotiation.getErrorMessage());
          case OK:
            break;
          case REMOTE_CANCEL:
            message =
                MessageFormat.format(
                    Messages.NegotiationHandler_reference_point_negotiation_canceled_message,
                    peerName);

            NotificationPanel.showInformation(message, "Reference point sharing canceled remotely");

            return new Status(IStatus.CANCEL, SarosComponent.PLUGIN_ID, message);

          case REMOTE_ERROR:
            message =
                MessageFormat.format(
                    Messages.NegotiationHandler_sharing_reference_point_canceled_remotely_message,
                    peerName,
                    negotiation.getErrorMessage());
            NotificationPanel.showError(
                message,
                Messages.NegotiationHandler_sharing_reference_point_canceled_remotely_title);

            return new Status(IStatus.ERROR, SarosComponent.PLUGIN_ID, message);
        }
      } catch (Exception e) {
        log.error("This exception is not expected here: ", e);
        return new Status(IStatus.ERROR, SarosComponent.PLUGIN_ID, e.getMessage(), e);
      }

      return Status.OK_STATUS;
    }
  }
}

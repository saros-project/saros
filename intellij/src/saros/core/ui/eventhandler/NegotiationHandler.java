package saros.core.ui.eventhandler;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import java.awt.Window;
import java.text.MessageFormat;
import org.apache.log4j.Logger;
import saros.core.monitoring.IStatus;
import saros.core.monitoring.Status;
import saros.intellij.SarosComponent;
import saros.intellij.runtime.UIMonitoredJob;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.NotificationPanel;
import saros.intellij.ui.util.UIProjectUtils;
import saros.intellij.ui.wizards.AddProjectToSessionWizard;
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
 * This handler is responsible for presenting and running the session and project negotiations that
 * are received by the Saros Session Manager component.
 */
public class NegotiationHandler implements INegotiationHandler {

  private static final Logger LOG = Logger.getLogger(NegotiationHandler.class);

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
    projectUtils.runWithProject(project -> showIncomingProjectUI(project, negotiation));
  }

  private void showIncomingInvitationUI(
      Project project, final IncomingSessionNegotiation negotiation) {

    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              JoinSessionWizard wizard =
                  new JoinSessionWizard(project, getWindow(project), negotiation);
              wizard.setModal(true);
              wizard.open();
            },
            ModalityState.defaultModalityState());
  }

  private void showIncomingProjectUI(
      Project project, final AbstractIncomingProjectNegotiation negotiation) {

    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              AddProjectToSessionWizard wizard =
                  new AddProjectToSessionWizard(project, getWindow(project), negotiation);

              wizard.setModal(false);
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
              Messages.NegotiationHandler_inviting_user, getNickname(negotiation.getPeer())));
      this.negotiation = negotiation;
      peer = negotiation.getPeer().getBase();
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      try {
        SessionNegotiation.Status status = negotiation.start(monitor);

        switch (status) {
          case CANCEL:
            return Status.CANCEL_STATUS;
          case ERROR:
            return new Status(
                IStatus.ERROR, SarosComponent.PLUGIN_ID, negotiation.getErrorMessage());
          case OK:
            break;
          case REMOTE_CANCEL:
            NotificationPanel.showInformation(
                MessageFormat.format(Messages.NegotiationHandler_canceled_invitation_text, peer),
                Messages.NegotiationHandler_canceled_invitation);

            return new Status(
                IStatus.CANCEL,
                SarosComponent.PLUGIN_ID,
                MessageFormat.format(Messages.NegotiationHandler_canceled_invitation_text, peer));

          case REMOTE_ERROR:
            NotificationPanel.showError(
                MessageFormat.format(
                    Messages.NegotiationHandler_error_during_invitation_text,
                    peer,
                    negotiation.getErrorMessage()),
                Messages.NegotiationHandler_error_during_invitation);

            return new Status(
                IStatus.ERROR,
                SarosComponent.PLUGIN_ID,
                MessageFormat.format(
                    Messages.NegotiationHandler_error_during_invitation_text,
                    peer,
                    negotiation.getErrorMessage()));
        }
      } catch (Exception e) {
        LOG.error("This exception is not expected here: ", e);
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
      super(Messages.NegotiationHandler_sharing_project);
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
                    Messages.NegotiationHandler_project_sharing_canceled_text, peerName);

            ApplicationManager.getApplication()
                .invokeLater(
                    () ->
                        NotificationPanel.showInformation(
                            message, "Project sharing canceled remotely"));

            return new Status(IStatus.CANCEL, SarosComponent.PLUGIN_ID, message);

          case REMOTE_ERROR:
            message =
                MessageFormat.format(
                    Messages.NegotiationHandler_sharing_project_canceled_remotely,
                    peerName,
                    negotiation.getErrorMessage());
            NotificationPanel.showError(
                message, Messages.NegotiationHandler_sharing_project_canceled_remotely_text);

            return new Status(IStatus.ERROR, SarosComponent.PLUGIN_ID, message);
        }
      } catch (Exception e) {
        LOG.error("This exception is not expected here: ", e);
        return new Status(IStatus.ERROR, SarosComponent.PLUGIN_ID, e.getMessage(), e);
      }

      return Status.OK_STATUS;
    }
  }
}

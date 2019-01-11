package de.fu_berlin.inf.dpp.core.ui.eventhandler;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import de.fu_berlin.inf.dpp.core.monitoring.IStatus;
import de.fu_berlin.inf.dpp.core.monitoring.Status;
import de.fu_berlin.inf.dpp.intellij.SarosComponent;
import de.fu_berlin.inf.dpp.intellij.runtime.UIMonitoredJob;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.AddProjectToSessionWizard;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.AbstractIncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.negotiation.AbstractOutgoingProjectNegotiation;
import de.fu_berlin.inf.dpp.negotiation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.negotiation.OutgoingSessionNegotiation;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.negotiation.SessionNegotiation;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.INegotiationHandler;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import java.awt.Window;
import java.text.MessageFormat;
import org.apache.log4j.Logger;

/**
 * This handler is responsible for presenting and running the session and project negotiations that
 * are received by the Saros Session Manager component.
 */
public class NegotiationHandler implements INegotiationHandler {

  private static final Logger LOG = Logger.getLogger(NegotiationHandler.class);
  private final ISarosSessionManager sessionManager;

  public NegotiationHandler(ISarosSessionManager sessionManager) {
    sessionManager.setNegotiationHandler(this);
    this.sessionManager = sessionManager;
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
    showIncomingInvitationUI(negotiation);
  }

  @Override
  public void handleOutgoingProjectNegotiation(AbstractOutgoingProjectNegotiation negotiation) {

    OutgoingProjectJob job = new OutgoingProjectJob(negotiation);
    job.schedule();
  }

  @Override
  public void handleIncomingProjectNegotiation(AbstractIncomingProjectNegotiation negotiation) {
    showIncomingProjectUI(negotiation);
  }

  private void showIncomingInvitationUI(final IncomingSessionNegotiation negotiation) {

    ApplicationManager.getApplication()
        .invokeLater(
            new Runnable() {
              @Override
              public void run() {
                JoinSessionWizard wizard = new JoinSessionWizard(getWindow(), negotiation);
                wizard.setModal(true);
                wizard.open();
              }
            },
            ModalityState.defaultModalityState());
  }

  private void showIncomingProjectUI(final AbstractIncomingProjectNegotiation negotiation) {

    ApplicationManager.getApplication()
        .invokeLater(
            new Runnable() {
              @Override
              public void run() {

                AddProjectToSessionWizard wizard =
                    new AddProjectToSessionWizard(getWindow(), negotiation);

                wizard.setModal(false);
                wizard.open();
              }
            },
            ModalityState.defaultModalityState());
  }

  private static Window getWindow() {
    DataContext dataContext = DataManager.getInstance().getDataContext();
    Project project = DataKeys.PROJECT.getData(dataContext);
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

    public OutgoingInvitationJob(OutgoingSessionNegotiation negotiation) {
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

    public OutgoingProjectJob(AbstractOutgoingProjectNegotiation outgoingProjectNegotiation) {
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
                    new Runnable() {
                      @Override
                      public void run() {
                        NotificationPanel.showInformation(
                            message, "Project sharing canceled remotely");
                      }
                    });

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

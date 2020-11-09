package saros.intellij.ui.wizards;

import static saros.negotiation.NegotiationTools.CancelOption;

import com.intellij.openapi.project.Project;
import java.awt.Window;
import java.text.MessageFormat;
import org.apache.log4j.Logger;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.JobWithStatus;
import saros.intellij.ui.util.NotificationPanel;
import saros.intellij.ui.wizards.pages.HeaderPanel;
import saros.intellij.ui.wizards.pages.InfoPage;
import saros.intellij.ui.wizards.pages.PageActionListener;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.NullProgressMonitor;
import saros.negotiation.CancelListener;
import saros.negotiation.IncomingSessionNegotiation;
import saros.negotiation.NegotiationTools.CancelLocation;
import saros.net.xmpp.JID;
import saros.util.ThreadUtils;

/**
 * A wizard that guides the user through an incoming invitation process.
 *
 * <p>FIXME: Long-Running Operation after each step cancellation by a remote party auto-advance.
 */
public class JoinSessionWizard extends Wizard {
  private static final String PAGE_INFO_ID = "JoinSessionInfo";

  private static final Logger log = Logger.getLogger(JoinSessionWizard.class);

  private final IncomingSessionNegotiation negotiation;
  private final JID peer;

  private final PageActionListener actionListener =
      new PageActionListener() {
        @Override
        public void back() {
          // Not interested
        }

        @Override
        public void next() {
          joinSession();
        }

        @Override
        public void cancel() {
          performCancel();
        }
      };

  @SuppressWarnings("FieldCanBeLocal")
  private final CancelListener cancelListener =
      new CancelListener() {

        @Override
        public void canceled(final CancelLocation location, final String message) {
          if (location != CancelLocation.LOCAL) {
            showCancelMessage(peer, message, location);
            close();
          }
        }
      };

  /**
   * Instantiates the wizard and its pages.
   *
   * @param project the project to use for the wizard
   * @param parent the parent window relative to which the dialog is positioned
   * @param negotiation The negotiation this wizard displays
   */
  public JoinSessionWizard(Project project, Window parent, IncomingSessionNegotiation negotiation) {
    super(
        project,
        parent,
        Messages.JoinSessionWizard_title,
        new HeaderPanel(
            Messages.ShowDescriptionPage_title2, Messages.ShowDescriptionPage_description));

    setModal(true);

    this.negotiation = negotiation;
    this.peer = negotiation.getPeer();

    negotiation.addCancelListener(cancelListener);

    InfoPage infoPage = createInfoPage(negotiation);

    registerPage(infoPage);

    create();
  }

  private InfoPage createInfoPage(IncomingSessionNegotiation negotiation) {
    InfoPage infoPage =
        new InfoPage(PAGE_INFO_ID, Messages.JoinSessionWizard_accept, actionListener);
    infoPage.addText(negotiation.getPeer().getName() + " " + Messages.JoinSessionWizard_info);
    infoPage.addText(negotiation.getDescription());
    return infoPage;
  }

  /**
   * Runs {@link IncomingSessionNegotiation#accept(IProgressMonitor)} with {@link
   * #runTaskAsync(Runnable, String, Runnable, Runnable)}.
   *
   * <p>The result of the task is displayed using {@link #showStatus(JobWithStatus)}.
   */
  private void joinSession() {
    JobWithStatus job =
        new JobWithStatus() {
          @Override
          public void run() {
            status = negotiation.accept(new NullProgressMonitor());
          }
        };

    runTaskAsync(
        job, Messages.JoinSessionWizard_task_title, () -> showStatus(job), this::performCancel);

    close();
  }

  /**
   * Shows the result of the job used to join the session to the user.
   *
   * @param job the job whose status to show to the user
   */
  private void showStatus(JobWithStatus job) {
    switch (job.status) {
      case OK:
        break;

      case CANCEL:
      case ERROR:
        showCancelMessage(
            negotiation.getPeer(), negotiation.getErrorMessage(), CancelLocation.LOCAL);
        break;

      case REMOTE_CANCEL:
      case REMOTE_ERROR:
        showCancelMessage(
            negotiation.getPeer(), negotiation.getErrorMessage(), CancelLocation.REMOTE);
        break;

      default:
        throw new IllegalArgumentException("Encountered unhandled job status " + job.status);
    }
  }

  /**
   * Calls {@link IncomingSessionNegotiation#localCancel(String, CancelOption)} in a separate
   * thread.
   */
  private void performCancel() {
    ThreadUtils.runSafeAsync(
        "CancelJoinSessionWizard",
        log,
        () -> negotiation.localCancel(null, CancelOption.NOTIFY_PEER));
  }

  private void showCancelMessage(JID jid, String errorMsg, CancelLocation cancelLocation) {

    String peer = jid.getBase();

    if (errorMsg != null) {
      switch (cancelLocation) {
        case LOCAL:
          NotificationPanel.showError(
              Messages.JoinSessionWizard_inv_canceled_text
                  + Messages.JoinSessionWizard_8
                  + errorMsg,
              Messages.JoinSessionWizard_inv_canceled);
          break;
        case REMOTE:
          NotificationPanel.showError(
              MessageFormat.format(Messages.JoinSessionWizard_inv_canceled_text2, peer, errorMsg),
              Messages.JoinSessionWizard_inv_canceled);
      }
    } else {
      switch (cancelLocation) {
        case LOCAL:
          break;
        case REMOTE:
          NotificationPanel.showInformation(
              MessageFormat.format(Messages.JoinSessionWizard_inv_canceled_text3, peer),
              Messages.JoinSessionWizard_inv_canceled);
      }
    }
  }
}

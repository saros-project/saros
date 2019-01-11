package de.fu_berlin.inf.dpp.ui.wizards;

import de.fu_berlin.inf.dpp.monitoring.ProgressMonitorAdapterFactory;
import de.fu_berlin.inf.dpp.negotiation.CancelListener;
import de.fu_berlin.inf.dpp.negotiation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelLocation;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;
import de.fu_berlin.inf.dpp.negotiation.SessionNegotiation;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.wizards.dialogs.WizardDialogAccessable;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ShowDescriptionPage;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * A wizard that guides the user through an incoming invitation process.
 *
 * <p>TODO Automatically switch to follow mode
 *
 * <p>TODO Create a separate Wizard class with the following concerns implemented more nicely:
 * Long-Running Operation after each step, cancellation by a remote party, auto-advance.
 *
 * @author rdjemili
 */
public class JoinSessionWizard extends Wizard {

  private static final Logger LOG = Logger.getLogger(JoinSessionWizard.class);

  private boolean isNegotiationRunning = false;

  private final IncomingSessionNegotiation isn;

  private ShowDescriptionPage descriptionPage;

  private SessionNegotiation.Status status;

  private boolean isAutoCancel;

  private final CancelListener cancelListener =
      new CancelListener() {

        @Override
        public void canceled(final CancelLocation location, final String message) {
          /*
           * if location is local it will not matter because the wizard will
           * is already disposed at this point
           */
          /*
           * FIXME the message is not the same as returned by
           * Negotiation#getErrorMessage() see TODO in
           * Negotiation#generateErrorMessage
           */
          handleCanceledAsync(location, message);
        }
      };

  public JoinSessionWizard(IncomingSessionNegotiation isn) {
    this.isn = isn;

    setWindowTitle(Messages.JoinSessionWizard_title);
    setHelpAvailable(false);
    setNeedsProgressMonitor(true);

    descriptionPage = new ShowDescriptionPage(isn);
    addPage(descriptionPage);
  }

  @Override
  public void createPageControls(Composite pageContainer) {
    descriptionPage.createControl(pageContainer);

    if (getContainer() instanceof WizardDialogAccessable) {
      ((WizardDialogAccessable) getContainer())
          .setWizardButtonLabel(IDialogConstants.FINISH_ID, Messages.JoinSessionWizard_accept);
    }

    isn.addCancelListener(cancelListener);

    if (isn.isCanceled()) {
      /*
       * FIXME error message is only available after negotiation
       * termination, but in most cases it should be null at this point
       * anyway
       */
      handleCanceledAsync(
          isn.isLocalCancellation() ? CancelLocation.LOCAL : CancelLocation.REMOTE, null);
    }
  }

  @Override
  public boolean performFinish() {

    isNegotiationRunning = true;

    try {
      getContainer()
          .run(
              true,
              false,
              new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                  try {
                    status = isn.accept(ProgressMonitorAdapterFactory.convert(monitor));
                  } catch (Exception e) {
                    throw new InvocationTargetException(e);
                  }
                }
              });
    } catch (Exception e) {
      Throwable cause = e.getCause();

      if (cause == null) cause = e;

      showCancelMessageAsync(isn.getPeer(), cause.getMessage(), CancelLocation.LOCAL);

      // give up, close the wizard as we cannot do anything here !
      return true;
    }

    switch (status) {
      case OK:
        break;
      case CANCEL:
      case ERROR:
        showCancelMessageAsync(isn.getPeer(), isn.getErrorMessage(), CancelLocation.LOCAL);
        break;
      case REMOTE_CANCEL:
      case REMOTE_ERROR:
        showCancelMessageAsync(isn.getPeer(), isn.getErrorMessage(), CancelLocation.REMOTE);
        break;
    }
    return true;
  }

  @Override
  public boolean performCancel() {

    /*
     * the localCancel or remoteCancel method of the isn instance has
     * already been invoked, calling it one more time will do nothing beside
     * spawning a thread
     */
    if (isAutoCancel) return true;

    ThreadUtils.runSafeAsync(
        "dpp-isn-cancel",
        LOG,
        new Runnable() {
          @Override
          public void run() {
            isn.localCancel(null, CancelOption.NOTIFY_PEER);
          }
        });
    return true;
  }

  @Override
  public void dispose() {
    isn.removeCancelListener(cancelListener);
  }

  private void handleCanceledAsync(final CancelLocation location, final String message) {
    SWTUtils.runSafeSWTAsync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            autoCancelWizard(location, message);
          }
        });
  }

  private void showCancelMessageAsync(
      final JID jid, final String errorMsg, final CancelLocation cancelLocation) {
    SWTUtils.runSafeSWTAsync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            showCancelMessage(jid, errorMsg, cancelLocation);
          }
        });
  }

  // SWT
  private void autoCancelWizard(final CancelLocation location, final String message) {

    /*
     * do NOT CLOSE the wizard if it performs async operations
     *
     * see performFinish() -> getContainer().run(boolean, boolean,
     * IRunnableWithProgress)
     */
    if (isNegotiationRunning) return;

    final Shell shell = getShell();

    if (shell == null || shell.isDisposed()) return;

    isAutoCancel = true;

    // will call IWizard#performCancel()
    ((WizardDialog) getContainer()).close();

    showCancelMessageAsync(isn.getPeer(), message, location);
  }

  // SWT
  private void showCancelMessage(JID jid, String errorMsg, CancelLocation cancelLocation) {

    String nickname = getNickname(jid);

    Shell shell = SWTUtils.getShell();

    if (errorMsg != null) {
      switch (cancelLocation) {
        case LOCAL:
          DialogUtils.openErrorMessageDialog(
              shell,
              Messages.JoinSessionWizard_inv_canceled,
              Messages.JoinSessionWizard_inv_canceled_text
                  + Messages.JoinSessionWizard_8
                  + errorMsg);
          break;
        case REMOTE:
          DialogUtils.openErrorMessageDialog(
              shell,
              Messages.JoinSessionWizard_inv_canceled,
              MessageFormat.format(
                  Messages.JoinSessionWizard_inv_canceled_text2, nickname, errorMsg));
      }
    } else {
      switch (cancelLocation) {
        case LOCAL:
          break;
        case REMOTE:
          DialogUtils.openInformationMessageDialog(
              shell,
              Messages.JoinSessionWizard_inv_canceled,
              MessageFormat.format(Messages.JoinSessionWizard_inv_canceled_text3, nickname));
      }
    }
  }

  private static String getNickname(JID jid) {
    return XMPPUtils.getNickname(null, jid, jid.getBase());
  }
}

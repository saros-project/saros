package saros.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.annotations.Inject;
import saros.SarosConstants;
import saros.SarosPluginContext;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.net.xmpp.subscription.SubscriptionHandler;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.DialogUtils;
import saros.ui.util.SWTUtils;
import saros.ui.wizards.pages.AddContactWizardPage;

/**
 * Wizard for adding a new contact to the {@link Roster roster} of the currently connected user.
 *
 * @author bkahlert
 */
public class AddContactWizard extends Wizard {
  private static final Logger log = Logger.getLogger(AddContactWizard.class);

  public static final String TITLE = Messages.AddContactWizard_title;
  public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_ADD_CONTACT;

  @Inject protected XMPPConnectionService connectionService;

  @Inject protected SubscriptionHandler subscriptionManager;

  protected final AddContactWizardPage addContactWizardPage = new AddContactWizardPage();

  /**
   * Caches the {@link JID} reference in case the {@link WizardPage}s are already disposed but a
   * user still needs access.
   */
  protected JID cachedContact;

  protected static class DialogContent {

    public DialogContent(
        String dialogTitle, String dialogMessage, String invocationTargetExceptionMessage) {
      super();
      this.dialogTitle = dialogTitle;
      this.dialogMessage = dialogMessage;
      this.invocationTargetExceptionMessage = invocationTargetExceptionMessage;
    }

    /** Title displayed in the question dialog */
    String dialogTitle;

    /** Message displayed in the question dialog */
    String dialogMessage;

    /** Detailed message for the InvocationTargetMessage */
    String invocationTargetExceptionMessage;
  }

  public AddContactWizard() {
    SarosPluginContext.initComponent(this);
    setWindowTitle(TITLE);
    setDefaultPageImageDescriptor(IMAGE);
    setNeedsProgressMonitor(true);
  }

  @Override
  public void addPages() {
    addPage(addContactWizardPage);
  }

  @Override
  public boolean performFinish() {
    final JID jid = addContactWizardPage.getContact();
    final String nickname = addContactWizardPage.getNickname();

    if (addContactWizardPage.isContactAlreadyAdded()) {
      log.debug("contact " + jid.toString() + " already added");
      return true;
    }

    try {
      getContainer()
          .run(
              true,
              false,
              new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {

                  monitor.beginTask("Adding contact " + jid + "...", IProgressMonitor.UNKNOWN);

                  try {
                    addToRoster(connectionService.getConnection(), jid, nickname);

                    cachedContact = jid;
                  } catch (CancellationException e) {
                    throw new InterruptedException();
                  } catch (XMPPException e) {
                    throw new InvocationTargetException(e);
                  } finally {
                    monitor.done();
                  }
                }
              });
    } catch (InvocationTargetException e) {
      log.warn(e.getCause().getMessage(), e.getCause());
      addContactWizardPage.setErrorMessage(e.getMessage());
      // Leave the wizard open
      return false;
    } catch (InterruptedException e) {
      log.error("uninterruptible context was interrupted", e);
    }

    // Close the wizard
    return true;
  }

  /**
   * Returns {@JID} of the newly added contact
   *
   * @return
   */
  public JID getContact() {
    return cachedContact;
  }

  /**
   * Adds given contact to the {@link Roster}.
   *
   * @param connection
   * @param jid
   * @param nickname
   */
  private void addToRoster(Connection connection, final JID jid, String nickname)
      throws XMPPException {

    if (connection == null) throw new NullPointerException("connection is null");

    if (jid == null) throw new NullPointerException("jid is null");

    try {
      boolean jidOnServer = XMPPUtils.isJIDonServer(connection, jid, SarosConstants.RESOURCE);
      if (!jidOnServer) {
        boolean cancel = false;
        try {
          cancel =
              SWTUtils.runSWTSync(
                  new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                      return !DialogUtils.openQuestionMessageDialog(
                          null,
                          "Contact Unknown",
                          "You entered a valid XMPP server.\n\n"
                              + "Unfortunately your entered JID is unknown to the server.\n"
                              + "Please make sure you spelled the JID correctly.\n\n"
                              + "Do you want to add the contact anyway?");
                    }
                  });
        } catch (Exception e) {
          log.debug("Error opening questionMessageDialog", e);
        }

        if (cancel) {
          throw new XMPPException("Please make sure you spelled the JID correctly.");
        }
        log.debug(
            "The contact "
                + jid
                + " couldn't be found on the server."
                + " The user chose to add it anyway.");
      }
    } catch (XMPPException e) {
      final DialogContent dialogContent = getDialogContent(e);

      boolean cancel = false;

      try {
        cancel =
            SWTUtils.runSWTSync(
                new Callable<Boolean>() {
                  @Override
                  public Boolean call() throws Exception {
                    return !DialogUtils.openQuestionMessageDialog(
                        null, dialogContent.dialogTitle, dialogContent.dialogMessage);
                  }
                });
      } catch (Exception e1) {
        log.debug("Error opening questionMessageDialog", e);
      }

      if (cancel) throw new XMPPException(dialogContent.invocationTargetExceptionMessage);

      log.warn(
          "Problem while adding a contact. User decided to add contact anyway. Problem: "
              + e.getMessage());
    }

    connection.getRoster().createEntry(jid.getBase(), nickname, null);
  }

  private DialogContent getDialogContent(XMPPException e) {

    // FIXME: use e.getXMPPError().getCode(); !

    if (e.getMessage().contains("item-not-found")) {
      return new DialogContent(
          "Contact Unknown",
          "The contact is unknown to the XMPP server.\n\n"
              + "Do you want to add the contact anyway?",
          "Contact unknown to XMPP server.");
    }

    if (e.getMessage().contains("remote-server-not-found")) {
      return new DialogContent(
          "Server Not Found",
          "The responsible XMPP server could not be found.\n\n"
              + "Do you want to add the contact anyway?",
          "Unable to find the responsible XMPP server.");
    }

    if (e.getMessage().contains("501")) {
      return new DialogContent(
          "Unsupported Contact Status Check",
          "The responsible XMPP server does not support status requests.\n\n"
              + "If the contact exists you can still successfully add him.\n\n"
              + "Do you want to try to add the contact?",
          "Contact status check unsupported by XMPP server.");
    }

    if (e.getMessage().contains("503")) {
      return new DialogContent(
          "Unknown Contact Status",
          "For privacy reasons the XMPP server does not reply to status requests.\n\n"
              + "If the contact exists you can still successfully add him.\n\n"
              + "Do you want to try to add the contact?",
          "Unable to check the contact status.");
    }

    if (e.getMessage().contains("No response from the server")) {
      return new DialogContent(
          "Server Not Responding",
          "The responsible XMPP server is not connectable.\n"
              + "The server is either inexistent or offline right now.\n\n"
              + "Do you want to add the contact anyway?",
          "The XMPP server did not respond.");
    }

    return new DialogContent(
        "Unknown Error",
        "An unknown error has occured:\n\n"
            + e.getMessage()
            + "\n\n"
            + "Do you want to add the contact anyway?",
        "Unknown error: " + e.getMessage());
  }
}

package de.fu_berlin.inf.dpp.ui.wizards;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.wizards.pages.CreateXMPPAccountWizardPage;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.lang.reflect.InvocationTargetException;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.packet.XMPPError;
import org.picocontainer.annotations.Inject;

/**
 * @JTourBusStop 4, The Interface Tour:
 *
 * <p>Another important element to the Saros interface is the Wizard. Eclipse supplies an abstract
 * Wizard class that can be extended with your concrete functionality.
 *
 * <p>In this example, the CreateXMPPAccountWizard allows the user to enter the details of a new
 * account, validate them and store them in our account store.
 */

/**
 * A wizard that is used to create XMPP accounts.
 *
 * @author rdjemili
 * @author coezbek
 * @author bkahlert
 */
public class CreateXMPPAccountWizard extends Wizard {
  private static final Logger log = Logger.getLogger(CreateXMPPAccountWizard.class);

  @Inject private XMPPAccountStore accountStore;

  @Inject private ConnectionHandler connectionHandler;

  private final CreateXMPPAccountWizardPage createXMPPAccountPage;

  /*
   * Fields are cached in order to make the values accessible in case the
   * controls are already disposed. This is the case when the Wizard finished
   * or WizardDialog closed the Wizard.
   */
  protected String cachedServer;
  protected String cachedUsername;
  protected String cachedPassword;

  protected XMPPAccount createdXMPPAccount;

  public CreateXMPPAccountWizard(boolean showUseNowButton) {

    SarosPluginContext.initComponent(this);

    setWindowTitle(Messages.CreateXMPPAccountWizard_title);
    setDefaultPageImageDescriptor(ImageManager.WIZBAN_CREATE_XMPP_ACCOUNT);

    this.createXMPPAccountPage = new CreateXMPPAccountWizardPage(showUseNowButton);
    setNeedsProgressMonitor(true);
    setHelpAvailable(true);
  }

  @Override
  public void addPages() {
    addPage(createXMPPAccountPage);
  }

  /**
   * @JTourBusStop 5, The Interface Tour:
   *
   * <p>The performFinish() method is run when the user clicks the finish button on the wizard.
   */
  @Override
  public boolean performFinish() {
    cachedServer = getServer();
    cachedUsername = getUsername();
    cachedPassword = getPassword();

    try {
      // fork a new thread to prevent the GUI from hanging
      getContainer()
          .run(
              true,
              false,
              new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException {

                  monitor.beginTask("Registering account...", IProgressMonitor.UNKNOWN);

                  try {
                    Registration registration =
                        XMPPUtils.createAccount(cachedServer, cachedUsername, cachedPassword);

                    if (registration != null)
                      createAndThrowXMPPException(registration, cachedUsername);

                    log.debug(
                        "Account creation succeeded: username="
                            + cachedUsername
                            + ", server="
                            + cachedServer);
                  } catch (XMPPException e) {
                    throw new InvocationTargetException(e);
                  } finally {
                    monitor.done();
                  }
                }
              });
    } catch (InvocationTargetException e) {
      log.error(e.getCause().getMessage(), e.getCause());

      String message = null;
      Throwable t = e.getCause();

      if (t instanceof XMPPException) message = getErrorMessage((XMPPException) t, cachedServer);

      if (message == null && t != null) message = t.getMessage();

      createXMPPAccountPage.setErrorMessage(message);

      // Leave the wizard open
      return false;
    } catch (InterruptedException e) {
      log.error("uninterruptible context was interrupted", e);
      createXMPPAccountPage.setErrorMessage(e.getMessage());
      return false;
    }

    // add account to the accountStore
    this.createdXMPPAccount =
        accountStore.createAccount(
            cachedUsername, cachedPassword, cachedServer.toLowerCase(), "", 0, true, true);

    // reconnect if user wishes
    if (createXMPPAccountPage.useNow()) {
      boolean reconnect = true;
      if (connectionHandler.isConnected()) {
        reconnect =
            DialogUtils.openQuestionMessageDialog(
                getShell(),
                Messages.CreateXMPPAccountWizard_already_connected,
                Messages.CreateXMPPAccountWizard_already_connected_text);
      }

      if (reconnect) {
        accountStore.setAccountActive(createdXMPPAccount);
        ThreadUtils.runSafeAsync(
            "dpp-connect-demand",
            log,
            new Runnable() {
              @Override
              public void run() {
                connectionHandler.connect(createdXMPPAccount, false);
              }
            });
      }
    }

    return true;
  }

  /*
   * Wizard Results
   */

  /**
   * Returns the server (used) for account creation.
   *
   * @return
   */
  protected String getServer() {
    try {
      return createXMPPAccountPage.getServer();
    } catch (Exception e) {
      return cachedServer;
    }
  }

  /**
   * Returns the username (used) for account creation.
   *
   * @return
   */
  protected String getUsername() {
    try {
      return createXMPPAccountPage.getUsername();
    } catch (Exception e) {
      return cachedUsername;
    }
  }

  /**
   * Returns the password (used) for account creation.
   *
   * @return
   */
  protected String getPassword() {
    try {
      return createXMPPAccountPage.getPassword();
    } catch (Exception e) {
      return cachedPassword;
    }
  }

  /**
   * Returns the created {@link XMPPAccount}.
   *
   * @return null if the {@link XMPPAccount} has not (yet) been created.
   */
  public XMPPAccount getCreatedXMPPAccount() {
    return createdXMPPAccount;
  }

  private String getErrorMessage(XMPPException e, String server) {
    String message = null;
    XMPPError error = e.getXMPPError();

    if (error == null) return null;

    if (error.getCode() == 409) message = "The XMPP account already exists.";
    else
      message =
          "An unknown error occurred. Please register on "
              + ("saros-con.imp.fu-berlin.de".equals(server)
                  ? ("our website:" + " https://saros-con.imp.fu-berlin.de:5280/register/")
                  : "the provider's website.");

    return message;
  }

  private void createAndThrowXMPPException(Registration registration, String username)
      throws XMPPException {

    final String errorMessage;

    if (registration.getError() != null) {
      errorMessage = "No in-band registration. Please create account on provider's website.";
    } else if (registration.getAttributes().containsKey("registered")) {
      errorMessage = "Account " + username + " already exists on the server.";
    } else if (!registration.getAttributes().containsKey("username")) {
      if (registration.getInstructions() != null) {
        errorMessage =
            "Registration via Saros not possible.\n\n"
                + "Please follow these instructions:\n"
                + registration.getInstructions();
      } else {
        errorMessage =
            "Registration via Saros not possible.\n\n"
                + "Please see the server's web site for\n"
                + "information on how to create an account.";
      }
    } else {
      errorMessage = "No in-band registration. Please create account on provider's website.";
    }

    throw new XMPPException(errorMessage);
  }
}

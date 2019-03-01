package de.fu_berlin.inf.dpp.ui.wizards.pages;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.EnterXMPPAccountComposite;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.picocontainer.annotations.Inject;

/**
 * Allows the user to edit a given {@link XMPPAccount}.
 *
 * @author Björn Kahlert
 * @author Stefan Rossbach
 */
public class EditXMPPAccountWizardPage extends WizardPage {
  public static final String TITLE = Messages.EditXMPPAccountWizardPage_title;
  public static final String DESCRIPTION = Messages.EditXMPPAccountWizardPage_description;

  @Inject private XMPPAccountStore accountStore;

  private EnterXMPPAccountComposite enterXMPPAccountComposite;

  /** This flag is true if {@link JID} was already valid. */
  private boolean wasJIDValid = true;

  /** This flag is true if the password was already valid. */
  private boolean wasPasswordValid = true;

  /** This flag is true if the server was already valid. */
  private boolean wasServerValid = false;

  /** This flag is true if the port was already valid. */
  private boolean wasPortValid = false;

  private final JID initialJID;
  private final String initialPassword;
  private final String initialServer;
  private final String initialPort;

  private boolean useTLS;
  private boolean useSASL;

  public EditXMPPAccountWizardPage(XMPPAccount account) {
    super(EditXMPPAccountWizardPage.class.getName());

    SarosPluginContext.initComponent(this);
    setTitle(TITLE);
    setDescription(DESCRIPTION);

    initialJID = new JID(account.getUsername(), account.getDomain());

    initialPassword = account.getPassword();
    initialServer = account.getServer();

    if (account.getPort() == 0) initialPort = "";
    else initialPort = String.valueOf(account.getPort());

    useTLS = account.useTLS();
    useSASL = account.useSASL();

    wasServerValid = initialServer.length() != 0;
    wasPortValid = initialPort.length() != 0;
  }

  @Override
  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    setControl(composite);

    composite.setLayout(new GridLayout(1, false));

    enterXMPPAccountComposite = new EnterXMPPAccountComposite(composite, SWT.NONE);
    enterXMPPAccountComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    setInitialValues();
    hookListeners();
    updatePageCompletion();
  }

  private void setInitialValues() {
    enterXMPPAccountComposite.setJID(initialJID);
    enterXMPPAccountComposite.setPassword(initialPassword);
    enterXMPPAccountComposite.setServer(initialServer);
    enterXMPPAccountComposite.setPort(initialPort);
    enterXMPPAccountComposite.setUsingTLS(useTLS);
    enterXMPPAccountComposite.setUsingSASL(useSASL);
  }

  private void hookListeners() {
    this.enterXMPPAccountComposite.addModifyListener(
        new ModifyListener() {
          @Override
          public void modifyText(ModifyEvent e) {
            updatePageCompletion();
          }
        });
  }

  private void updatePageCompletion() {

    boolean isJIDValid = getJID().isValid() && !getJID().isResourceQualifiedJID();

    boolean isPasswordValid = !getPassword().isEmpty();
    boolean isServerValid =
        (getServer().isEmpty() && getPort().isEmpty()) || !getServer().isEmpty();

    Boolean accountExists = null;

    int port;

    try {
      port = Integer.parseInt(enterXMPPAccountComposite.getPort());
      if (port <= 0 || port > 65535) port = -1;

    } catch (NumberFormatException e) {
      port = -1;
    }

    boolean isPortValid = port != -1;

    if (enterXMPPAccountComposite.getPort().isEmpty()
        && enterXMPPAccountComposite.getServer().isEmpty()) {
      port = 0;
      isPortValid = true;
    }

    if (!enterXMPPAccountComposite.getPort().isEmpty()) wasPortValid = true;

    if (isJIDValid) wasJIDValid = true;

    if (isPasswordValid) wasPasswordValid = true;

    if (isServerValid) wasServerValid = true;

    setPageComplete(false);
    String errorMessage = null;

    /*
     * only query if the account exists when all required fields are filled
     * out properly otherwise "accountExists" is null -> state not known yet
     */
    if ((isJIDValid && isPasswordValid && isServerValid && isPortValid))
      accountExists =
          accountStore.exists(getJID().getName(), getJID().getDomain(), getServer(), port);

    // allow password modification
    if (accountExists != null
        && accountExists.booleanValue()
        && initialJID.equals(getJID())
        && initialServer.equals(getServer())
        && initialPort.equals(getPort())) accountExists = false;

    if (!isJIDValid && wasJIDValid) {
      errorMessage = Messages.jid_format_errorMessage;
    } else if (!isPasswordValid && wasPasswordValid) {
      errorMessage = Messages.password_empty_errorMessage;
    } else if (!isServerValid && wasServerValid) {
      errorMessage = "The server field must not be empty";
    } else if (!isPortValid && wasPortValid && getPort().isEmpty()) {
      errorMessage = "Port field must not be empty";
    } else if (!isPortValid && wasPortValid) {
      errorMessage = "Invalid port number";
    } else if (accountExists != null && accountExists.booleanValue()) {
      errorMessage = Messages.account_exists_errorMessage;
    }

    updateErrorMessage(errorMessage);

    boolean isAdvancedSectionValid =
        ((getServer().isEmpty() && getPort().isEmpty())
            || (!getServer().isEmpty() && !getPort().isEmpty() && port != 0));

    if (isAdvancedSectionValid && accountExists != null && !accountExists.booleanValue())
      setPageComplete(true);
  }

  private String lastErrorMessage = null;

  private void updateErrorMessage(String message) {
    if (lastErrorMessage != null && lastErrorMessage.equals(message)) return;

    lastErrorMessage = message;

    setErrorMessage(message);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (!visible) return;

    this.enterXMPPAccountComposite.setFocus();
  }

  /*
   * WizardPage Results
   */

  /**
   * Returns the entered {@link XMPPAccount}.
   *
   * @return
   */
  public JID getJID() {
    return this.enterXMPPAccountComposite.getJID();
  }

  /**
   * Returns the entered password.
   *
   * @return
   */
  public String getPassword() {
    return this.enterXMPPAccountComposite.getPassword();
  }

  /**
   * Returns the entered server.
   *
   * @return empty string if no server has been provided
   */
  public String getServer() {
    return this.enterXMPPAccountComposite.getServer();
  }

  /**
   * Returns the entered port
   *
   * @return the entered port
   */
  public String getPort() {
    return enterXMPPAccountComposite.getPort();
  }

  public boolean isUsingTLS() {
    return enterXMPPAccountComposite.isUsingTLS();
  }

  public boolean isUsingSASL() {
    return enterXMPPAccountComposite.isUsingSASL();
  }
}

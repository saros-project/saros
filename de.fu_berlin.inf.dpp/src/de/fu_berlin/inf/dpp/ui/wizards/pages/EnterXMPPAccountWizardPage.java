package de.fu_berlin.inf.dpp.ui.wizards.pages;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleNoteComposite;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.EnterXMPPAccountComposite;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.picocontainer.annotations.Inject;

/**
 * Allows the user to enter an XMPP account defined by a {@link JID}, a password and an optional
 * server.
 *
 * @author bkahlert
 */
public class EnterXMPPAccountWizardPage extends WizardPage {

  public static final String TITLE = Messages.EnterXMPPAccountWizardPage_title;
  public static final String DESCRIPTION = Messages.EnterXMPPAccountWizardPage_description;

  @Inject private XMPPAccountStore accountStore;

  private EnterXMPPAccountComposite enterXMPPAccountComposite;

  /** True if the entered account already exists in the {@linkplain XMPPAccountStore}. */
  private boolean isExistingAccount = false;

  /** This flag is true if {@link JID} was already valid. */
  private boolean wasJIDValid = false;

  /** This flag is true if the password was already valid. */
  private boolean wasPasswordValid = false;

  /** This flag is true if the server was already valid. */
  private boolean wasServerValid = false;

  /** This flag is true if the port was already valid. */
  private boolean wasPortValid = false;

  /** This flag is true if Saros's XMPP server restriction should be displayed. */
  private boolean showSarosXMPPRestriction = false;

  public EnterXMPPAccountWizardPage() {
    super(EnterXMPPAccountWizardPage.class.getName());

    SarosPluginContext.initComponent(this);
    setTitle(TITLE);
    setDescription(DESCRIPTION);
  }

  @Override
  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    setControl(composite);

    composite.setLayout(new GridLayout(1, false));

    SimpleNoteComposite noteComposite =
        new SimpleNoteComposite(
            composite,
            SWT.BORDER,
            SWT.ICON_INFORMATION,
            Messages.EnterXMPPAccountWizardPage_info_already_created_account);

    noteComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    noteComposite.setSpacing(8);

    enterXMPPAccountComposite = new EnterXMPPAccountComposite(composite, SWT.NONE);

    enterXMPPAccountComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    enterXMPPAccountComposite.setUsingTLS(true);
    enterXMPPAccountComposite.setUsingSASL(true);

    hookListeners();
    updatePageCompletion();
  }

  private void hookListeners() {
    enterXMPPAccountComposite.addModifyListener(
        new ModifyListener() {
          @Override
          public void modifyText(ModifyEvent e) {
            updatePageCompletion();
          }
        });

    enterXMPPAccountComposite.addFocusListener(
        new FocusListener() {
          @Override
          public void focusGained(FocusEvent e) {
            // NOP
          }

          // JID combo has always the focus if the page is shown
          @Override
          public void focusLost(FocusEvent e) {
            wasJIDValid = true;
            /*
             * do not call updatePageCompletion here or the wizard will ****
             * up, redirecting clicks on the Finish button to the wrong
             * widget
             */
          }
        });
  }

  /**
   * Fills out the account credentials required by this page using the given account.
   *
   * <p><b>Note:</b> It is the callers responsibility to ensure that the account already exists if
   * this method is called with <code>isExistingAccount=true</code> .
   *
   * @param account the account whose credentials to use
   * @param isExistingAccount if <code>true</code> it will no longer be possible to fill out the
   *     credentials manually, in other words the user input fields for inserting account
   *     credentials will be disabled
   */
  public void setAccount(XMPPAccount account, boolean isExistingAccount) {
    if (account == null) return;

    enterXMPPAccountComposite.setEnabled(!isExistingAccount);
    enterXMPPAccountComposite.setJID(new JID(account.getUsername(), account.getDomain()));

    enterXMPPAccountComposite.setPassword(account.getPassword());

    /*
     * TODO this is currently only called when an account was registered so
     * the server and port values are always not set, same applies to TLS
     * and SASL
     */

    enterXMPPAccountComposite.setServer("");
    enterXMPPAccountComposite.setPort("");

    enterXMPPAccountComposite.setUsingTLS(true);
    enterXMPPAccountComposite.setUsingSASL(true);

    this.isExistingAccount = isExistingAccount;
    updatePageCompletion();
  }

  private void updatePageCompletion() {

    if (showSarosXMPPRestriction) setMessage(Messages.xmpp_saros_restriction_short, WARNING);
    else setMessage(null);

    if (isExistingAccount) {
      setErrorMessage(null);
      setPageComplete(true);
      return;
    }

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

    enterXMPPAccountComposite.setFocus();
  }

  /*
   * WizardPage Results
   */

  /**
   * Returns the entered {@link JID}.
   *
   * @return
   */
  public JID getJID() {
    return enterXMPPAccountComposite.getJID();
  }

  /**
   * Returns the entered password.
   *
   * @return
   */
  public String getPassword() {
    return enterXMPPAccountComposite.getPassword();
  }

  /**
   * Returns the entered server.
   *
   * @return empty string if no server has been provided
   */
  public String getServer() {
    return enterXMPPAccountComposite.getServer();
  }

  /** True if the entered account already exists in the {@link XMPPAccountStore}. */
  public boolean isExistingAccount() {
    return isExistingAccount;
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

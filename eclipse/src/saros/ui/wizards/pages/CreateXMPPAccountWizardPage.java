package saros.ui.wizards.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.account.XMPPAccountStore;
import saros.preferences.Preferences;
import saros.ui.Messages;

/**
 * Allows the user to create an XMPP account.
 *
 * @author bkahlert
 * @author Stefan Rossbach
 */
public class CreateXMPPAccountWizardPage extends WizardPage {

  public static final String TITLE = Messages.CreateXMPPAccountWizardPage_title;
  public static final String DESCRIPTION = Messages.CreateXMPPAccountWizardPage_description;

  private Combo serverText;
  private Text usernameText;
  private Text passwordText;
  private Text repeatPasswordText;
  private Button useNowButton;

  private boolean wasServerValid;
  private boolean wasUsernameValid;
  private boolean wasPasswordValid;
  private boolean wasRepeatedPasswordValid;

  private boolean showUseNowButton;

  private String lastMessage;

  @Inject private Preferences preferences;

  @Inject private XMPPAccountStore accountStore;

  /** @param showUseNowButton show button for setting "useNow" */
  public CreateXMPPAccountWizardPage(boolean showUseNowButton) {
    super(CreateXMPPAccountWizardPage.class.getName());
    SarosPluginContext.initComponent(this);
    setTitle(TITLE);
    setDescription(DESCRIPTION);

    this.showUseNowButton = showUseNowButton;
  }

  @Override
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    setControl(container);

    container.setLayout(new GridLayout(1, true));

    Composite leftColumn = createLeftColumn(container);
    leftColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    setInitialValues();

    hookListeners();

    setPageComplete(false);
  }

  @Override
  public void performHelp() {
    Shell shell = new Shell(getShell());
    shell.setText("Saros XMPP Accounts");
    shell.setLayout(new GridLayout());
    shell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    Browser browser = new Browser(shell, SWT.NONE);
    browser.setUrl("http://www.saros-project.org/setupXMPP");
    browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    shell.open();
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (!visible) return;

    usernameText.setFocus();
  }

  /*
   * WizardPage Results
   */

  public String getServer() {
    return serverText.getText().trim();
  }

  public String getUsername() {
    return usernameText.getText().trim();
  }

  public String getPassword() {
    return passwordText.getText();
  }

  public String getRepeatedPassword() {
    return repeatPasswordText.getText();
  }

  public boolean useNow() {
    if (showUseNowButton) {
      return useNowButton.getSelection();
    }
    return false;
  }

  private Composite createLeftColumn(Composite composite) {
    Composite leftColumn = new Composite(composite, SWT.NONE);
    leftColumn.setLayout(new GridLayout(2, false));

    /*
     * Row 1
     */
    Label serverLabel = new Label(leftColumn, SWT.NONE);
    serverLabel.setText(Messages.CreateXMPPAccountWizardPage_label_server);

    serverText = new Combo(leftColumn, SWT.BORDER);
    serverText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    /*
     * Row 2
     */
    Label usernameLabel = new Label(leftColumn, SWT.NONE);
    usernameLabel.setText(Messages.CreateXMPPAccountWizardPage_label_username);

    usernameText = new Text(leftColumn, SWT.BORDER);
    usernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    /*
     * Row 3
     */
    Label passwordLabel = new Label(leftColumn, SWT.NONE);
    passwordLabel.setText(Messages.CreateXMPPAccountWizardPage_label_password);

    passwordText = new Text(leftColumn, SWT.BORDER);
    passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    passwordText.setEchoChar('*');

    /*
     * Row 4
     */
    Label repeatPasswordLabel = new Label(leftColumn, SWT.NONE);
    repeatPasswordLabel.setText(Messages.CreateXMPPAccountWizardPage_label_repeat_password);

    repeatPasswordText = new Text(leftColumn, SWT.BORDER);
    repeatPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    repeatPasswordText.setEchoChar('*');

    /*
     * Row 5
     */
    Composite spacer = new Composite(leftColumn, SWT.NONE);
    spacer.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false, 1, 2));

    useNowButton = new Button(leftColumn, SWT.CHECK | SWT.SEPARATOR);
    useNowButton.setSelection(false);
    useNowButton.setText(Messages.CreateXMPPAccountWizardPage_button_use_new_account);
    useNowButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    useNowButton.setVisible(this.showUseNowButton);

    return leftColumn;
  }

  private void setInitialValues() {
    usernameText.setText("");
    passwordText.setText("");
    repeatPasswordText.setText("");
    String defaultServer = preferences.getDefaultServer();

    Set<String> servers = new HashSet<String>(accountStore.getDomains());
    servers.add(defaultServer);

    List<String> serverList = new ArrayList<String>(servers);
    Collections.sort(serverList);

    for (String server : serverList) serverText.add(server);

    serverText.select(serverList.indexOf(defaultServer));
  }

  private void hookListeners() {
    ModifyListener listener =
        new ModifyListener() {
          @Override
          public void modifyText(ModifyEvent e) {
            updatePageCompletion();
          }
        };

    serverText.addModifyListener(listener);
    usernameText.addModifyListener(listener);
    passwordText.addModifyListener(listener);
    repeatPasswordText.addModifyListener(listener);
    useNowButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            updatePageCompletion();
          }
        });
  }

  private void updatePageCompletion() {

    boolean accountExists = accountStore.exists(getUsername(), getServer(), "", 0);

    boolean isUsernameEmpty = getUsername().length() == 0;

    boolean isPasswordEmpty = getPassword().length() == 0;

    boolean isRepeatedPasswordEmpty = getRepeatedPassword().length() == 0;

    boolean isServerEmpty = getServer().length() == 0;

    boolean passwordsMatch = passwordText.getText().equals(repeatPasswordText.getText());

    boolean complete =
        !isUsernameEmpty && !accountExists && !isPasswordEmpty && !isServerEmpty && passwordsMatch;

    wasUsernameValid |= !isUsernameEmpty;
    wasPasswordValid |= !isPasswordEmpty;
    wasRepeatedPasswordValid |= !isRepeatedPasswordEmpty;
    wasServerValid |= !isServerEmpty;

    boolean isSarosServer = getServer().equals(preferences.getSarosXMPPServer());

    // only display those messages after the user has at least typed in one
    // character

    if (accountExists) updateMessage(Messages.account_exists_errorMessage, ERROR);
    else if (isUsernameEmpty && wasUsernameValid)
      updateMessage(Messages.CreateXMPPAccountWizardPage_error_enter_username, ERROR);
    else if (isPasswordEmpty && wasPasswordValid)
      updateMessage(Messages.password_empty_errorMessage, ERROR);
    else if (!passwordsMatch && wasRepeatedPasswordValid)
      updateMessage(Messages.CreateXMPPAccountWizardPage_error_password_no_match, ERROR);
    else if (isServerEmpty && wasServerValid)
      updateMessage(Messages.CreateXMPPAccountWizardPage_error_enter_server, ERROR);
    else if (isSarosServer && !complete)
      updateMessage(Messages.xmpp_saros_restriction_short, WARNING);
    else if (complete && useNowButton.getSelection()) {
      updateMessage(Messages.CreateXMPPAccountWizardPage_message_you_will_connect, INFORMATION);
    } else updateMessage(DESCRIPTION, NONE);

    setPageComplete(complete);
  }

  private void updateMessage(String message, int type) {
    if (lastMessage == null) lastMessage = "";

    if (message.equals(lastMessage)) return;

    lastMessage = message;
    setMessage(message, type);
  }
}

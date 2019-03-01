package saros.ui.preferencePages;

import java.text.MessageFormat;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.picocontainer.annotations.Inject;
import saros.Saros;
import saros.SarosPluginContext;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.annotations.Component;
import saros.preferences.PreferenceConstants;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.WizardUtils;

/**
 * Contains the basic preferences for Saros.
 *
 * @author Sebastian Schlaak
 */
@Component(module = "prefs")
public final class GeneralPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

  public static final int COLUMNS_IN_ACCOUNTGROUP = 2;

  // labels
  public static final String ACCOUNT_GROUP_TITLE =
      Messages.GeneralPreferencePage_ACCOUNT_GROUP_TITLE;
  public static final String ACTIVATE_BTN_TEXT = Messages.GeneralPreferencePage_ACTIVATE_BTN_TEXT;
  public static final String CHANGE_BTN_TEXT = Messages.GeneralPreferencePage_CHANGE_BTN_TEXT;
  public static final String ADD_BTN_TEXT = Messages.GeneralPreferencePage_ADD_BTN_TEXT;
  public static final String REMOVE_BTN_TEXT = Messages.GeneralPreferencePage_REMOVE_BTN_TEXT;
  public static final String ENCRYPT_PASSWORD_TEXT =
      Messages.GeneralPreferencePage_ENCRYPT_PASSWORD_TEXT;
  public static final String STARTUP_CONNECT_TEXT =
      Messages.GeneralPreferencePage_STARTUP_CONNECT_TEXT;
  public static final String FOLLOW_MODE_TEXT = Messages.GeneralPreferencePage_FOLLOW_MODE_TEXT;
  public static final String CONCURRENT_UNDO_TEXT =
      Messages.GeneralPreferencePage_CONCURRENT_UNDO_TEXT;
  public static final String DISABLE_VERSION_CONTROL_TEXT =
      Messages.GeneralPreferencePage_DISABLE_VERSION_CONTROL_TEXT;

  // icons
  public static final Image ADD_IMAGE =
      ImageManager.getImage("icons/btn/addaccount.png"); // $NON-NLS-1$
  public static final Image ACTIVATE_IMAGE =
      ImageManager.getImage("icons/btn/activateaccount.png"); // $NON-NLS-1$
  public static final Image DELETE_IMAGE =
      ImageManager.getImage("icons/btn/deleteaccount.png"); // $NON-NLS-1$
  public static final Image CHANGE_IMAGE =
      ImageManager.getImage("icons/btn/changeaccount.png"); // $NON-NLS-1$

  @Inject private Saros saros;

  @Inject private XMPPAccountStore accountStore;

  private Composite parent;
  private Label infoLabel;
  private Group accountGroup;
  private List accountList;

  private Button activateAccountButton;
  private Button removeAccountButton;
  private Button editAccountButton;

  public GeneralPreferencePage() {
    super(FieldEditorPreferencePage.GRID);
    SarosPluginContext.initComponent(this);
    setPreferenceStore(saros.getPreferenceStore());
  }

  @Override
  protected void createFieldEditors() {
    parent = new Composite(getFieldEditorParent(), SWT.NONE);
    layoutParent();
    createAccountsGroup();
    createAutomaticConnectField(parent);
    createConcurrentUndoField(parent);
  }

  /*
   * Creates a grid-layout with one column.
   */
  private void layoutParent() {
    parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    parent.setLayout(new GridLayout(1, false));
  }

  /*
   * Creates the account-list, buttons and the 'activeUserLabel'.
   */
  private void createAccountsGroup() {
    accountGroup = new Group(parent, SWT.NONE);
    accountGroup.setLayout(new GridLayout(COLUMNS_IN_ACCOUNTGROUP, false));
    accountGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    accountGroup.setText(ACCOUNT_GROUP_TITLE);

    createAccountList(accountGroup);
    createAccountListControls(accountGroup);
    createInfoLabel(accountGroup);
  }

  /*
   * Creates a list-component filled with the accounts.
   */
  private void createAccountList(Composite composite) {

    accountList = new List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    GridData data = new GridData(GridData.FILL_BOTH);
    accountList.setLayoutData(data);

    updateList();

    accountList.addSelectionListener(
        new SelectionListener() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            handleEvent();
          }

          @Override
          public void widgetDefaultSelected(SelectionEvent e) {
            handleEvent();
          }

          private void handleEvent() {

            activateAccountButton.setEnabled(true);
            removeAccountButton.setEnabled(true);
            editAccountButton.setEnabled(true);

            if (getSelectedAccount().equals(accountStore.getActiveAccount())) {
              activateAccountButton.setEnabled(false);
              removeAccountButton.setEnabled(false);
            }
          }
        });
  }

  /*
   * Creates the buttons: activate, add, change and delete.
   */
  private void createAccountListControls(Composite parent) {
    Composite controlComposite = createAccountListComposite(parent);
    createActivateAccountButton(controlComposite);
    createAddAccountButton(controlComposite);
    createRemoveAccountButton(controlComposite);
    createEditAccountButton(controlComposite);
  }

  private Composite createAccountListComposite(Composite parent) {
    Composite buildControlComposite = new Composite(parent, SWT.NONE);
    GridLayout gridLayout = new GridLayout(1, true);
    buildControlComposite.setLayout(gridLayout);
    buildControlComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    return buildControlComposite;
  }

  private void createInfoLabel(Composite composite) {
    infoLabel = new Label(composite, SWT.SINGLE);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    infoLabel.setLayoutData(data);
    updateInfoLabel();
  }

  private Button createAccountGroupButton(
      Composite composite, Image icon, String text, Listener listener) {
    Button button = new Button(composite, SWT.PUSH);
    button.setImage(icon);
    button.setText(text);
    button.addListener(SWT.Selection, listener);
    button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    return button;
  }

  private XMPPAccount getSelectedAccount() {
    return (XMPPAccount) accountList.getData(String.valueOf(accountList.getSelectionIndex()));
  }

  private void updateList() {
    accountList.removeAll();
    int index = 0;
    for (XMPPAccount account : accountStore.getAllAccounts()) {
      accountList.add(createHumanDisplayAbleName(account));
      accountList.setData(String.valueOf(index++), account);
    }
  }

  private void updateInfoLabel() {
    if (!accountStore.isEmpty())
      infoLabel.setText(
          Messages.GeneralPreferencePage_active
              + createHumanDisplayAbleName(accountStore.getActiveAccount()));
    else infoLabel.setText("");
  }

  private String createHumanDisplayAbleName(XMPPAccount account) {
    return account.getUsername() + "@" + account.getDomain();
  }

  private void createAddAccountButton(Composite composite) {
    createAccountGroupButton(
        composite,
        ADD_IMAGE,
        ADD_BTN_TEXT,
        new Listener() {
          @Override
          public void handleEvent(Event event) {
            WizardUtils.openAddXMPPAccountWizard();
            activateAccountButton.setEnabled(false);
            removeAccountButton.setEnabled(false);
            editAccountButton.setEnabled(false);
            updateInfoLabel();
            updateList();
          }
        });
  }

  private void createEditAccountButton(Composite composite) {
    editAccountButton =
        createAccountGroupButton(
            composite,
            CHANGE_IMAGE,
            CHANGE_BTN_TEXT,
            new Listener() {
              @Override
              public void handleEvent(Event event) {
                if (WizardUtils.openEditXMPPAccountWizard(getSelectedAccount()) != null) {
                  activateAccountButton.setEnabled(false);
                  removeAccountButton.setEnabled(false);
                  editAccountButton.setEnabled(false);
                  updateInfoLabel();
                  updateList();
                }
              }
            });
    editAccountButton.setEnabled(false);
  }

  private void createRemoveAccountButton(Composite composite) {
    removeAccountButton =
        createAccountGroupButton(
            composite,
            DELETE_IMAGE,
            REMOVE_BTN_TEXT,
            new Listener() {
              @Override
              public void handleEvent(Event event) {
                if (MessageDialog.openQuestion(
                    GeneralPreferencePage.this.getShell(),
                    Messages.GeneralPreferencePage_REMOVE_ACCOUNT_DIALOG_TITLE,
                    MessageFormat.format(
                        Messages.GeneralPreferencePage_REMOVE_ACCOUNT_DIALOG_MESSAGE,
                        createHumanDisplayAbleName(getSelectedAccount())))) {

                  accountStore.deleteAccount(getSelectedAccount());
                  activateAccountButton.setEnabled(false);
                  removeAccountButton.setEnabled(false);
                  editAccountButton.setEnabled(false);
                  updateList();
                }
              }
            });
    removeAccountButton.setEnabled(false);
  }

  private void createActivateAccountButton(Composite composite) {
    activateAccountButton =
        createAccountGroupButton(
            composite,
            ACTIVATE_IMAGE,
            ACTIVATE_BTN_TEXT,
            new Listener() {
              @Override
              public void handleEvent(Event event) {
                accountStore.setAccountActive(getSelectedAccount());
                updateInfoLabel();
                activateAccountButton.setEnabled(false);
                removeAccountButton.setEnabled(false);
                MessageDialog.openInformation(
                    GeneralPreferencePage.this.getShell(),
                    Messages.GeneralPreferencePage_ACTIVATE_ACCOUNT_DIALOG_TITLE,
                    MessageFormat.format(
                        Messages.GeneralPreferencePage_ACTIVATE_ACCOUNT_DIALOG_MESSAGE,
                        createHumanDisplayAbleName(getSelectedAccount())));
              }
            });
    activateAccountButton.setEnabled(false);
  }

  private void createAutomaticConnectField(Composite group) {
    addField(new BooleanFieldEditor(PreferenceConstants.AUTO_CONNECT, STARTUP_CONNECT_TEXT, group));
  }

  private void createConcurrentUndoField(Composite group) {
    addField(
        new BooleanFieldEditor(PreferenceConstants.CONCURRENT_UNDO, CONCURRENT_UNDO_TEXT, group));
  }

  @Override
  public void init(IWorkbench workbench) {
    // Nothing to initialize
  }

  @Override
  public boolean performOk() {
    boolean done = super.performOk();

    return done;
  }

  @Override
  protected void performApply() {
    super.performApply();
  }
}

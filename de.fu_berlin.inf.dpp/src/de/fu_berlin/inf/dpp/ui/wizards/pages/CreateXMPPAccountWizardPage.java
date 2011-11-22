package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Text;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.PublicXMPPServerComposite;
import de.fu_berlin.inf.nebula.explanation.note.SimpleNoteComposite;
import de.fu_berlin.inf.nebula.utils.LayoutUtils;

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

    private String defaultServer;
    private String defaultUsername;
    private String defaultPassword;

    private boolean showUseNowButton;

    @Inject
    private PreferenceUtils preferenceUtils;

    @Inject
    private XMPPAccountStore accountStore;

    /**
     * @param showUseNowButton
     *            show button for setting "useNow"
     */
    public CreateXMPPAccountWizardPage(boolean showUseNowButton) {
        super(CreateXMPPAccountWizardPage.class.getName());
        SarosPluginContext.initComponent(this);
        setTitle(TITLE);
        setDescription(DESCRIPTION);

        this.showUseNowButton = showUseNowButton;
    }

    public void createControl(Composite parent) {
        Composite compositex = new Composite(parent, SWT.NONE);
        setControl(compositex);

        compositex.setLayout(new GridLayout(1, true));

        Composite leftColumn = createLeftColumn(compositex);
        leftColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        setInitialValues();

        hookListeners();
        updatePageCompletion();
    }

    private Composite createLeftColumn(Composite composite) {
        Composite leftColumn = new Composite(composite, SWT.NONE);
        leftColumn.setLayout(new GridLayout(2, false));

        /*
         * Row 1
         */
        Label serverLabel = new Label(leftColumn, SWT.NONE);
        serverLabel.setText(Messages.CreateXMPPAccountWizardPage_label_server);

        this.serverText = new Combo(leftColumn, SWT.BORDER);
        this.serverText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));

        new Label(leftColumn, SWT.NONE);
        PublicXMPPServerComposite publicServers = new PublicXMPPServerComposite(
            leftColumn, SWT.BORDER);
        publicServers.setLayoutData(LayoutUtils.createFillHGrabGridData());

        new Label(leftColumn, SWT.NONE);
        SimpleNoteComposite sarosRestriction = new SimpleNoteComposite(
            leftColumn, SWT.BORDER, SWT.ICON_WARNING,
            Messages.xmpp_saros_restriction);
        sarosRestriction.setLayoutData(LayoutUtils.createFillHGrabGridData());

        /*
         * Row 2
         */
        Label usernameLabel = new Label(leftColumn, SWT.NONE);
        usernameLabel
            .setText(Messages.CreateXMPPAccountWizardPage_label_username);

        this.usernameText = new Text(leftColumn, SWT.BORDER);
        this.usernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));

        /*
         * Row 3
         */
        Label passwordLabel = new Label(leftColumn, SWT.NONE);
        passwordLabel
            .setText(Messages.CreateXMPPAccountWizardPage_label_password);

        this.passwordText = new Text(leftColumn, SWT.BORDER);
        this.passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));
        this.passwordText.setEchoChar('*');

        /*
         * Row 4
         */
        Label repeatPasswordLabel = new Label(leftColumn, SWT.NONE);
        repeatPasswordLabel
            .setText(Messages.CreateXMPPAccountWizardPage_label_repeat_password);

        this.repeatPasswordText = new Text(leftColumn, SWT.BORDER);
        this.repeatPasswordText.setLayoutData(new GridData(SWT.FILL,
            SWT.CENTER, true, false));
        this.repeatPasswordText.setEchoChar('*');

        /*
         * Row 5
         */
        Composite spacer = new Composite(leftColumn, SWT.NONE);
        spacer.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false,
            1, 2));

        this.useNowButton = new Button(leftColumn, SWT.CHECK | SWT.SEPARATOR);
        this.useNowButton.setSelection(false);
        this.useNowButton
            .setText(Messages.CreateXMPPAccountWizardPage_button_use_new_account);
        this.useNowButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));

        this.useNowButton.setVisible(this.showUseNowButton);

        return leftColumn;
    }

    private void setInitialValues() {
        defaultUsername = ""; //$NON-NLS-1$
        defaultPassword = ""; //$NON-NLS-1$
        defaultServer = preferenceUtils.getServer();

        if (defaultServer.length() == 0)
            defaultServer = preferenceUtils.getDefaultServer();

        List<String> servers = accountStore.getDomains();
        servers.add(defaultServer);

        this.serverText.removeAll();

        for (String server : servers)
            this.serverText.add(server);

        this.serverText.select(servers.indexOf(defaultServer));
    }

    private void hookListeners() {
        ModifyListener listener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updatePageCompletion();
            }
        };

        this.serverText.addModifyListener(listener);
        this.usernameText.addModifyListener(listener);
        this.passwordText.addModifyListener(listener);
        this.repeatPasswordText.addModifyListener(listener);
        this.useNowButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updatePageCompletion();
            }
        });

    }

    private void updatePageCompletion() {

        boolean passwordsMatch = this.passwordText.getText().equals(
            this.repeatPasswordText.getText());

        boolean accountExists = accountStore.exists(getUsername(), getServer());

        boolean isUsernameEmpty = this.getUsername().length() == 0;

        boolean isPasswordEmpty = this.getPassword().length() == 0;

        boolean isServerEmpty = this.getServer().length() == 0;

        boolean complete = !isUsernameEmpty && !accountExists
            && !isPasswordEmpty && !isServerEmpty && passwordsMatch;

        setErrorMessage(null);
        setMessage(DESCRIPTION);

        // only display those messages after the user has at least typed in one
        // character

        if (accountExists && haveDefaultsChanged())
            setErrorMessage(Messages.account_exists_errorMessage);

        else if (isUsernameEmpty && haveDefaultsChanged())
            setErrorMessage(Messages.CreateXMPPAccountWizardPage_error_enter_username);

        else if (isPasswordEmpty && haveDefaultsChanged())
            setErrorMessage(Messages.password_empty_errorMessage);

        else if (!passwordsMatch && haveDefaultsChanged())
            setErrorMessage(Messages.CreateXMPPAccountWizardPage_error_password_no_match);

        else if (isServerEmpty && haveDefaultsChanged())
            setErrorMessage(Messages.CreateXMPPAccountWizardPage_error_enter_server);

        if (complete && this.useNowButton.getSelection()) {
            setMessage(
                Messages.CreateXMPPAccountWizardPage_message_you_will_connect,
                IMessageProvider.INFORMATION);
        }

        setPageComplete(complete);
    }

    private boolean changed = false;

    private boolean haveDefaultsChanged() {
        if (changed)
            return changed;

        changed = !(getUsername().equals(defaultUsername)
            && getPassword().equals(defaultPassword) && getServer().equals(
            defaultServer));

        return changed;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible)
            return;

        this.usernameText.setFocus();
    }

    /*
     * WizardPage Results
     */

    public String getServer() {
        return this.serverText.getText().trim();
    }

    public String getUsername() {
        return this.usernameText.getText().trim();
    }

    public String getPassword() {
        return this.passwordText.getText();
    }

    public boolean useNow() {
        if (this.showUseNowButton) {
            return this.useNowButton.getSelection();
        }
        return false;
    }
}
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

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.note.SimpleNoteComposite;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.PublicXMPPServerComposite;

/**
 * Allows the user to create an XMPP account.
 * 
 * @author bkahlert
 */
public class CreateXMPPAccountWizardPage extends WizardPage {
    public static final String TITLE = "Create XMPP/Jabber Account";
    public static final String DESCRIPTION = "Create a new XMPP/Jabber account for use with Saros.";

    public static final String RESULTING_JID_INITIAL_HINT = "Fill in the XMPP/Jabber server and username field\n"
        + "to get a preview of your XMPP/Jabber ID.";
    public static final String RESULTING_JID_HINT = "Your XMPP/Jabber ID will be:\n%s";

    protected Combo serverText;
    protected Text usernameText;
    protected Text passwordText;
    protected Text repeatPasswordText;

    protected Button useNowButton;

    protected final boolean showUseNowButton;

    @Inject
    protected Saros saros;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected XMPPAccountStore accountStore;

    /**
     * True if the username was already valid.
     */
    protected boolean usernameWasValid = false;

    /**
     * True if the password did match already.
     */
    protected boolean passwordWasValid = false;

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

    protected Composite createLeftColumn(Composite composite) {
        Composite leftColumn = new Composite(composite, SWT.NONE);
        leftColumn.setLayout(new GridLayout(2, false));

        /*
         * Row 1
         */
        Label serverLabel = new Label(leftColumn, SWT.NONE);
        serverLabel.setText("XMPP/Jabber Server");

        this.serverText = new Combo(leftColumn, SWT.BORDER);
        this.serverText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));

        new Label(leftColumn, SWT.NONE);
        PublicXMPPServerComposite publicServers = new PublicXMPPServerComposite(
            leftColumn, SWT.BORDER);
        publicServers.setLayoutData(LayoutUtils.createFillHGrabGridData());

        new Label(leftColumn, SWT.NONE);
        SimpleNoteComposite sarosRestriction = new SimpleNoteComposite(
            leftColumn, SWT.BORDER, Messages.xmpp_saros_restriction);
        sarosRestriction.setLayoutData(LayoutUtils.createFillHGrabGridData());

        /*
         * Row 2
         */
        Label usernameLabel = new Label(leftColumn, SWT.NONE);
        usernameLabel.setText("Username");

        this.usernameText = new Text(leftColumn, SWT.BORDER);
        this.usernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));

        /*
         * Row 3
         */
        Label passwordLabel = new Label(leftColumn, SWT.NONE);
        passwordLabel.setText("Password");

        this.passwordText = new Text(leftColumn, SWT.BORDER);
        this.passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));
        this.passwordText.setEchoChar('*');

        /*
         * Row 4
         */
        Label repeatPasswordLabel = new Label(leftColumn, SWT.NONE);
        repeatPasswordLabel.setText("Repeat Password");

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

        if (this.showUseNowButton) {
            this.useNowButton = new Button(leftColumn, SWT.CHECK
                | SWT.SEPARATOR);
            this.useNowButton.setSelection(false);
            this.useNowButton.setText("Use new account immediately");
            this.useNowButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
                true, false));
        }

        return leftColumn;
    }

    public void setInitialValues() {
        String defaultServer = preferenceUtils.getServer();
        if (defaultServer.isEmpty())
            defaultServer = preferenceUtils.getDefaultServer();

        List<String> servers = accountStore.getDomains();
        if (servers.size() == 0)
            servers.add(defaultServer);
        this.serverText.removeAll();
        int selectIndex = 0;
        for (int i = 0, j = servers.size(); i < j; i++) {
            String server = servers.get(i);
            this.serverText.add(server);
            if (defaultServer.equals(server))
                selectIndex = i;
        }
        this.serverText.select(selectIndex);
    }

    protected void hookListeners() {
        ModifyListener listener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updatePageCompletion();
            }
        };

        this.serverText.addModifyListener(listener);
        this.usernameText.addModifyListener(listener);
        this.passwordText.addModifyListener(listener);
        this.repeatPasswordText.addModifyListener(listener);
        if (this.useNowButton != null) {
            this.useNowButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updatePageCompletion();
                }
            });
        }
    }

    protected void updatePageCompletion() {
        boolean done = !this.getServer().isEmpty()
            && !this.getUsername().isEmpty() && !this.getPassword().isEmpty();

        boolean passwordsMatch = this.passwordText.getText().equals(
            this.repeatPasswordText.getText());
        done &= passwordsMatch;

        boolean accountExists = accountStore.contains(getUsername(),
            getServer());
        done &= !accountExists;

        if (!this.getUsername().isEmpty()) {
            usernameWasValid = true;
        }

        if (!this.getPassword().isEmpty() && passwordsMatch) {
            passwordWasValid = true;
        }

        if (!this.getUsername().isEmpty() && !accountExists
            && !this.getPassword().isEmpty() && passwordsMatch) {
            setErrorMessage(null);

            if (this.useNowButton != null && this.useNowButton.getSelection()) {
                setMessage(
                    "You will automatically connect to your new account after completion.",
                    IMessageProvider.INFORMATION);
            } else {
                setMessage(null);
            }
        } else if (accountExists) {
            setErrorMessage(Messages.account_exists_errorMessage);
        } else if (this.getUsername().isEmpty() && usernameWasValid) {
            setErrorMessage("Username must not be empty.");
        } else if (this.getPassword().isEmpty() && passwordWasValid) {
            setErrorMessage(Messages.password_empty_errorMessage);
        } else if (!passwordsMatch) {
            setErrorMessage("Both passwords must match.");
        }
        setPageComplete(done);
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
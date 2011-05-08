package de.fu_berlin.inf.dpp.ui.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.note.SimpleNoteComposite;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.EnterXMPPAccountComposite;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.EnterXMPPAccountCompositeListener;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.IsSarosXMPPServerChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.XMPPServerChangedEvent;
import de.fu_berlin.inf.dpp.ui.wizards.CreateXMPPAccountWizard;
import de.fu_berlin.inf.dpp.util.FontUtils;

/**
 * Allows the user to enter an XMPP account defined by a {@link JID}, a password
 * and an optional server.
 * 
 * @author bkahlert
 */
public class EnterXMPPAccountWizardPage extends WizardPage {
    public static final String TITLE = "Enter XMPP/Jabber ID";
    public static final String DESCRIPTION = "In order to use Saros you need to configure an XMPP/Jabber account.";

    @Inject
    protected Saros saros;

    @Inject
    protected XMPPAccountStore accountStore;

    protected Button createAccountButton;
    protected EnterXMPPAccountComposite enterXMPPAccountComposite;

    /**
     * True if the entered account was freshly created by the
     * {@link CreateXMPPAccountWizard}.
     */
    protected boolean isXMPPAccountCreated = false;

    /**
     * This flag is true if {@link JID} was already valid.
     */
    protected boolean wasJIDValid = false;

    /**
     * This flag is true if the password was already valid.
     */
    protected boolean wasPasswordValid = false;

    /**
     * This flag is true if the server was already valid.
     */
    protected boolean wasXMPPServerValid = false;
    protected boolean isXMPPServerValid = false;

    /**
     * This flag is true if Saros's Jabber server restriction should be
     * displayed.
     */
    protected boolean showSarosXMPPRestriction = false;

    public EnterXMPPAccountWizardPage() {
        super(EnterXMPPAccountWizardPage.class.getName());

        SarosPluginContext.initComponent(this);
        setTitle(TITLE);
        setDescription(DESCRIPTION);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(3, false));

        GridData leftRightGridData = new GridData(SWT.FILL, SWT.FILL, true,
            true);

        /*
         * LEFT COLUMN
         */
        Composite leftColumn = createLeftColumn(composite);
        leftColumn.setLayoutData(leftRightGridData);

        /*
         * SEPARATOR "OR"
         */
        Label separator = new Label(composite, SWT.NONE);
        separator.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
            true));
        separator.setText("OR");
        FontUtils.changeFontSizeBy(separator, 10);

        /*
         * RIGHT COLUMN
         */
        Composite rightColumn = createRightColumn(composite);
        rightColumn.setLayoutData(leftRightGridData);

        /*
         * 2nd row
         */

        SimpleNoteComposite x = new SimpleNoteComposite(
            composite,
            SWT.BORDER,
            SWT.ICON_INFORMATION,
            "If you already have some XMPP/Jabber account you can use it right away.\n"
                + "For example users of Google, GMX, web.de or 1&&1 can directly use their mail address as their XMPP/Jabber ID with Saros.");
        x.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1));
        x.setSpacing(8);

        hookListeners();
        updatePageCompletion();
    }

    protected Composite createLeftColumn(Composite composite) {
        Group newAccountColumnGroup = new Group(composite, SWT.NONE);
        newAccountColumnGroup.setLayout(new GridLayout(2, false));
        newAccountColumnGroup.setText("New Account");

        Composite newAccountComposite = new Composite(newAccountColumnGroup,
            SWT.NONE);
        newAccountComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
            true, true));
        newAccountComposite.setLayout(new GridLayout(1, false));

        this.createAccountButton = new Button(newAccountComposite, SWT.PUSH);
        this.createAccountButton.setText("Create New Account...");
        this.createAccountButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openCreateXMPPAccountWizard();
            }

        });

        return newAccountColumnGroup;
    }

    protected Composite createRightColumn(Composite composite) {
        Group existingAccountColumnGroup = new Group(composite, SWT.NONE);
        existingAccountColumnGroup.setLayout(new GridLayout(2, false));
        existingAccountColumnGroup.setText("Existing Account");

        this.enterXMPPAccountComposite = new EnterXMPPAccountComposite(
            existingAccountColumnGroup, SWT.NONE);
        GridData gridData2 = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        gridData2.verticalIndent = 23;
        gridData2.minimumWidth = 350;
        this.enterXMPPAccountComposite.setLayoutData(gridData2);

        this.isXMPPServerValid = this.enterXMPPAccountComposite
            .isXMPPServerValid();

        return existingAccountColumnGroup;
    }

    protected void hookListeners() {
        this.enterXMPPAccountComposite.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updatePageCompletion();
            }
        });

        this.enterXMPPAccountComposite
            .addEnterXMPPAccountCompositeListener(new EnterXMPPAccountCompositeListener() {

                public void xmppServerValidityChanged(
                    XMPPServerChangedEvent event) {
                    isXMPPServerValid = event.isXMPPServerValid();
                    updatePageCompletion();
                }

                public void isSarosXMPPServerChanged(
                    IsSarosXMPPServerChangedEvent event) {
                    showSarosXMPPRestriction = event.isSarosXMPPServer();
                    updatePageCompletion();
                }
            });
    }

    /**
     * Opens a {@link CreateXMPPAccountWizard} and takes over the created
     * account.
     */
    protected void openCreateXMPPAccountWizard() {
        getContainer().getShell().setVisible(false);

        CreateXMPPAccountWizard createXMPPAccountWizard = WizardUtils
            .openCreateXMPPAccountWizard(false);
        if (createXMPPAccountWizard != null
            && createXMPPAccountWizard.getCreatedXMPPAccount() != null) {
            this.isXMPPAccountCreated = true;

            XMPPAccount account = createXMPPAccountWizard
                .getCreatedXMPPAccount();

            String server = account.getServer();
            String username = account.getUsername();
            String password = account.getPassword();

            String jid;
            if (username.contains("@")) {
                jid = username;
            } else {
                jid = username + "@" + server;
                server = "";
            }

            this.createAccountButton.setEnabled(false);
            this.enterXMPPAccountComposite.setEnabled(false);
            this.enterXMPPAccountComposite.setJID(new JID(jid));
            this.enterXMPPAccountComposite.setPassword(password);
            this.enterXMPPAccountComposite.setServer(server);
        }

        getContainer().getShell().setVisible(true);
        getContainer().getShell().setFocus();
    }

    protected void updatePageCompletion() {
        boolean isJIDValid = this.getJID().isValid();
        boolean isPasswordNotEmpty = !this.getPassword().isEmpty();
        boolean accountExists = accountStore.contains(getJID());

        if (isJIDValid)
            wasJIDValid = true;
        if (isPasswordNotEmpty)
            wasPasswordValid = true;
        if (getServer().isEmpty() || isXMPPServerValid) {
            wasXMPPServerValid = true;
        }

        /*
         * Saros XMPP restriction message
         */
        if (showSarosXMPPRestriction && isJIDValid) {
            setMessage(Messages.xmpp_saros_restriction, WARNING);
        } else {
            setMessage(null);
        }

        if ((isJIDValid && isPasswordNotEmpty && isXMPPServerValid && !accountExists)
            || isXMPPAccountCreated) {
            /*
             * TODO Connect and login attempt to new server Not done because
             * Saros.connect holds the whole connection code. Few reusable code.
             * 2011/02/19 bkahlert
             */
            setErrorMessage(null);
            setPageComplete(true);
        } else {
            if (!isJIDValid && wasJIDValid) {
                setErrorMessage(Messages.jid_format_errorMessage);
            } else if (accountExists) {
                setErrorMessage(Messages.account_exists_errorMessage);
            } else if (!isPasswordNotEmpty && wasPasswordValid) {
                setErrorMessage(Messages.password_empty_errorMessage);
            } else if (!isXMPPServerValid && wasXMPPServerValid) {
                setErrorMessage(Messages.server_unresolvable_errorMessage);
            }
            setPageComplete(false);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible)
            return;

        this.enterXMPPAccountComposite.setFocus();
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
     * True if the entered account was freshly created by the
     * {@link CreateXMPPAccountWizard}.
     */
    public boolean isXMPPAccountCreated() {
        return this.isXMPPAccountCreated;
    }
}
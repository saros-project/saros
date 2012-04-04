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

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.EnterXMPPAccountComposite;
import de.fu_berlin.inf.dpp.ui.wizards.CreateXMPPAccountWizard;
import de.fu_berlin.inf.nebula.explanation.note.SimpleNoteComposite;
import de.fu_berlin.inf.nebula.utils.FontUtils;

/**
 * Allows the user to enter an XMPP account defined by a {@link JID}, a password
 * and an optional server.
 * 
 * @author bkahlert
 */
public class EnterXMPPAccountWizardPage extends WizardPage {
    public static final String TITLE = Messages.EnterXMPPAccountWizardPage_title;
    public static final String DESCRIPTION = Messages.EnterXMPPAccountWizardPage_description;

    @Inject
    private XMPPAccountStore accountStore;

    private Button createAccountButton;
    private EnterXMPPAccountComposite enterXMPPAccountComposite;

    /**
     * True if the entered account was freshly created by the
     * {@link CreateXMPPAccountWizard}.
     */
    private boolean isXMPPAccountCreated = false;

    /**
     * This flag is true if {@link JID} was already valid.
     */
    private boolean wasJIDValid = false;

    /**
     * This flag is true if the password was already valid.
     */
    private boolean wasPasswordValid = false;

    /**
     * This flag is true if the server was already valid.
     */
    private boolean wasServerValid = false;

    /**
     * This flag is true if the port was already valid.
     */
    private boolean wasPortValid = false;

    /**
     * This flag is true if Saros's Jabber server restriction should be
     * displayed.
     */
    private boolean showSarosXMPPRestriction = false;

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
        separator.setText(Messages.EnterXMPPAccountWizardPage_or);
        FontUtils.changeFontSizeBy(separator, 10);

        /*
         * RIGHT COLUMN
         */
        Composite rightColumn = createRightColumn(composite);
        rightColumn.setLayoutData(leftRightGridData);

        /*
         * 2nd row
         */

        SimpleNoteComposite x = new SimpleNoteComposite(composite, SWT.BORDER,
            SWT.ICON_INFORMATION,
            Messages.EnterXMPPAccountWizardPage_info_already_created_account);

        x.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1));
        x.setSpacing(8);

        hookListeners();
        updatePageCompletion();
    }

    private Composite createLeftColumn(Composite composite) {
        Group newAccountColumnGroup = new Group(composite, SWT.NONE);
        newAccountColumnGroup.setLayout(new GridLayout(2, false));

        newAccountColumnGroup
            .setText(Messages.EnterXMPPAccountWizardPage_new_account);

        Composite newAccountComposite = new Composite(newAccountColumnGroup,
            SWT.NONE);

        newAccountComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
            true, true));

        newAccountComposite.setLayout(new GridLayout(1, false));

        createAccountButton = new Button(newAccountComposite, SWT.PUSH);
        createAccountButton
            .setText(Messages.EnterXMPPAccountWizardPage_create_new_account);

        createAccountButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openCreateXMPPAccountWizard();
            }

        });

        return newAccountColumnGroup;
    }

    private Composite createRightColumn(Composite composite) {
        Group existingAccountColumnGroup = new Group(composite, SWT.NONE);
        existingAccountColumnGroup.setLayout(new GridLayout(2, false));

        existingAccountColumnGroup
            .setText(Messages.EnterXMPPAccountWizardPage_existing_account);

        enterXMPPAccountComposite = new EnterXMPPAccountComposite(
            existingAccountColumnGroup, SWT.NONE);

        GridData gridData2 = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        gridData2.verticalIndent = 23;
        gridData2.minimumWidth = 350;
        enterXMPPAccountComposite.setLayoutData(gridData2);
        enterXMPPAccountComposite.setUsingTSL(true);
        enterXMPPAccountComposite.setUsingSASL(true);

        return existingAccountColumnGroup;
    }

    private void hookListeners() {
        this.enterXMPPAccountComposite.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updatePageCompletion();
            }
        });

    }

    /**
     * Opens a {@link CreateXMPPAccountWizard} and takes over the created
     * account.
     */
    private void openCreateXMPPAccountWizard() {
        getContainer().getShell().setVisible(false);

        CreateXMPPAccountWizard createXMPPAccountWizard = WizardUtils
            .openCreateXMPPAccountWizard(false);

        if (createXMPPAccountWizard != null
            && createXMPPAccountWizard.getCreatedXMPPAccount() != null) {
            isXMPPAccountCreated = true;

            XMPPAccount account = createXMPPAccountWizard
                .getCreatedXMPPAccount();

            createAccountButton.setEnabled(false);
            enterXMPPAccountComposite.setEnabled(false);
            enterXMPPAccountComposite.setJID(new JID(account.getUsername(),
                account.getDomain()));
            enterXMPPAccountComposite.setPassword(account.getPassword());
            enterXMPPAccountComposite.setServer("");
            enterXMPPAccountComposite.setPort("");
            enterXMPPAccountComposite.setUsingTSL(true);
            enterXMPPAccountComposite.setUsingSASL(true);
        }

        getContainer().getShell().setVisible(true);
        getContainer().getShell().setFocus();
    }

    private void updatePageCompletion() {

        if (showSarosXMPPRestriction)
            setMessage(Messages.xmpp_saros_restriction_short, WARNING);
        else
            setMessage(null);

        if (isXMPPAccountCreated) {
            setPageComplete(true);
            return;
        }

        boolean isJIDValid = getJID().isValid();
        boolean isPasswordValid = !getPassword().isEmpty();
        boolean isServerValid = (getServer().isEmpty() && getPort().isEmpty())
            || !getServer().isEmpty();

        Boolean accountExists = null;

        int port;

        try {
            port = Integer.parseInt(enterXMPPAccountComposite.getPort());
            if (port <= 0 || port > 65535)
                port = -1;

        } catch (NumberFormatException e) {
            port = -1;
        }

        boolean isPortValid = port != -1;

        if (enterXMPPAccountComposite.getPort().isEmpty()
            && enterXMPPAccountComposite.getServer().isEmpty()) {
            port = 0;
            isPortValid = true;
        }

        if (!enterXMPPAccountComposite.getPort().isEmpty())
            wasPortValid = true;

        if (isJIDValid)
            wasJIDValid = true;

        if (isPasswordValid)
            wasPasswordValid = true;

        if (isServerValid)
            wasServerValid = true;

        setPageComplete(false);
        String errorMessage = null;

        /*
         * only query if the account exists when all required fields are filled
         * out properly otherwise "accountExists" is null -> state not known yet
         */
        if ((isJIDValid && isPasswordValid && isServerValid && isPortValid))
            accountExists = accountStore.exists(getJID().getName(), getJID()
                .getDomain(), getServer(), port);

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

        boolean isAdvancedSectionValid = ((getServer().isEmpty() && getPort()
            .isEmpty()) || (!getServer().isEmpty() && !getPort().isEmpty() && port != 0));

        if (isAdvancedSectionValid && accountExists != null
            && !accountExists.booleanValue())
            setPageComplete(true);

    }

    private String lastErrorMessage = null;

    private void updateErrorMessage(String message) {
        if (lastErrorMessage != null && lastErrorMessage.equals(message))
            return;

        lastErrorMessage = message;

        setErrorMessage(message);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible)
            return;

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

    /**
     * True if the entered account was freshly created by the
     * {@link CreateXMPPAccountWizard}.
     */
    public boolean isXMPPAccountCreated() {
        return isXMPPAccountCreated;
    }

    /**
     * Returns the entered port
     * 
     * @return the entered port
     */
    public String getPort() {
        return enterXMPPAccountComposite.getPort();
    }

    public boolean isUsingTSL() {
        return enterXMPPAccountComposite.isUsingTSL();
    }

    public boolean isUsingSASL() {
        return enterXMPPAccountComposite.isUsingSASL();
    }
}
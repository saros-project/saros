/**
 * 
 */
package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.wizards.CreateAccountWizard;
import de.fu_berlin.inf.dpp.util.LinkListener;
import de.fu_berlin.inf.dpp.util.Utils;

public class RegisterAccountPage extends WizardPage implements IWizardPage2 {

    private static final Logger log = Logger
        .getLogger(RegisterAccountPage.class.getName());

    public static final String LIST_OF_XMPP_SERVERS = "http://www.saros-project.org/InstallUsing#Using_Public_XMPP_Servers";

    protected Text serverText;

    protected Text userText;

    protected Text passwordText;

    protected Text repeatPasswordText;

    protected Button prefButton;

    protected final boolean createAccount;

    protected final boolean showUseNowButton;

    protected final boolean useNow;

    protected final Saros saros;

    protected final PreferenceUtils preferenceUtils;

    @Inject
    protected XMPPAccountStore accountStore;

    /**
     * 
     * @param createAccount
     *            are we going to create a new account?
     * @param showUseNowButton
     *            show button for setting "useNow"
     * @param useNow
     *            default value for activating this account
     * @param preferenceUtils
     */
    public RegisterAccountPage(Saros saros, boolean createAccount,
        boolean showUseNowButton, boolean useNow,
        PreferenceUtils preferenceUtils) {

        super("create");
        this.createAccount = createAccount;
        this.showUseNowButton = showUseNowButton;
        this.useNow = useNow;
        this.saros = saros;
        this.preferenceUtils = preferenceUtils;

        Saros.injectDependenciesOnly(this);
    }

    public void createControl(Composite parent) {
        Composite root = new Composite(parent, SWT.NONE);

        root.setLayout(new GridLayout(2, false));

        if (this.createAccount) {
            setTitle("Create New XMPP Account");
            setDescription("Create a new account on a XMPP/Jabber server");
        } else {
            setTitle("Enter User Account");
            setDescription("Enter your account information and XMPP/Jabber server");
        }
        // present a link to a list of public XMPP servers
        Link link = new Link(root, SWT.NONE);
        link.setText("For a list of public XMPP/Jabber servers click <a href=\""
            + LIST_OF_XMPP_SERVERS + "\">here</a>");
        GridData gdLayout = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gdLayout.horizontalSpan = 2;
        link.setLayoutData(gdLayout);
        link.addListener(SWT.Selection, new LinkListener());

        createSpacer(root, 2);

        Label serverLabel = new Label(root, SWT.NONE);
        serverLabel.setText("XMPP/Jabber Server");

        this.serverText = new Text(root, SWT.BORDER);
        this.serverText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));

        Label userLabel = new Label(root, SWT.NONE);
        userLabel.setText("Username");

        this.userText = new Text(root, SWT.BORDER);
        this.userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));

        Label pwLabel = new Label(root, SWT.NONE);
        pwLabel.setText("Password");

        this.passwordText = new Text(root, SWT.BORDER);
        this.passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));
        this.passwordText.setEchoChar('*');

        if (this.createAccount) {
            Label rpwLabel = new Label(root, SWT.NONE);
            rpwLabel.setText("Repeat Password");

            this.repeatPasswordText = new Text(root, SWT.BORDER);
            this.repeatPasswordText.setLayoutData(new GridData(SWT.FILL,
                SWT.CENTER, true, false));
            this.repeatPasswordText.setEchoChar('*');
        }

        if (this.showUseNowButton) {
            this.prefButton = new Button(root, SWT.CHECK | SWT.SEPARATOR);
            this.prefButton.setSelection(this.useNow);
            this.prefButton
                .setText("Use this account now. (Will disconnect if you are already connected with another account.)");
            this.prefButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
                true, false, 2, 1));
        }

        if (!this.createAccount) {
            Group group = new Group(root, SWT.NONE);
            group.setLayout(new GridLayout(2, false));
            group.setText("Create new XMPP/Jabber account");
            GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.horizontalSpan = 2;
            gd.verticalIndent = 30;
            group.setLayoutData(gd);

            Label createAccountLabel = new Label(group, SWT.WRAP);
            createAccountLabel
                .setText("If you don't have an existing XMPP/Jabber account you can create one now");
            gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
            gd.widthHint = 250;
            createAccountLabel.setLayoutData(gd);

            Button createAccountButton = new Button(group, SWT.PUSH);
            createAccountButton.setLayoutData(new GridData(SWT.RIGHT,
                SWT.CENTER, false, false));
            createAccountButton.setText("Create new account...");
            createAccountButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    Utils.runSafeSWTSync(log, new Runnable() {
                        public void run() {
                            openCreateAccount();
                        }
                    });
                }

            });
        }

        setInitialValues();

        hookListeners();
        updateNextEnablement();

        setControl(root);
    }

    protected void createSpacer(Composite composite, int columnSpan) {
        Label label = new Label(composite, SWT.NONE);
        GridData gd = new GridData();
        gd.horizontalSpan = columnSpan;
        label.setLayoutData(gd);
    }

    protected void openCreateAccount() {
        try {
            Point loc = this.getShell().getLocation();
            Shell shell = new Shell(this.getShell());
            shell.setLocation(loc.x - 250, loc.y - 50);

            CreateAccountWizard wizard = new CreateAccountWizard(saros,
                preferenceUtils, true, false, false);
            WizardDialog dialog = new WizardDialog(shell, wizard);
            dialog.create();

            // init fields with input from this RegisterAccountPage
            if (this.getServer().length() > 0) {
                /*
                 * only set the server string if it's not empty use the default
                 * otherwise
                 */
                wizard.page.serverText.setText(this.getServer());
            }
            wizard.page.userText.setText(this.getUsername());
            wizard.page.passwordText.setText(this.getPassword());

            boolean success = Window.OK == dialog.open();

            if (success) {
                this.passwordText.setText(wizard.getPassword());
                this.serverText.setText(wizard.getServer());
                this.userText.setText(wizard.getUsername());
            }

        } catch (Exception e) {
            log.error("Error while running enter account wizard", e);
        }
    }

    public String getServer() {
        return this.serverText.getText();
    }

    public String getUsername() {
        return this.userText.getText();
    }

    public String getPassword() {
        return this.passwordText.getText();
    }

    public boolean useNow() {
        if (this.showUseNowButton) {
            return this.prefButton.getSelection();
        }
        return this.useNow;
    }

    private void hookListeners() {
        ModifyListener listener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateNextEnablement();
            }
        };

        this.serverText.addModifyListener(listener);
        this.userText.addModifyListener(listener);
        this.passwordText.addModifyListener(listener);
        if (this.createAccount)
            this.repeatPasswordText.addModifyListener(listener);
    }

    private void updateNextEnablement() {
        // check if every field is not empty
        boolean done = (this.serverText.getText().length() > 0)
            && (this.userText.getText().length() > 0)
            && (this.passwordText.getText().length() > 0);

        // check if passwords match only if a new account should be created
        if (this.createAccount) {
            boolean passwordsMatch = this.passwordText.getText().equals(
                this.repeatPasswordText.getText());
            done &= passwordsMatch;

            if (passwordsMatch) {
                setErrorMessage(null);
            } else {
                setErrorMessage("Passwords don't match.");
            }
        }

        setPageComplete(done);
    }

    public void setInitialValues() {
        IPreferenceStore preferences = saros.getPreferenceStore();
        String serverText = preferences.getString(PreferenceConstants.SERVER);
        String usernameText = preferences
            .getString(PreferenceConstants.USERNAME);
        this.serverText.setText(serverText);
        this.userText.setText(usernameText);
    }

    public boolean performFinish() {

        String server = getServer();
        String username = getUsername();
        String password = getPassword();

        /*
         * TODO think about providing cancellation for this operation, so that
         * the users preferences are not overwritten, but keep in mind that
         * account creation can not be reverted
         */

        if (this.createAccount) {
            if (!createAccount(server, username, password))
                return false;
        }

        XMPPAccount account = accountStore.createNewAccount(username, password,
            server);
        accountStore.saveAccounts();
        // TODO warn user when already connected
        if (this.useNow()) {
            accountStore.setAccountActive(account);
            saros.connect(false);
        }
        return true;
    }

    protected boolean createAccount(final String server, final String username,
        final String password) {

        try {
            // fork a new thread to prevent the GUI from hanging
            getContainer().run(true, false, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException {

                    try {
                        saros
                            .createAccount(server, username, password, monitor);
                    } catch (final XMPPException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });

        } catch (InvocationTargetException e) {
            setErrorMessage(extractErrorMessage(e).trim());
            return false;
        } catch (InterruptedException e) {
            log.error("An internal error occurred: InterruptedException"
                + " thrown from uninterruptable method", e);
            setErrorMessage(e.getCause().getMessage());
            return false;
        }

        return true;
    }

    protected String extractErrorMessage(InvocationTargetException e) {
        try {
            // Unpack wrapped exception
            throw e.getCause();
        } catch (XMPPException x) {

            String errorMessage = x.getMessage();

            XMPPError error = x.getXMPPError();
            if (error != null) {

                String errorCode = error.getMessage();

                if (errorCode == null) {
                    if (error.getCode() == 409) {
                        errorCode = "Account already exists\n(or possibly the server does not support direct registration)";
                    } else {
                        errorCode = "No Explanation";
                    }
                }

                // Don't repeat error message
                if (!errorCode.equals(errorMessage)) {
                    errorMessage = errorMessage + ": " + errorCode;
                }
            }
            return errorMessage;
        } catch (Throwable t) {
            log.error("An internal error occurred: ", t);
            String message = t.getMessage();
            return message == null ? "" : message;
        }
    }
}
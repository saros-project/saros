package de.fu_berlin.inf.dpp.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.util.LinkListener;

/**
 * Simple WizardPage for creating and changing XMPP-Accounts.
 * 
 * @author Sebastian Schlaak
 */
public class AccountPage extends WizardPage {

    public static final String LIST_OF_XMPP_SERVERS = "http://www.saros-project.org/#PublicServers";
    public static final String DONT_MATCH_LABEL = "Passwords don't match";

    Label usernameLabel;
    Label passwordLabel;
    Label serverLabel;
    Label passwordConfirmLabel;
    Label passwordDontMatchLabel;
    Text userNameInput;
    Text passwordInput;
    Text passwordConfirmInput;
    Text serverTextInput;
    XMPPAccount account;
    Composite parent;
    GridData data = new GridData(GridData.FILL_HORIZONTAL);

    protected AccountPage() {
        super("AccountManagement");
    }

    protected void setJabberAccount(XMPPAccount account) {
        this.account = account;
    }

    protected void setPageTitle(String text) {
        setTitle(text);
    }

    public void createControl(Composite parent) {
        this.parent = parent;
        layoutParent();
        createServerLink();
        createServerForm();
        createUserNameForm();
        createPasswordForm();
        createConfirmPasswordLabel();
        createPasswordDontMatchLabel();
        fillInputs();
    }

    private void createServerLink() {
        Label label = new Label(parent, SWT.NONE);
        label.setText("Info:");
        Link link = new Link(parent, SWT.NONE);
        link.setText("For a list of public XMPP servers click <a href=\""
            + LIST_OF_XMPP_SERVERS + "\">here</a>");
        link.addListener(SWT.Selection, new LinkListener());
    }

    private void layoutParent() {
        this.parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));
        this.parent.setLayout(new GridLayout(2, false));
    }

    private void createUserNameForm() {
        this.usernameLabel = createStyledLabel("Username:");
        this.userNameInput = createStyledInputText();
    }

    private void createServerForm() {
        this.serverLabel = createStyledLabel("Server");
        this.serverTextInput = createStyledInputText();
    }

    private void createPasswordForm() {
        this.passwordLabel = createStyledLabel("Password:");
        this.passwordInput = createStyledPasswordInput();
    }

    private void createConfirmPasswordLabel() {
        this.passwordConfirmLabel = createStyledLabel("Confirm:");
        this.passwordConfirmInput = createStyledPasswordInput();
    }

    private Label createStyledLabel(String labelText) {
        Label label = new Label(this.parent, SWT.NONE);
        label.setText(labelText);
        setControl(label);
        return label;
    }

    private Text createStyledInputText() {
        Text input = new Text(parent, SWT.SINGLE | SWT.BORDER);
        input.setText("");
        input.setLayoutData(data);
        return input;
    }

    private Text createStyledPasswordInput() {
        Text input = new Text(parent, SWT.SINGLE | SWT.BORDER);
        input.setEchoChar('*');
        input.setLayoutData(data);
        input.addModifyListener(new PasswordConfirmListener());
        return input;
    }

    class PasswordConfirmListener implements ModifyListener {
        public void modifyText(ModifyEvent e) {
            if (isPasswordsMatch()) {
                hidePassWordDontMatchLabel();
            } else {
                showPassWordDontMatchLabel();
            }
        }
    }

    private boolean isPasswordsMatch() {
        return this.passwordInput.getText().equals(
            this.passwordConfirmInput.getText());
    }

    private void showPassWordDontMatchLabel() {
        this.passwordDontMatchLabel.setVisible(true);
        this.passwordDontMatchLabel.setText(DONT_MATCH_LABEL);
    }

    private void hidePassWordDontMatchLabel() {
        this.passwordDontMatchLabel.setVisible(false);
    }

    protected void fillInputs() {
        if (isNoAccountToChange()) {
            return;
        }
        this.userNameInput.setText(this.account.getUsername());
        this.serverTextInput.setText(this.account.getServer());
    }

    private void createPasswordDontMatchLabel() {
        new Label(parent, SWT.NONE);
        passwordDontMatchLabel = new Label(parent, SWT.NONE);
        passwordDontMatchLabel.setText("");
        passwordDontMatchLabel.setForeground(parent.getDisplay()
            .getSystemColor(SWT.COLOR_RED));
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        passwordDontMatchLabel.setLayoutData(data);
    }

    private boolean isNoAccountToChange() {
        return this.account == null;
    }

    protected String getUserName() {
        return this.userNameInput.getText();
    }

    protected String getPassword() {
        return this.passwordInput.getText();
    }

    protected String getServer() {
        return this.serverTextInput.getText();
    }
}
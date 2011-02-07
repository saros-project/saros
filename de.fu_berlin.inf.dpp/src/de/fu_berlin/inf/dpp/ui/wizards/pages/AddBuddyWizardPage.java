package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.util.regex.Pattern;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.fu_berlin.inf.dpp.net.JID;

public class AddBuddyWizardPage extends WizardPage {
    protected Text idText;

    protected Text nicknameText;

    public AddBuddyWizardPage() {
        super("create");

        setTitle("New Buddy");
        setDescription("Add a new buddy to your Saros buddies");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);

        composite.setLayout(new GridLayout(2, false));

        Label idLabel = new Label(composite, SWT.NONE);
        idLabel.setText("XMPP/Jabber ID");

        this.idText = new Text(composite, SWT.BORDER);
        this.idText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));

        hookListeners();
        updateNextEnablement();

        setControl(composite);
    }

    public JID getJID() {
        return new JID(this.idText.getText().trim());
    }

    private void hookListeners() {
        ModifyListener listener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateNextEnablement();
            }
        };

        this.idText.addModifyListener(listener);
    }

    /**
     * Email-Pattern was too strict:
     * 
     * <code> Pattern emailPattern = Pattern.compile(
     * "^[A-Z0-9._%+-]+@[A-Z0-9.-]+$\\.[A-Z]{2,4}",
     * Pattern.CASE_INSENSITIVE); </code>
     */
    Pattern userAtHostPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+$",
        Pattern.CASE_INSENSITIVE);

    private void updateNextEnablement() {

        boolean done = (this.idText.getText().length() > 0);

        if (!done) {
            this.setErrorMessage(null);
            this.setMessage("Please enter a XMPP/Jabber ID");
            this.setPageComplete(false);
            return;
        }

        if (!userAtHostPattern.matcher(this.idText.getText().trim()).matches()) {
            this.setErrorMessage("Not a valid XMPP/Jabber ID (should be: id@server.domain)!");
            this.setMessage(null);
            this.setPageComplete(false);
            return;
        }

        this.setErrorMessage(null);
        setPageComplete(true);
    }
}
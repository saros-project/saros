package de.fu_berlin.inf.dpp.ui.wizards;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.fu_berlin.inf.dpp.invitation.IncomingInvitationProcess;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.VersionManager;

/**
 * A wizard page that displays the name of the inviter and the description
 * provided with the invitation.
 */
class ShowDescriptionPage extends WizardPage {

    private final JoinSessionWizard joinSessionWizard;

    protected ShowDescriptionPage(JoinSessionWizard joinSessionWizard,
        VersionManager manager, IncomingInvitationProcess invProcess) {
        super("firstPage");
        this.joinSessionWizard = joinSessionWizard;

        setTitle("Session Invitation");
        setDescription("You have been invited to join on a session for a "
            + "shared project. Click next if you want to accept the invitation.");
        setImageDescriptor(SarosUI
            .getImageDescriptor("icons/start_invitation.png"));

        /*
         * Show compatibility issues and inform the user what to do (but the
         * user can always proceed).
         */
        VersionManager.VersionInfo vInfo = invProcess.versionInfo;

        String remoteSarosVersion = vInfo.version.toString();
        switch (vInfo.compatibility) {

        case TOO_NEW:
            setMessage("Your Saros version is: " + manager.getVersion()
                + ". Your peer has an older version: " + remoteSarosVersion
                + ".\n Please tell your peer to check for updates!"
                + " Proceeding with incompatible versions"
                + " may cause malfunctions!", WARNING);
            break;

        case OK:
            setMessage("Your Saros version " + manager.getVersion().toString()
                + " is compatible with your peer's one " + remoteSarosVersion,
                INFORMATION);
            break;

        case TOO_OLD:
        default:
            setMessage("Your Saros version is too old: "
                + manager.getVersion().toString()
                + ". Your peer has a newer version: " + remoteSarosVersion
                + ".\n Please check for updates!"
                + " Proceeding with incompatible versions"
                + " may cause malfunctions!", WARNING);
        }
    }

    @Override
    public boolean canFlipToNextPage() {
        // Important! otherwise getNextPage() is called
        return true;
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label inviterLabel = new Label(composite, SWT.NONE);
        inviterLabel.setText("Inviter");

        Text inviterText = new Text(composite, SWT.READ_ONLY | SWT.SINGLE
            | SWT.BORDER);

        inviterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
            false));
        inviterText.setText(this.joinSessionWizard.process.getPeer().getBase());

        Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
            false, false));
        descriptionLabel.setText("Project");

        Text descriptionText = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
        descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
            true));
        descriptionText
            .setText(this.joinSessionWizard.process.getDescription());

        setControl(composite);

        if (joinSessionWizard.getPreferenceUtils().isAutoAcceptInvitation()) {
            joinSessionWizard.pressWizardButton(IDialogConstants.NEXT_ID);
        }
    }
}